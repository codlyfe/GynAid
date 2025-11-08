// Test script to verify backend authentication
const axios = require('axios');

const API_BASE = 'http://localhost:8080';

async function testAuth() {
  console.log('🔍 Testing Gynassist Authentication...\n');

  // Test 1: Register new user
  try {
    console.log('1️⃣ Testing Registration...');
    const registerData = {
      email: 'test@gynassist.com',
      password: '123456',
      firstName: 'Test',
      lastName: 'User',
      role: 'CLIENT'
    };

    const registerResponse = await axios.post(`${API_BASE}/api/auth/register`, registerData);
    console.log('✅ Registration Success:', registerResponse.data.message);
    console.log('🔑 Token received:', registerResponse.data.token ? 'Yes' : 'No');
    
    // Test 2: Login with same credentials
    console.log('\n2️⃣ Testing Login...');
    const loginData = {
      email: 'test@gynassist.com',
      password: '123456'
    };

    const loginResponse = await axios.post(`${API_BASE}/api/auth/login`, loginData);
    console.log('✅ Login Success:', loginResponse.data.message);
    console.log('🔑 Token received:', loginResponse.data.token ? 'Yes' : 'No');

    console.log('\n🎉 Authentication is working correctly!');
    
  } catch (error) {
    console.error('❌ Error:', error.response?.data || error.message);
    console.log('\n🔧 Issues found - see roadmap below');
  }
}

testAuth();