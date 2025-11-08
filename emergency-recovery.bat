@echo off
echo ========================================
echo    GYNASSIST EMERGENCY RECOVERY
echo ========================================

echo 🚨 Emergency system recovery initiated...

REM Kill any existing processes
echo Terminating existing processes...
taskkill /f /im java.exe 2>nul
taskkill /f /im node.exe 2>nul

REM Backup current config
echo Creating configuration backup...
copy "Gynassist-backend\src\main\resources\application.yaml" "application-current-backup.yaml" 2>nul

REM Apply emergency configuration
echo Applying emergency configuration...
copy "application-backup.properties" "Gynassist-backend\src\main\resources\application.properties" 2>nul

REM Clean and rebuild
echo Cleaning backend...
cd Gynassist-backend
call mvnw clean compile

REM Start backend in recovery mode
echo Starting backend in recovery mode...
start "Backend-Recovery" cmd /k "mvnw spring-boot:run"

REM Wait for backend startup
echo Waiting for backend startup...
timeout /t 20

REM Start frontend
echo Starting frontend...
cd ..\gynassist-frontend
start "Frontend" cmd /k "npm run dev"

REM Run recovery probe
echo Running system recovery probe...
cd ..
timeout /t 10
node system-recovery-probe.js

echo.
echo ========================================
echo    EMERGENCY RECOVERY COMPLETE
echo ========================================
echo Check recovery-report.json for details
pause