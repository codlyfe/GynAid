# Gynassist Application Startup Script
# This script starts both backend and frontend services

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Gynassist Application Startup" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Check if Java is installed
Write-Host "Checking prerequisites..." -ForegroundColor Yellow
try {
    $javaVersion = java -version 2>&1 | Select-Object -First 1
    Write-Host "✓ Java found: $javaVersion" -ForegroundColor Green
} catch {
    Write-Host "✗ Java not found. Please install Java 21 or later." -ForegroundColor Red
    exit 1
}

# Check if Node.js is installed
try {
    $nodeVersion = node --version
    Write-Host "✓ Node.js found: $nodeVersion" -ForegroundColor Green
} catch {
    Write-Host "✗ Node.js not found. Please install Node.js." -ForegroundColor Red
    exit 1
}

Write-Host ""

# Start Backend
Write-Host "Starting Backend (Spring Boot) on port 8080..." -ForegroundColor Yellow
Write-Host "This may take 30-60 seconds on first run..." -ForegroundColor Gray
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PSScriptRoot\Gynassist-backend'; .\mvnw.cmd spring-boot:run" -WindowStyle Normal

# Wait a bit for backend to start
Start-Sleep -Seconds 5

# Start Frontend
Write-Host "Starting Frontend (Vite) on port 5173..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PSScriptRoot\gynassist-frontend'; npm run dev" -WindowStyle Normal

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Services Starting..." -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Backend:  http://localhost:8080" -ForegroundColor Green
Write-Host "Frontend: http://localhost:5173" -ForegroundColor Green
Write-Host ""
Write-Host "Two PowerShell windows have been opened:" -ForegroundColor Yellow
Write-Host "  1. Backend console (shows Spring Boot logs)" -ForegroundColor White
Write-Host "  2. Frontend console (shows Vite dev server logs)" -ForegroundColor White
Write-Host ""
Write-Host "Wait for both services to fully start, then:" -ForegroundColor Yellow
Write-Host "  - Open http://localhost:5173 in your browser" -ForegroundColor White
Write-Host "  - The frontend will communicate with the backend on port 8080" -ForegroundColor White
Write-Host ""
Write-Host "Press any key to exit this script (services will continue running)..." -ForegroundColor Gray
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")

