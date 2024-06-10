FROM amazoncorretto:17

ARG JAR_FILE=build/libs/dart-0.0.1-SNAPSHOT.jar

COPY ${JAR_FILE} /app.jar

COPY src/main/resources/application-dev.yml /config/application-dev.yml

ENTRYPOINT ["java", "-Dspring.config.location=file:/config/application-dev.yml", "-Dspring.profiles.active=dev", "-jar", "/app.jar"]
