# Simple runtime Dockerfile. Build the JAR first, then build this image.
FROM eclipse-temurin:17-jre
ARG JAR_FILE=build/libs/gestion-perfiles-0.0.1-SNAPSHOT.jar
WORKDIR /app
COPY ${JAR_FILE} app.jar
EXPOSE 8083
ENV JAVA_OPTS=""
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
