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
# Note the whitelisting in .dockerignore. To run a container issue
#
#     docker run --rm -itd -p 15001:15001 f0bec0d/zabbix-gateway --zabbix-server=172.17.0.1
#
# The default  Zabbix Server address  is localhost, which is  not what
# you may  think inside the container.  The IP address of  your Docker
# Host or rather Zabbix Server Hosts may differ though.
#
FROM openjdk:8-jre-alpine
WORKDIR /app

# FWIW, uberjars, created  with lein uberjar, or all of  them (?)  are
# not "stable". Rebuild  with "lein uberjar" changes the  hash. So the
# image will always be rebuilt after a lein uberjar:
COPY /target/zabbix-gateway.jar /app/app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]

# This is the reverse of the Zabbix server port 10051:
EXPOSE 15001/tcp
