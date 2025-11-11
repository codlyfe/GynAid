import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import AsyncStorage from '@react-native-async-storage/async-storage';
import * as SecureStore from 'expo-secure-store';
import * as LocalAuthentication from 'expo-local-authentication';
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
      const storedToken = await SecureStore.getItemAsync('jwt_token');
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
      
      await SecureStore.setItemAsync('jwt_token', jwtToken);
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
      
      await SecureStore.setItemAsync('jwt_token', jwtToken);
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
      await SecureStore.deleteItemAsync('jwt_token');
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
    try {
      const hasHardware = await LocalAuthentication.hasHardwareAsync();
      const isEnrolled = await LocalAuthentication.isEnrolledAsync();
      
      if (!hasHardware || !isEnrolled) {
        return false;
      }

      const result = await LocalAuthentication.authenticateAsync({
        promptMessage: 'Enable biometric authentication for GynaId',
        cancelLabel: 'Cancel',
        fallbackLabel: 'Use password',
      });

      if (result.success) {
        await AsyncStorage.setItem('biometric_enabled', 'true');
        return true;
      }
      
      return false;
    } catch (error) {
      console.error('Error enabling biometric:', error);
      return false;
    }
  };

  const loginWithBiometric = async () => {
    try {
      const biometricEnabled = await AsyncStorage.getItem('biometric_enabled');
      
      if (biometricEnabled !== 'true') {
        throw new Error('Biometric authentication not enabled');
      }

      const result = await LocalAuthentication.authenticateAsync({
        promptMessage: 'Sign in to GynaId',
        cancelLabel: 'Cancel',
        fallbackLabel: 'Use password',
      });

      if (result.success) {
        await loadStoredAuth();
      } else {
        throw new Error('Biometric authentication failed');
      }
    } catch (error) {
      console.error('Error with biometric login:', error);
      throw error;
    }
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
