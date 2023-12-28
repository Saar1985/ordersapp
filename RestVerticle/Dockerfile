FROM openjdk:17-slim
EXPOSE 7777


# Declare the /resources/authentication directory as a volume
VOLUME /resources/authentication

WORKDIR /app
# Add the application JAR
ADD target/RestVerticle-1.0.0-SNAPSHOT-fat.jar app.jar
COPY /src/main/resources/default_permissions.csv .
# Run the application
CMD ["java", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005", "-jar", "/app/app.jar", "-cluster"]
