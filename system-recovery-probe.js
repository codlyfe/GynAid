// System Recovery Probe - Gynassist Application
const axios = require('axios');
const { spawn, exec } = require('child_process');
const fs = require('fs');
const path = require('path');

const CONFIG = {
  backend: { port: 8080, url: 'http://localhost:8080' },
  frontend: { port: 5173, url: 'http://localhost:5173' },
  timeouts: { startup: 30000, request: 5000 },
  retries: 3
};

class SystemRecovery {
  constructor() {
    this.issues = [];
    this.recoveryLog = [];
  }

  log(message) {
    console.log(message);
    this.recoveryLog.push(`${new Date().toISOString()}: ${message}`);
  }

  async sleep(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
  }

  async testEndpoint(url, timeout = CONFIG.timeouts.request) {
    try {
      const response = await axios.get(url, { timeout });
      return { success: true, status: response.status, data: response.data };
    } catch (error) {
      return { success: false, error: error.code || error.message, status: error.response?.status };
    }
  }

  async isProcessRunning(port) {
    return new Promise((resolve) => {
      exec(`netstat -ano | findstr :${port}`, (error, stdout) => {
        resolve(stdout.includes(`:${port}`));
      });
    });
  }

  async startBackend() {
    return new Promise((resolve) => {
      this.log('🚀 Starting backend service...');
      const backend = spawn('cmd', ['/c', 'cd Gynassist-backend && mvnw.cmd spring-boot:run'], {
        detached: true,
        stdio: 'ignore'
      });
      
      backend.unref();
      
      // Wait for startup
      setTimeout(async () => {
        const isRunning = await this.isProcessRunning(CONFIG.backend.port);
        resolve(isRunning);
      }, CONFIG.timeouts.startup);
    });
  }

  async phase1_BackendRecovery() {
    this.log('\n🔧 PHASE 1: Backend Service Recovery');
    
    // Test if backend is responding
    const healthTest = await this.testEndpoint(`${CONFIG.backend.url}/actuator/health`);
    
    if (!healthTest.success) {
      this.log('❌ Backend not responding, attempting recovery...');
      
      // Check if process is running on port
      const isRunning = await this.isProcessRunning(CONFIG.backend.port);
      
      if (!isRunning) {
        this.log('🔄 Starting backend process...');
        const started = await this.startBackend();
        
        if (started) {
          this.log('✅ Backend startup initiated');
          // Wait and test again
          await this.sleep(10000);
          const retryTest = await this.testEndpoint(`${CONFIG.backend.url}/actuator/health`);
          if (retryTest.success) {
            this.log('✅ Backend recovery successful');
          } else {
            this.issues.push('Backend startup failed after recovery attempt');
          }
        } else {
          this.issues.push('Failed to start backend process');
        }
      } else {
        this.issues.push('Backend process running but not responding');
      }
    } else {
      this.log('✅ Backend service operational');
    }
  }

  async phase2_DatabaseRecovery() {
    this.log('\n🗄️ PHASE 2: Database Connection Recovery');
    
    // Test H2 console
    const h2Test = await this.testEndpoint(`${CONFIG.backend.url}/h2-console`);
    
    if (!h2Test.success) {
      this.log('❌ H2 console not accessible');
      this.issues.push('H2 console configuration issue');
    } else {
      this.log('✅ H2 console accessible');
    }
    
    // Test database through API
    const testUser = {
      email: 'recovery@test.com',
      password: 'test123',
      firstName: 'Recovery',
      lastName: 'Test',
      phoneNumber: '+256700000000',
      role: 'CLIENT'
    };
    
    const dbTest = await axios.post(`${CONFIG.backend.url}/api/auth/register`, testUser, {
      timeout: CONFIG.timeouts.request,
      validateStatus: () => true
    }).catch(e => ({ status: 500, data: { message: e.message } }));
    
    if (dbTest.status === 200) {
      this.log('✅ Database connectivity working');
    } else if (dbTest.status === 400 && dbTest.data.message?.includes('already exists')) {
      this.log('✅ Database working - user exists');
    } else {
      this.log(`❌ Database error: ${dbTest.data?.message}`);
      this.issues.push('Database schema or connectivity issue');
    }
  }

  async phase3_AuthenticationRecovery() {
    this.log('\n🔐 PHASE 3: Authentication Flow Recovery');
    
    // Test login
    const loginTest = await axios.post(`${CONFIG.backend.url}/api/auth/login`, {
      email: 'recovery@test.com',
      password: 'test123'
    }, {
      timeout: CONFIG.timeouts.request,
      validateStatus: () => true
    }).catch(e => ({ status: 500, data: { message: e.message } }));
    
    if (loginTest.status === 200 && loginTest.data.token) {
      this.log('✅ JWT authentication working');
      
      // Test protected endpoint
      const meTest = await axios.get(`${CONFIG.backend.url}/api/auth/me`, {
        headers: { Authorization: `Bearer ${loginTest.data.token}` },
        timeout: CONFIG.timeouts.request,
        validateStatus: () => true
      }).catch(e => ({ status: 500 }));
      
      if (meTest.status === 200) {
        this.log('✅ JWT token validation working');
      } else {
        this.issues.push('JWT token validation failed');
      }
    } else {
      this.log(`❌ Authentication failed: ${loginTest.data?.message}`);
      this.issues.push('Authentication system failure');
    }
  }

  async phase4_FinalValidation() {
    this.log('\n✅ PHASE 4: Final System Validation');
    
    // Test CORS
    const corsTest = await axios.options(`${CONFIG.backend.url}/api/auth/register`, {
      headers: {
        'Origin': CONFIG.frontend.url,
        'Access-Control-Request-Method': 'POST'
      },
      timeout: CONFIG.timeouts.request
    }).catch(() => ({ status: 500 }));
    
    if (corsTest.status === 200) {
      this.log('✅ CORS configuration working');
    } else {
      this.issues.push('CORS configuration issue');
    }
    
    // Test frontend
    const frontendTest = await this.testEndpoint(CONFIG.frontend.url);
    if (frontendTest.success) {
      this.log('✅ Frontend accessible');
    } else {
      this.issues.push('Frontend not accessible');
    }
  }

  generateRecoveryReport() {
    const report = {
      timestamp: new Date().toISOString(),
      status: this.issues.length === 0 ? 'RECOVERED' : 'ISSUES_FOUND',
      issues: this.issues,
      recoveryLog: this.recoveryLog,
      manualSteps: this.issues.length > 0 ? [
        'cd Gynassist-backend && mvnw clean spring-boot:run',
        'cd gynassist-frontend && npm run dev',
        'Check application.yaml configuration',
        'Verify H2 database settings'
      ] : []
    };
    
    fs.writeFileSync('recovery-report.json', JSON.stringify(report, null, 2));
    return report;
  }

  async run() {
    this.log('🔍 GYNASSIST SYSTEM RECOVERY PROBE STARTED\n');
    
    try {
      await this.phase1_BackendRecovery();
      await this.phase2_DatabaseRecovery();
      await this.phase3_AuthenticationRecovery();
      await this.phase4_FinalValidation();
      
      const report = this.generateRecoveryReport();
      
      this.log('\n📋 RECOVERY SUMMARY:');
      if (report.status === 'RECOVERED') {
        this.log('🎉 SYSTEM FULLY RECOVERED');
        this.log('✅ All services operational');
      } else {
        this.log(`❌ ${this.issues.length} issues require manual intervention:`);
        this.issues.forEach((issue, i) => this.log(`   ${i + 1}. ${issue}`));
        
        this.log('\n🔧 MANUAL RECOVERY STEPS:');
        report.manualSteps.forEach((step, i) => this.log(`   ${i + 1}. ${step}`));
      }
      
      this.log(`\n📄 Full report saved to: recovery-report.json`);
      
    } catch (error) {
      this.log(`❌ Recovery probe failed: ${error.message}`);
    }
  }
}

// Run recovery
const recovery = new SystemRecovery();
recovery.run();