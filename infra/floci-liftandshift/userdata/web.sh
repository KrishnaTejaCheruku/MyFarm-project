#!/bin/bash
# Deliberately does NOT start nginx: the upstream "app01:8080" can't resolve
# until app01's private IP is known, which doesn't exist yet at this
# instance's own boot time. wire.sh writes /etc/hosts for app01 and starts
# nginx once app01's IP exists.
set -euxo pipefail

dnf install -y nginx

cat > /etc/nginx/conf.d/vprofile.conf <<'NGINXCONF'
upstream vprofile_app {
    server app01:8080;
    keepalive 16;
}

server {
    listen 80;
    server_name _;

    location = /nginx-health {
        access_log off;
        default_type text/plain;
        return 200 "ok\n";
    }

    location / {
        proxy_pass http://vprofile_app;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_set_header Connection "";
        proxy_connect_timeout 5s;
        proxy_read_timeout 60s;
    }
}
NGINXCONF
rm -f /etc/nginx/conf.d/default.conf

echo "web.sh done -- nginx left stopped, waiting for wire.sh" > /tmp/web-userdata-complete
