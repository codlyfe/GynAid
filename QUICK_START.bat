@echo off
echo ========================================
echo    GYNASSIST - QUICK START SETUP
echo ========================================
echo.

echo 📦 Installing dependencies...
call npm run setup
if %errorlevel% neq 0 (
    echo ❌ Setup failed. Please check Node.js installation.
    pause
    exit /b 1
)

echo.
echo 🚀 Starting all services...
call npm run start:all
if %errorlevel% neq 0 (
    echo ❌ Failed to start services.
    pause
    exit /b 1
)

echo.
echo ✅ Gynassist is now running!
echo 🌐 Web: http://localhost:5173
echo 📊 API: http://localhost:8080
echo.
echo Press any key to continue...
pause > nul