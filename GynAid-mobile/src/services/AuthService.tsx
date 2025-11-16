import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { ApiService } from './ApiService';

interface User {
  id: number;
  email: string;
  firstName?: string;
  lastName?: string;
  role: 'ADMIN' | 'CLIENT' | 'PROVIDER_INDIVIDUAL' | 'PROVIDER_INSTITUTION';
}

interface AuthContextType {
  user: User | null;
  token: string | null;
  isLoading: boolean;
login: (email: string, password: string) => Promise<void>;
  register: (email: string, password: string, firstName: string, lastName: string, role: string) => Promise<void>;
  logout: () => Promise<void>;
  enableBiometric: () => Promise<boolean>;
  loginWithBiometric: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

interface AuthProviderProps {
  children: ReactNode;
}

export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [token, setToken] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    loadStoredAuth();
  }, []);

const loadStoredAuth = async () => {
    try {
      // Using AsyncStorage as fallback for now
      const storedToken = await AsyncStorage.getItem('jwt_token');
      const storedUser = await AsyncStorage.getItem('user');
      
      if (storedToken && storedUser) {
        setToken(storedToken);
        setUser(JSON.parse(storedUser));
        ApiService.setAuthToken(storedToken);
      }
    } catch (error) {
      console.error('Error loading stored auth:', error);
    } finally {
      setIsLoading(false);
    }
  };

const login = async (email: string, password: string) => {
    try {
      const response = await ApiService.post('/api/auth/login', { email, password });
      const { token: jwtToken, user: userData } = response.data;
      
      await AsyncStorage.setItem('jwt_token', jwtToken);
      await AsyncStorage.setItem('user', JSON.stringify(userData));
      
      setToken(jwtToken);
      setUser(userData);
      ApiService.setAuthToken(jwtToken);
    } catch (error: any) {
      throw new Error(error.response?.data?.message || 'Login failed');
    }
  };

const register = async (email: string, password: string, firstName: string, lastName: string, role: string) => {
    try {
      const response = await ApiService.post('/api/auth/register', {
        email,
        password,
        firstName,
        lastName,
        role,
      });
      
      const { token: jwtToken, user: userData } = response.data;
      
      await AsyncStorage.setItem('jwt_token', jwtToken);
      await AsyncStorage.setItem('user', JSON.stringify(userData));
      
      setToken(jwtToken);
      setUser(userData);
      ApiService.setAuthToken(jwtToken);
    } catch (error: any) {
      throw new Error(error.response?.data?.message || 'Registration failed');
    }
  };

const logout = async () => {
    try {
      await AsyncStorage.removeItem('jwt_token');
      await AsyncStorage.removeItem('user');
      await AsyncStorage.removeItem('biometric_enabled');
      
      setToken(null);
      setUser(null);
      ApiService.setAuthToken(null);
    } catch (error) {
      console.error('Error during logout:', error);
    }
  };

const enableBiometric = async (): Promise<boolean> => {
    // Placeholder for biometric authentication - not implemented yet
    console.log('Biometric authentication not available in current version');
    return false;
  };

  const loginWithBiometric = async () => {
    // Placeholder for biometric login - not implemented yet
    throw new Error('Biometric authentication not available in current version');
  };

  return (
    <AuthContext.Provider 
      value={{ 
        user, 
        token, 
        isLoading, 
        login, 
        register, 
        logout, 
        enableBiometric, 
        loginWithBiometric 
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};
