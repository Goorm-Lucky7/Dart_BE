FROM amazoncorretto:17

ARG JAR_FILE=build/libs/dart-0.0.1-SNAPSHOT.jar

COPY ${JAR_FILE} /dart.jar

ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=dev", "/dart.jar"]
