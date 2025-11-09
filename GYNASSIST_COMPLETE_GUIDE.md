# Gynassist - Complete Project Guide

## 🌟 Project Overview

Gynassist is a comprehensive cross-platform reproductive health application supporting women across Uganda with specialized care for conditions like infertility, endometriosis, cycle complications, and reproductive health disorders.

### Core Features
- **AI Health Assistant** - Text and voice-enabled chatbot for health guidance
- **Cycle Tracker** - Comprehensive menstrual cycle monitoring with predictions
- **Video Consultations** - Connect with verified gynecologists and specialists
- **Payment Integration** - Mobile Money (MTN/Airtel), Stripe, and Bank Transfer support
- **MOH Notifications** - Real-time alerts from Uganda's Ministry of Health via DHIS2
- **Health Tips & Emergency Care** - Evidence-based health information and first aid
- **Location-based Provider Search** - Find nearby healthcare professionals

## 🚀 Quick Start

### Prerequisites
- Node.js 18+
- Java 21+ (for backend)
- Git

### Installation
```bash
# Clone the repository
git clone <repository-url>
cd Gynassist

# Windows: Use the optimized launcher
Launch-Gynassist.bat

# Or start all platforms via npm
npm run start:all
```

### Platform URLs
- **Web Frontend**: http://localhost:5173
- **Backend API**: http://localhost:8080
- **Mobile**: Expo DevTools
- **Desktop**: Electron window

## 🏗️ Architecture

```
Gynassist/
├── gynassist-frontend/          # Web application (React + Vite)
├── gynassist-mobile/           # Mobile app (React Native + Expo)
├── gynassist-desktop/          # Desktop app (Electron + React)
├── Gynassist-backend/          # Backend API (Spring Boot)
├── shared/                     # Shared utilities and types
└── scripts/                    # Build and deployment scripts
```

## 🔧 Development Setup

### Individual Platform Setup

#### Web Application
```bash
cd gynassist-frontend
npm install
npm run dev
```

#### Mobile Application
```bash
cd gynassist-mobile
npm install
npx expo start
```

#### Desktop Application
```bash
cd gynassist-desktop
npm install
npm run dev
```

#### Backend
```bash
cd Gynassist-backend
mvn spring-boot:run
```

## 🌐 API Integration

All platforms connect to the same Spring Boot backend:
- **Development**: http://localhost:8080
- **Production**: Configure in environment files

### Supported APIs
- Authentication (JWT)
- User management
- Health data tracking
- Consultation booking
- Payment processing
- MOH notifications
- Location services

## 🚀 Deployment Guide

### Quick Deployment
```bash
# Copy environment template
cp .env.example .env

# Deploy platform
./deploy.sh  # Linux/macOS
deploy.bat   # Windows
```

### Docker Deployment
```bash
# Start all services
docker-compose -f docker-compose.prod.yml up -d

# View logs
docker-compose -f docker-compose.prod.yml logs -f
```

### Environment Variables
```bash
# Required
DATABASE_PASSWORD=your_secure_password
JWT_SECRET=your_jwt_secret_256_bits
MOH_API_KEY=your_moh_api_key

# Optional
MOH_API_URL=https://api.health.go.ug/validate
PORT=8080
```

## 📊 Database Schema

### Migration History
- **V1**: Base schema (users, providers)
- **V2**: Health profiles
- **V3**: Gynecological profiles
- **V4**: AI features (insights, analysis)
- **V5**: Advanced AI (matching, trends)
- **V6**: UX features (voice, notifications)

## 🔒 Security Features

- End-to-end encryption for sensitive data
- JWT authentication with 24-hour expiration
- Biometric authentication on mobile
- Secure token storage
- HIPAA-compliant data handling
- CORS configuration
- Input validation and sanitization

## 📱 Platform-Specific Features

### Mobile (iOS/Android)
- Push notifications for health alerts
- Biometric authentication
- Camera integration for document upload
- GPS location services
- Offline data synchronization
- Native calendar integration

### Desktop (Windows/macOS/Linux)
- System tray integration
- Desktop notifications
- File system access
- Multi-window support
- Keyboard shortcuts

### Web
- Progressive Web App (PWA) capabilities
- Browser notifications
- Responsive design
- Cross-browser compatibility

## 🌍 Localization

- English (primary)
- Luganda
- Swahili
- Other Ugandan languages (planned)

## 📊 Monitoring & Health

### Health Endpoints
- `/actuator/health` - Application health
- `/actuator/info` - Application info
- `/actuator/metrics` - Application metrics

### Performance Optimization
- Connection pooling enabled
- Query optimization with indexes
- Static asset caching (1 year)
- API response caching (Redis recommended)

## 🔧 Troubleshooting

### Common Issues

**Backend won't start:**
```bash
# Check database connection
docker-compose logs gynassist-db

# Check environment variables
docker-compose config
```

**Frontend 404 errors:**
```bash
# Check build output
ls -la gynassist-frontend/dist/

# Restart development server
cd gynassist-frontend && npm run dev
```

**Authentication Issues:**
- Ensure JWT_SECRET is set (256-bit minimum)
- Check CORS configuration
- Verify token expiration settings

### Performance Issues

**Slow API responses:**
- Check database query performance
- Enable query logging temporarily
- Monitor JVM memory usage

**High memory usage:**
- Adjust JVM heap size: `-Xmx2g`
- Enable garbage collection logging
- Monitor connection pool usage

## 📦 Build & Distribution

### Web Build
```bash
cd gynassist-frontend
npm run build
npm run preview
```

### Mobile Build
```bash
cd gynassist-mobile
# iOS
npx expo build:ios
# Android
npx expo build:android
```

### Desktop Build
```bash
cd gynassist-desktop
# Windows
npm run build:win
# macOS
npm run build:mac
# Linux
npm run build:linux
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test across all platforms
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License.

## 🆘 Support

- **Documentation**: [docs.gynassist.ug](https://docs.gynassist.ug)
- **Support Email**: support@gynassist.ug
- **Emergency**: Use in-app emergency features

## 🚀 Production Deployment

### Web Deployment
- Vercel/Netlify for frontend
- AWS/DigitalOcean for backend

### Mobile Deployment
- App Store (iOS)
- Google Play Store (Android)
- Direct APK distribution

### Desktop Deployment
- Microsoft Store (Windows)
- Mac App Store (macOS)
- Snap Store (Linux)

---

**Gynassist** - Empowering women's reproductive health across Uganda 🇺🇬

*This guide consolidates all essential project information for development, deployment, and maintenance.*