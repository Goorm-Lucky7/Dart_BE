FROM amazoncorretto:17

ARG JAR_FILE=build/libs/dart-0.0.1-SNAPSHOT.jar

COPY wait-for-it.sh /wait-for-it.sh

COPY ${JAR_FILE} /dart.jar

RUN chmod +x /wait-for-it.sh

ENTRYPOINT ["/wait-for-it.sh", "mysql-container:3306", "--", "java", "-jar", "-Dspring.profiles.active=dev", "/dart.jar"]
