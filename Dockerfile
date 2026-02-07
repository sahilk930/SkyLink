# ---- Build Stage ----
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# ---- Runtime Stage ----
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/target/sky-link-1.0-SNAPSHOT.jar app.jar

EXPOSE 4567
ENV PORT=4567
CMD ["java", "-jar", "app.jar"]

