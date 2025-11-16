#!/usr/bin/env node

const { spawn } = require('child_process');
const path = require('path');

console.log('🚀 Starting GynAid Application Suite...\n');

// Colors for console output
const colors = {
  reset: '\x1b[0m',
  bright: '\x1b[1m',
  red: '\x1b[31m',
  green: '\x1b[32m',
  yellow: '\x1b[33m',
  blue: '\x1b[34m',
  magenta: '\x1b[35m',
  cyan: '\x1b[36m'
};

function log(service, message, color = 'reset') {
  const timestamp = new Date().toLocaleTimeString();
  console.log(`${colors[color]}[${timestamp}] ${service}: ${message}${colors.reset}`);
}

// Start Backend Service
function startBackend() {
  log('BACKEND', 'Starting Spring Boot application...', 'blue');

  const backendProcess = spawn('mvnw.cmd', ['spring-boot:run'], {
    cwd: path.join(__dirname, '..', 'GynAid-backend'),
    stdio: 'inherit',
    shell: true
  });

  backendProcess.on('error', (error) => {
    log('BACKEND', `Failed to start: ${error.message}`, 'red');
  });

  backendProcess.on('close', (code) => {
    log('BACKEND', `Process exited with code ${code}`, code === 0 ? 'green' : 'red');
  });

  return backendProcess;
}

// Start Frontend Service
function startFrontend() {
  log('FRONTEND', 'Starting React development server...', 'green');

  const frontendProcess = spawn('npm', ['run', 'dev'], {
    cwd: path.join(__dirname, '..', 'GynAid-frontend'),
    stdio: 'inherit',
    shell: true
  });

  frontendProcess.on('error', (error) => {
    log('FRONTEND', `Failed to start: ${error.message}`, 'red');
  });

  frontendProcess.on('close', (code) => {
    log('FRONTEND', `Process exited with code ${code}`, code === 0 ? 'green' : 'red');
  });

  return frontendProcess;
}

// Handle process termination
function handleShutdown() {
  log('SYSTEM', 'Shutting down all services...', 'yellow');
  process.exit(0);
}

process.on('SIGINT', handleShutdown);
process.on('SIGTERM', handleShutdown);

// Start all services
try {
  const backend = startBackend();
  const frontend = startFrontend();

  log('SYSTEM', 'All services started successfully!', 'green');
  log('SYSTEM', 'Backend: http://localhost:8080', 'cyan');
  log('SYSTEM', 'Frontend: http://localhost:5173', 'cyan');
  log('SYSTEM', 'H2 Console: http://localhost:8080/h2-console', 'cyan');
  log('SYSTEM', 'Press Ctrl+C to stop all services', 'yellow');

} catch (error) {
  log('SYSTEM', `Failed to start services: ${error.message}`, 'red');
  process.exit(1);
}
