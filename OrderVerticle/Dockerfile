FROM openjdk:17-slim
EXPOSE 8888
VOLUME /resources/orders

WORKDIR /app
ADD target/OrderVerticle-1.0.0-SNAPSHOT-fat.jar app.jar
CMD ["java","-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5006", "-jar", "/app/app.jar"]
