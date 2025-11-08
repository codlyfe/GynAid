// Complete Authentication Test Script
const axios = require('axios');

const baseUrl = 'http://localhost:8080';
const testUser = {
  email: 'test@example.com',
  password: 'password123',
  firstName: 'Test',
  lastName: 'User',
  phoneNumber: '+256700123456',
  role: 'CLIENT'
};

async function testCompleteAuthFlow() {
  console.log('🧪 Testing Complete Authentication Flow\n');
  
  try {
    // 1. Test CSRF Token Endpoint
    console.log('1️⃣ Testing CSRF Token...');
    const csrfResponse = await axios.get(`${baseUrl}/csrf-token`, {
      withCredentials: true
    });
    console.log('✅ CSRF Token received:', csrfResponse.data.token.substring(0, 20) + '...');
    
    // 2. Test User Registration
    console.log('\n2️⃣ Testing User Registration...');
    const registerResponse = await axios.post(`${baseUrl}/api/auth/register`, testUser, {
      headers: {
        'Content-Type': 'application/json',
        [csrfResponse.data.headerName]: csrfResponse.data.token
      },
      withCredentials: true
    });
    
    if (registerResponse.status === 200) {
      console.log('✅ Registration successful');
      console.log('📧 User email:', registerResponse.data.user.email);
      console.log('🔑 JWT Token received:', registerResponse.data.token.substring(0, 30) + '...');
    }
    
    // 3. Test User Login
    console.log('\n3️⃣ Testing User Login...');
    const loginResponse = await axios.post(`${baseUrl}/api/auth/login`, {
      email: testUser.email,
      password: testUser.password
    }, {
      headers: { 'Content-Type': 'application/json' },
      withCredentials: true
    });
    
    if (loginResponse.status === 200) {
      console.log('✅ Login successful');
      console.log('👤 User ID:', loginResponse.data.user.id);
      console.log('🔑 New JWT Token:', loginResponse.data.token.substring(0, 30) + '...');
    }
    
    // 4. Test Protected Endpoint
    console.log('\n4️⃣ Testing Protected Endpoint...');
    const meResponse = await axios.get(`${baseUrl}/api/auth/me`, {
      headers: {
        'Authorization': `Bearer ${loginResponse.data.token}`
      }
    });
    
    if (meResponse.status === 200) {
      console.log('✅ Protected endpoint accessible');
      console.log('👤 Current user:', meResponse.data.firstName, meResponse.data.lastName);
    }
    
    console.log('\n🎉 All authentication tests passed!');
    console.log('\n📋 Test Summary:');
    console.log('   ✅ CSRF Token Generation');
    console.log('   ✅ User Registration');
    console.log('   ✅ User Login');
    console.log('   ✅ Protected Route Access');
    
  } catch (error) {
    console.error('\n❌ Test failed:', error.response?.data || error.message);
    
    if (error.response?.status === 400 && error.response?.data?.message?.includes('already exists')) {
      console.log('\n💡 User already exists. Testing login only...');
      await testLoginOnly();
    }
  }
}

async function testLoginOnly() {
  try {
    const loginResponse = await axios.post(`${baseUrl}/api/auth/login`, {
      email: testUser.email,
      password: testUser.password
    }, {
      headers: { 'Content-Type': 'application/json' }
    });
    
    console.log('✅ Login test passed for existing user');
    return loginResponse.data.token;
  } catch (error) {
    console.error('❌ Login test failed:', error.response?.data || error.message);
  }
}

// Run tests
if (require.main === module) {
  testCompleteAuthFlow();
}

module.exports = { testCompleteAuthFlow, testLoginOnly };