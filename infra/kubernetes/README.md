# Kubernetes platform (Phase 5): k3s + Cilium + Traefik Gateway API

A 3-node Kubernetes cluster matching the target architecture's "3-node
k3s, Cilium/Hubble, Traefik Gateway API" line -- built with
[k3d](https://k3d.io) (k3s nodes as Docker containers) since k3s itself
needs a real Linux kernel and this runs on a Mac. Everything else is the
real, unmodified upstream stack: real k3s, real Cilium, real Traefik,
real Gateway API CRDs -- k3d is just how the nodes are hosted.

## Run it

```bash
brew install k3d helm kubectl   # if not already installed
cd infra/kubernetes
./scripts/bootstrap.sh
```

Takes a few minutes -- Cilium's DaemonSet has to actually go Ready
before anything else in the script proceeds (see "Why order matters"
below). Watch the numbered `=== N/6 ===` lines; if it stops on one,
that's exactly which stage to look at.

Tear down: `k3d cluster delete myfarm`.

## Why order matters

Confirmed against a real, documented failure
([k3d-io/k3d#580](https://github.com/k3d-io/k3d/discussions/580)):
someone disabled flannel to run Cilium, but Traefik came up before
Cilium finished deploying and never recovered -- nothing needing pod
networking can start before the CNI itself is Ready, because until
then there IS no pod networking. `bootstrap.sh` installs the cluster
with flannel/kube-proxy/traefik/servicelb all disabled at creation
time, installs Cilium, explicitly waits on its rollout status, waits
for all 3 nodes to report `Ready` (they can't, without a CNI), and only
then touches Gateway API/Traefik/anything else.

## The one step not yet verified live

`k8sServiceHost=k3d-myfarm-server-0` in the Cilium Helm install --
Cilium's agent pods need to reach the k3s API server directly (they run
`hostNetwork: true` specifically so they can, before pod networking
exists to reach it any other way). The reasoning this value should
work: k3d node containers sit together on one Docker user-defined
bridge network, where Docker's embedded DNS resolves sibling container
names, and a `hostNetwork: true` pod uses its node container's own
network namespace -- so it should be able to resolve
`k3d-myfarm-server-0` the same way any other container on that Docker
network can. This is standard Docker networking behavior, not
Kubernetes-specific, but it hasn't been run for real yet as of this
commit.

**If `cilium`/`cilium-operator` pods sit in `CrashLoopBackOff` or
`Error`** (`kubectl -n kube-system get pods -l k8s-app=cilium`), check
their logs first for the actual failure
(`kubectl -n kube-system logs -l k8s-app=cilium --tail=50`) rather than
assuming it's this -- same "read the real evidence" rule as the AWS
piece. If it IS an API-server-unreachable error specifically, the
fallback is to expose the API server on a host port k3d controls
directly instead of relying on Docker DNS:

```bash
k3d cluster create myfarm --servers 1 --agents 2 --api-port 6550 \
  --k3s-arg "--flannel-backend=none@server:0" \
  --k3s-arg "--disable-network-policy@server:0" \
  --k3s-arg "--disable-kube-proxy@server:0" \
  --k3s-arg "--disable=traefik@server:0" \
  --k3s-arg "--disable=servicelb@server:0" \
  --wait
# then re-run the Cilium helm install with:
#   --set k8sServiceHost=host.docker.internal --set k8sServicePort=6550
```

## Files

```
infra/kubernetes/
├── README.md
├── scripts/bootstrap.sh          # the whole thing, in the order that matters
├── gateway/traefik-values.yaml   # Traefik as a Gateway API controller, not classic Ingress
├── gateway/httproute-demo.yaml   # proves routing works: nginx-demo.localhost -> nginx-demo Service
└── demo/nginx-demo.yaml          # minimal Deployment+Service, deliberately not MyFarm-specific yet
```

## Versions pinned (checked current as of 2026-08-30, not guessed)

- Cilium `1.19.3` -- github.com/cilium/cilium/releases
- Gateway API `v1.6.1` (standard-install.yaml, the CRD channel, not experimental) -- github.com/kubernetes-sigs/gateway-api/releases
- Traefik: whatever `helm repo add traefik https://traefik.github.io/charts` resolves to at install time (chart tracks Traefik v3.x; `providers.kubernetesGateway.enabled` confirmed against Traefik's own v3.7 docs)

## Not yet done

- Nothing MyFarm-specific deployed here yet -- `services/api`,
  `services/worker`, the storefront aren't containerized-for-k8s or
  given manifests. This phase is the platform only, matching the
  project's phased plan (Phase 5 is the platform; wiring MyFarm's own
  services onto it is a separate, later step).
- Hubble UI/relay are enabled but not yet opened and looked at for real.
- No Kyverno/Trivy/Cosign (Phase 7) or Argo CD/GitHub Actions (Phase 6) yet.
