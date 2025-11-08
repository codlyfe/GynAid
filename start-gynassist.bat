@echo off
title Gynassist - AI Health Platform
echo 🚀 Starting Gynassist Cross-Platform Application...

echo 📊 Starting Backend API...
cd Gynassist-backend
start "Backend API" cmd /k "mvnw.cmd spring-boot:run"

echo ⏳ Waiting for backend startup...
timeout /t 15 /nobreak > nul

echo 🌐 Starting Web Frontend...
cd ..\gynassist-frontend
start "Web Frontend" cmd /k "npm run dev"

echo 📱 Starting Mobile App...
cd ..\gynassist-mobile
start "Mobile App" cmd /k "npx expo start"

echo 🖥️ Starting Desktop App...
cd ..\gynassist-desktop
start "Desktop App" cmd /k "npm run dev"

echo ✅ All platforms started successfully!
echo.
echo 📊 Backend API:    http://localhost:8080
echo 🌐 Web Frontend:   http://localhost:5173
echo 📱 Mobile Expo:    http://localhost:19000
echo 🖥️ Desktop:       New Electron window
echo.
echo Keep this window open to monitor all services.
pause