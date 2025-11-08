#!/usr/bin/env node

const { spawn } = require('child_process');
const path = require('path');
const fs = require('fs');

console.log('🚀 Starting Gynassist Cross-Platform Application');
console.log('================================================');

const processes = [];
const isWindows = process.platform === 'win32';

// Function to spawn a process
function spawnProcess(name, command, args, cwd, color) {
  console.log(`\n${color}Starting ${name}...${'\x1b[0m'}`);
  
  const child = spawn(command, args, {
    cwd,
    stdio: 'pipe',
    shell: isWindows
  });

  child.stdout.on('data', (data) => {
    const lines = data.toString().split('\n').filter(line => line.trim());
    lines.forEach(line => {
      console.log(`${color}[${name}]${'\x1b[0m'} ${line}`);
    });
  });

  child.stderr.on('data', (data) => {
    const lines = data.toString().split('\n').filter(line => line.trim());
    lines.forEach(line => {
      console.log(`${color}[${name}]${'\x1b[0m'} ${line}`);
    });
  });

  child.on('close', (code) => {
    console.log(`${color}[${name}]${'\x1b[0m'} Process exited with code ${code}`);
  });

  child.on('error', (error) => {
    console.error(`${color}[${name}]${'\x1b[0m'} Error: ${error.message}`);
  });

  processes.push({ name, child });
  return child;
}

// Check if directories exist
const projectRoot = path.join(__dirname, '..');
const backendDir = path.join(projectRoot, 'Gynassist-backend');
const frontendDir = path.join(projectRoot, 'gynassist-frontend');
const mobileDir = path.join(projectRoot, 'gynassist-mobile');
const desktopDir = path.join(projectRoot, 'gynassist-desktop');

// Colors for different processes
const colors = {
  backend: '\x1b[32m',    // Green
  frontend: '\x1b[34m',   // Blue
  mobile: '\x1b[35m',     // Magenta
  desktop: '\x1b[36m'     // Cyan
};

async function startServices() {
  try {
    // Start Backend (Spring Boot)
    if (fs.existsSync(backendDir)) {
      console.log('\n📊 Starting Backend Service...');
      const mvnCommand = isWindows ? 'mvnw.cmd' : './mvnw';
      spawnProcess('Backend', mvnCommand, ['spring-boot:run'], backendDir, colors.backend);
      
      // Wait a bit for backend to start
      await new Promise(resolve => setTimeout(resolve, 5000));
    } else {
      console.log('⚠️  Backend directory not found, skipping...');
    }

    // Start Frontend (React + Vite)
    if (fs.existsSync(frontendDir)) {
      console.log('\n🌐 Starting Web Frontend...');
      spawnProcess('Frontend', 'npm', ['run', 'dev'], frontendDir, colors.frontend);
      
      await new Promise(resolve => setTimeout(resolve, 3000));
    } else {
      console.log('⚠️  Frontend directory not found, skipping...');
    }

    // Start Mobile (React Native + Expo)
    if (fs.existsSync(mobileDir)) {
      console.log('\n📱 Starting Mobile App...');
      spawnProcess('Mobile', 'npx', ['expo', 'start'], mobileDir, colors.mobile);
      
      await new Promise(resolve => setTimeout(resolve, 3000));
    } else {
      console.log('⚠️  Mobile directory not found, skipping...');
    }

    // Start Desktop (Electron)
    if (fs.existsSync(desktopDir)) {
      console.log('\n🖥️  Starting Desktop App...');
      spawnProcess('Desktop', 'npm', ['run', 'electron:dev'], desktopDir, colors.desktop);
    } else {
      console.log('⚠️  Desktop directory not found, skipping...');
    }

    // Display service URLs
    setTimeout(() => {
      console.log('\n' + '='.repeat(60));
      console.log('🎉 Gynassist Services Started Successfully!');
      console.log('='.repeat(60));
      console.log('📊 Backend API:     http://localhost:8080');
      console.log('🌐 Web Frontend:    http://localhost:5173');
      console.log('📱 Mobile App:      Expo DevTools in browser');
      console.log('🖥️  Desktop App:     Electron window');
      console.log('='.repeat(60));
      console.log('');
      console.log('📋 Available Endpoints:');
      console.log('   • Dashboard:     /dashboard');
      console.log('   • AI Chat:       /chat');
      console.log('   • Cycle Tracker: /cycle-tracker');
      console.log('   • Consultations: /consultations');
      console.log('   • Health Tips:   /health-tips');
      console.log('   • MOH Alerts:    /notifications');
      console.log('   • Emergency:     /emergency');
      console.log('');
      console.log('🔧 Development Commands:');
      console.log('   • Stop all:      Ctrl+C');
      console.log('   • Restart:       npm run start:all');
      console.log('   • Build all:     npm run build:all');
      console.log('');
      console.log('📱 Mobile Development:');
      console.log('   • iOS Simulator: Press "i" in Expo CLI');
      console.log('   • Android:       Press "a" in Expo CLI');
      console.log('   • Web:           Press "w" in Expo CLI');
      console.log('');
      console.log('🖥️  Desktop Development:');
      console.log('   • DevTools:      Ctrl+Shift+I (in Electron)');
      console.log('   • Reload:        Ctrl+R (in Electron)');
      console.log('');
    }, 8000);

  } catch (error) {
    console.error('❌ Error starting services:', error.message);
    process.exit(1);
  }
}

// Handle process termination
process.on('SIGINT', () => {
  console.log('\n\n🛑 Shutting down all services...');
  
  processes.forEach(({ name, child }) => {
    console.log(`Stopping ${name}...`);
    if (isWindows) {
      spawn('taskkill', ['/pid', child.pid, '/f', '/t']);
    } else {
      child.kill('SIGTERM');
    }
  });
  
  setTimeout(() => {
    console.log('✅ All services stopped.');
    process.exit(0);
  }, 2000);
});

process.on('SIGTERM', () => {
  console.log('\n\n🛑 Received SIGTERM, shutting down...');
  process.exit(0);
});

// Start all services
startServices().catch(error => {
  console.error('❌ Failed to start services:', error);
  process.exit(1);
});