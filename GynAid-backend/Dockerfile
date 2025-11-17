# Use OpenJDK 17 with Alpine for smaller image size
FROM openjdk:17-jdk-alpine

# Set working directory
WORKDIR /app

# Install Maven
RUN apk add --no-cache maven

# Copy Maven files
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Make Maven wrapper executable
RUN chmod +x ./mvnw

# Download dependencies (this layer will be cached)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN ./mvnw clean package -DskipTests

# Expose port
EXPOSE 8080

# Add health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application
CMD ["java", "-jar", "target/gynaid-backend-0.0.1.jar"]