#
# You need to first build the Uberjar with
#
#     lein uberjar
#
# Also note the whitelisting in .dockerignore.
#
FROM openjdk:8-jre-alpine
WORKDIR /app
COPY /target/zabbix-gateway.jar /app/app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
