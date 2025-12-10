FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

COPY mvnw .
COPY pom.xml .
COPY .mvn .mvn

RUN ./mvnw dependency:go-offline -B

COPY src ./src

RUN ./mvnw clean package -DskipTests

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "target/hrms-backend-0.0.1-SNAPSHOT.jar"]