FROM openjdk:18-jdk-alpine3.15
WORKDIR /app
COPY target/check-notification.jar /app/
ENTRYPOINT ["java","-jar","check-notification.jar"]