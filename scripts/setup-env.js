#!/usr/bin/env node

const fs = require('fs');
const path = require('path');
const readline = require('readline');

console.log('🔧 Gynassist Environment Setup');
console.log('==============================');

const projectRoot = path.join(__dirname, '..');

const rl = readline.createInterface({
  input: process.stdin,
  output: process.stdout
});

function question(query) {
  return new Promise(resolve => rl.question(query, resolve));
}

async function setupEnvironment() {
  console.log('\n📋 This script will help you configure environment variables for all platforms.\n');

  // Collect environment variables
  const config = {};

  console.log('🌐 Backend Configuration:');
  config.apiUrl = await question('API URL (default: http://localhost:8080): ') || 'http://localhost:8080';
  config.dbUrl = await question('Database URL (optional): ') || '';
  config.dbUsername = await question('Database Username (optional): ') || '';
  config.dbPassword = await question('Database Password (optional): ') || '';
  config.jwtSecret = await question('JWT Secret (leave empty to generate): ') || generateJwtSecret();

  console.log('\n💳 Payment Configuration:');
  config.stripePublicKey = await question('Stripe Public Key (optional): ') || '';
  config.stripeSecretKey = await question('Stripe Secret Key (optional): ') || '';
  config.mtnApiKey = await question('MTN Mobile Money API Key (optional): ') || '';
  config.airtelApiKey = await question('Airtel Money API Key (optional): ') || '';

  console.log('\n🏥 MOH Integration:');
  config.mohApiUrl = await question('MOH API URL (optional): ') || '';
  config.mohApiKey = await question('MOH API Key (optional): ') || '';
  config.dhisUrl = await question('DHIS2 URL (optional): ') || '';
  config.dhisUsername = await question('DHIS2 Username (optional): ') || '';
  config.dhisPassword = await question('DHIS2 Password (optional): ') || '';

  console.log('\n📱 Mobile Configuration:');
  config.expoProjectId = await question('Expo Project ID (optional): ') || '';
  config.firebaseApiKey = await question('Firebase API Key (optional): ') || '';

  console.log('\n☁️ Cloud Services:');
  config.awsAccessKey = await question('AWS Access Key (optional): ') || '';
  config.awsSecretKey = await question('AWS Secret Key (optional): ') || '';
  config.awsRegion = await question('AWS Region (default: us-east-1): ') || 'us-east-1';

  rl.close();

  // Create environment files
  await createEnvironmentFiles(config);
  
  console.log('\n✅ Environment setup complete!');
  console.log('\n📁 Created files:');
  console.log('   • gynassist-frontend/.env');
  console.log('   • gynassist-frontend/.env.production');
  console.log('   • gynassist-mobile/.env');
  console.log('   • gynassist-desktop/.env');
  console.log('   • Gynassist-backend/src/main/resources/application-local.yaml');
  console.log('\n🔐 Security Notes:');
  console.log('   • Never commit .env files to version control');
  console.log('   • Use different secrets for production');
  console.log('   • Rotate API keys regularly');
  console.log('\n🚀 Next steps:');
  console.log('   1. Review and update the generated files');
  console.log('   2. Run "npm run setup" to install dependencies');
  console.log('   3. Run "npm run dev" to start development');
}

async function createEnvironmentFiles(config) {
  // Frontend .env
  const frontendEnv = `# Gynassist Frontend Environment Variables
VITE_API_URL=${config.apiUrl}
VITE_APP_NAME=Gynassist
VITE_APP_VERSION=1.0.0
VITE_STRIPE_PUBLIC_KEY=${config.stripePublicKey}
VITE_FIREBASE_API_KEY=${config.firebaseApiKey}
VITE_MOH_API_URL=${config.mohApiUrl}
`;

  const frontendProdEnv = `# Gynassist Frontend Production Environment Variables
VITE_API_URL=https://api.gynassist.ug
VITE_APP_NAME=Gynassist
VITE_APP_VERSION=1.0.0
VITE_STRIPE_PUBLIC_KEY=${config.stripePublicKey}
VITE_FIREBASE_API_KEY=${config.firebaseApiKey}
VITE_MOH_API_URL=${config.mohApiUrl}
`;

  // Mobile .env
  const mobileEnv = `# Gynassist Mobile Environment Variables
EXPO_PROJECT_ID=${config.expoProjectId}
API_URL=${config.apiUrl}
STRIPE_PUBLIC_KEY=${config.stripePublicKey}
FIREBASE_API_KEY=${config.firebaseApiKey}
MTN_API_KEY=${config.mtnApiKey}
AIRTEL_API_KEY=${config.airtelApiKey}
`;

  // Desktop .env
  const desktopEnv = `# Gynassist Desktop Environment Variables
REACT_APP_API_URL=${config.apiUrl}
REACT_APP_STRIPE_PUBLIC_KEY=${config.stripePublicKey}
REACT_APP_FIREBASE_API_KEY=${config.firebaseApiKey}
`;

  // Backend application-local.yaml
  const backendConfig = `# Gynassist Backend Local Configuration
spring:
  profiles:
    active: local
  datasource:
    url: ${config.dbUrl || 'jdbc:h2:mem:testdb'}
    username: ${config.dbUsername || 'sa'}
    password: ${config.dbPassword || 'password'}
    driver-class-name: ${config.dbUrl ? 'org.postgresql.Driver' : 'org.h2.Driver'}
  
  jpa:
    hibernate:
      ddl-auto: ${config.dbUrl ? 'update' : 'create-drop'}
    show-sql: true
    database-platform: ${config.dbUrl ? 'org.hibernate.dialect.PostgreSQLDialect' : 'org.hibernate.dialect.H2Dialect'}

app:
  jwt:
    secret: ${config.jwtSecret}
    expiration: 86400000
  
  payment:
    stripe:
      secret-key: ${config.stripeSecretKey}
    mtn:
      api-key: ${config.mtnApiKey}
      api-url: https://sandbox.momodeveloper.mtn.com
    airtel:
      api-key: ${config.airtelApiKey}
      api-url: https://openapiuat.airtel.africa
  
  moh:
    api-url: ${config.mohApiUrl}
    api-key: ${config.mohApiKey}
  
  dhis:
    url: ${config.dhisUrl}
    username: ${config.dhisUsername}
    password: ${config.dhisPassword}

server:
  port: 8080

logging:
  level:
    com.gynassist: DEBUG
    org.springframework.web: INFO
`;

  // Write files
  const files = [
    { path: 'gynassist-frontend/.env', content: frontendEnv },
    { path: 'gynassist-frontend/.env.production', content: frontendProdEnv },
    { path: 'gynassist-mobile/.env', content: mobileEnv },
    { path: 'gynassist-desktop/.env', content: desktopEnv },
    { path: 'Gynassist-backend/src/main/resources/application-local.yaml', content: backendConfig }
  ];

  for (const file of files) {
    const filePath = path.join(projectRoot, file.path);
    const dir = path.dirname(filePath);
    
    // Create directory if it doesn't exist
    if (!fs.existsSync(dir)) {
      fs.mkdirSync(dir, { recursive: true });
    }
    
    fs.writeFileSync(filePath, file.content);
    console.log(`✅ Created ${file.path}`);
  }

  // Create .gitignore entries
  const gitignoreEntries = [
    'gynassist-frontend/.gitignore',
    'gynassist-mobile/.gitignore',
    'gynassist-desktop/.gitignore'
  ];

  for (const gitignorePath of gitignoreEntries) {
    const fullPath = path.join(projectRoot, gitignorePath);
    if (fs.existsSync(fullPath)) {
      let content = fs.readFileSync(fullPath, 'utf8');
      if (!content.includes('.env')) {
        content += '\n# Environment variables\n.env\n.env.local\n.env.production\n';
        fs.writeFileSync(fullPath, content);
      }
    }
  }
}

function generateJwtSecret() {
  const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=';
  let result = '';
  for (let i = 0; i < 64; i++) {
    result += chars.charAt(Math.floor(Math.random() * chars.length));
  }
  return result;
}

// Run setup
setupEnvironment().catch(error => {
  console.error('❌ Setup failed:', error.message);
  process.exit(1);
});