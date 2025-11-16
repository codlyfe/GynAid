# GynAid Multiplatform Application - Setup & Run Instructions

## ⚠️ Quick Fix for npm Issues

The npm error you're seeing is common. Here's how to resolve it and run each component:

---

## 🗄️ Step 1: Backend Setup (Java Spring Boot)

**Prerequisites**: Java 21, Maven

```bash
# Navigate to backend directory
cd GynAid-backend

# Clean and run the backend
mvn clean compile
mvn spring-boot:run
```

**Expected Output**: Spring Boot will start on http://localhost:8080
**Health Check**: Visit http://localhost:8080/api/actuator/health

---

## 🌐 Step 2: Frontend Setup (React Web App)

**Prerequisites**: Node.js 18+

```bash
# Navigate to frontend directory
cd GynAid-frontend

# Install dependencies (if not already installed)
npm install

# Run the development server
npm run dev
```

**Expected Output**: Vite dev server will start on http://localhost:5173
**Auto-reload**: Changes will auto-refresh in browser

---

## 📱 Step 3: Mobile Setup (React Native/Expo)

**Prerequisites**: Node.js 18+, Expo CLI

```bash
# Install Expo CLI globally (if not installed)
npm install -g @expo/cli

# Navigate to mobile directory
cd GynAid-mobile

# Install dependencies
npm install

# Start the Expo development server
npx expo start
```

**Options**:
- Press `w` for web browser
- Press `a` for Android emulator
- Press `i` for iOS simulator
- Scan QR code with Expo Go app

---

## 🖥️ Step 4: Desktop Setup (Electron) - FIXED

### Complete Reset of Desktop Dependencies:

```bash
# Navigate to desktop directory
cd GynAid-desktop

# Delete ALL node_modules and lock files (run these commands one by one)
rmdir /s /q node_modules
del package-lock.json

# Clear npm cache completely
npm cache clean --force

# Fresh install of ALL dependencies
npm install

# Alternative if above fails:
npm install --legacy-peer-deps

# Run the desktop app
npm run dev
```

### If npm still fails - Manual Installation:

```bash
cd GynAid-desktop

# Create fresh package.json if needed
npm init -y

# Install all required dependencies manually
npm install electron --save-dev
npm install electron-is-dev --save
npm install electron-reload --save
npm install electron-store --save
npm install electron-updater --save

# Run the desktop app
npm run dev
```

---

## 🚀 Complete Workflow (Recommended Order)

### Terminal 1 - Backend:
```bash
cd GynAid-backend
mvn spring-boot:run
```

### Terminal 2 - Frontend:
```bash
cd GynAid-frontend
npm install (if needed)
npm run dev
```

### Terminal 3 - Mobile:
```bash
cd GynAid-mobile
npm install (if needed)
npx expo start
```

### Terminal 4 - Desktop (after fixing npm issues):
```bash
cd GynAid-desktop
rmdir /s /q node_modules
npm cache clean --force
npm install
npm run dev
```

---

## 🔧 Troubleshooting Common Issues

### Backend Issues:
- **Java not found**: Ensure Java 21 is installed and JAVA_HOME is set
- **Port 8080 busy**: Use `netstat -ano | findstr :8080` then kill the process
- **Maven issues**: Run `mvn clean install`

### Frontend Issues:
- **Port 5173 busy**: Vite will auto-assign next available port
- **Dependencies**: Run `npm install` in the GynAid-frontend directory

### Mobile Issues:
- **Expo CLI not found**: Run `npm install -g @expo/cli`
- **Metro bundler issues**: Run `npx expo start --clear`
- **Network issues**: Ensure all devices are on same WiFi

### Desktop Issues:
- **fs-extra module error**: The solution above completely resets dependencies
- **Permission issues**: Run Command Prompt as Administrator
- **Corrupted node_modules**: Delete folder and reinstall completely

---

## 📊 Default URLs & Ports

| Component | URL | Port |
|-----------|-----|------|
| Backend API | http://localhost:8080/api | 8080 |
| Frontend Web | http://localhost:5173 | 5173 |
| Mobile Expo | Metro Bundler | 19000+ |
| Desktop Electron | Native Window | - |

---

## 🎯 Quick Start Commands Summary

```bash
# Backend (Terminal 1)
cd GynAid-backend && mvn spring-boot:run

# Frontend (Terminal 2) 
cd GynAid-frontend && npm run dev

# Mobile (Terminal 3)
cd GynAid-mobile && npx expo start

# Desktop (Terminal 4) - after fixing npm
cd GynAid-desktop && rmdir /s /q node_modules && npm cache clean --force && npm install && npm run dev
```

All components are configured to work together. Start with the backend first!

---

## 🛠️ For Desktop App - Use This Command:

**Run this exactly as shown to fix the desktop app:**

```bash
cd GynAid-desktop
rmdir /s /q node_modules 2>nul
npm cache clean --force
npm install
npm run dev
```

If the above still fails, the desktop app is optional - you can run the other 3 components without it!