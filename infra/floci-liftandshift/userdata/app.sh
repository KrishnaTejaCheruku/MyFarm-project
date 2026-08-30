#!/bin/bash
# Builds and deploys the vprofile WAR, but deliberately does NOT start Tomcat:
# app01's app config references the bare hostnames "db01"/"mc01"/"rmq01",
# which can't resolve until every other instance's private IP is known --
# impossible at this instance's own boot time. Terraform's null_resource.wire
# (scripts/wire.sh) writes /etc/hosts via SSM RunCommand once every IP exists,
# then starts Tomcat itself.
set -euxo pipefail

dnf install -y git tar java-17-amazon-corretto-devel

# Maven isn't in AL2023's default repos -- pull the same binary distribution
# the app's own Docker image (legacy/vprofile/containers/app/Dockerfile) gets
# for free from its maven:3.9.16 base image, just without a base image to
# inherit it from here.
curl -fsSL https://archive.apache.org/dist/maven/maven-3/3.9.9/binaries/apache-maven-3.9.9-bin.tar.gz -o /tmp/maven.tar.gz
mkdir -p /opt/maven
tar -xzf /tmp/maven.tar.gz -C /opt/maven --strip-components=1
export PATH="/opt/maven/bin:$PATH"

# v9.0.121 to match legacy/vprofile/containers/app/Dockerfile's own
# "FROM tomcat:9.0.121-jre17-temurin-noble" -- and archive.apache.org, not
# dlcdn.apache.org: dlcdn only serves the CURRENT release of each Apache
# project, so a specific point release 404s there the moment a newer one
# ships. This is exactly what happened on the first real run: curl -f
# exited 22 (HTTP error) fetching v9.0.98 from dlcdn, which -- under
# set -e -- silently killed the rest of this script.
curl -fsSL https://archive.apache.org/dist/tomcat/tomcat-9/v9.0.121/bin/apache-tomcat-9.0.121.tar.gz -o /tmp/tomcat.tar.gz
mkdir -p /opt/tomcat
tar -xzf /tmp/tomcat.tar.gz -C /opt/tomcat --strip-components=1

id -u tomcat >/dev/null 2>&1 || useradd --system --home-dir /opt/tomcat --shell /sbin/nologin tomcat

git clone --depth 1 --branch "${vprofile_repo_branch}" "${vprofile_repo_url}" /tmp/repo
cd /tmp/repo/legacy/vprofile
mvn -B -ntp clean verify

rm -rf /opt/tomcat/webapps/*
cp target/vprofile-v2.war /opt/tomcat/webapps/ROOT.war

cat > /opt/tomcat/bin/setenv.sh <<SETENV
export CATALINA_OPTS="\$CATALINA_OPTS \\
  -Djdbc.url='jdbc:mysql://db01:3306/${mysql_database}?useUnicode=true&characterEncoding=UTF-8&zeroDateTimeBehavior=convertToNull&useSSL=false&allowPublicKeyRetrieval=true' \\
  -Djdbc.username=${mysql_user} \\
  -Djdbc.password=${mysql_password} \\
  -Dmemcached.active.host=mc01 \\
  -Dmemcached.active.port=11211 \\
  -Drabbitmq.address=rmq01 \\
  -Drabbitmq.port=5672 \\
  -Drabbitmq.username=${rabbitmq_user} \\
  -Drabbitmq.password=${rabbitmq_password}"
SETENV
chmod +x /opt/tomcat/bin/setenv.sh
chown -R tomcat:tomcat /opt/tomcat

echo "app.sh done -- tomcat left stopped, waiting for wire.sh" > /tmp/app-userdata-complete
