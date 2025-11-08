#!/usr/bin/env node

const { spawn, exec } = require('child_process');
const path = require('path');
const fs = require('fs');
const util = require('util');

const execAsync = util.promisify(exec);

console.log('🚀 Deploying Gynassist Cross-Platform Application');
console.log('================================================');

const projectRoot = path.join(__dirname, '..');
const isWindows = process.platform === 'win32';

// Deployment configurations
const deployments = {
  web: {
    name: 'Web Application',
    platforms: ['vercel', 'netlify', 'aws-s3'],
    buildDir: 'gynassist-frontend/dist',
    commands: {
      vercel: 'npx vercel --prod',
      netlify: 'npx netlify deploy --prod --dir=dist',
      'aws-s3': 'aws s3 sync dist/ s3://gynassist-web --delete'
    }
  },
  backend: {
    name: 'Backend API',
    platforms: ['heroku', 'aws-eb', 'digitalocean'],
    buildDir: 'Gynassist-backend/target',
    commands: {
      heroku: 'git push heroku main',
      'aws-eb': 'eb deploy',
      digitalocean: 'doctl apps create-deployment'
    }
  },
  mobile: {
    name: 'Mobile Applications',
    platforms: ['app-store', 'google-play', 'expo'],
    buildDir: 'gynassist-mobile',
    commands: {
      'app-store': 'eas submit --platform ios',
      'google-play': 'eas submit --platform android',
      expo: 'eas update'
    }
  },
  desktop: {
    name: 'Desktop Applications',
    platforms: ['github-releases', 'microsoft-store', 'mac-app-store'],
    buildDir: 'gynassist-desktop/dist',
    commands: {
      'github-releases': 'gh release create',
      'microsoft-store': 'electron-builder --win --publish=always',
      'mac-app-store': 'electron-builder --mac --publish=always'
    }
  }
};

async function checkPrerequisites() {
  console.log('🔍 Checking deployment prerequisites...\n');
  
  const checks = [
    { name: 'Node.js', command: 'node --version' },
    { name: 'npm', command: 'npm --version' },
    { name: 'Git', command: 'git --version' },
    { name: 'Java', command: 'java -version' }
  ];

  const optional = [
    { name: 'Vercel CLI', command: 'vercel --version' },
    { name: 'Netlify CLI', command: 'netlify --version' },
    { name: 'AWS CLI', command: 'aws --version' },
    { name: 'Heroku CLI', command: 'heroku --version' },
    { name: 'EAS CLI', command: 'eas --version' },
    { name: 'GitHub CLI', command: 'gh --version' }
  ];

  // Check required tools
  for (const check of checks) {
    try {
      await execAsync(check.command);
      console.log(`✅ ${check.name} is available`);
    } catch (error) {
      console.log(`❌ ${check.name} is not available`);
      console.log(`   Install: ${getInstallCommand(check.name)}`);
    }
  }

  console.log('\n📦 Optional deployment tools:');
  
  // Check optional tools
  for (const check of optional) {
    try {
      await execAsync(check.command);
      console.log(`✅ ${check.name} is available`);
    } catch (error) {
      console.log(`⚠️  ${check.name} is not available (optional)`);
    }
  }
}

function getInstallCommand(tool) {
  const commands = {
    'Node.js': 'Download from https://nodejs.org/',
    'npm': 'Comes with Node.js',
    'Git': 'Download from https://git-scm.com/',
    'Java': 'Download from https://adoptium.net/',
    'Vercel CLI': 'npm install -g vercel',
    'Netlify CLI': 'npm install -g netlify-cli',
    'AWS CLI': 'Download from https://aws.amazon.com/cli/',
    'Heroku CLI': 'Download from https://devcenter.heroku.com/articles/heroku-cli',
    'EAS CLI': 'npm install -g @expo/eas-cli',
    'GitHub CLI': 'Download from https://cli.github.com/'
  };
  
  return commands[tool] || 'Check official documentation';
}

async function deployPlatform(type, platform, config) {
  console.log(`\n🚀 Deploying ${config.name} to ${platform}...`);
  
  const buildDir = path.join(projectRoot, config.buildDir);
  
  if (!fs.existsSync(buildDir)) {
    console.log(`❌ Build directory not found: ${config.buildDir}`);
    console.log('   Run "npm run build:all" first');
    return false;
  }

  const command = config.commands[platform];
  if (!command) {
    console.log(`❌ No deployment command configured for ${platform}`);
    return false;
  }

  try {
    console.log(`⚡ Running: ${command}`);
    
    // Special handling for different deployment types
    switch (platform) {
      case 'vercel':
        await deployToVercel(buildDir);
        break;
      case 'netlify':
        await deployToNetlify(buildDir);
        break;
      case 'heroku':
        await deployToHeroku();
        break;
      case 'app-store':
      case 'google-play':
        await deployMobile(platform);
        break;
      case 'github-releases':
        await deployToGitHubReleases();
        break;
      default:
        await execAsync(command, { cwd: buildDir });
    }
    
    console.log(`✅ Successfully deployed to ${platform}`);
    return true;
  } catch (error) {
    console.error(`❌ Failed to deploy to ${platform}:`);
    console.error(error.message);
    return false;
  }
}

async function deployToVercel(buildDir) {
  const frontendDir = path.join(projectRoot, 'gynassist-frontend');
  
  // Check if vercel.json exists
  const vercelConfig = path.join(frontendDir, 'vercel.json');
  if (!fs.existsSync(vercelConfig)) {
    const config = {
      "name": "gynassist-web",
      "version": 2,
      "builds": [
        {
          "src": "dist/**/*",
          "use": "@vercel/static"
        }
      ],
      "routes": [
        {
          "src": "/(.*)",
          "dest": "/index.html"
        }
      ],
      "env": {
        "VITE_API_URL": "https://api.gynassist.ug"
      }
    };
    
    fs.writeFileSync(vercelConfig, JSON.stringify(config, null, 2));
    console.log('📝 Created vercel.json configuration');
  }
  
  await execAsync('npx vercel --prod', { cwd: frontendDir });
}

async function deployToNetlify(buildDir) {
  const frontendDir = path.join(projectRoot, 'gynassist-frontend');
  
  // Check if netlify.toml exists
  const netlifyConfig = path.join(frontendDir, 'netlify.toml');
  if (!fs.existsSync(netlifyConfig)) {
    const config = `[build]
  publish = "dist"
  command = "npm run build"

[build.environment]
  VITE_API_URL = "https://api.gynassist.ug"

[[redirects]]
  from = "/*"
  to = "/index.html"
  status = 200`;
    
    fs.writeFileSync(netlifyConfig, config);
    console.log('📝 Created netlify.toml configuration');
  }
  
  await execAsync('npx netlify deploy --prod --dir=dist', { cwd: frontendDir });
}

async function deployToHeroku() {
  const backendDir = path.join(projectRoot, 'Gynassist-backend');
  
  // Check if Procfile exists
  const procfile = path.join(backendDir, 'Procfile');
  if (!fs.existsSync(procfile)) {
    fs.writeFileSync(procfile, 'web: java -jar target/*.jar --server.port=$PORT');
    console.log('📝 Created Procfile for Heroku');
  }
  
  // Check if system.properties exists
  const systemProps = path.join(backendDir, 'system.properties');
  if (!fs.existsSync(systemProps)) {
    fs.writeFileSync(systemProps, 'java.runtime.version=21');
    console.log('📝 Created system.properties for Heroku');
  }
  
  await execAsync('git add . && git commit -m "Deploy to Heroku" || true', { cwd: backendDir });
  await execAsync('git push heroku main', { cwd: backendDir });
}

async function deployMobile(platform) {
  const mobileDir = path.join(projectRoot, 'gynassist-mobile');
  
  if (platform === 'app-store') {
    await execAsync('eas submit --platform ios', { cwd: mobileDir });
  } else if (platform === 'google-play') {
    await execAsync('eas submit --platform android', { cwd: mobileDir });
  }
}

async function deployToGitHubReleases() {
  const desktopDir = path.join(projectRoot, 'gynassist-desktop');
  const packageJson = JSON.parse(fs.readFileSync(path.join(desktopDir, 'package.json')));
  const version = packageJson.version;
  
  // Create GitHub release
  await execAsync(`gh release create v${version} dist/* --title "Gynassist Desktop v${version}" --notes "Desktop application release"`, { cwd: desktopDir });
}

async function updateEnvironmentVariables() {
  console.log('\n🔧 Updating environment variables for production...');
  
  const envUpdates = [
    {
      file: 'gynassist-frontend/.env.production',
      vars: {
        VITE_API_URL: 'https://api.gynassist.ug',
        VITE_APP_ENV: 'production'
      }
    },
    {
      file: 'Gynassist-backend/src/main/resources/application-prod.yaml',
      content: `spring:
  profiles:
    active: prod
  datasource:
    url: \${DATABASE_URL}
    username: \${DB_USERNAME}
    password: \${DB_PASSWORD}
server:
  port: \${PORT:8080}`
    }
  ];

  for (const update of envUpdates) {
    const filePath = path.join(projectRoot, update.file);
    
    if (update.vars) {
      const envContent = Object.entries(update.vars)
        .map(([key, value]) => `${key}=${value}`)
        .join('\n');
      
      fs.writeFileSync(filePath, envContent);
      console.log(`✅ Updated ${update.file}`);
    } else if (update.content) {
      fs.writeFileSync(filePath, update.content);
      console.log(`✅ Updated ${update.file}`);
    }
  }
}

async function main() {
  const args = process.argv.slice(2);
  const targetPlatform = args[0];
  const targetService = args[1];

  console.log('🚀 Starting deployment process...\n');

  // Check prerequisites
  await checkPrerequisites();

  // Update environment variables
  await updateEnvironmentVariables();

  console.log('\n📋 Available deployment targets:');
  for (const [type, config] of Object.entries(deployments)) {
    console.log(`\n${config.name}:`);
    config.platforms.forEach(platform => {
      console.log(`  • ${platform}`);
    });
  }

  if (!targetPlatform) {
    console.log('\n💡 Usage examples:');
    console.log('   npm run deploy web vercel');
    console.log('   npm run deploy backend heroku');
    console.log('   npm run deploy mobile app-store');
    console.log('   npm run deploy desktop github-releases');
    console.log('   npm run deploy all');
    return;
  }

  const results = {};

  if (targetPlatform === 'all') {
    // Deploy all platforms
    for (const [type, config] of Object.entries(deployments)) {
      console.log(`\n🎯 Deploying ${config.name}...`);
      
      // Deploy to first available platform for each type
      const platform = config.platforms[0];
      results[`${type}-${platform}`] = await deployPlatform(type, platform, config);
    }
  } else if (deployments[targetPlatform]) {
    // Deploy specific platform
    const config = deployments[targetPlatform];
    const platform = targetService || config.platforms[0];
    
    if (!config.platforms.includes(platform)) {
      console.error(`❌ Platform ${platform} not supported for ${config.name}`);
      console.log(`Available platforms: ${config.platforms.join(', ')}`);
      process.exit(1);
    }
    
    results[`${targetPlatform}-${platform}`] = await deployPlatform(targetPlatform, platform, config);
  } else {
    console.error(`❌ Unknown deployment target: ${targetPlatform}`);
    process.exit(1);
  }

  // Summary
  console.log('\n' + '='.repeat(60));
  console.log('🎉 Deployment Process Complete!');
  console.log('='.repeat(60));

  const successful = Object.values(results).filter(Boolean).length;
  const total = Object.keys(results).length;

  for (const [deployment, success] of Object.entries(results)) {
    const status = success ? '✅' : '❌';
    console.log(`${status} ${deployment}`);
  }

  console.log(`\n📊 Success rate: ${successful}/${total}`);

  if (successful < total) {
    console.log('\n⚠️  Some deployments failed. Check the logs above for details.');
    process.exit(1);
  } else {
    console.log('\n🎊 All deployments completed successfully!');
    
    console.log('\n🌐 Access your applications:');
    console.log('   • Web App: Check deployment URL');
    console.log('   • API: Check backend deployment URL');
    console.log('   • Mobile: Available in app stores');
    console.log('   • Desktop: Available for download');
  }
}

// Handle interruption
process.on('SIGINT', () => {
  console.log('\n\n🛑 Deployment process interrupted.');
  process.exit(1);
});

// Run the deployment process
main().catch(error => {
  console.error('\n❌ Deployment process failed:', error.message);
  process.exit(1);
});