# vprofile lift-and-shift on Floci

Recreates `devopshydclub/vprofile-project`'s `aws-LiftAndShift` branch --
5 EC2 instances (db/mariadb, cache/memcached, mq/rabbitmq, app/tomcat,
web/nginx) -- as real Terraform `aws_instance` resources, pointed at
[Floci](https://floci.io) (a local, LocalStack-compatible AWS emulator)
instead of real AWS. It targets the exact same app as
`legacy/vprofile/compose.yaml` (Java 17, Tomcat 9, `vprofile-v2.war`) --
this is the EC2-shaped version of that same topology, not the MyFarm
services themselves.

## Why this exists

The MyFarm project's own Kubernetes/GitOps/observability phases (5-7) are
still ahead. This piece stands alone: it's a real Terraform-authored,
Floci-backed AWS deployment you can `apply`/`destroy` tonight and speak
to directly -- security groups, EC2 launch, SSM RunCommand, the
config-management chicken-and-egg problem multi-instance deployments
always hit.

## Prerequisite (already satisfied)

EC2 needs the Docker socket mounted into the `floci` container to launch
instances as real Docker containers. Checked directly against
`legacy/vprofile/compose.yaml` -- it's already there
(`/var/run/docker.sock:/var/run/docker.sock`), so nothing to change.

## Run it

```bash
cd legacy/vprofile
docker compose --profile stack up -d floci    # just the floci service, not the whole vprofile stack
# wait for it to report healthy, then:

cd ../../infra/floci-liftandshift
terraform init
terraform apply
```

`terraform apply` provisions all 5 instances, then runs
`scripts/wire.sh` automatically via a `null_resource` provisioner. Watch
its output -- it's the SSM RunCommand step that finishes wiring app01
and web01 together (see "The chicken-and-egg problem" below).

Once it finishes:

```bash
docker logs vprofile-floci-floci-1 2>&1 | grep Published
```

finds the host ports Floci published for the security group's open
ports (80, 8080, 3306, 11211, 5672, 15672) -- they land in a random
range (default 30000-30999), not the ports themselves.

Tear down with `terraform destroy`.

## Design decisions and why

- **Terraform AWS provider pointed at Floci**: standard LocalStack-style
  endpoint override (dummy `test`/`test` credentials,
  `skip_credentials_validation`/`skip_metadata_api_check`/
  `skip_requesting_account_id`, `endpoints { ec2 = ssm = floci_endpoint }`)
  -- confirmed against Floci's own "Migrating from LocalStack" guide,
  which describes it as a drop-in replacement using the same
  port/credential/SDK pattern as LocalStack.
- **AMI**: `ami-amazonlinux2023` for db/cache/app/web (Floci's EC2 image
  catalog alias -> `public.ecr.aws/amazonlinux/amazonlinux:2023`, the
  plain non-systemd container lifecycle -- confirmed against Floci's own
  EC2 service docs). Every userdata script daemonizes its service
  directly instead (`mysqld_safe &`, `memcached -d`, `catalina.sh
  start`, plain `nginx`) since none of these need an init system.
  Deliberately NOT `ami-ubuntu2404-cloud` (Floci's docs flag that
  systemd/cloud-init variant experimental).
- **RabbitMQ is the one exception -- Ubuntu, not Amazon Linux 2023**:
  AL2023 has no working Erlang/RabbitMQ install path (confirmed --
  `rabbitmq/erlang-rpm`'s own issue tracker has an open "Investigate if
  Amazon Linux 2023 can be supported" issue and a separate "Erlang Does
  Not Build on ARM Based Amazon Linux 2023" discussion). `mq` uses
  `ami-ubuntu2404` instead (still Floci's plain non-systemd lifecycle),
  where `apt-get install -y rabbitmq-server` pulls a working Erlang
  automatically. This is the file most likely to need a tweak if
  Ubuntu's apt sources on the image don't already have `universe`
  enabled -- `mq.sh` tries `add-apt-repository universe` defensively but
  this is the one install path that couldn't be verified against a live
  pull from this sandbox.
- **The chicken-and-egg problem**: app01's config and web01's nginx
  upstream reference bare hostnames (`db01`, `mc01`, `rmq01`, `app01` --
  straight from the original repo's `application.properties`/
  `default.conf`), but those hostnames only resolve once every
  instance's private IP is known -- which userdata can't know about its
  peers at boot time. Solved with a 2-phase design: userdata installs/
  builds/deploys but leaves Tomcat and nginx *stopped*; a Terraform
  `null_resource` with a `local-exec` provisioner (`scripts/wire.sh`,
  `depends_on` all 5 instances) runs after every private IP is known,
  uses **SSM RunCommand** to write `/etc/hosts` entries on app01/web01
  and start the two services. Confirmed viable via Floci's own SSM
  docs: RunCommand on a Floci-launched instance runs the script directly
  inside that instance's container -- no SSM agent or IAM instance
  profile needed, unlike real AWS.
- **Security group**: one shared SG, every port (80/8080/3306/11211/
  5672/15672) opened to `0.0.0.0/0` -- this is what makes Floci
  auto-publish each port to the Mac's `localhost` via a `socat` sidecar
  (confirmed: only CIDR-sourced ingress rules get published, per
  Floci's EC2 docs' "Security Group Port Publishing" section). Published
  host ports aren't predictable in advance -- find them with
  `docker logs vprofile-floci-floci-1 2>&1 | grep Published`.
- **App source**: each instance clones `KrishnaTejaCheruku/MyFarm-project`
  itself (branch `floci-complete` by default -- see `variables.tf`) and
  builds from `legacy/vprofile/`, rather than the upstream
  `devopshydclub/vprofile-project` -- so the EC2 topology always matches
  exactly what `legacy/vprofile/compose.yaml` already runs locally
  (same `db_backup.sql`, same `pom.xml` producing `vprofile-v2.war`).

## Files

```
infra/floci-liftandshift/
├── README.md
├── versions.tf        # terraform + aws provider, Floci endpoint override
├── variables.tf        # floci_endpoint, ami_id, mq_ami_id, repo/branch, credentials
├── network.tf           # one shared security group
├── instances.tf          # 5x aws_instance + null_resource.wire (phase-2 wiring)
├── outputs.tf
├── scripts/wire.sh      # SSM RunCommand wiring, invoked by null_resource.wire
└── userdata/
    ├── db.sh     # mariadb105, seeded from legacy/vprofile's db_backup.sql
    ├── cache.sh  # memcached
    ├── mq.sh     # rabbitmq on Ubuntu (see AMI note above)
    ├── app.sh    # builds the WAR from source via Maven, deploys, does NOT start tomcat
    └── web.sh    # nginx reverse proxy config, does NOT start nginx
```

## Not yet verified end-to-end

Written and internally reviewed (every `.tf` file hand-checked for brace/
argument correctness -- no `terraform` binary available in the sandbox
this was written from, so `terraform validate`/`plan`/`apply` have not
actually been run by anyone yet). Per the project's standing workflow
rule, Krishna owns running this. Most likely rough edges, in the order
you'll hit them:

1. `terraform init`/`plan` -- straightforward HCL/provider errors, if any.
2. `mysql_install_db` vs `mariadb-install-db` naming on `mariadb105-server`
   -- `db.sh` tries the older name and swallows the error; if the datadir
   never actually initializes, `mysqld_safe` will hang instead of failing
   loudly.
3. `mq.sh`'s `apt-get install -y rabbitmq-server` -- depends on Ubuntu's
   `universe` component being enabled on Floci's `ami-ubuntu2404` image;
   `add-apt-repository universe` is a defensive fallback, not a
   confirmed-necessary step.
4. `scripts/wire.sh`'s SSM RunCommand calls -- if app01/web01 haven't
   finished their own userdata (Maven build, Tomcat/nginx install) by
   the time `null_resource.wire` fires, the `catalina.sh start`/`nginx`
   commands could run before the software they're starting exists.
   Re-running `aws ssm send-command` manually (or re-`apply`ing) should
   recover.

Report back with real output at each stage -- same pattern as every
other phase in this project -- rather than assuming green.
