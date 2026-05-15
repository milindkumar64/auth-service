FROM maven:3.9-eclipse-temurin-21

WORKDIR /app

COPY . .

RUN mvn clean package -DskipTests

ENTRYPOINT ["java","-jar","target/auth-service.jar"]