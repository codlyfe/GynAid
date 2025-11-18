# Use Liberica JDK 21 (Alpine-based, smaller image)
FROM bellsoft/liberica-openjdk-alpine:21

WORKDIR /app

# Copy the built JAR into the container
COPY target/*.jar app.jar

# Expose port
EXPOSE 8080

# Health check (install curl first)
RUN apk add --no-cache curl
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application
CMD ["java", "-jar", "app.jar"]
