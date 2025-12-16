FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/lingecho-assistant-service-*.jar app.jar

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "app.jar"]

