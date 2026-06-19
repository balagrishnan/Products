# Step 1: Use an officially supported, lightweight Java 17 runtime image
FROM eclipse-temurin:17-jre-alpine

# Step 2: Set the working directory inside the container
WORKDIR /app

# Step 3: Automatically find and copy any compiled .jar file from the target directory
COPY target/*.jar app.jar

# Step 4: Expose the port your application runs on
EXPOSE 8081

# Step 5: Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]