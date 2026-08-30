#!/bin/bash
# Runs inside the plain (non-systemd) Amazon Linux 2023 container Floci
# launches for db01 -- no systemctl anywhere in this file, everything is
# started as a background process directly, since Floci's catalog image for
# ami-amazonlinux2023 uses a "tail -f /dev/null" lifecycle, not an init system.
set -euxo pipefail

dnf install -y git mariadb105-server || dnf install -y git mariadb-server

mkdir -p /var/lib/mysql
chown -R mysql:mysql /var/lib/mysql
mysql_install_db --user=mysql --datadir=/var/lib/mysql >/tmp/mysql_install_db.log 2>&1 || true

mysqld_safe --datadir=/var/lib/mysql --bind-address=0.0.0.0 &

for i in $(seq 1 30); do
  mysqladmin ping >/dev/null 2>&1 && break
  sleep 1
done

mysql -u root <<SQL
CREATE DATABASE IF NOT EXISTS \`${mysql_database}\`;
CREATE USER IF NOT EXISTS '${mysql_user}'@'%' IDENTIFIED BY '${mysql_password}';
GRANT ALL PRIVILEGES ON \`${mysql_database}\`.* TO '${mysql_user}'@'%';
ALTER USER 'root'@'localhost' IDENTIFIED BY '${mysql_root_password}';
FLUSH PRIVILEGES;
SQL

# Seed with the exact same dump the containerized dev stack uses
# (legacy/vprofile/src/main/resources/db_backup.sql), by cloning the same
# repo/branch app.sh builds from -- one source of truth for both.
git clone --depth 1 --branch "${vprofile_repo_branch}" "${vprofile_repo_url}" /tmp/repo
mysql -u root "${mysql_database}" < /tmp/repo/legacy/vprofile/src/main/resources/db_backup.sql

echo "db.sh done" > /tmp/db-userdata-complete
