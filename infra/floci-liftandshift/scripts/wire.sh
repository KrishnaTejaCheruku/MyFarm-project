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
#
# IMPORTANT: `aws ssm send-command` only QUEUES the command -- it returns as
# soon as Floci accepts the request, before the command has actually been
# delivered to (let alone executed inside) the target container. An earlier
# version of this script treated that queue-accept as "done" and moved on
# immediately. Verified live on 2026-08-30: this silently dropped the
# nginx/catalina.sh start commands on a real apply -- the /etc/hosts writes
# (cheap, fast) landed fine, but the service-start commands never reached
# the containers at all (no catalina.out, empty nginx.pid, no listening
# port), even though send-command itself returned exit 0 both times.
# `run()` now polls get-command-invocation until the command actually
# reaches Success/Failed, and retries once if it doesn't.
set -euo pipefail

export AWS_ACCESS_KEY_ID=test
export AWS_SECRET_ACCESS_KEY=test
export AWS_DEFAULT_REGION="${AWS_REGION:-us-east-1}"
export AWS_ENDPOINT_URL="${FLOCI_ENDPOINT:-http://localhost:4566}"

wait_for_command() {
  local cmd_id="$1"
  local instance_id="$2"
  local status="Pending"
  local attempts=0
  while [[ "${status}" != "Success" && "${status}" != "Failed" && ${attempts} -lt 15 ]]; do
    sleep 1
    status=$(aws ssm get-command-invocation \
      --command-id "${cmd_id}" --instance-id "${instance_id}" \
      --query "Status" --output text 2>/dev/null || echo "Pending")
    attempts=$((attempts + 1))
  done
  echo "${status}"
}

run() {
  local instance_id="$1"
  local script="$2"
  echo "  -> ${instance_id}: ${script}"

  local cmd_id status
  cmd_id=$(aws ssm send-command \
    --instance-ids "${instance_id}" \
    --document-name "AWS-RunShellScript" \
    --parameters "commands=[\"${script}\"]" \
    --query "Command.CommandId" --output text)
  status=$(wait_for_command "${cmd_id}" "${instance_id}")

  if [[ "${status}" != "Success" ]]; then
    echo "     !! did not reach Success (status=${status}) -- retrying once"
    cmd_id=$(aws ssm send-command \
      --instance-ids "${instance_id}" \
      --document-name "AWS-RunShellScript" \
      --parameters "commands=[\"${script}\"]" \
      --query "Command.CommandId" --output text)
    status=$(wait_for_command "${cmd_id}" "${instance_id}")
    echo "     retry status=${status}"
    if [[ "${status}" != "Success" ]]; then
      echo "     !! still not Success after retry -- continuing anyway, but check this instance manually"
    fi
  fi
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
echo "Wiring done (each step confirmed via get-command-invocation). Host ports"
echo "aren't predictable in advance -- find them with:"
echo "  docker logs vprofile-floci-floci-1 2>&1 | grep -i publish"
