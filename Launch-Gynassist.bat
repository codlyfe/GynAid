@echo off
title Gynassist - Reproductive Health Companion
color 0A
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

REM Check if Node.js is installed
node --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Node.js is not installed or not in PATH
    echo.
    echo Please install Node.js from: https://nodejs.org/
    echo.
    pause
    exit /b 1
)

echo ✅ Node.js is available
echo.

REM Check if we're in the right directory
if not exist "package.json" (
    echo ❌ package.json not found. Make sure you're running this from the Gynassist directory.
    echo.
    pause
    exit /b 1
)

echo ✅ Project directory confirmed
echo.

REM Check if dependencies are installed
if not exist "node_modules" (
    echo 📦 Installing dependencies...
    echo This may take a few minutes on first run...
    echo.
    npm install
    if %errorlevel% neq 0 (
        echo ❌ Failed to install dependencies
        pause
        exit /b 1
    )
    echo.
    echo ✅ Dependencies installed successfully
    echo.
)

echo 🚀 Starting Gynassist Cross-Platform Application...
echo.
echo 📊 Backend API will start on:     http://localhost:8080
echo 🌐 Web Frontend will start on:   http://localhost:5173
echo 📱 Mobile Expo will start on:    http://localhost:19000
echo 🖥️  Desktop Electron will start: New window
echo.
echo ⏳ Please wait while all services start up...
echo    This may take 30-60 seconds on first run.
echo.
echo 💡 Tip: Keep this window open to see logs from all services
echo.

REM Start the application
npm run start:all

echo.
echo 🛑 Gynassist has been stopped.
echo.
pause