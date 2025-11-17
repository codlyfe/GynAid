# GynAid Deployment Guide

## Overview

This guide provides step-by-step instructions for deploying the GynAid application using:
- **Backend**: Railway (Spring Boot + PostgreSQL)
- **Frontend**: Vercel (React + TypeScript)
- **Database**: PostgreSQL (Railway)

## Architecture

```
Frontend (Vercel) ←→ Backend API (Railway) ←→ PostgreSQL (Railway)
     React/TS              Spring Boot            Database
```

## Prerequisites

- [ ] GitHub account
- [ ] Railway account (signup with GitHub)
- [ ] Vercel account (signup with GitHub)
- [ ] Your code pushed to a GitHub repository

## Phase 1: Backend Deployment on Railway

### 1.1 Prepare Backend for Deployment

#### Update Application Properties
Create/update `GynAid-backend/src/main/resources/application-production.yml`:

```yaml
spring:
  profiles:
    active: production
  datasource:
    url: ${DATABASE_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: false
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 10MB

server:
  port: ${PORT:8080}
  servlet:
    context-path: /

# JWT Configuration
app:
  jwt:
    secret: ${JWT_SECRET:your-super-secret-jwt-key-change-in-production}
    expiration: 86400000 # 24 hours

# Logging
logging:
  level:
    com.gynaid: INFO
    org.springframework.security: WARN
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: /app/logs/application.log

# CORS Configuration
cors:
  allowed-origins: ${ALLOWED_ORIGINS:*}
  allowed-methods: GET,POST,PUT,DELETE,OPTIONS,PATCH
  allowed-headers: '*'
  allow-credentials: true
```

#### Update Application.java for Port Configuration
Ensure your main application class handles port configuration:

```java
package com.gynaid;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.ConfigurableEnvironment;

@SpringBootApplication
public class GynAidBackendApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(GynAidBackendApplication.class);
        ConfigurableEnvironment env = app.run(args).getEnvironment();
        
        String port = env.getProperty("server.port", "8080");
        String contextPath = env.getProperty("server.servlet.context-path", "");
        
        System.out.println("""
            
            ====================================
            Application Started Successfully!
            ====================================
            Local URL: http://localhost:%s%s
            ====================================
            """, port, contextPath);
    }
}
```

### 1.2 Deploy to Railway

1. **Push Code to GitHub**
   ```bash
   git add .
   git commit -m "Prepare for Railway deployment"
   git push origin main
   ```

2. **Connect Railway to GitHub**
   - Go to [railway.app](https://railway.app)
   - Sign up/login with GitHub
   - Click "New Project"
   - Select "Deploy from GitHub repo"
   - Choose your GynAid repository

3. **Add PostgreSQL Database**
   - In Railway dashboard, click "New" → "Database" → "Add PostgreSQL"
   - Railway will generate DATABASE_URL automatically

4. **Configure Backend Service**
   - Click on your backend service in Railway
   - Go to "Settings" → "Service"
   - Set build command: `mvn clean package -DskipTests`
   - Set start command: `java -jar target/gynaid-backend-0.0.1.jar`

5. **Add Environment Variables**
   Go to "Variables" tab and add:
   ```
   SPRING_PROFILES_ACTIVE=production
   JWT_SECRET=your-super-secret-jwt-key-minimum-256-bits
   ALLOWED_ORIGINS=https://your-vercel-app.vercel.app
   PORT=8080
   ```

6. **Deploy**
   - Railway will automatically deploy
   - Monitor logs in the "Deploy" tab
   - Note your Railway app URL (e.g., `https://gynaid-backend-production.up.railway.app`)

## Phase 2: Frontend Deployment on Vercel

### 2.1 Prepare Frontend for Deployment

#### Update Environment Configuration
Create `GynAid-frontend/.env.production`:

```env
VITE_API_URL=https://your-railway-app-url.up.railway.app
VITE_APP_NAME=GynAid
VITE_APP_VERSION=1.0.0
```

#### Update API Configuration
Ensure `GynAid-frontend/src/config/api.ts` uses environment variables:

```typescript
export const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

export const apiConfig = {
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
};

export const endpoints = {
  auth: {
    login: '/api/auth/login',
    register: '/api/auth/register',
    logout: '/api/auth/logout',
    refresh: '/api/auth/refresh',
  },
  providers: {
    search: '/api/providers/search',
    specialization: '/api/providers/specialization',
    topRated: '/api/providers/top-rated',
    nearby: '/api/providers/nearby',
    available: '/api/providers/available',
  },
  // Add other endpoints as needed
};
```

### 2.2 Deploy to Vercel

1. **Install Vercel CLI**
   ```bash
   npm install -g vercel
   ```

2. **Login to Vercel**
   ```bash
   vercel login
   ```

3. **Deploy Frontend**
   ```bash
   cd GynAid-frontend
   vercel --prod
   ```

4. **Configure Environment Variables**
   - Go to Vercel dashboard
   - Select your project
   - Go to "Settings" → "Environment Variables"
   - Add:
     ```
     VITE_API_URL=https://your-railary-app-url.up.railway.app
     VITE_APP_NAME=GynAid
     VITE_APP_VERSION=1.0.0
     ```

5. **Deploy via GitHub Integration** (Alternative)
   - Go to [vercel.com](https://vercel.com)
   - Import your GitHub repository
   - Configure build settings:
     - Framework: Vite
     - Build Command: `npm run build`
     - Output Directory: `dist`
   - Add environment variables in Vercel dashboard

## Phase 3: Database Setup and Migrations

### 3.1 Initial Database Setup
Railway automatically creates your PostgreSQL database. You need to:

1. **Get Database Connection Details**
   - In Railway dashboard, go to your PostgreSQL database
   - Copy the `DATABASE_URL` from the Connect tab

2. **Run Database Migrations**
   - Railway will automatically run your Hibernate DDL
   - Alternatively, connect using Railway CLI:
   ```bash
   npm install -g @railway/cli
   railway login
   railway link
   railway database connect
   ```

3. **Create Initial Data** (if needed)
   Run any necessary seed scripts or use the application's data initialization features.

### 3.2 Database Backup and Recovery
- Railway provides automatic daily backups
- To create manual backup: Railway Dashboard → Database → Backup
- To restore: Railway Dashboard → Database → Restore

## Phase 4: Environment Configuration

### 4.1 Railway Environment Variables
```
SPRING_PROFILES_ACTIVE=production
DATABASE_URL=postgresql://user:pass@host:port/dbname
DB_USERNAME=your_db_username
DB_PASSWORD=your_db_password
JWT_SECRET=your-super-secret-jwt-key-256-bits-minimum
ALLOWED_ORIGINS=https://your-app.vercel.app,https://your-custom-domain.com
PORT=8080
```

### 4.2 Vercel Environment Variables
```
VITE_API_URL=https://your-railway-backend.up.railway.app
VITE_APP_NAME=GynAid
VITE_APP_VERSION=1.0.0
VITE_APP_ENV=production
```

## Phase 5: Custom Domain Setup (Optional)

### 5.1 Railway Custom Domain
1. Go to Railway service → Settings → Domains
2. Add custom domain (e.g., `api.gynaid.com`)
3. Update DNS records to point to Railway
4. Update CORS settings in Railway environment variables

### 5.2 Vercel Custom Domain
1. Go to Vercel project → Settings → Domains
2. Add custom domain (e.g., `app.gynaid.com`)
3. Configure DNS records
4. Update API URL in environment variables

## Phase 6: Monitoring and Troubleshooting

### 6.1 Backend Monitoring (Railway)
- **Logs**: Railway Dashboard → Service → Logs
- **Metrics**: Railway Dashboard → Service → Metrics
- **Database**: Railway Dashboard → Database → Connect

### 6.2 Frontend Monitoring (Vercel)
- **Analytics**: Vercel Dashboard → Analytics
- **Functions**: Vercel Dashboard → Functions (if using serverless)
- **Edge Network**: Vercel Dashboard → Edge Network

### 6.3 Common Issues and Solutions

#### Backend Issues
1. **Port Binding Error**
   ```yaml
   # Ensure server.port is configured
   server:
     port: ${PORT:8080}
   ```

2. **Database Connection Error**
   ```yaml
   # Verify DATABASE_URL format
   spring:
     datasource:
       url: ${DATABASE_URL}
       driver-class-name: org.postgresql.Driver
   ```

3. **CORS Issues**
   ```yaml
   # Add frontend domain to allowed origins
   cors:
     allowed-origins: https://your-app.vercel.app
   ```

#### Frontend Issues
1. **API Connection Failed**
   - Verify `VITE_API_URL` points to your Railway backend
   - Check CORS settings in backend
   - Ensure backend is deployed and accessible

2. **Build Failures**
   ```bash
   # Test build locally
   npm run build
   # Check for TypeScript errors
   npm run type-check
   ```

## Phase 7: Production Checklist

### Backend
- [ ] Database migrations run successfully
- [ ] Environment variables configured
- [ ] CORS properly configured
- [ ] JWT secrets changed from defaults
- [ ] Logging configured for production
- [ ] Health checks responding
- [ ] SSL/TLS enabled (Railway provides this)

### Frontend
- [ ] Environment variables set
- [ ] Build process successful
- [ ] API URLs updated to production
- [ ] Performance optimizations applied
- [ ] Error tracking configured
- [ ] Analytics tracking added

### Database
- [ ] Connection pool configured
- [ ] Backup strategy in place
- [ ] Monitoring alerts set up
- [ ] SSL connections enforced

## Phase 8: Deployment Commands Summary

### Quick Deployment Commands
```bash
# Backend Deployment (Railway)
cd GynAid-backend
git add .
git commit -m "Deploy to production"
git push origin main
# Railway auto-deploys from main branch

# Frontend Deployment (Vercel)
cd GynAid-frontend
npm run build
vercel --prod
```

## Support and Resources

- **Railway Documentation**: [docs.railway.app](https://docs.railway.app)
- **Vercel Documentation**: [vercel.com/docs](https://vercel.com/docs)
- **PostgreSQL on Railway**: [railway.app/postgresql](https://railway.app/postgresql)

## Cost Optimization Tips

1. **Railway**: Monitor usage and scale appropriately
2. **Vercel**: Optimize build size and use edge network
3. **Database**: Use connection pooling and optimize queries
4. **CDN**: Leverage Vercel's global edge network for static assets

This deployment setup provides a robust, scalable architecture with minimal operational overhead while maintaining high performance and reliability.