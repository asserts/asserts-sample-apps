FROM java:8

WORKDIR /opt/demo_app

COPY tmp/ ./

CMD ["/bin/sh", "-c", "java -jar kafka-*.jar --spring.config.location=application.yml"]
EXPOSE 8090

