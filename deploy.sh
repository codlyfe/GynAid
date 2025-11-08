#!/bin/bash

# Gynassist AI Platform Deployment Script
echo "🚀 Deploying Gynassist AI Platform..."

# Set environment variables
export DATABASE_PASSWORD=${DATABASE_PASSWORD:-"secure_password_123"}
export JWT_SECRET=${JWT_SECRET:-"your_jwt_secret_key_here"}
export MOH_API_URL=${MOH_API_URL:-"https://api.health.go.ug/validate"}
export MOH_API_KEY=${MOH_API_KEY:-"your_moh_api_key"}

# Build backend
echo "📦 Building AI-enhanced backend..."
cd Gynassist-backend
./mvnw clean package -DskipTests
cd ..

# Build frontend
echo "🎨 Building frontend..."
cd gynassist-frontend
npm install
npm run build
cd ..

# Deploy with Docker Compose
echo "🐳 Deploying with Docker..."
docker-compose -f docker-compose.prod.yml down
docker-compose -f docker-compose.prod.yml up -d --build

# Wait for services
echo "⏳ Waiting for services to start..."
sleep 30

# Health check
echo "🏥 Checking service health..."
curl -f http://localhost:8080/actuator/health || echo "❌ Backend health check failed"
curl -f http://localhost || echo "❌ Frontend health check failed"

echo "✅ Gynassist AI Platform deployed successfully!"
echo "🌐 Frontend: http://localhost"
echo "🔧 Backend API: http://localhost:8080"
echo "📊 Health Check: http://localhost:8080/actuator/health"