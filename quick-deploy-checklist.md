# Quick Deployment Checklist

## 🚀 Pre-Deployment Requirements
- [ ] GitHub repository with your GynAid code
- [ ] Railway account (sign up with GitHub)
- [ ] Vercel account (sign up with GitHub)
- [ ] Custom domain (optional but recommended)

## 📦 Phase 1: Railway Backend Deployment

### Step 1: Prepare Backend
```bash
# 1. Navigate to backend directory
cd GynAid-backend

# 2. Test local build
./mvnw clean package -DskipTests

# 3. Commit changes
git add .
git commit -m "Prepare for Railway deployment"
git push origin main
```

### Step 2: Deploy to Railway
1. **Create Railway Project**
   - Go to [railway.app](https://railway.app)
   - Click "New Project" → "Deploy from GitHub repo"
   - Select your GynAid repository

2. **Add PostgreSQL Database**
   - In Railway dashboard, click "New" → "Database" → "Add PostgreSQL"

3. **Configure Backend Service**
   - Build Command: `mvn clean package -DskipTests`
   - Start Command: `java -jar target/gynaid-backend-0.0.1.jar`

4. **Set Environment Variables**
   ```
   SPRING_PROFILES_ACTIVE=production
   JWT_SECRET=your-super-secret-jwt-key-minimum-256-bits
   ALLOWED_ORIGINS=https://your-vercel-app.vercel.app
   PORT=8080
   ```

5. **Wait for Deployment**
   - Monitor logs in Railway dashboard
   - Note your Railway app URL (e.g., `https://gynaid-backend-production.up.railway.app`)

### Step 3: Test Backend
- Visit: `https://your-railway-app-url.up.railway.app/actuator/health`
- Should return: `{"status": "UP", ...}`

## 🌐 Phase 2: Vercel Frontend Deployment

### Step 1: Update Frontend Configuration
```bash
# 1. Navigate to frontend directory
cd GynAid-frontend

# 2. Update .env.production with your Railway URL
echo "VITE_API_URL=https://your-railway-app-url.up.railway.app" > .env.production

# 3. Test local build
npm run build
```

### Step 2: Deploy to Vercel

**Option A: CLI Deployment**
```bash
# 1. Install Vercel CLI
npm install -g vercel

# 2. Login to Vercel
vercel login

# 3. Deploy
vercel --prod
```

**Option B: GitHub Integration**
1. Go to [vercel.com](https://vercel.com)
2. Import your GitHub repository
3. Configure build settings:
   - Framework: Vite
   - Build Command: `npm run build`
   - Output Directory: `dist`
4. Add environment variable:
   ```
   VITE_API_URL=https://your-railway-app-url.up.railway.app
   ```

### Step 3: Test Frontend
- Visit your Vercel app URL
- Check browser console for API connectivity
- Test user flows

## 🔧 Phase 3: Production Configuration

### Railway Environment Variables
```
DATABASE_URL=postgresql://user:pass@host:port/dbname (auto-generated)
SPRING_PROFILES_ACTIVE=production
JWT_SECRET=your-super-secret-jwt-key-256-bits-minimum
ALLOWED_ORIGINS=https://your-app.vercel.app,https://your-custom-domain.com
PORT=8080
DB_USERNAME=your_db_username (from DATABASE_URL)
DB_PASSWORD=your_db_password (from DATABASE_URL)
```

### Vercel Environment Variables
```
VITE_API_URL=https://your-railway-backend.up.railway.app
VITE_APP_NAME=GynAid
VITE_APP_VERSION=1.0.0
VITE_APP_ENV=production
```

## 🔍 Phase 4: Health Checks

### Backend Health Checks
- [ ] `GET /actuator/health` returns 200 OK
- [ ] `GET /actuator/info` returns app info
- [ ] Database connection successful
- [ ] CORS properly configured

### Frontend Health Checks
- [ ] App loads without JavaScript errors
- [ ] API calls to backend successful
- [ ] User authentication working
- [ ] Responsive design intact

## 🚨 Troubleshooting Common Issues

### Backend Issues
1. **Port Binding Error**
   - Ensure `server.port=${PORT:8080}` in application.yml
   - Check Railway logs for errors

2. **Database Connection Error**
   - Verify DATABASE_URL is properly set
   - Check Railway PostgreSQL service status

3. **CORS Issues**
   - Update ALLOWED_ORIGINS with your Vercel URL
   - Ensure proper headers in responses

### Frontend Issues
1. **API Connection Failed**
   - Verify VITE_API_URL points to correct Railway backend
   - Check browser network tab for errors

2. **Build Failures**
   ```bash
   # Test locally first
   npm run build
   npm run preview
   ```

## 📊 Monitoring URLs

After deployment, monitor these endpoints:

**Backend (Railway)**
- Health: `https://your-app.up.railway.app/actuator/health`
- Info: `https://your-app.up.railway.app/actuator/info`

**Frontend (Vercel)**
- App: `https://your-app.vercel.app`
- Analytics: Available in Vercel dashboard

## 🎯 Final Verification

- [ ] Backend health check passes
- [ ] Frontend loads successfully
- [ ] Database migrations completed
- [ ] Environment variables configured
- [ ] SSL certificates active (automatic)
- [ ] Custom domains configured (if applicable)
- [ ] Monitoring alerts set up
- [ ] Backup strategy in place

## 💡 Deployment Commands Summary

```bash
# Quick deployment sequence
cd GynAid-backend
git add .
git commit -m "Deploy to production"
git push origin main

cd ../GynAid-frontend
npm run build
vercel --prod

# Monitor deployments
# Railway: Check dashboard logs
# Vercel: Check deployment status
```

## 🆘 Emergency Contacts & Resources

- **Railway Support**: [railway.app/support](https://railway.app/support)
- **Vercel Support**: [vercel.com/support](https://vercel.com/support)
- **PostgreSQL Docs**: [postgresql.org/docs](https://www.postgresql.org/docs/)

---
**🎉 Deployment Complete!** Your GynAid app is now live and accessible to users worldwide!