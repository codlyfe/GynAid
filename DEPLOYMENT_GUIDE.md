# Gynassist AI Platform Deployment Guide

## 🚀 **Quick Deployment**

### **Prerequisites**
- Docker & Docker Compose installed
- Node.js 18+ (for frontend build)
- Java 21+ (for backend build)

### **1. Environment Setup**
```bash
# Copy environment template
cp .env.example .env

# Edit environment variables
nano .env
```

### **2. Deploy Platform**
```bash
# Linux/macOS
chmod +x deploy.sh
./deploy.sh

# Windows
deploy.bat
```

### **3. Verify Deployment**
- **Frontend**: http://localhost
- **Backend API**: http://localhost:8080
- **Health Check**: http://localhost:8080/actuator/health

## 🔧 **Manual Deployment**

### **Backend Deployment**
```bash
cd Gynassist-backend
./mvnw clean package -DskipTests
java -jar target/backend-*.jar --spring.profiles.active=prod
```

### **Frontend Deployment**
```bash
cd gynassist-frontend
npm install
npm run build
# Serve dist/ folder with web server
```

### **Database Setup**
```bash
# PostgreSQL setup
createdb gynassist_prod
# Flyway migrations run automatically on startup
```

## 🐳 **Docker Deployment**

### **Production Stack**
```bash
# Start all services
docker-compose -f docker-compose.prod.yml up -d

# View logs
docker-compose -f docker-compose.prod.yml logs -f

# Stop services
docker-compose -f docker-compose.prod.yml down
```

### **Individual Services**
```bash
# Backend only
docker build -t gynassist-backend ./Gynassist-backend
docker run -p 8080:8080 gynassist-backend

# Frontend only
docker build -t gynassist-frontend ./gynassist-frontend
docker run -p 80:80 gynassist-frontend
```

## 🌐 **Production Configuration**

### **Environment Variables**
```bash
# Required
DATABASE_PASSWORD=your_secure_password
JWT_SECRET=your_jwt_secret_256_bits
MOH_API_KEY=your_moh_api_key

# Optional
MOH_API_URL=https://api.health.go.ug/validate
PORT=8080
```

### **Database Migration**
- **V1**: Base schema (users, providers)
- **V2**: Health profiles
- **V3**: Gynecological profiles
- **V4**: AI features (insights, analysis)
- **V5**: Advanced AI (matching, trends)
- **V6**: UX features (voice, notifications)

## 📊 **Monitoring & Health**

### **Health Endpoints**
- `/actuator/health` - Application health
- `/actuator/info` - Application info
- `/actuator/metrics` - Application metrics

### **Logging**
- **Console**: Structured JSON logs
- **File**: `logs/gynassist.log`
- **Level**: INFO (production)

## 🔒 **Security Configuration**

### **JWT Security**
- 256-bit secret key required
- 24-hour token expiration
- Secure HTTP-only cookies

### **Database Security**
- PostgreSQL with SSL
- Connection pooling
- Prepared statements

### **API Security**
- CORS configured
- Rate limiting (recommended)
- HTTPS in production

## 🚀 **Scaling & Performance**

### **Horizontal Scaling**
```bash
# Scale backend instances
docker-compose -f docker-compose.prod.yml up -d --scale gynassist-backend=3

# Load balancer configuration needed
```

### **Database Optimization**
- Connection pooling enabled
- Query optimization with indexes
- Read replicas for analytics

### **Caching Strategy**
- Static asset caching (1 year)
- API response caching (Redis recommended)
- Database query caching

## 🔧 **Troubleshooting**

### **Common Issues**

**Backend won't start:**
```bash
# Check database connection
docker-compose logs gynassist-db

# Check environment variables
docker-compose config
```

**Frontend 404 errors:**
```bash
# Check Nginx configuration
docker exec gynassist-frontend-prod cat /etc/nginx/nginx.conf

# Check build output
docker exec gynassist-frontend-prod ls -la /usr/share/nginx/html
```

**Database migration fails:**
```bash
# Check migration status
docker exec gynassist-backend-prod java -jar app.jar --spring.profiles.active=prod --flyway.info

# Manual migration
docker exec gynassist-backend-prod java -jar app.jar --spring.profiles.active=prod --flyway.migrate
```

### **Performance Issues**

**Slow API responses:**
- Check database query performance
- Enable query logging temporarily
- Monitor JVM memory usage

**High memory usage:**
- Adjust JVM heap size: `-Xmx2g`
- Enable garbage collection logging
- Monitor connection pool usage

## 📱 **Mobile & Desktop Deployment**

### **Mobile Apps (React Native)**
```bash
cd gynassist-mobile

# iOS
npx expo build:ios
# Upload to App Store

# Android
npx expo build:android
# Upload to Google Play Store
```

### **Desktop Apps (Electron)**
```bash
cd gynassist-desktop

# Windows
npm run build:win

# macOS
npm run build:mac

# Linux
npm run build:linux
```

## 🌍 **Multi-Environment Setup**

### **Development**
```bash
docker-compose up -d
# Uses H2 database, hot reload enabled
```

### **Staging**
```bash
docker-compose -f docker-compose.staging.yml up -d
# Uses PostgreSQL, production-like setup
```

### **Production**
```bash
docker-compose -f docker-compose.prod.yml up -d
# Full production configuration
```

## 📈 **Monitoring Setup**

### **Application Monitoring**
- Spring Boot Actuator endpoints
- Custom health checks
- Performance metrics

### **Infrastructure Monitoring**
- Docker container health
- Database performance
- System resource usage

### **Error Tracking**
- Structured logging
- Error aggregation (Sentry recommended)
- Alert configuration

---

**Gynassist AI Platform is now ready for production deployment with full AI capabilities, multi-language support, and comprehensive health tracking features.**