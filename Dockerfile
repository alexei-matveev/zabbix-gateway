#
# You need to first build the Uberjar with
#
#     lein uberjar
#
# then
#
#     docker build -t f0bec0d/zabbix-gateway .
#
# and push it to Docker Hub:
#
#     docker login
#     docker push f0bec0d/zabbix-gateway
#
# Note the whitelisting in .dockerignore.
#
FROM openjdk:8-jre-alpine
WORKDIR /app

# FWIW, uberjars, created  with lein uberjar, or all of  them (?)  are
# not "stable". Rebuild  with "lein uberjar" changes the  hash. So the
# image will always be rebuilt after a lein uberjar:
COPY /target/zabbix-gateway.jar /app/app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
