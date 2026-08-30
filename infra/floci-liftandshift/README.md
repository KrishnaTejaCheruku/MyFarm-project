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

## Status: applied and verified end-to-end (2026-08-30)

Real `terraform apply` on Krishna's Mac, multiple times, culminating in
a real request/response chain: `curl http://localhost:<published-port>/`
-> nginx (web01) -> Tomcat/Spring Security (app01) 302 redirect ->
`/login` -> the actual rendered VProfile login page HTML, RabbitMQ
connected, WAR deployed. Four real bugs were found and fixed along the
way -- worth knowing about before your next `apply`:

1. **Point 4566 at the wrong Floci instance and everything "works" against
   an empty universe.** If any *other* Floci-compatible stack (e.g. the
   [floci-ui](https://github.com/floci-io/floci-ui) dashboard, which
   bundles its own `floci` service) is also running, it can grab host
   port 4566 first. Terraform will happily apply against whichever
   Floci answers on that port -- Terraform itself gives no signal
   anything is wrong. Always confirm
   `docker ps --format "table {{.Names}}\t{{.Ports}}" | grep floci`
   shows `vprofile-floci-floci-1` (not some other project's `floci`)
   bound to `0.0.0.0:4566` before `apply`ing. If you run `floci-ui`
   alongside this, start it with `docker compose up -d --no-deps
   floci-ui floci-api` -- its `floci-api` service `depends_on` its own
   `floci`/`floci-az`/`floci-gcp`/`floci-seed`, so a plain `up` without
   `--no-deps` pulls in a second, unrelated Floci instance.
2. **`wire.sh`'s SSM commands were fire-and-forget.** `aws ssm
   send-command` only queues a command -- it returns as soon as Floci
   *accepts* the request, not once it's actually delivered/executed.
   The original script treated accept-and-return as done. Confirmed
   live: the `/etc/hosts` writes (cheap, fast) landed reliably, but the
   `catalina.sh start`/`nginx` commands were silently dropped on a real
   run -- no error, no log entry, just nothing happened. Fixed:
   `wire.sh` now polls `get-command-invocation` until each command
   reaches `Success`/`Failed`, and retries once if it doesn't.
3. **Even delivered, `catalina.sh start` can still lose the race against
   `app.sh`'s own Maven build.** `null_resource.wire` only waits for the
   5 instances to exist, not for their userdata to finish. If app01's
   Maven build/Tomcat install is still running when `wire.sh` fires,
   the start command executes against a Tomcat that isn't fully in
   place yet and fails quietly (nginx then 502s once web01's leg is
   fine). Not yet fixed in the script itself -- if `curl` 502s well
   after `apply` finished, check app01 directly:
   ```bash
   export AWS_ACCESS_KEY_ID=test AWS_SECRET_ACCESS_KEY=test \
     AWS_ENDPOINT_URL=http://localhost:4566 AWS_DEFAULT_REGION=us-east-1
   aws ssm send-command --instance-ids <app01-id> \
     --document-name "AWS-RunShellScript" \
     --parameters 'commands=["su -s /bin/bash tomcat -c \"/opt/tomcat/bin/catalina.sh start\""]'
   ```
   and re-`curl` after ~10s. A real fix would have `wire.sh` poll for
   `/tmp/app-userdata-complete` (app01) and nginx's own marker before
   attempting to start either service.
4. **Resource contention produces the exact same symptom as a config
   bug.** `aws_instance` create failing with `unexpected state
   'terminated'` (not a Floci/AWS API error) means Docker itself killed
   the container the instant it started -- almost always because too
   much else is running at once on a ~3.8GB Docker Desktop cap (the k3d
   cluster from `infra/kubernetes/` is the single biggest offender; stop
   it with `docker stop k3d-<cluster>-server-0 k3d-<cluster>-agent-*
   k3d-<cluster>-serverlb k3d-<cluster>-tools` before applying this).
   Also watch for orphaned `floci-ec2-i-*`/`floci-ec2-fwd-*` containers
   left behind by bug #1 above -- `terraform destroy` run against the
   *correct* Floci has no record of instances created against the
   *wrong* one, so it reports success without actually removing them;
   clean those up with `docker rm -f` directly.

Find published ports and verify:

```bash
docker logs vprofile-floci-floci-1 2>&1 | grep -i publish
curl -i http://localhost:<web01's port 80 mapping>/
curl -i http://localhost:<web01's port 80 mapping>/login
```

Tear down with `terraform destroy` when done.
