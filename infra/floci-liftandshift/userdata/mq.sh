#!/bin/bash
# Runs on Ubuntu 24.04 (not Amazon Linux 2023 -- see variables.tf mq_ami_id
# for why). No systemd here either: Floci's ami-ubuntu2404 catalog entry uses
# the same plain "tail -f /dev/null" container lifecycle as amazonlinux2023,
# so RabbitMQ is started as a background process directly, not via
# "systemctl start rabbitmq-server".
set -euxo pipefail
export DEBIAN_FRONTEND=noninteractive

apt-get update
apt-get install -y software-properties-common >/dev/null 2>&1 || true
add-apt-repository universe -y >/dev/null 2>&1 || true
apt-get update
apt-get install -y erlang rabbitmq-server

rabbitmq-server -detached
for i in $(seq 1 30); do
  rabbitmqctl status >/dev/null 2>&1 && break
  sleep 1
done

rabbitmq-plugins enable rabbitmq_management

rabbitmqctl add_user ${rabbitmq_user} ${rabbitmq_password} || true
rabbitmqctl set_user_tags ${rabbitmq_user} administrator
rabbitmqctl set_permissions -p / ${rabbitmq_user} ".*" ".*" ".*"
rabbitmqctl delete_user guest || true

echo "mq.sh done" > /tmp/mq-userdata-complete
