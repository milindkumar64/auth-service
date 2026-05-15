####Stage 1: The Build Stage

# Use a heavy image that contains all the tools needed to compile
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app
COPY . .
# This creates the .jar file in /app/target/
RUN mvn clean package -DskipTests

# AS build: This aliases the stage so we can reference it later.
# Note: This stage is "disposable." Once it finishes, everything except what we explicitly copy out will be discarded.

####Stage 2: The Final Runtime Stage

# Use a lightweight 'JRE' image (Runtime only, no compiler)
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app
# THE MAGIC LINE: Copy ONLY the jar from the 'build' stage
COPY --from=build /app/target/*.jar auth-service.jar

ENTRYPOINT ["java","-jar","auth-service.jar"]


#--from=build: This is the most important part of the syntax. It tells Docker to reach back into the previous stage and grab a specific file.
#alpine: (Optional tip) Using Alpine-based images further reduces size by using a minimal Linux distribution.