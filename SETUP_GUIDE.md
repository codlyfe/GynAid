# Gynassist - Complete Setup & Authentication Guide

## 🚀 Quick Start (5 Minutes)

### Prerequisites
- **Node.js 18+** - [Download](https://nodejs.org/)
- **Java 21+** - [Download](https://adoptium.net/)
- **Git** - [Download](https://git-scm.com/)

### 1. Clone & Setup
```bash
git clone <repository-url>
cd Gynassist

# Install all dependencies
npm run setup
```

### 2. Start All Services
```bash
# Windows (Recommended)
Launch-Gynassist.bat

# Or manually
npm run start:all
```

### 3. Access Applications
- **Web**: http://localhost:5173
- **Backend API**: http://localhost:8080
- **Desktop**: Electron window opens automatically
- **Mobile**: Expo DevTools in browser

## 🔐 Authentication Testing

### Test User Registration
1. Go to http://localhost:5173/register
2. Fill form:
   - **Email**: test@example.com
   - **Password**: password123
   - **First Name**: Test
   - **Last Name**: User
   - **Phone**: +256700123456
   - **Role**: Client
3. Click "Create Account"

### Test User Login
1. Go to http://localhost:5173/login
2. Use credentials from registration
3. Should redirect to dashboard

## 🛠️ Manual Setup (If Quick Start Fails)

### Backend Setup
```bash
cd Gynassist-backend

# Windows
./mvnw spring-boot:run

# Linux/Mac
./mvnw spring-boot:run
```

### Frontend Setup
```bash
cd gynassist-frontend
npm install
npm run dev
```

### Mobile Setup
```bash
cd gynassist-mobile
npm install
npx expo start
```

### Desktop Setup
```bash
cd gynassist-desktop
npm install
npm run electron:dev
```

## 🔧 Troubleshooting

### CSRF Token Issues
If you see 403 errors:
1. Ensure backend is running on port 8080
2. Check browser console for CSRF token fetch
3. Verify cookies are enabled

### Port Conflicts
- Backend: Change port in `application.properties`
- Frontend: Change port in `vite.config.ts`

### Database Issues
- H2 database runs in memory (no setup needed)
- Access H2 console: http://localhost:8080/h2-console

## 📱 Platform-Specific Features

### Web (localhost:5173)
- Full authentication flow
- CSRF protection enabled
- Responsive design

### Mobile (Expo)
- Same authentication API
- Native mobile UI
- Offline capabilities

### Desktop (Electron)
- Native desktop experience
- System integration
- Auto-updater ready

## 🧪 API Testing

### Test CSRF Token
```bash
curl http://localhost:8080/csrf-token
```

### Test Registration
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -H "X-CSRF-TOKEN: <token>" \
  -d '{
    "email": "test@example.com",
    "password": "password123",
    "firstName": "Test",
    "lastName": "User",
    "phoneNumber": "+256700123456",
    "role": "CLIENT"
  }'
```

### Test Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123"
  }'
```

## 🔍 Health Checks

### Backend Health
```bash
curl http://localhost:8080/actuator/health
```

### Frontend Health
- Visit http://localhost:5173
- Should show Gynassist homepage

### Database Health
- Visit http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `sa`
- Password: (empty)

## 🚨 Common Errors & Fixes

### Error: "Port 8080 already in use"
```bash
# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Linux/Mac
lsof -ti:8080 | xargs kill -9
```

### Error: "CSRF token missing"
- Ensure cookies are enabled
- Check network tab for CSRF token request
- Verify backend CSRF endpoint is accessible

### Error: "User already exists"
- Use different email
- Or check H2 console to clear users table

### Error: "Invalid credentials"
- Verify password matches registration
- Check user exists in database

## 📊 Development Commands

```bash
# Start individual services
npm run start:backend    # Spring Boot API
npm run start:frontend   # React Web App
npm run start:mobile     # React Native/Expo
npm run start:desktop    # Electron App

# Build for production
npm run build:all        # All platforms
npm run build:frontend   # Web only
npm run build:mobile     # Mobile only
npm run build:desktop    # Desktop only

# Testing
npm run test:all         # All tests
npm run health-check     # Service health check

# Cleanup
npm run clean:all        # Clean all builds
```

## 🎯 Success Indicators

✅ **Backend Ready**: Console shows "Started GynassistBackendApplication"
✅ **Frontend Ready**: Browser opens to http://localhost:5173
✅ **CSRF Working**: Registration form submits without 403 errors
✅ **Auth Working**: Can register and login successfully
✅ **Database Working**: H2 console accessible with user data

## 📞 Support

If issues persist:
1. Check console logs for specific errors
2. Verify all ports are available (8080, 5173)
3. Ensure Java 21+ and Node 18+ are installed
4. Try `npm run clean:all` then `npm run setup`

---

**Ready to go!** 🎉 Your Gynassist application should now be fully functional with working authentication.