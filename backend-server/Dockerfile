
FROM postgres:latest

FROM maven:3.9.9 AS build
COPY pom.xml .
COPY /src ./src/
RUN mvn clean package -DskipTests

FROM openjdk:17 AS prod
COPY --from=build target/backend-server-0.0.1-SNAPSHOT.jar backend-server.jar
EXPOSE 9000

ENTRYPOINT ["java", "-jar", "backend-server.jar"]