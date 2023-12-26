FROM openjdk:17-slim
EXPOSE 8888
VOLUME /app
ADD target/OrderVerticle-1.0.0-SNAPSHOT-fat.jar app.jar
CMD ["java", "-jar", "/app.jar"]
