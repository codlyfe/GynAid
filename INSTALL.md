# Gynassist Installation Guide

Complete installation guide for the Gynassist cross-platform reproductive health application.

## 🎯 Quick Start

```bash
# Clone the repository
git clone <repository-url>
cd Gynassist

# Run automated setup
npm install
npm run setup

# Configure environment
npm run setup:env

# Start all platforms
npm run dev
```

## 📋 Prerequisites

### Required Software

| Software | Version | Purpose | Download |
|----------|---------|---------|----------|
| **Node.js** | 18+ | JavaScript runtime | [nodejs.org](https://nodejs.org/) |
| **Java** | 21+ | Backend runtime | [adoptium.net](https://adoptium.net/) |
| **Git** | Latest | Version control | [git-scm.com](https://git-scm.com/) |

### Optional Software

| Software | Purpose | Download |
|----------|---------|----------|
| **Android Studio** | Mobile development | [developer.android.com](https://developer.android.com/studio) |
| **Xcode** | iOS development (macOS only) | Mac App Store |
| **PostgreSQL** | Production database | [postgresql.org](https://www.postgresql.org/) |

## 🚀 Platform-Specific Installation

### 🌐 Web Application

```bash
# Navigate to frontend directory
cd gynassist-frontend

# Install dependencies
npm install

# Start development server
npm run dev

# Build for production
npm run build
```

**Access:** http://localhost:5173

### 📱 Mobile Application

```bash
# Navigate to mobile directory
cd gynassist-mobile

# Install dependencies
npm install

# Install Expo CLI globally
npm install -g @expo/eas-cli

# Start Expo development server
npx expo start
```

**Development Options:**
- **iOS Simulator:** Press `i` in Expo CLI
- **Android Emulator:** Press `a` in Expo CLI  
- **Physical Device:** Scan QR code with Expo Go app
- **Web Browser:** Press `w` in Expo CLI

### 🖥️ Desktop Application

```bash
# Navigate to desktop directory
cd gynassist-desktop

# Install dependencies
npm install

# Start development
npm run electron:dev

# Build for distribution
npm run build:electron
```

### 📊 Backend API

```bash
# Navigate to backend directory
cd Gynassist-backend

# Start with Maven wrapper (Windows)
mvnw.cmd spring-boot:run

# Start with Maven wrapper (macOS/Linux)
./mvnw spring-boot:run

# Or with system Maven
mvn spring-boot:run
```

**Access:** http://localhost:8080

## 🔧 Configuration

### Environment Variables

Run the interactive setup:
```bash
npm run setup:env
```

Or manually create environment files:

#### Frontend (.env)
```env
VITE_API_URL=http://localhost:8080
VITE_STRIPE_PUBLIC_KEY=your_stripe_public_key
VITE_FIREBASE_API_KEY=your_firebase_key
```

#### Mobile (.env)
```env
EXPO_PROJECT_ID=your_expo_project_id
API_URL=http://localhost:8080
STRIPE_PUBLIC_KEY=your_stripe_public_key
```

#### Backend (application-local.yaml)
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    username: sa
    password: password

app:
  jwt:
    secret: your_jwt_secret
  payment:
    stripe:
      secret-key: your_stripe_secret_key
```

### Database Setup

#### Development (H2 - Default)
No setup required. H2 runs in-memory.

#### Production (PostgreSQL)
```bash
# Install PostgreSQL
# Create database
createdb gynassist_db

# Update application.yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/gynassist_db
    username: your_username
    password: your_password
```

## 🏗️ Build & Deployment

### Build All Platforms
```bash
npm run build:all
```

### Individual Platform Builds
```bash
# Web application
npm run build:frontend

# Mobile applications
npm run build:mobile

# Desktop applications  
npm run build:desktop

# Backend JAR
npm run build:backend
```

### Deployment
```bash
# Deploy all platforms
npm run deploy:all

# Deploy specific platforms
npm run deploy:web
npm run deploy:backend
npm run deploy:mobile
npm run deploy:desktop
```

## 🔍 Verification

### Health Check
```bash
npm run health-check
```

### Manual Testing
1. **Backend:** Visit http://localhost:8080/health
2. **Frontend:** Visit http://localhost:5173
3. **Mobile:** Check Expo DevTools
4. **Desktop:** Launch Electron app

## 🛠️ Development Workflow

### Daily Development
```bash
# Start all services
npm run dev

# In separate terminals:
npm run start:backend   # Backend API
npm run start:frontend  # Web app
npm run start:mobile    # Mobile app
npm run start:desktop   # Desktop app
```

### Code Quality
```bash
# Run all tests
npm run test:all

# Run linting
npm run lint:all

# Type checking (TypeScript)
npm run type-check
```

### Database Management
```bash
# Reset development database
npm run db:reset

# Run migrations
npm run db:migrate

# Seed test data
npm run db:seed
```

## 📱 Mobile Development

### iOS Development (macOS only)
1. Install Xcode from Mac App Store
2. Install iOS Simulator
3. Run: `npx expo start --ios`

### Android Development
1. Install Android Studio
2. Set up Android SDK
3. Create/start Android emulator
4. Run: `npx expo start --android`

### Physical Device Testing
1. Install Expo Go app
2. Scan QR code from Expo DevTools
3. App loads on device

## 🖥️ Desktop Development

### Platform-Specific Builds
```bash
# Windows
npm run build:win

# macOS  
npm run build:mac

# Linux
npm run build:linux
```

### Distribution
- **Windows:** Creates `.exe` installer
- **macOS:** Creates `.dmg` file
- **Linux:** Creates AppImage

## 🔐 Security Setup

### SSL/HTTPS (Production)
```bash
# Generate SSL certificates
openssl req -x509 -newkey rsa:4096 -keyout key.pem -out cert.pem -days 365

# Update backend configuration
server:
  ssl:
    key-store: classpath:keystore.p12
    key-store-password: password
    key-store-type: PKCS12
```

### API Keys & Secrets
- Store in environment variables
- Use different keys for development/production
- Rotate keys regularly
- Never commit secrets to version control

## 🚨 Troubleshooting

### Common Issues

#### Port Already in Use
```bash
# Kill process on port 8080 (Backend)
npx kill-port 8080

# Kill process on port 5173 (Frontend)  
npx kill-port 5173
```

#### Node Modules Issues
```bash
# Clean install
npm run clean:all
npm run install:all
```

#### Java Version Issues
```bash
# Check Java version
java -version

# Set JAVA_HOME (Windows)
set JAVA_HOME=C:\Program Files\Java\jdk-21

# Set JAVA_HOME (macOS/Linux)
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
```

#### Mobile Build Issues
```bash
# Clear Expo cache
npx expo start --clear

# Reset Metro bundler
npx expo start --reset-cache
```

### Getting Help

1. **Check Logs:** Each service outputs detailed logs
2. **Health Check:** Run `npm run health-check`
3. **Documentation:** Check individual README files
4. **Issues:** Create GitHub issue with logs
5. **Community:** Join our Discord/Slack

## 📚 Additional Resources

- [Frontend Documentation](./gynassist-frontend/README.md)
- [Mobile Documentation](./gynassist-mobile/README.md)  
- [Desktop Documentation](./gynassist-desktop/README.md)
- [Backend Documentation](./Gynassist-backend/README.md)
- [API Documentation](http://localhost:8080/swagger-ui.html)
- [Deployment Guide](./DEPLOYMENT.md)

## 🎉 Success!

If all services are running:
- ✅ Backend API: http://localhost:8080
- ✅ Web App: http://localhost:5173  
- ✅ Mobile: Expo DevTools
- ✅ Desktop: Electron window

You're ready to develop Gynassist! 🚀

---

**Need help?** Check our [troubleshooting guide](./TROUBLESHOOTING.md) or [create an issue](https://github.com/gynassist/gynassist-app/issues).