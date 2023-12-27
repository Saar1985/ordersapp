FROM openjdk:17-slim
EXPOSE 8889
VOLUME /app
ADD target/RestVerticle-1.0.0-SNAPSHOT-fat.jar app.jar
CMD ["java", "-jar", "/app.jar"]
