@echo off
title Gynassist - Reproductive Health Companion
color 0A
:MAIN_MENU
cls
echo.
echo  ██████╗ ██╗   ██╗███╗   ██╗ █████╗ ███████╗███████╗██╗███████╗████████╗
echo ██╔════╝ ██║   ██║████╗  ██║██╔══██╗██╔════╝██╔════╝██║██╔════╝╚══██╔══╝
echo ██║  ███╗██║   ██║██╔██╗ ██║███████║███████╗███████╗██║███████╗   ██║   
echo ██║   ██║██║   ██║██║╚██╗██║██╔══██║╚════██║╚════██║██║╚════██║   ██║   
echo ╚██████╔╝╚██████╔╝██║ ╚████║██║  ██║███████║███████║██║███████║   ██║   
echo  ╚═════╝  ╚═════╝ ╚═╝  ╚═══╝╚═╝  ╚═╝╚══════╝╚══════╝╚═╝╚══════╝   ╚═╝   
echo.
echo                    Reproductive Health Companion for Women
echo                           Empowering Health Across Uganda
echo.
echo ================================================================================
echo.
echo Select an option:
echo.
echo [1] 🚀 Quick Start (Normal Launch)
echo [2] 🔧 Clean Restart (Clear caches and restart)
echo [3] 🚨 Emergency Recovery (Fix critical issues)
echo [4] ⚡ Quick Fix (Schema and connectivity fix)
echo [5] 📊 Status Check (Check system status)
echo [6] ❌ Exit
echo.
set /p choice="Enter your choice (1-6): "

if "%choice%"=="1" goto QUICK_START
if "%choice%"=="2" goto CLEAN_RESTART
if "%choice%"=="3" goto EMERGENCY_RECOVERY
if "%choice%"=="4" goto QUICK_FIX
if "%choice%"=="5" goto STATUS_CHECK
if "%choice%"=="6" goto EXIT
echo Invalid choice. Please try again.
timeout /t 2 > nul
goto MAIN_MENU

:QUICK_START
cls
echo 🚀 Starting Gynassist Cross-Platform Application...
echo.

REM Check Node.js
node --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Node.js not found. Install from: https://nodejs.org/
    pause
    goto MAIN_MENU
)

REM Check project directory
if not exist "package.json" (
    echo ❌ package.json not found. Run from Gynassist directory.
    pause
    goto MAIN_MENU
)

REM Install dependencies if needed
if not exist "node_modules" (
    echo 📦 Installing dependencies...
    npm install
    if %errorlevel% neq 0 (
        echo ❌ Failed to install dependencies
        pause
        goto MAIN_MENU
    )
)

echo 📊 Backend API:      http://localhost:8080
echo 🌐 Web Frontend:     http://localhost:5173
echo 📱 Mobile Expo:      http://localhost:19000
echo 🖥️ Desktop:         New Electron window
echo.
echo ⏳ Starting all services...

npm run start:all
echo.
echo 🛑 Services stopped.
pause
goto MAIN_MENU

:CLEAN_RESTART
cls
echo 🧹 CLEAN RESTART PROTOCOL
echo ========================================
echo.

echo Terminating processes...
taskkill /f /im java.exe 2>nul
taskkill /f /im node.exe 2>nul

echo Cleaning backend...
cd Gynassist-backend
call ./mvnw clean
rmdir /s /q target 2>nul

echo Cleaning frontend...
cd ../gynassist-frontend
rmdir /s /q node_modules 2>nul
rmdir /s /q dist 2>nul
rmdir /s /q .vite 2>nul
call npm cache clean --force

echo Cleaning mobile...
cd ../gynassist-mobile
rmdir /s /q node_modules 2>nul
rmdir /s /q .expo 2>nul

echo Cleaning desktop...
cd ../gynassist-desktop
rmdir /s /q node_modules 2>nul
rmdir /s /q build 2>nul
rmdir /s /q dist 2>nul

cd ..
echo 📦 Reinstalling dependencies...
call npm run setup

echo 🚀 Starting clean application...
call npm run start:all

echo ✅ Clean restart complete!
pause
goto MAIN_MENU

:EMERGENCY_RECOVERY
cls
echo 🚨 EMERGENCY RECOVERY
echo ========================================
echo.

echo Terminating processes...
taskkill /f /im java.exe 2>nul
taskkill /f /im node.exe 2>nul

echo Creating backup...
copy "Gynassist-backend\src\main\resources\application.yaml" "application-backup-%date:~-4,4%%date:~-10,2%%date:~-7,2%.yaml" 2>nul

echo Cleaning backend...
cd Gynassist-backend
call mvnw clean compile

echo Starting backend in recovery mode...
start "Backend-Recovery" cmd /k "mvnw spring-boot:run"

echo Waiting for startup...
timeout /t 20

echo Starting frontend...
cd ..\gynassist-frontend
start "Frontend" cmd /k "npm run dev"

cd ..
echo ✅ Emergency recovery complete!
echo Check services at:
echo - Backend: http://localhost:8080
echo - Frontend: http://localhost:5173
pause
goto MAIN_MENU

:QUICK_FIX
cls
echo 🔧 QUICK SCHEMA FIX
echo ========================================
echo.

echo Stopping processes...
taskkill /f /im java.exe 2>nul

echo Cleaning backend...
cd Gynassist-backend
call ./mvnw clean

echo Starting backend with fix...
start "Backend" cmd /k "./mvnw spring-boot:run"

echo Waiting for startup...
timeout /t 15

cd ..
echo ✅ Quick fix applied!
pause
goto MAIN_MENU

:STATUS_CHECK
cls
echo 📊 SYSTEM STATUS CHECK
echo ========================================
echo.

echo Checking Node.js...
node --version 2>nul && echo ✅ Node.js: Available || echo ❌ Node.js: Not found

echo Checking Java...
java -version 2>nul && echo ✅ Java: Available || echo ❌ Java: Not found

echo Checking project structure...
if exist "package.json" (echo ✅ Project: Valid) else (echo ❌ Project: Invalid directory)
if exist "Gynassist-backend" (echo ✅ Backend: Found) else (echo ❌ Backend: Missing)
if exist "gynassist-frontend" (echo ✅ Frontend: Found) else (echo ❌ Frontend: Missing)

echo Checking dependencies...
if exist "node_modules" (echo ✅ Dependencies: Installed) else (echo ❌ Dependencies: Missing)

echo Checking services...
netstat -an | find "8080" >nul && echo ✅ Backend: Running on :8080 || echo ❌ Backend: Not running
netstat -an | find "5173" >nul && echo ✅ Frontend: Running on :5173 || echo ❌ Frontend: Not running

echo.
pause
goto MAIN_MENU

:EXIT
echo.
echo Thank you for using Gynassist! 👋
echo.
timeout /t 2 > nul
exit /b 0