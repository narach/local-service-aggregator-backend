### Build stage
FROM gradle:8.8.1-jdk21-alpine AS build
WORKDIR /workspace
COPY . .
RUN gradle -PskipTests clean bootJar

### Runtime stage (~80 MB)
FROM gcr.io/distroless/java21-debian12
USER nonroot
WORKDIR /app
COPY --from=build /workspace/build/libs/*.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]
