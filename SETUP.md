# Gynassist Application Setup

## Overview
This is a full-stack telemedicine application with:
- **Backend**: Spring Boot (Java 21) running on port 8080
- **Frontend**: React + TypeScript + Vite running on port 5173

## Prerequisites
- Java 21 or later
- Node.js and npm
- Maven (included via Maven Wrapper)

## Changes Made

### 1. Port Configuration
- **Frontend**: Changed from port 8080 to 5173 to avoid conflict with backend
- **Backend**: Configured to run on port 8080
- **CORS**: Updated to allow frontend on port 5173

### 2. Redis Configuration
- Made Redis optional for development
- Backend can now run without Redis installed
- Redis auto-configuration is excluded for dev environment

## Running the Application

### Option 1: Use the Startup Script (Recommended)
```powershell
.\start-app.ps1
```

This will open two separate PowerShell windows:
- One for the backend
- One for the frontend

### Option 2: Manual Start

#### Start Backend
```powershell
cd Gynassist-backend
.\mvnw.cmd spring-boot:run
```

#### Start Frontend (in a new terminal)
```powershell
cd gynassist-frontend
npm run dev
```

## Service URLs

- **Frontend**: http://localhost:5173
- **Backend API**: http://localhost:8080
- **Backend Health**: http://localhost:8080
- **H2 Console** (dev): http://localhost:8080/h2-console

## Database

The application uses H2 in-memory database for development:
- **JDBC URL**: `jdbc:h2:mem:testdb`
- **Username**: `sa`
- **Password**: `password`
- **Console**: Available at `/h2-console` in dev mode

## API Endpoints

### Public Endpoints
- `POST /api/auth/register` - User registration
- `POST /api/auth/login` - User authentication
- `GET /api/public/**` - Public resources

### Protected Endpoints
- `GET /api/client/**` - Client resources (requires CLIENT role)
- `GET /api/provider/**` - Provider resources (requires PROVIDER role)
- `GET /api/admin/**` - Admin resources (requires ADMIN role)

## Development Notes

1. **First Run**: Backend may take 30-60 seconds to start as Maven downloads dependencies
2. **Hot Reload**: 
   - Frontend: Vite provides instant hot reload
   - Backend: Spring Boot DevTools provides automatic restart on code changes
3. **Database**: H2 database is in-memory and will reset on backend restart
4. **CORS**: Frontend on port 5173 is configured to communicate with backend

## Troubleshooting

### Backend won't start
- Check Java version: `java -version` (should be 21+)
- Check if port 8080 is available: `netstat -ano | findstr :8080`
- Check backend logs in the console window

### Frontend won't start
- Ensure dependencies are installed: `cd gynassist-frontend && npm install`
- Check if port 5173 is available: `netstat -ano | findstr :5173`

### CORS Errors
- Verify backend CORS configuration includes `http://localhost:5173`
- Check that backend is running on port 8080
- Verify frontend API URL in `src/lib/api.ts` points to `http://localhost:8080`

### Redis Errors
- Redis is now optional and excluded in dev mode
- If you see Redis-related errors, ensure the changes to `GynassistBackendApplication.java` are applied

## Project Structure

```
Gynassist/
├── Gynassist-backend/     # Spring Boot backend
│   ├── src/
│   │   └── main/
│   │       ├── java/      # Java source code
│   │       └── resources/ # Configuration files
│   └── pom.xml           # Maven dependencies
│
└── gynassist-frontend/    # React frontend
    ├── src/
    │   ├── components/    # React components
    │   ├── pages/         # Page components
    │   └── lib/           # Utilities and API client
    └── package.json       # npm dependencies
```

## Next Steps

1. Open http://localhost:5173 in your browser
2. Register a new user or login
3. Explore the application features

