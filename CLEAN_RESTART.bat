@echo off
echo ========================================
echo    GYNASSIST - CLEAN RESTART PROTOCOL
echo ========================================

echo 🧹 Cleaning all caches...

REM Backend cleanup
cd Gynassist-backend
echo Cleaning backend...
call ./mvnw clean
rmdir /s /q target 2>nul

REM Frontend cleanup  
cd ../gynassist-frontend
echo Cleaning frontend...
rmdir /s /q node_modules 2>nul
rmdir /s /q dist 2>nul
rmdir /s /q .vite 2>nul
call npm cache clean --force

REM Mobile cleanup
cd ../gynassist-mobile
echo Cleaning mobile...
rmdir /s /q node_modules 2>nul
rmdir /s /q .expo 2>nul

REM Desktop cleanup
cd ../gynassist-desktop
echo Cleaning desktop...
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