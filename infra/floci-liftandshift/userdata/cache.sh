#!/bin/bash
set -euxo pipefail

dnf install -y memcached
memcached -d -m 128 -I 4m -u root -l 0.0.0.0 -p 11211

echo "cache.sh done" > /tmp/cache-userdata-complete
