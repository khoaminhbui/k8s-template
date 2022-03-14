FROM openjdk:18-jdk-alpine3.15
WORKDIR /app
COPY target/notification.jar /app/
ENTRYPOINT ["java","-jar","notification.jar"]