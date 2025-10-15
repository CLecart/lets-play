## Multi-stage Dockerfile for Let's Play
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace
COPY . /workspace
RUN ./mvnw -DskipTests package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/target/lets-play-0.0.1-SNAPSHOT.jar /app/app.jar
ENV SPRING_PROFILES_ACTIVE=prod
# Read JWT secret from env APP_JWT_SECRET
ENV APP_JWT_SECRET=change-me
EXPOSE 8080
USER 1000
ENTRYPOINT ["java","-jar","/app/app.jar"]
