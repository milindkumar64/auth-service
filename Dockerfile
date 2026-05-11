FROM openjdk:27-ea-trixie
ADD target/auth-service.jar  /tmp/auth-service.jar
ENTRYPOINT ["java", "-jar","/tmp/auth-service.jar"]