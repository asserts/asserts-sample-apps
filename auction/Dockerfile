FROM openjdk:8-jdk-alpine
COPY build/libs/auction.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]