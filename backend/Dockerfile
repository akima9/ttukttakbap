# 백엔드 빌드 → 실행 멀티스테이지. eclipse-temurin은 arm64 멀티아치라 Oracle ARM VM에서도 동작.
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
COPY . .
RUN chmod +x gradlew && ./gradlew --no-daemon clean bootJar

FROM eclipse-temurin:17-jre
WORKDIR /app
# bootJar 산출물만 복사(-plain.jar 제외). version=0.0.1-SNAPSHOT.
COPY --from=build /app/build/libs/*-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
