import axios, { AxiosInstance, AxiosResponse } from 'axios';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { SecureStore } from 'expo-secure-store';

declare const __DEV__: boolean;

class ApiServiceClass {
  private api: AxiosInstance;
  private baseURL: string;
  private isOnline: boolean = true;
  private requestQueue: Array<() => Promise<any>> = [];

  constructor() {
    this.baseURL = __DEV__ ? 'http://10.0.2.2:8080' : 'https://api.GynAid.ug';
    
    this.api = axios.create({
      baseURL: this.baseURL,
      timeout: 30000,
      headers: {
        'Content-Type': 'application/json',
      },
    });

    this.setupInterceptors();
    this.setupNetworkListener();
  }

  private setupInterceptors() {
    // Request interceptor
    this.api.interceptors.request.use(
      async (config) => {
        // Add auth token if available
        const token = await this.getStoredToken();
        if (token) {
          config.headers.Authorization = `Bearer ${token}`;
        }
        
        // Add device info
        config.headers['X-Device-Platform'] = 'mobile';
        
        return config;
      },
      (error) => {
        return Promise.reject(error);
      }
    );

    // Response interceptor
    this.api.interceptors.response.use(
      (response) => response,
      async (error) => {
        if (error.response?.status === 401) {
          // Token expired, logout user
          await this.handleTokenExpiry();
        }
        
        // Handle network errors
        if (!error.response && !this.isOnline) {
          // Queue request for retry when online
          this.queueRequest(() => this.api.request(error.config));
          throw new Error('No internet connection. Request queued for retry.');
        }
        
        return Promise.reject(error);
      }
    );
  }

  private setupNetworkListener() {
    // Mock network listener - in real app this would use expo-network
    // For now, just set online to true and skip network monitoring
    this.isOnline = true;
    
    // In a real implementation, you would use:
    // import { Network } from 'expo-network';
    // Network.addNetworkStateListener((state) => {
    //   const wasOffline = !this.isOnline;
    //   this.isOnline = state.isConnected ?? false;
    //   
    //   if (wasOffline && this.isOnline) {
    //     this.processRequestQueue();
    //   }
    // });
  }

  private async getStoredToken(): Promise<string | null> {
    try {
      return await SecureStore.getItemAsync('jwt_token');
    } catch (error) {
      console.warn('Error getting stored token:', error);
      return null;
    }
  }

  private async handleTokenExpiry() {
    try {
      await SecureStore.deleteItemAsync('jwt_token');
      await AsyncStorage.removeItem('user');
      
      // Navigate to login screen
      // This would be handled by the auth context
    } catch (error) {
      console.error('Error handling token expiry:', error);
    }
  }

  private queueRequest(request: () => Promise<any>) {
    this.requestQueue.push(request);
  }

  private async processRequestQueue() {
    while (this.requestQueue.length > 0) {
      const request = this.requestQueue.shift();
      if (request) {
        try {
          await request();
        } catch (error) {
          console.error('Error processing queued request:', error);
        }
      }
    }
  }

  public setAuthToken(token: string | null) {
    if (token) {
      this.api.defaults.headers.common['Authorization'] = `Bearer ${token}`;
    } else {
      delete this.api.defaults.headers.common['Authorization'];
    }
  }

  // HTTP Methods
  public async get<T = any>(url: string, config?: any): Promise<AxiosResponse<T>> {
    return this.api.get(url, config);
  }

  public async post<T = any>(url: string, data?: any, config?: any): Promise<AxiosResponse<T>> {
    return this.api.post(url, data, config);
  }

  public async put<T = any>(url: string, data?: any, config?: any): Promise<AxiosResponse<T>> {
    return this.api.put(url, data, config);
  }

  public async patch<T = any>(url: string, data?: any, config?: any): Promise<AxiosResponse<T>> {
    return this.api.patch(url, data, config);
  }

  public async delete<T = any>(url: string, config?: any): Promise<AxiosResponse<T>> {
    return this.api.delete(url, config);
  }

  // File upload
  public async uploadFile(url: string, file: any, onProgress?: (progress: number) => void) {
    const formData = new FormData();
    formData.append('file', {
      uri: file.uri,
      type: file.type,
      name: file.name,
    } as any);

    return this.api.post(url, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
      onUploadProgress: (progressEvent) => {
        if (onProgress && progressEvent.total) {
          const progress = (progressEvent.loaded / progressEvent.total) * 100;
          onProgress(progress);
        }
      },
    });
  }

  // Offline storage methods
  public async cacheData(key: string, data: any) {
    try {
      await AsyncStorage.setItem(`cache_${key}`, JSON.stringify({
        data,
        timestamp: Date.now(),
      }));
    } catch (error) {
      console.error('Error caching data:', error);
    }
  }

  public async getCachedData(key: string, maxAge: number = 3600000): Promise<any> {
    try {
      const cached = await AsyncStorage.getItem(`cache_${key}`);
      if (cached) {
        const { data, timestamp } = JSON.parse(cached);
        if (Date.now() - timestamp < maxAge) {
          return data;
        }
      }
      return null;
    } catch (error) {
      console.error('Error getting cached data:', error);
      return null;
    }
  }

  public async clearCache() {
    try {
      const keys = await AsyncStorage.getAllKeys();
      const cacheKeys = keys.filter((key: string) => key.startsWith('cache_'));
      await AsyncStorage.multiRemove(cacheKeys);
    } catch (error) {
      console.error('Error clearing cache:', error);
    }
  }

  // Health check
  public async healthCheck(): Promise<boolean> {
    try {
      await this.api.get('/health');
      return true;
    } catch {
      return false;
    }
  }

  // Get network status
  public getNetworkStatus(): boolean {
    return this.isOnline;
  }
}

export const ApiService = new ApiServiceClass();
