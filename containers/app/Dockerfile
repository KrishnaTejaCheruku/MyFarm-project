# syntax=docker/dockerfile:1.7

FROM maven:3.9.16-eclipse-temurin-17-noble AS build
WORKDIR /workspace

COPY pom.xml ./

COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -ntp clean verify

FROM tomcat:9.0.121-jre17-temurin-noble

RUN rm -rf "${CATALINA_HOME}/webapps"/* \
    && (getent group tomcat >/dev/null || groupadd --system tomcat) \
    && (id -u tomcat >/dev/null 2>&1 \
        || useradd --system --gid tomcat --home-dir "${CATALINA_HOME}" --shell /usr/sbin/nologin tomcat) \
    && chown -R tomcat:tomcat "${CATALINA_HOME}"

COPY --from=build --chown=tomcat:tomcat \
    /workspace/target/vprofile-v2.war \
    ${CATALINA_HOME}/webapps/ROOT.war

USER tomcat
EXPOSE 8080
