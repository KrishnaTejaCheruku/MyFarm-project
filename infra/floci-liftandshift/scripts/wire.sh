#!/bin/bash
# Invoked by null_resource.wire's local-exec once every instance's private IP
# is known. Writes the /etc/hosts entries app01 and web01 need (their
# userdata scripts reference bare hostnames db01/mc01/rmq01/app01, which
# can't resolve at each instance's own boot time -- see the chicken-and-egg
# note in README.md), then starts the two services userdata deliberately
# left stopped.
#
# Uses SSM RunCommand (AWS-RunShellScript), not SSH: confirmed against
# Floci's own SSM docs that RunCommand on a Floci-launched EC2 instance runs
# the script directly inside that instance's container -- no SSM agent or
# IAM instance profile required, unlike real AWS.
set -euo pipefail

export AWS_ACCESS_KEY_ID=test
export AWS_SECRET_ACCESS_KEY=test
export AWS_DEFAULT_REGION="${AWS_REGION:-us-east-1}"
export AWS_ENDPOINT_URL="${FLOCI_ENDPOINT:-http://localhost:4566}"

run() {
  local instance_id="$1"
  local script="$2"
  echo "  -> ${instance_id}: ${script}"
  aws ssm send-command \
    --instance-ids "${instance_id}" \
    --document-name "AWS-RunShellScript" \
    --parameters "commands=[\"${script}\"]" \
    --output text >/dev/null
}

echo "Wiring app01 (${APP_ID})..."
run "${APP_ID}" "echo '${DB_IP} db01' | tee -a /etc/hosts"
run "${APP_ID}" "echo '${CACHE_IP} mc01' | tee -a /etc/hosts"
run "${APP_ID}" "echo '${MQ_IP} rmq01' | tee -a /etc/hosts"
run "${APP_ID}" "su -s /bin/bash tomcat -c '/opt/tomcat/bin/catalina.sh start'"

echo "Wiring web01 (${WEB_ID})..."
run "${WEB_ID}" "echo '${APP_IP} app01' | tee -a /etc/hosts"
run "${WEB_ID}" "nginx"

echo
echo "Wiring done. Host ports aren't predictable in advance -- find them with:"
echo "  docker logs floci 2>&1 | grep Published"
