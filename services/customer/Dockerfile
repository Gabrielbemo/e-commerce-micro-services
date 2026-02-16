#build
FROM maven:3.9.12-eclipse-temurin-25-alpine AS build

WORKDIR /app

COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src

RUN mvn -B clean package -DskipTests

#runtime
FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8090

USER spring

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-jar", "app.jar"]