FROM java:8

WORKDIR /opt/demo_app

COPY tmp/ ./

COPY jmx_prometheus_javaagent-0.15.0.jar ./
COPY config.yml ./

CMD ["/bin/sh", "-c", "java -javaagent:./jmx_prometheus_javaagent-0.15.0.jar=8090:config.yml -jar kafka-*.jar --spring.config.location=application.yml"]
EXPOSE 8090

