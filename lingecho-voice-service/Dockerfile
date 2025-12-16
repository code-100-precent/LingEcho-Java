FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/lingecho-voice-service-*.jar app.jar

EXPOSE 8084

ENTRYPOINT ["java", "-jar", "app.jar"]

