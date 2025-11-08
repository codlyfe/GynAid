// CSRF Integration Test Script
const testCsrfIntegration = async () => {
  const baseUrl = 'http://localhost:8080';
  
  try {
    // 1. Get CSRF token
    console.log('Fetching CSRF token...');
    const csrfResponse = await fetch(`${baseUrl}/api/csrf-token`, {
      credentials: 'include'
    });
    const csrfData = await csrfResponse.json();
    console.log('CSRF Token:', csrfData.token);
    console.log('CSRF Header:', csrfData.headerName);
    
    // 2. Test registration with CSRF token
    console.log('\nTesting registration with CSRF...');
    const registerResponse = await fetch(`${baseUrl}/api/auth/register`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        [csrfData.headerName]: csrfData.token
      },
      credentials: 'include',
      body: JSON.stringify({
        email: 'test@example.com',
        password: 'password123',
        firstName: 'Test',
        lastName: 'User',
        role: 'CLIENT'
      })
    });
    
    if (registerResponse.ok) {
      console.log('✅ Registration successful with CSRF');
    } else {
      console.log('❌ Registration failed:', await registerResponse.text());
    }
    
  } catch (error) {
    console.error('Test failed:', error);
  }
};

// Run test if in Node.js environment
if (typeof module !== 'undefined') {
  testCsrfIntegration();
}