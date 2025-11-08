import { prepareSafeInsert } from './validateInsertPayload';

interface User {
  dateOfBirth?: Date;
  email: string;
  firstName: string;
  lastName: string;
  phoneNumber: string;
  physicalAddress?: string;
  preferredLanguage?: string;
  role: string;
}

export function prepareUserInsert(user: User, hashedPassword: string) {
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
    'updated_at'
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
    user.preferredLanguage
  ];

  const defaults = {
    'profile_completion_status': 'incomplete',
    'role': 'CLIENT',
    'status': 'pending',
    'updated_at': new Date(),
    'physical_address': null,
    'preferred_language': 'en'
  };

  return prepareSafeInsert({ table: 'users', columns, values, defaults });
}