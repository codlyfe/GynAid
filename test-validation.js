// Test the validation functions
const { validateInsertPayload } = require('./shared/utils/validateInsertPayload.ts');

// Test data
const user = {
  email: 'test@example.com',
  firstName: 'Test',
  lastName: 'User',
  phoneNumber: '+256700123456',
  role: 'CLIENT'
};

const hashedPassword = 'hashed_password_123';

const columns = [
  'created_at',
  'date_of_birth',
  'email',
  'first_name',
  'last_name',
  'password',
  'phone_number',
  'physical_address',
  'preferred_language',
  'profile_completion_status',
  'role',
  'status',
  'updated_at',
  'id'
];

const values = [
  new Date(),
  user.dateOfBirth,
  user.email,
  user.firstName,
  user.lastName,
  hashedPassword,
  user.phoneNumber,
  user.physicalAddress,
  user.preferredLanguage,
  'incomplete',
  user.role,
  'pending',
  new Date(),
  undefined
];

console.log('🧪 Testing Insert Payload Validation:');
console.log(validateInsertPayload({ table: 'users', columns, values }));

// Test mismatch
const shortValues = values.slice(0, 10);
console.log('\n🧪 Testing Column/Value Mismatch:');
console.log(validateInsertPayload({ table: 'users', columns, values: shortValues }));