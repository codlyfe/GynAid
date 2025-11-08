#!/usr/bin/env node

const { spawn, exec } = require('child_process');
const path = require('path');
const fs = require('fs');
const util = require('util');

const execAsync = util.promisify(exec);

console.log('🏗️  Building Gynassist Cross-Platform Application');
console.log('================================================');

const projectRoot = path.join(__dirname, '..');
const isWindows = process.platform === 'win32';

// Build configurations
const builds = {
  backend: {
    name: 'Backend (Spring Boot)',
    dir: 'Gynassist-backend',
    command: isWindows ? 'mvnw.cmd' : './mvnw',
    args: ['clean', 'package', '-DskipTests'],
    output: 'target/*.jar'
  },
  frontend: {
    name: 'Frontend (React + Vite)',
    dir: 'gynassist-frontend',
    command: 'npm',
    args: ['run', 'build'],
    output: 'dist/'
  },
  mobile: {
    name: 'Mobile (React Native + Expo)',
    dir: 'gynassist-mobile',
    command: 'eas',
    args: ['build', '--platform', 'all', '--non-interactive'],
    output: 'builds/'
  },
  desktop: {
    name: 'Desktop (Electron)',
    dir: 'gynassist-desktop',
    command: 'npm',
    args: ['run', 'build:electron'],
    output: 'dist/'
  }
};

async function runCommand(command, args, cwd) {
  return new Promise((resolve, reject) => {
    const child = spawn(command, args, {
      cwd,
      stdio: 'pipe',
      shell: isWindows
    });

    let stdout = '';
    let stderr = '';

    child.stdout.on('data', (data) => {
      const output = data.toString();
      stdout += output;
      process.stdout.write(output);
    });

    child.stderr.on('data', (data) => {
      const output = data.toString();
      stderr += output;
      process.stderr.write(output);
    });

    child.on('close', (code) => {
      if (code === 0) {
        resolve({ stdout, stderr });
      } else {
        reject(new Error(`Process exited with code ${code}\n${stderr}`));
      }
    });

    child.on('error', (error) => {
      reject(error);
    });
  });
}

async function buildProject(key, config) {
  const buildDir = path.join(projectRoot, config.dir);
  
  if (!fs.existsSync(buildDir)) {
    console.log(`⚠️  ${config.name} directory not found, skipping...`);
    return false;
  }

  console.log(`\n🔨 Building ${config.name}...`);
  console.log(`📁 Directory: ${config.dir}`);
  console.log(`⚡ Command: ${config.command} ${config.args.join(' ')}`);
  
  try {
    const startTime = Date.now();
    await runCommand(config.command, config.args, buildDir);
    const duration = ((Date.now() - startTime) / 1000).toFixed(2);
    
    console.log(`✅ ${config.name} built successfully in ${duration}s`);
    
    // Check if output exists
    const outputPath = path.join(buildDir, config.output);
    if (fs.existsSync(outputPath) || config.output.includes('*')) {
      console.log(`📦 Output: ${config.output}`);
    }
    
    return true;
  } catch (error) {
    console.error(`❌ Failed to build ${config.name}:`);
    console.error(error.message);
    return false;
  }
}

async function createDistributionPackage() {
  console.log('\n📦 Creating distribution package...');
  
  const distDir = path.join(projectRoot, 'dist');
  if (!fs.existsSync(distDir)) {
    fs.mkdirSync(distDir, { recursive: true });
  }

  // Copy build artifacts
  const artifacts = [
    {
      source: 'Gynassist-backend/target/*.jar',
      dest: 'dist/backend/',
      name: 'Backend JAR'
    },
    {
      source: 'gynassist-frontend/dist/',
      dest: 'dist/web/',
      name: 'Web Application'
    },
    {
      source: 'gynassist-desktop/dist/',
      dest: 'dist/desktop/',
      name: 'Desktop Applications'
    }
  ];

  for (const artifact of artifacts) {
    try {
      const sourcePath = path.join(projectRoot, artifact.source);
      const destPath = path.join(projectRoot, artifact.dest);
      
      if (fs.existsSync(sourcePath.replace('*', ''))) {
        if (!fs.existsSync(destPath)) {
          fs.mkdirSync(destPath, { recursive: true });
        }
        
        if (artifact.source.includes('*')) {
          // Handle glob patterns
          const { stdout } = await execAsync(`cp -r ${sourcePath} ${destPath}`, { shell: true });
        } else {
          // Handle directories
          await execAsync(`cp -r ${sourcePath} ${destPath}`, { shell: true });
        }
        
        console.log(`✅ Copied ${artifact.name} to ${artifact.dest}`);
      }
    } catch (error) {
      console.log(`⚠️  Could not copy ${artifact.name}: ${error.message}`);
    }
  }
}

async function generateBuildReport() {
  console.log('\n📊 Generating build report...');
  
  const report = {
    timestamp: new Date().toISOString(),
    platform: process.platform,
    node_version: process.version,
    builds: {},
    artifacts: []
  };

  // Check build artifacts
  const checkPaths = [
    { path: 'Gynassist-backend/target', type: 'backend' },
    { path: 'gynassist-frontend/dist', type: 'frontend' },
    { path: 'gynassist-desktop/dist', type: 'desktop' },
    { path: 'gynassist-mobile/builds', type: 'mobile' }
  ];

  for (const check of checkPaths) {
    const fullPath = path.join(projectRoot, check.path);
    const exists = fs.existsSync(fullPath);
    
    report.builds[check.type] = {
      success: exists,
      path: check.path,
      size: exists ? await getFolderSize(fullPath) : 0
    };

    if (exists) {
      report.artifacts.push({
        type: check.type,
        path: check.path,
        files: fs.readdirSync(fullPath)
      });
    }
  }

  // Write report
  const reportPath = path.join(projectRoot, 'build-report.json');
  fs.writeFileSync(reportPath, JSON.stringify(report, null, 2));
  
  console.log(`📋 Build report saved to: build-report.json`);
  return report;
}

async function getFolderSize(folderPath) {
  try {
    const { stdout } = await execAsync(`du -sb ${folderPath}`, { shell: true });
    return parseInt(stdout.split('\t')[0]);
  } catch {
    return 0;
  }
}

async function main() {
  const startTime = Date.now();
  const results = {};

  console.log('🚀 Starting cross-platform build process...\n');

  // Build each platform
  for (const [key, config] of Object.entries(builds)) {
    results[key] = await buildProject(key, config);
  }

  // Create distribution package
  await createDistributionPackage();

  // Generate build report
  const report = await generateBuildReport();

  // Summary
  const totalTime = ((Date.now() - startTime) / 1000).toFixed(2);
  const successful = Object.values(results).filter(Boolean).length;
  const total = Object.keys(results).length;

  console.log('\n' + '='.repeat(60));
  console.log('🎉 Build Process Complete!');
  console.log('='.repeat(60));
  console.log(`⏱️  Total time: ${totalTime}s`);
  console.log(`✅ Successful builds: ${successful}/${total}`);
  console.log('');

  // Build results
  for (const [key, success] of Object.entries(results)) {
    const status = success ? '✅' : '❌';
    const name = builds[key].name;
    console.log(`${status} ${name}`);
  }

  console.log('');
  console.log('📦 Distribution files:');
  console.log('   • Backend JAR:     dist/backend/');
  console.log('   • Web App:         dist/web/');
  console.log('   • Desktop Apps:    dist/desktop/');
  console.log('   • Mobile Apps:     Check EAS dashboard');
  console.log('');
  console.log('🚀 Deployment commands:');
  console.log('   • Web:     Deploy dist/web/ to hosting service');
  console.log('   • Backend: Deploy JAR to server');
  console.log('   • Desktop: Distribute from dist/desktop/');
  console.log('   • Mobile:  Submit to app stores via EAS');
  console.log('');

  if (successful < total) {
    console.log('⚠️  Some builds failed. Check the logs above for details.');
    process.exit(1);
  } else {
    console.log('🎊 All builds completed successfully!');
    process.exit(0);
  }
}

// Handle interruption
process.on('SIGINT', () => {
  console.log('\n\n🛑 Build process interrupted.');
  process.exit(1);
});

// Run the build process
main().catch(error => {
  console.error('\n❌ Build process failed:', error.message);
  process.exit(1);
});