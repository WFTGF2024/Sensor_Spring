FROM gradle:8.10.0-jdk17 AS build
WORKDIR /home/gradle/project
COPY . .
RUN gradle clean bootJar --no-daemon

FROM eclipse-temurin:17-jre
ENV TZ=Asia/Shanghai \
    JAVA_OPTS="-XX:+UseContainerSupport -XX:InitialRAMPercentage=50.0 -XX:MaxRAMPercentage=75.0"
WORKDIR /app
COPY --from=build /home/gradle/project/build/libs/*.jar /app/app.jar
RUN useradd -ms /bin/bash appuser
USER appuser
EXPOSE 8080
ENTRYPOINT ["sh","-lc","java $JAVA_OPTS -jar /app/app.jar"]
