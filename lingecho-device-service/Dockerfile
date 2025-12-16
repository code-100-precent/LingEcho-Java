FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/lingecho-device-service-*.jar app.jar

EXPOSE 8089

ENTRYPOINT ["java", "-jar", "app.jar"]

