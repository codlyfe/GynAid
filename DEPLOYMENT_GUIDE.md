# 🚀 GynAid Live Deployment Guide
## Railway (Backend + Database) + Vercel (Frontend)

### 📋 Prerequisites
- Railway account (railway.app)
- Vercel account (vercel.com)
- Git repository (GitHub/GitLab/Bitbucket)
- Domain name (optional but recommended)

---

## 🏗️ PART 1: Railway Backend & Database Deployment

### Step 1: Prepare Backend for Railway

#### 1.1 Create Railway Configuration
Create `Procfile` in GynAid-backend root:
```
web: java -Dserver.port=$PORT $JAVA_OPTS -jar target/*.jar
```

Create `railway.toml` in GynAid-backend root:
```toml
[build]
builder = "nixpacks"

[deploy]
startCommand = "java -Dserver.port=$PORT $JAVA_OPTS -jar target/backend-0.0.1-SNAPSHOT.jar"
restartPolicyType = "on_failure"
restartPolicyMaxRetries = 10
```

#### 1.2 Update Application Configuration
Update `application.yaml` for Railway environment:

**Add Railway-specific configuration at the top of the file:**
```yaml
# Railway Production Configuration
spring:
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:railway}
  config:
    activate:
      on-profile: railway
      
# Railway Database Configuration  
spring:
  datasource:
    url: jdbc:postgresql://${RAILWAY_DATABASE_HOST}:${RAILWAY_DATABASE_PORT}/${RAILWAY_DATABASE_NAME}
    username: ${RAILWAY_DATABASE_USERNAME}
    password: ${RAILWAY_DATABASE_PASSWORD}
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: ${DB_POOL_SIZE:10}
      minimum-idle: ${DB_MIN_IDLE:2}
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: false

# Railway Redis Configuration
spring:
  redis:
    host: ${REDIS_HOST}
    port: ${REDIS_PORT}
    password: ${REDIS_PASSWORD}
    timeout: ${REDIS_TIMEOUT:2000ms}
    lettuce:
      pool:
        max-active: ${REDIS_POOL_MAX_ACTIVE:8}
        max-idle: ${REDIS_POOL_MAX_IDLE:8}
        min-idle: ${REDIS_POOL_MIN_IDLE:0}

# Railway Mail Configuration
spring:
  mail:
    host: ${MAIL_HOST}
    port: ${MAIL_PORT}
    username: ${MAIL_USERNAME}
    password: ${MAIL_PASSWORD}
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
          ssl:
            trust: "*"

# Railway Server Configuration
server:
  port: ${PORT:8080}
  servlet:
    context-path: /api

# Railway CORS Configuration
app:
  cors:
    allowed-origins: ${FRONTEND_URL:https://your-app.vercel.app},${DOMAIN_URL:https://yourdomain.com}
    allowed-methods: GET,POST,PUT,DELETE,OPTIONS,PATCH
    allowed-headers: "Authorization,Content-Type,X-Requested-With,Accept,Origin,Access-Control-Request-Method,Access-Control-Request-Headers"
    allow-credentials: true
    
# Railway JWT Configuration  
app:
  jwt:
    secret: ${JWT_SECRET}
    expiration: ${JWT_EXPIRATION:86400000}
    refresh-expiration: ${JWT_REFRESH_EXPIRATION:604800000}

# Railway Security Configuration
app:
  security:
    rate-limiting:
      enabled: true
      requests-per-minute: ${RATE_LIMIT_PER_MINUTE:100}
      burst-capacity: ${RATE_LIMIT_BURST:200}
    jwt:
      blacklist-enabled: true

# Railway External Service Configuration
app:
  sms:
    twilio:
      account-sid: ${TWILIO_ACCOUNT_SID}
      auth-token: ${TWILIO_AUTH_TOKEN}
      phone-number: ${TWILIO_PHONE_NUMBER}
  stripe:
    secret-key: ${STRIPE_SECRET_KEY}
    webhook-secret: ${STRIPE_WEBHOOK_SECRET}
  mobile-money:
    mtn:
      client-id: ${MTN_CLIENT_ID}
      client-secret: ${MTN_CLIENT_SECRET}
      base-url: ${MTN_BASE_URL:https://sandbox.mtn.com}
    airtel:
      client-id: ${AIRTEL_CLIENT_ID}
      client-secret: ${AIRTEL_CLIENT_SECRET}
      base-url: ${AIRTEL_BASE_URL:https://sandbox.airtel.africa}
  mfa:
    issuer: ${MFA_ISSUER:"GynAid Production"}
    qr-code-url: ${MFA_QR_CODE_URL:"https://api.qrserver.com/v1/create-qr-code"}

# Railway Actuator Configuration
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
      base-path: /actuator
  endpoint:
    health:
      show-details: always
  metrics:
    export:
      prometheus:
        enabled: true

# Railway Logging Configuration
logging:
  level:
    com.gynaid: ${LOG_LEVEL:INFO}
    org.springframework.security: WARN
    org.springframework.web: WARN
    org.hibernate: WARN
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
```

### Step 2: Deploy to Railway

#### 2.1 Initialize Git and Push to Repository
```bash
# In project root
git init
git add .
git commit -m "Initial commit - Ready for Railway deployment"
git remote add origin https://github.com/yourusername/gynaid.git
git push -u origin main
```

#### 2.2 Deploy Backend to Railway
1. **Create Railway Project:**
   ```bash
   npm install -g @railway/cli
   railway login
   railway init
   ```

2. **Add PostgreSQL Database:**
   ```bash
   railway add postgresql
   ```

3. **Add Redis Plugin (Optional):**
   ```bash
   railway add redis
   ```

4. **Set Environment Variables:**
   ```bash
   # Core App Configuration
   railway variables set SPRING_PROFILES_ACTIVE=railway
   railway variables set FRONTEND_URL=https://your-frontend.vercel.app
   
   # JWT Configuration (Generate secure keys)
   railway variables set JWT_SECRET=your-super-secure-jwt-secret-key-here
   
   # Database (Auto-populated by Railway)
   # RAILWAY_DATABASE_URL, RAILWAY_DATABASE_HOST, etc. are auto-set
   
   # Email Configuration (Use Railway's built-in email or external SMTP)
   railway variables set MAIL_HOST=smtp.gmail.com
   railway variables set MAIL_PORT=587
   railway variables set MAIL_USERNAME=your-email@gmail.com
   railway variables set MAIL_PASSWORD=your-app-password
   
   # Twilio Configuration (for SMS)
   railway variables set TWILIO_ACCOUNT_SID=your-twilio-account-sid
   railway variables set TWILIO_AUTH_TOKEN=your-twilio-auth-token
   railway variables set TWILIO_PHONE_NUMBER=your-twilio-phone-number
   
   # Stripe Configuration (for payments)
   railway variables set STRIPE_SECRET_KEY=sk_live_your_stripe_secret_key
   railway variables set STRIPE_WEBHOOK_SECRET=whsec_your_webhook_secret
   
   # Mobile Money Configuration (MTN/Airtel)
   railway variables set MTN_CLIENT_ID=your-mtn-client-id
   railway variables set MTN_CLIENT_SECRET=your-mtn-client-secret
   railway variables set AIRTEL_CLIENT_ID=your-airtel-client-id
   railway variables set AIRTEL_CLIENT_SECRET=your-airtel-client-secret
   
   # Rate Limiting Configuration
   railway variables set RATE_LIMIT_PER_MINUTE=60
   railway variables set RATE_LIMIT_BURST=100
   
   # Domain Configuration (if you have a custom domain)
   railway variables set DOMAIN_URL=https://yourdomain.com
   ```

5. **Deploy:**
   ```bash
   railway up
   ```

6. **Wait for deployment and note the Railway-provided URL:**
   ```bash
   railway domain  # This shows your app URL
   ```

---

## 🌐 PART 2: Vercel Frontend Deployment

### Step 1: Prepare Frontend for Vercel

#### 1.1 Create Vercel Configuration
Create `vercel.json` in GynAid-frontend root:
```json
{
  "version": 2,
  "builds": [
    {
      "src": "package.json",
      "use": "@vercel/static-build",
      "config": {
        "distDir": "dist"
      }
    }
  ],
  "routes": [
    {
      "src": "/static/(.*)",
      "headers": {
        "cache-control": "s-maxage=31536000,immutable"
      },
      "dest": "/static/$1"
    },
    {
      "src": "/(.*)",
      "dest": "/index.html"
    }
  ],
  "env": {
    "NODE_VERSION": "18"
  },
  "build": {
    "env": {
      "VITE_API_URL": "@vite-api-url"
    }
  }
}
```

Create `netlify.toml` as backup:
```toml
[build]
  command = "npm run build"
  publish = "dist"

[[redirects]]
  from = "/*"
  to = "/index.html"
  status = 200
```

#### 1.2 Update Environment Configuration
Update `GynAid-frontend/.env.production`:
```env
# Production API URL (Railway backend URL)
VITE_API_URL=https://your-railway-backend.up.railway.app/api

# App Configuration
VITE_APP_NAME=GynAid
VITE_APP_VERSION=1.0.0
VITE_APP_ENV=production

# Feature Flags
VITE_ENABLE_ANALYTICS=true
VITE_ENABLE_ERROR_TRACKING=true
VITE_ENABLE_PERFORMANCE_MONITORING=true

# Security
VITE_ENABLE_CSP=true

# Production Analytics (Optional)
VITE_GA_TRACKING_ID=GA-XXXXXXXXX
VITE_GTM_ID=GTM-XXXXXXX

# Error Tracking (Optional)
VITE_SENTRY_DSN=https://your-sentry-dsn@sentry.io/project-id

# Log Level
VITE_LOG_LEVEL=error
```

### Step 2: Deploy to Vercel

#### 2.1 Install Vercel CLI
```bash
npm install -g vercel
```

#### 2.2 Deploy Frontend
```bash
cd GynAid-frontend
vercel login
vercel --prod
```

#### 2.3 Configure Environment Variables in Vercel Dashboard
1. **Go to Vercel Dashboard**
2. **Select your project**
3. **Go to Settings > Environment Variables**
4. **Add these variables:**

```
VITE_API_URL=https://your-railway-backend.up.railway.app/api
VITE_APP_NAME=GynAid
VITE_APP_VERSION=1.0.0
VITE_APP_ENV=production
VITE_ENABLE_ANALYTICS=true
VITE_ENABLE_ERROR_TRACKING=true
VITE_ENABLE_PERFORMANCE_MONITORING=true
VITE_GA_TRACKING_ID=GA-XXXXXXXXX
VITE_SENTRY_DSN=https://your-sentry-dsn@sentry.io/project-id
VITE_LOG_LEVEL=error
```

#### 2.4 Update Railway CORS Settings
After getting your Vercel URL, update Railway:
```bash
railway variables set FRONTEND_URL=https://your-app.vercel.app
railway variables set DOMAIN_URL=https://yourdomain.com  # If you have custom domain
```

---

## 🔑 PART 3: API Keys & Environment Configuration

### Required Service Accounts & API Keys

#### 1. **Twilio (SMS Service)**
- Sign up at [twilio.com](https://www.twilio.com)
- Get Account SID, Auth Token, and Phone Number
- **Cost**: ~$0.0075 per SMS

#### 2. **Stripe (Payment Processing)**
- Sign up at [stripe.com](https://stripe.com)
- Get Secret Key and Webhook Secret
- **Cost**: 2.9% + 30¢ per transaction

#### 3. **Email Service (Gmail/SendGrid)**
- **Option A**: Gmail App Password
  - Enable 2FA on Gmail
  - Generate App Password
- **Option B**: SendGrid
  - Sign up at [sendgrid.com](https://sendgrid.com)
  - Get API Key
- **Cost**: Free up to 100 emails/day (Gmail), $15/month (SendGrid)

#### 4. **Google Analytics (Optional)**
- Set up at [analytics.google.com](https://analytics.google.com)
- Get Tracking ID (GA-XXXXXXXXX)

#### 5. **Sentry (Error Tracking - Optional)**
- Sign up at [sentry.io](https://sentry.io)
- Get DSN URL
- **Cost**: Free for limited usage

#### 6. **MTN Mobile Money (Uganda)**
- Apply at MTN Developer Portal
- Get Client ID and Client Secret
- **Production Environment**: Requires business registration

#### 7. **Airtel Money (Uganda)**
- Apply at Airtel Developer Portal  
- Get Client ID and Client Secret
- **Production Environment**: Requires business registration

### Security Best Practices

#### 1. **Generate Secure JWT Secret**
```bash
# Generate a 256-bit JWT secret
openssl rand -base64 64

# Or use an online JWT secret generator
```

#### 2. **Use Strong Passwords**
- All environment variables should use strong, unique passwords
- Use Railway and Vercel's built-in secret management

#### 3. **Enable HTTPS Everywhere**
- Railway automatically provides HTTPS
- Vercel provides HTTPS by default
- Always use `https://` in production URLs

---

## 🧪 PART 4: Testing Your Live Deployment

### Backend Testing
```bash
# Test health endpoint
curl https://your-railway-backend.up.railway.app/api/health

# Test placeholder images
curl https://your-railway-backend.up.railway.app/api/placeholder/100/100

# Test login endpoint
curl -X POST https://your-railway-backend.up.railway.app/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password"}'
```

### Frontend Testing
1. **Open your Vercel URL**: `https://your-app.vercel.app`
2. **Check browser console** for any errors
3. **Test login functionality** with test credentials
4. **Test image loading** (placeholder images should work)
5. **Test API connectivity** (no CORS errors)

### Database Verification
```bash
# Check Railway database connection
railway run psql $RAILWAY_DATABASE_URL -c "\l"
```

---

## 🔧 PART 5: Custom Domain Setup (Optional)

### 1. Configure Custom Domain in Vercel
1. **Go to Vercel Dashboard > Project > Settings > Domains**
2. **Add your domain** (e.g., `gynaid.com`)
3. **Update DNS records** as instructed
4. **Update Railway environment:**
   ```bash
   railway variables set DOMAIN_URL=https://gynaid.com
   ```

### 2. Update CORS Settings
Update Railway CORS to allow your custom domain:
```bash
railway variables set DOMAIN_URL=https://gynaid.com
railway variables set FRONTEND_URL=https://gynaid.com
```

---

## 📊 PART 6: Monitoring & Maintenance

### 1. Railway Monitoring
- **URL**: `https://railway.app/dashboard`
- **Key Metrics**: CPU, Memory, Database connections, Response times
- **Logs**: `railway logs`

### 2. Vercel Analytics
- **URL**: `https://vercel.com/analytics`
- **Track**: Page views, performance, errors

### 3. Database Monitoring
- Railway provides built-in database monitoring
- Set up alerts for high CPU/memory usage

### 4. Error Tracking
- Use Sentry for error tracking
- Set up alerts for critical errors

---

## 🚨 TROUBLESHOOTING

### Common Issues & Solutions

#### 1. **CORS Errors**
- **Problem**: Frontend can't connect to backend
- **Solution**: Update Railway `FRONTEND_URL` environment variable

#### 2. **Database Connection Failed**
- **Problem**: Backend can't connect to database
- **Solution**: Check Railway database credentials in variables

#### 3. **Build Failures**
- **Problem**: Frontend/Backend build fails
- **Solution**: Check logs in Railway/Vercel dashboard

#### 4. **Environment Variables Not Working**
- **Problem**: Variables not loaded correctly
- **Solution**: Ensure proper naming and restart services

#### 5. **Images Not Loading**
- **Problem**: Placeholder images return 404
- **Solution**: Check if PlaceholderController is deployed correctly

---

## 💰 ESTIMATED MONTHLY COSTS

### Railway (Backend + Database)
- **Starter Plan**: $5/month
  - 512MB RAM, 1GB storage, unlimited bandwidth
- **Pro Plan**: $20/month (recommended)
  - 8GB RAM, 100GB storage, priority support

### Vercel (Frontend)
- **Hobby Plan**: Free
  - 100GB bandwidth, unlimited deployments
- **Pro Plan**: $20/month (if you need more features)

### External Services
- **Twilio SMS**: ~$10/month (depending on usage)
- **Stripe**: 2.9% + 30¢ per transaction
- **Email**: Free (Gmail) or $15/month (SendGrid)
- **Domain**: ~$12/year
- **Total Estimated**: $40-60/month for moderate usage

---

## ✅ DEPLOYMENT CHECKLIST

### Backend (Railway)
- [ ] Create Railway project
- [ ] Add PostgreSQL database
- [ ] Add Redis (optional)
- [ ] Set all environment variables
- [ ] Deploy successfully
- [ ] Test API endpoints
- [ ] Verify database migrations

### Frontend (Vercel)
- [ ] Create Vercel project
- [ ] Set environment variables
- [ ] Deploy successfully
- [ ] Test in production
- [ ] Verify API connectivity
- [ ] Test responsive design

### Testing
- [ ] Test login/registration
- [ ] Test image loading
- [ ] Test mobile responsiveness
- [ ] Test error scenarios
- [ ] Monitor performance

### Security
- [ ] Enable HTTPS
- [ ] Configure CORS properly
- [ ] Set strong JWT secrets
- [ ] Configure rate limiting
- [ ] Test security headers

### Launch
- [ ] Configure custom domain (optional)
- [ ] Set up monitoring
- [ ] Create backup strategy
- [ ] Document maintenance procedures
- [ ] Team training

---

## 📞 SUPPORT & RESOURCES

- **Railway Docs**: [docs.railway.app](https://docs.railway.app)
- **Vercel Docs**: [vercel.com/docs](https://vercel.com/docs)
- **Spring Boot Docs**: [spring.io/projects/spring-boot](https://spring.io/projects/spring-boot)
- **React Deployment**: [create-react-app.dev/deployment](https://create-react-app.dev/docs/deployment/)

---

**🎉 Congratulations! Your GynAid application is now live and accessible to users worldwide!**