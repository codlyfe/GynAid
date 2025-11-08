// Comprehensive Debug Script
const axios = require('axios');

const baseUrl = 'http://localhost:8080';
const frontendUrl = 'http://localhost:5173';

async function comprehensiveDebug() {
  console.log('🔍 COMPREHENSIVE ERROR DETECTION STARTED\n');
  
  const errors = [];
  
  try {
    // 1. Backend Health Check
    console.log('1️⃣ Backend Health Check...');
    try {
      const health = await axios.get(`${baseUrl}/actuator/health`, { timeout: 5000 });
      console.log('✅ Backend is running');
    } catch (e) {
      errors.push('❌ Backend not responding');
      console.log('❌ Backend not responding:', e.message);
    }
    
    // 2. CORS Preflight Test
    console.log('\n2️⃣ CORS Preflight Test...');
    try {
      const options = await axios.options(`${baseUrl}/api/auth/register`, {
        headers: {
          'Origin': frontendUrl,
          'Access-Control-Request-Method': 'POST',
          'Access-Control-Request-Headers': 'Content-Type'
        }
      });
      console.log('✅ CORS preflight passed');
    } catch (e) {
      errors.push('❌ CORS preflight failed');
      console.log('❌ CORS preflight failed:', e.response?.status, e.response?.statusText);
    }
    
    // 3. Registration Endpoint Test
    console.log('\n3️⃣ Registration Endpoint Test...');
    try {
      const testUser = {
        email: 'debug@test.com',
        password: 'password123',
        firstName: 'Debug',
        lastName: 'User',
        phoneNumber: '+256700000000',
        role: 'CLIENT'
      };
      
      const response = await axios.post(`${baseUrl}/api/auth/register`, testUser, {
        headers: {
          'Content-Type': 'application/json',
          'Origin': frontendUrl
        },
        timeout: 10000
      });
      
      if (response.status === 200) {
        console.log('✅ Registration endpoint working');
        console.log('📧 User created:', response.data.user?.email);
      }
    } catch (e) {
      errors.push(`❌ Registration failed: ${e.response?.status} ${e.response?.statusText}`);
      console.log('❌ Registration failed:', e.response?.status, e.response?.data || e.message);
    }
    
    // 4. Frontend Accessibility Test
    console.log('\n4️⃣ Frontend Accessibility Test...');
    try {
      const frontend = await axios.get(frontendUrl, { timeout: 5000 });
      console.log('✅ Frontend accessible');
    } catch (e) {
      errors.push('❌ Frontend not accessible');
      console.log('❌ Frontend not accessible:', e.message);
    }
    
    // 5. Database Connection Test
    console.log('\n5️⃣ Database Connection Test...');
    try {
      const h2Console = await axios.get(`${baseUrl}/h2-console`, { timeout: 5000 });
      console.log('✅ H2 Database accessible');
    } catch (e) {
      errors.push('❌ Database connection issue');
      console.log('❌ Database connection issue:', e.message);
    }
    
  } catch (globalError) {
    errors.push(`❌ Global error: ${globalError.message}`);
  }
  
  // Summary Report
  console.log('\n📋 ERROR SUMMARY:');
  if (errors.length === 0) {
    console.log('🎉 NO ERRORS FOUND - App should be working!');
  } else {
    console.log(`❌ Found ${errors.length} errors:`);
    errors.forEach(error => console.log(`   ${error}`));
  }
  
  console.log('\n🔧 RECOMMENDED ACTIONS:');
  if (errors.some(e => e.includes('Backend not responding'))) {
    console.log('   • Start backend: cd Gynassist-backend && ./mvnw spring-boot:run');
  }
  if (errors.some(e => e.includes('Frontend not accessible'))) {
    console.log('   • Start frontend: cd gynassist-frontend && npm run dev');
  }
  if (errors.some(e => e.includes('CORS'))) {
    console.log('   • Check SecurityConfig CORS settings');
  }
  if (errors.some(e => e.includes('Registration failed'))) {
    console.log('   • Check AuthController and validation');
  }
}

comprehensiveDebug();