FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /workspace
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B -DskipTests dependency:go-offline
COPY src src
RUN ./mvnw -B -DskipTests package

FROM eclipse-temurin:17-jre-alpine
RUN addgroup -S lifemaster && adduser -S lifemaster -G lifemaster
WORKDIR /app
COPY --from=build /workspace/target/life-master-0.0.1-SNAPSHOT.jar app.jar
USER lifemaster
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]
