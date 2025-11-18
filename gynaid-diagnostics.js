#!/usr/bin/env node

/**
 * GynAid COMPREHENSIVE DIAGNOSTICS TOOL
 * Consolidates all testing, debugging, and validation functionality
 */

const axios = require('axios');
const fs = require('fs');
const path = require('path');

class GynAidDiagnostics {
    constructor() {
        this.baseURL = 'http://localhost:8080';
        this.frontendURL = 'http://localhost:5173';
        this.apiURL = `${this.baseURL}/api`;
        this.results = {
            timestamp: new Date().toISOString(),
            issues: [],
            fixes: [],
            status: 'UNKNOWN'
        };
    }

    async runDiagnostics(mode = 'full') {
        console.log('🔍 GynAid COMPREHENSIVE DIAGNOSTICS\n');
        
        switch (mode) {
            case 'connectivity':
                await this.testConnectivity();
                break;
            case 'auth':
                await this.testAuthentication();
                break;
            case 'schema':
                await this.validateSchema();
                break;
            case 'recovery':
                await this.systemRecovery();
                break;
            default:
                await this.fullDiagnostics();
        }
        
        this.generateReport();
    }

    async fullDiagnostics() {
        await this.checkBackendHealth();
        await this.testConnectivity();
        await this.validateSchema();
        await this.testAuthentication();
        await this.checkSecurityConfig();
    }

    async checkBackendHealth() {
        console.log('1️⃣ Backend Health Check...');
        try {
            const health = await axios.get(`${this.baseURL}/actuator/health`, { timeout: 5000 });
            console.log('✅ Backend responding:', health.data.status);
        } catch (e) {
            this.results.issues.push({
                type: 'BACKEND_DOWN',
                message: 'Backend server not responding',
                severity: 'CRITICAL'
            });
            console.log('❌ Backend unreachable:', e.code);
        }
    }

    async testConnectivity() {
        console.log('\n2️⃣ Connectivity Tests...');
        
        // CORS Test
        try {
            await axios.options(`${this.apiURL}/auth/register`, {
                headers: {
                    'Origin': this.frontendURL,
                    'Access-Control-Request-Method': 'POST'
                }
            });
            console.log('✅ CORS configured properly');
        } catch (e) {
            this.results.issues.push({
                type: 'CORS_FAILED',
                message: 'CORS configuration issue',
                severity: 'HIGH'
            });
            console.log('❌ CORS issue');
        }

        // Frontend Test
        try {
            await axios.get(this.frontendURL, { timeout: 5000 });
            console.log('✅ Frontend accessible');
        } catch (e) {
            this.results.issues.push({
                type: 'FRONTEND_DOWN',
                message: 'Frontend not accessible',
                severity: 'MEDIUM'
            });
            console.log('❌ Frontend unreachable');
        }

        // H2 Console Test
        try {
            await axios.get(`${this.baseURL}/h2-console`, { timeout: 3000 });
            console.log('✅ H2 Console accessible');
        } catch (e) {
            console.log('⚠️ H2 Console not accessible');
        }
    }

    async validateSchema() {
        console.log('\n3️⃣ Database Schema Validation...');
        
        const testUsers = [
            {
                email: `schema${Date.now()}@test.com`,
                password: 'test123',
                firstName: 'Schema',
                lastName: 'Test',
                phoneNumber: '+256700000010',
                role: 'CLIENT'
            }
        ];

        for (const user of testUsers) {
            try {
                const response = await axios.post(`${this.apiURL}/auth/register`, user, {
                    validateStatus: () => true
                });

                if (response.status === 200) {
                    console.log('✅ User schema working');
                    
                    // Validate response structure
                    const userData = response.data.user;
                    const requiredFields = ['id', 'email', 'firstName', 'lastName', 'role'];
                    const missingFields = requiredFields.filter(field => !userData[field]);
                    
                    if (missingFields.length > 0) {
                        this.results.issues.push({
                            type: 'SCHEMA_INCOMPLETE',
                            message: `Missing fields: ${missingFields.join(', ')}`,
                            severity: 'MEDIUM'
                        });
                    }
                } else {
                    this.results.issues.push({
                        type: 'SCHEMA_ERROR',
                        message: `User creation failed: ${response.status}`,
                        severity: 'HIGH'
                    });
                }
            } catch (e) {
                this.results.issues.push({
                    type: 'SCHEMA_ERROR',
                    message: `Schema validation error: ${e.message}`,
                    severity: 'HIGH'
                });
            }
        }
    }

    async testAuthentication() {
        console.log('\n4️⃣ Authentication Flow Tests...');
        
        const testEmail = `auth${Date.now()}@test.com`;
        const testPassword = 'password123';

        try {
            // Test Registration
            const regResponse = await axios.post(`${this.apiURL}/auth/register`, {
                email: testEmail,
                password: testPassword,
                firstName: 'Auth',
                lastName: 'Test',
                phoneNumber: '+256700000000',
                role: 'CLIENT'
            }, { validateStatus: () => true });

            if (regResponse.status === 200) {
                console.log('✅ Registration working');

                // Test Login
                const loginResponse = await axios.post(`${this.apiURL}/auth/login`, {
                    email: testEmail,
                    password: testPassword
                }, { validateStatus: () => true });

                if (loginResponse.status === 200 && loginResponse.data.token) {
                    console.log('✅ Login working');

                    // Test Protected Endpoint
                    const meResponse = await axios.get(`${this.apiURL}/auth/me`, {
                        headers: { Authorization: `Bearer ${loginResponse.data.token}` },
                        validateStatus: () => true
                    });

                    if (meResponse.status === 200) {
                        console.log('✅ JWT authentication working');
                    } else {
                        this.results.issues.push({
                            type: 'JWT_VALIDATION_FAILED',
                            message: 'JWT token validation failed',
                            severity: 'HIGH'
                        });
                    }
                } else if (loginResponse.status === 403) {
                    this.results.issues.push({
                        type: 'AUTH_ENDPOINT_BLOCKED',
                        message: 'Login endpoint returning 403',
                        severity: 'CRITICAL'
                    });
                    console.log('❌ 403 error on login');
                } else {
                    this.results.issues.push({
                        type: 'LOGIN_FAILED',
                        message: `Login failed with status: ${loginResponse.status}`,
                        severity: 'HIGH'
                    });
                }
            } else {
                this.results.issues.push({
                    type: 'REGISTRATION_FAILED',
                    message: `Registration failed: ${regResponse.status}`,
                    severity: 'HIGH'
                });
            }
        } catch (e) {
            this.results.issues.push({
                type: 'AUTH_ERROR',
                message: `Authentication error: ${e.message}`,
                severity: 'HIGH'
            });
        }
    }

    async checkSecurityConfig() {
        console.log('\n5️⃣ Security Configuration Check...');
        
        const securityConfigPath = path.join(__dirname, 'Gynassist-backend/src/main/java/com/gynassist/backend/config/SecurityConfig.java');
        
        if (fs.existsSync(securityConfigPath)) {
            const content = fs.readFileSync(securityConfigPath, 'utf8');
            
            if (!content.includes('.anyRequest().permitAll()') && !content.includes('.requestMatchers("/api/auth/**").permitAll()')) {
                this.results.issues.push({
                    type: 'SECURITY_CONFIG_RESTRICTIVE',
                    message: 'Security config may be blocking auth endpoints',
                    severity: 'HIGH'
                });
            }
            console.log('✅ Security configuration analyzed');
        }
    }

    async systemRecovery() {
        console.log('\n🚨 SYSTEM RECOVERY MODE...');
        
        // Kill existing processes
        console.log('Terminating existing processes...');
        require('child_process').exec('taskkill /f /im java.exe 2>nul');
        require('child_process').exec('taskkill /f /im node.exe 2>nul');
        
        await new Promise(resolve => setTimeout(resolve, 3000));
        
        // Start backend
        console.log('Starting backend...');
        const { spawn } = require('child_process');
        const backend = spawn('cmd', ['/c', 'cd Gynassist-backend && mvnw spring-boot:run'], {
            detached: true,
            stdio: 'ignore'
        });
        backend.unref();
        
        // Wait and test
        await new Promise(resolve => setTimeout(resolve, 20000));
        await this.checkBackendHealth();
        
        console.log('✅ Recovery attempt completed');
    }

    generateFixes() {
        for (const issue of this.results.issues) {
            switch (issue.type) {
                case 'AUTH_ENDPOINT_BLOCKED':
                    this.results.fixes.push({
                        issue: issue.type,
                        fix: 'Update SecurityConfig to allow auth endpoints',
                        steps: [
                            'Open SecurityConfig.java',
                            'Add: .requestMatchers("/api/auth/**").permitAll()',
                            'Restart backend server'
                        ]
                    });
                    break;
                case 'CORS_FAILED':
                    this.results.fixes.push({
                        issue: issue.type,
                        fix: 'Fix CORS configuration',
                        steps: [
                            'Check CorsConfig.java',
                            'Ensure frontend URL is allowed',
                            'Restart backend'
                        ]
                    });
                    break;
                case 'BACKEND_DOWN':
                    this.results.fixes.push({
                        issue: issue.type,
                        fix: 'Start backend server',
                        steps: [
                            'cd Gynassist-backend',
                            'mvnw spring-boot:run'
                        ]
                    });
                    break;
            }
        }
    }

    generateReport() {
        this.generateFixes();
        
        console.log('\n📊 DIAGNOSTIC REPORT');
        console.log('====================');
        
        if (this.results.issues.length === 0) {
            console.log('✅ No issues found - System healthy!');
            this.results.status = 'HEALTHY';
        } else {
            console.log(`❌ Found ${this.results.issues.length} issues:`);
            this.results.status = 'ISSUES_FOUND';
            
            this.results.issues.forEach((issue, index) => {
                console.log(`\n${index + 1}. ${issue.type} (${issue.severity})`);
                console.log(`   ${issue.message}`);
            });
            
            if (this.results.fixes.length > 0) {
                console.log('\n🔧 RECOMMENDED FIXES:');
                this.results.fixes.forEach((fix, index) => {
                    console.log(`\n${index + 1}. ${fix.issue}:`);
                    console.log(`   ${fix.fix}`);
                    if (fix.steps) {
                        fix.steps.forEach(step => console.log(`   - ${step}`));
                    }
                });
            }
        }
        
        // Save report
        fs.writeFileSync('Gynassist-diagnostic-report.json', JSON.stringify(this.results, null, 2));
        console.log('\n📄 Report saved to: Gynassist-diagnostic-report.json');
        
        console.log('\n🔗 Quick Links:');
        console.log('   - Frontend: http://localhost:5173');
        console.log('   - Backend: http://localhost:8080');
        console.log('   - H2 Console: http://localhost:8080/h2-console');
    }
}

// CLI Interface
async function main() {
    const args = process.argv.slice(2);
    const mode = args[0] || 'full';
    
    const diagnostics = new GynAidDiagnostics();
    
    console.log('Available modes: full, connectivity, auth, schema, recovery\n');
    
    try {
        await diagnostics.runDiagnostics(mode);
    } catch (error) {
        console.error('❌ Diagnostics failed:', error.message);
    }
}

if (require.main === module) {
    main();
}

module.exports = GynAidDiagnostics;
