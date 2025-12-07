# Use Temurin 17
FROM eclipse-temurin:17-jdk-jammy

# Install Maven
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy pom and source code
COPY pom.xml .
COPY src ./src

# Build application
RUN mvn clean package -DskipTests

# Expose Spring Boot port
EXPOSE 8080

# Run the Spring Boot application
CMD ["sh", "-c", "java -jar target/*.jar"]
