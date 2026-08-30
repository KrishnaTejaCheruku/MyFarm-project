#!/bin/bash
# Stands up the Phase 5 Kubernetes platform: a 3-node k3s cluster (via k3d,
# since k3s itself needs a real Linux kernel and this runs on a Mac) with
# Cilium as CNI (kube-proxy replacement + Hubble) and Traefik wired to the
# Gateway API instead of classic Ingress.
#
# ORDER MATTERS. Confirmed against a real k3d/Cilium bug thread
# (k3d-io/k3d#580): if Traefik (or anything else needing pod networking)
# starts before Cilium is actually Ready, it never recovers on its own --
# the whole cluster is created with flannel/traefik/servicelb disabled,
# Cilium goes in first and we wait for it, then everything else.
#
# NOT running kube-proxy-free (a deliberate change from the first attempt):
# Cilium's kube-proxy replacement (socket-based load balancing for
# ClusterIP routing) failed to work in this environment -- coredns and
# hubble-relay both timed out reaching other pods' ClusterIPs, tracing to
# Cilium's socket-LB cgroup BPF programs not attaching correctly, a real
# documented issue in nested-container setups (cilium/cilium#42659 shows
# the identical symptom). A cgroup.autoMount/hostRoot Helm fix was tried
# live and did not resolve it after 6+ minutes. Rather than keep chasing
# eBPF/cgroup internals in a Docker-Desktop-in-Docker environment, this
# keeps k3s's normal kube-proxy running and lets Cilium be "just" the
# CNI -- a completely standard, well-supported Cilium mode, just not the
# fully kube-proxy-free one originally intended.
set -euxo pipefail

CLUSTER_NAME="${CLUSTER_NAME:-myfarm}"
CILIUM_VERSION="1.19.3"        # confirmed current at https://github.com/cilium/cilium/releases
GATEWAY_API_VERSION="v1.6.1"   # confirmed current at https://github.com/kubernetes-sigs/gateway-api/releases

command -v k3d >/dev/null || { echo "k3d not found -- brew install k3d"; exit 1; }
command -v helm >/dev/null || { echo "helm not found -- brew install helm"; exit 1; }
command -v kubectl >/dev/null || { echo "kubectl not found -- brew install kubectl"; exit 1; }

echo "=== 1/6: creating 3-node k3d cluster (1 server + 2 agents), flannel/kube-proxy/traefik/servicelb disabled ==="
k3d cluster create "${CLUSTER_NAME}" \
  --servers 1 --agents 2 \
  --k3s-arg "--flannel-backend=none@server:0" \
  --k3s-arg "--disable-network-policy@server:0" \
  --k3s-arg "--disable=traefik@server:0" \
  --k3s-arg "--disable=servicelb@server:0" \
  --wait

kubectl config use-context "k3d-${CLUSTER_NAME}"

echo "=== 2/6: installing Cilium (this is what gives pods networking at all -- nothing else can start until this is Ready) ==="
helm repo add cilium https://helm.cilium.io/ >/dev/null
helm repo update cilium >/dev/null

# k8sServiceHost verified working live (k3d node containers resolve each
# other by container name over Docker's embedded DNS, confirmed via
# Cilium's own logs: "Connected to apiserver"). kubeProxyReplacement is
# deliberately NOT set here -- see the comment above main(); k3s's own
# kube-proxy (left enabled at cluster-create time) handles ClusterIP
# routing instead of Cilium's socket-LB path.
helm install cilium cilium/cilium --version "${CILIUM_VERSION}" \
  --namespace kube-system \
  --set operator.replicas=1 \
  --set ipam.operator.clusterPoolIPv4PodCIDRList="10.42.0.0/16" \
  --set k8sServiceHost="k3d-${CLUSTER_NAME}-server-0" \
  --set k8sServicePort=6443 \
  --set hubble.enabled=true \
  --set hubble.relay.enabled=true \
  --set hubble.ui.enabled=true

echo "=== 3/6: waiting for Cilium to actually be Ready (not just Running) ==="
kubectl -n kube-system rollout status daemonset/cilium --timeout=180s
kubectl -n kube-system rollout status deployment/cilium-operator --timeout=120s

echo "=== 4/6: waiting for all 3 nodes to go Ready now that CNI exists ==="
kubectl wait --for=condition=Ready nodes --all --timeout=120s
kubectl get nodes -o wide

echo "=== 5/6: installing Gateway API CRDs, then Traefik as the Gateway controller ==="
kubectl apply -f "https://github.com/kubernetes-sigs/gateway-api/releases/download/${GATEWAY_API_VERSION}/standard-install.yaml"

helm repo add traefik https://traefik.github.io/charts >/dev/null
helm repo update traefik >/dev/null
helm install traefik traefik/traefik \
  -f "$(dirname "$0")/../gateway/traefik-values.yaml" \
  --wait --timeout 120s

echo "=== 6/6: deploying the demo app + HTTPRoute to prove the full chain routes traffic ==="
kubectl apply -f "$(dirname "$0")/../demo/nginx-demo.yaml"
kubectl apply -f "$(dirname "$0")/../gateway/httproute-demo.yaml"
kubectl rollout status deployment/nginx-demo --timeout=60s

cat <<MSG

Bootstrap done. Verify with:
  kubectl get pods -A
  kubectl get gateway,httproute -A
  kubectl port-forward -n default svc/traefik 8000:80 &
  curl -H "Host: nginx-demo.localhost" http://localhost:8000/
  kubectl port-forward -n kube-system svc/hubble-ui 8001:80 &   # then open http://localhost:8001

MSG
