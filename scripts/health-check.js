#!/usr/bin/env node

const http = require('http');
const https = require('https');
const { exec } = require('child_process');
const util = require('util');

const execAsync = util.promisify(exec);

console.log('🏥 Gynassist Health Check');
console.log('=========================');

const services = [
  {
    name: 'Backend API',
    url: 'http://localhost:8080/health',
    type: 'http',
    critical: true
  },
  {
    name: 'Frontend Web',
    url: 'http://localhost:5173',
    type: 'http',
    critical: true
  },
  {
    name: 'Database Connection',
    url: 'http://localhost:8080/actuator/health/db',
    type: 'http',
    critical: true
  },
  {
    name: 'Mobile Expo Server',
    url: 'http://localhost:19000',
    type: 'http',
    critical: false
  },
  {
    name: 'Desktop App',
    process: 'electron',
    type: 'process',
    critical: false
  }
];

const externalServices = [
  {
    name: 'Stripe API',
    url: 'https://api.stripe.com/v1',
    type: 'http',
    critical: false
  },
  {
    name: 'MTN MoMo API',
    url: 'https://sandbox.momodeveloper.mtn.com',
    type: 'http',
    critical: false
  },
  {
    name: 'MOH DHIS2',
    url: 'https://hmis.health.go.ug/api/system/info',
    type: 'http',
    critical: false
  }
];

async function checkHttpService(service) {
  return new Promise((resolve) => {
    const protocol = service.url.startsWith('https') ? https : http;
    const timeout = 5000;

    const req = protocol.get(service.url, { timeout }, (res) => {
      const isHealthy = res.statusCode >= 200 && res.statusCode < 400;
      resolve({
        name: service.name,
        status: isHealthy ? 'healthy' : 'unhealthy',
        statusCode: res.statusCode,
        responseTime: Date.now() - startTime,
        critical: service.critical
      });
    });

    const startTime = Date.now();

    req.on('timeout', () => {
      req.destroy();
      resolve({
        name: service.name,
        status: 'timeout',
        error: 'Request timeout',
        critical: service.critical
      });
    });

    req.on('error', (error) => {
      resolve({
        name: service.name,
        status: 'error',
        error: error.message,
        critical: service.critical
      });
    });
  });
}

async function checkProcess(service) {
  try {
    const { stdout } = await execAsync(`tasklist /FI "IMAGENAME eq ${service.process}.exe"`, { shell: true });
    const isRunning = stdout.includes(service.process);
    
    return {
      name: service.name,
      status: isRunning ? 'running' : 'stopped',
      critical: service.critical
    };
  } catch (error) {
    return {
      name: service.name,
      status: 'error',
      error: error.message,
      critical: service.critical
    };
  }
}

async function checkSystemResources() {
  console.log('\n💻 System Resources:');
  
  try {
    // Memory usage
    const { stdout: memInfo } = await execAsync('wmic OS get TotalVisibleMemorySize,FreePhysicalMemory /value', { shell: true });
    const memLines = memInfo.split('\n').filter(line => line.includes('='));
    const memData = {};
    memLines.forEach(line => {
      const [key, value] = line.split('=');
      if (key && value) {
        memData[key.trim()] = parseInt(value.trim());
      }
    });
    
    if (memData.TotalVisibleMemorySize && memData.FreePhysicalMemory) {
      const totalMB = Math.round(memData.TotalVisibleMemorySize / 1024);
      const freeMB = Math.round(memData.FreePhysicalMemory / 1024);
      const usedMB = totalMB - freeMB;
      const usagePercent = Math.round((usedMB / totalMB) * 100);
      
      console.log(`   Memory: ${usedMB}MB / ${totalMB}MB (${usagePercent}% used)`);
      
      if (usagePercent > 90) {
        console.log('   ⚠️  High memory usage detected');
      }
    }

    // CPU usage (simplified)
    const { stdout: cpuInfo } = await execAsync('wmic cpu get loadpercentage /value', { shell: true });
    const cpuMatch = cpuInfo.match(/LoadPercentage=(\d+)/);
    if (cpuMatch) {
      const cpuUsage = parseInt(cpuMatch[1]);
      console.log(`   CPU: ${cpuUsage}% usage`);
      
      if (cpuUsage > 80) {
        console.log('   ⚠️  High CPU usage detected');
      }
    }

    // Disk space
    const { stdout: diskInfo } = await execAsync('wmic logicaldisk get size,freespace,caption /value', { shell: true });
    const diskLines = diskInfo.split('\n').filter(line => line.includes('='));
    let currentDisk = {};
    
    diskLines.forEach(line => {
      const [key, value] = line.split('=');
      if (key && value) {
        currentDisk[key.trim()] = value.trim();
        
        if (key.trim() === 'Size' && currentDisk.Caption && currentDisk.FreeSpace) {
          const totalGB = Math.round(parseInt(currentDisk.Size) / (1024 ** 3));
          const freeGB = Math.round(parseInt(currentDisk.FreeSpace) / (1024 ** 3));
          const usedGB = totalGB - freeGB;
          const usagePercent = Math.round((usedGB / totalGB) * 100);
          
          console.log(`   Disk ${currentDisk.Caption}: ${usedGB}GB / ${totalGB}GB (${usagePercent}% used)`);
          
          if (usagePercent > 90) {
            console.log(`   ⚠️  Low disk space on ${currentDisk.Caption}`);
          }
          
          currentDisk = {};
        }
      }
    });

  } catch (error) {
    console.log('   ❌ Could not retrieve system information');
  }
}

async function checkNetworkConnectivity() {
  console.log('\n🌐 Network Connectivity:');
  
  const testUrls = [
    'google.com',
    'github.com',
    'npmjs.com'
  ];

  for (const url of testUrls) {
    try {
      await execAsync(`ping -n 1 ${url}`, { shell: true });
      console.log(`   ✅ ${url} - reachable`);
    } catch (error) {
      console.log(`   ❌ ${url} - unreachable`);
    }
  }
}

async function checkDependencies() {
  console.log('\n📦 Dependencies:');
  
  const deps = [
    { name: 'Node.js', command: 'node --version', required: true },
    { name: 'npm', command: 'npm --version', required: true },
    { name: 'Java', command: 'java -version', required: true },
    { name: 'Git', command: 'git --version', required: true },
    { name: 'Maven', command: 'mvn --version', required: false },
    { name: 'Expo CLI', command: 'npx expo --version', required: false }
  ];

  for (const dep of deps) {
    try {
      const { stdout, stderr } = await execAsync(dep.command, { shell: true });
      const version = (stdout || stderr).split('\n')[0];
      const status = dep.required ? '✅' : '📦';
      console.log(`   ${status} ${dep.name}: ${version}`);
    } catch (error) {
      const status = dep.required ? '❌' : '⚠️ ';
      console.log(`   ${status} ${dep.name}: Not installed`);
    }
  }
}

async function generateHealthReport(results) {
  const report = {
    timestamp: new Date().toISOString(),
    overall: 'healthy',
    services: results,
    summary: {
      total: results.length,
      healthy: 0,
      unhealthy: 0,
      critical_issues: 0
    }
  };

  results.forEach(result => {
    if (result.status === 'healthy' || result.status === 'running') {
      report.summary.healthy++;
    } else {
      report.summary.unhealthy++;
      if (result.critical) {
        report.summary.critical_issues++;
      }
    }
  });

  if (report.summary.critical_issues > 0) {
    report.overall = 'critical';
  } else if (report.summary.unhealthy > 0) {
    report.overall = 'degraded';
  }

  // Save report
  const fs = require('fs');
  const path = require('path');
  const reportPath = path.join(__dirname, '..', 'health-report.json');
  fs.writeFileSync(reportPath, JSON.stringify(report, null, 2));

  return report;
}

async function main() {
  console.log('🔍 Checking Gynassist application health...\n');

  // Check system resources
  await checkSystemResources();

  // Check network connectivity
  await checkNetworkConnectivity();

  // Check dependencies
  await checkDependencies();

  console.log('\n🏥 Service Health Checks:');

  const results = [];

  // Check internal services
  for (const service of services) {
    let result;
    
    if (service.type === 'http') {
      result = await checkHttpService(service);
    } else if (service.type === 'process') {
      result = await checkProcess(service);
    }
    
    if (result) {
      results.push(result);
      
      const statusIcon = getStatusIcon(result.status);
      const criticalFlag = result.critical ? ' [CRITICAL]' : '';
      
      console.log(`   ${statusIcon} ${result.name}${criticalFlag}`);
      
      if (result.responseTime) {
        console.log(`      Response time: ${result.responseTime}ms`);
      }
      
      if (result.error) {
        console.log(`      Error: ${result.error}`);
      }
    }
  }

  console.log('\n🌍 External Service Checks:');

  // Check external services
  for (const service of externalServices) {
    const result = await checkHttpService(service);
    results.push(result);
    
    const statusIcon = getStatusIcon(result.status);
    console.log(`   ${statusIcon} ${result.name}`);
    
    if (result.error) {
      console.log(`      Error: ${result.error}`);
    }
  }

  // Generate report
  const report = await generateHealthReport(results);

  // Summary
  console.log('\n' + '='.repeat(50));
  console.log('📊 Health Check Summary');
  console.log('='.repeat(50));
  console.log(`Overall Status: ${getOverallStatusIcon(report.overall)} ${report.overall.toUpperCase()}`);
  console.log(`Services: ${report.summary.healthy}/${report.summary.total} healthy`);
  
  if (report.summary.critical_issues > 0) {
    console.log(`❌ Critical Issues: ${report.summary.critical_issues}`);
  }
  
  console.log(`📋 Report saved to: health-report.json`);

  // Recommendations
  if (report.overall !== 'healthy') {
    console.log('\n💡 Recommendations:');
    
    results.forEach(result => {
      if (result.status !== 'healthy' && result.status !== 'running') {
        if (result.name === 'Backend API') {
          console.log('   • Start the backend: npm run start:backend');
        } else if (result.name === 'Frontend Web') {
          console.log('   • Start the frontend: npm run start:frontend');
        } else if (result.name === 'Mobile Expo Server') {
          console.log('   • Start mobile dev server: npm run start:mobile');
        }
      }
    });
  }

  // Exit with appropriate code
  process.exit(report.summary.critical_issues > 0 ? 1 : 0);
}

function getStatusIcon(status) {
  switch (status) {
    case 'healthy':
    case 'running':
      return '✅';
    case 'unhealthy':
    case 'stopped':
      return '❌';
    case 'timeout':
      return '⏱️ ';
    case 'error':
      return '🔥';
    default:
      return '❓';
  }
}

function getOverallStatusIcon(status) {
  switch (status) {
    case 'healthy':
      return '✅';
    case 'degraded':
      return '⚠️ ';
    case 'critical':
      return '❌';
    default:
      return '❓';
  }
}

// Run health check
main().catch(error => {
  console.error('❌ Health check failed:', error.message);
  process.exit(1);
});