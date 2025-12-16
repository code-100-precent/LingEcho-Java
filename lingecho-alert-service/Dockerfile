FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/lingecho-alert-service-*.jar app.jar

EXPOSE 8088

ENTRYPOINT ["java", "-jar", "app.jar"]

