# Quick API Test Script for Gynassist
# Tests basic API endpoints to verify functionality

Write-Host "=== Gynassist API Test ===" -ForegroundColor Cyan
Write-Host ""

$baseUrl = "http://localhost:8080"

# Test 1: Backend Health Check
Write-Host "1. Testing Backend Health..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "$baseUrl" -Method GET -UseBasicParsing -TimeoutSec 5
    Write-Host "   ✓ Backend is responding (Status: $($response.StatusCode))" -ForegroundColor Green
} catch {
    Write-Host "   ✗ Backend not responding: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 2: Auth Endpoint (should be accessible)
Write-Host "`n2. Testing Auth Endpoint..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "$baseUrl/api/auth/register" -Method POST -ContentType "application/json" -Body '{"test":"test"}' -UseBasicParsing -TimeoutSec 5 -ErrorAction SilentlyContinue
} catch {
    if ($_.Exception.Response.StatusCode -eq 400) {
        Write-Host "   ✓ Auth endpoint is accessible (400 expected for invalid data)" -ForegroundColor Green
    } else {
        Write-Host "   ⚠ Auth endpoint response: $($_.Exception.Message)" -ForegroundColor Yellow
    }
}

# Test 3: Protected Endpoint (should require auth)
Write-Host "`n3. Testing Protected Endpoint..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "$baseUrl/api/provider/location/nearby?latitude=0&longitude=0&radiusKm=10" -Method GET -UseBasicParsing -TimeoutSec 5 -ErrorAction SilentlyContinue
} catch {
    if ($_.Exception.Response.StatusCode -eq 401 -or $_.Exception.Response.StatusCode -eq 403) {
        Write-Host "   ✓ Protected endpoint requires authentication (expected)" -ForegroundColor Green
    } else {
        Write-Host "   ⚠ Unexpected response: $($_.Exception.Message)" -ForegroundColor Yellow
    }
}

Write-Host "`n=== Test Complete ===" -ForegroundColor Cyan
Write-Host "Note: These are basic connectivity tests. Full functionality requires valid authentication." -ForegroundColor Gray

