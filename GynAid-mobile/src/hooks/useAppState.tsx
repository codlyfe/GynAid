import React, { createContext, useContext, useReducer, useEffect, ReactNode } from 'react';

// Types
export interface UserProfile {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  role: 'ADMIN' | 'CLIENT' | 'PROVIDER_INDIVIDUAL' | 'PROVIDER_INSTITUTION';
  avatar?: string;
  phoneNumber?: string;
  dateOfBirth?: string;
  lastUpdated: string;
}

export interface AppSettings {
  theme: 'light' | 'dark' | 'auto';
  notifications: {
    enabled: boolean;
    cycleReminders: boolean;
    consultationReminders: boolean;
    healthTips: boolean;
    emergencyAlerts: boolean;
  };
  privacy: {
    shareHealthData: boolean;
    anonymousAnalytics: boolean;
  };
  units: {
    weight: 'kg' | 'lbs';
    temperature: 'celsius' | 'fahrenheit';
    height: 'cm' | 'ft';
  };
}

export interface HealthData {
  cycleEntries: any[];
  symptoms: string[];
  medications: any[];
  consultations: any[];
  lastSync: string;
}

interface AppState {
  userProfile: UserProfile | null;
  settings: AppSettings | null;
  healthData: HealthData | null;
  isLoading: boolean;
  error: string | null;
  isOnline: boolean;
}

type AppAction =
  | { type: 'SET_LOADING'; payload: boolean }
  | { type: 'SET_ERROR'; payload: string | null }
  | { type: 'SET_USER_PROFILE'; payload: UserProfile }
  | { type: 'SET_SETTINGS'; payload: AppSettings }
  | { type: 'SET_HEALTH_DATA'; payload: HealthData }
  | { type: 'SET_ONLINE_STATUS'; payload: boolean }
  | { type: 'UPDATE_SETTINGS'; payload: Partial<AppSettings> }
  | { type: 'UPDATE_HEALTH_DATA'; payload: Partial<HealthData> }
  | { type: 'CLEAR_DATA' };

// Initial state
const initialState: AppState = {
  userProfile: null,
  settings: null,
  healthData: null,
  isLoading: false,
  error: null,
  isOnline: true,
};

// Reducer
function appReducer(state: AppState, action: AppAction): AppState {
  switch (action.type) {
    case 'SET_LOADING':
      return { ...state, isLoading: action.payload };
    
    case 'SET_ERROR':
      return { ...state, error: action.payload };
    
    case 'SET_USER_PROFILE':
      return { ...state, userProfile: action.payload };
    
    case 'SET_SETTINGS':
      return { ...state, settings: action.payload };
    
    case 'SET_HEALTH_DATA':
      return { ...state, healthData: action.payload };
    
    case 'SET_ONLINE_STATUS':
      return { ...state, isOnline: action.payload };
    
    case 'UPDATE_SETTINGS':
      return {
        ...state,
        settings: state.settings ? { ...state.settings, ...action.payload } : action.payload as AppSettings,
      };
    
    case 'UPDATE_HEALTH_DATA':
      return {
        ...state,
        healthData: state.healthData ? { ...state.healthData, ...action.payload } : action.payload as HealthData,
      };
    
    case 'CLEAR_DATA':
      return {
        ...initialState,
        isOnline: state.isOnline,
      };
    
    default:
      return state;
  }
}

// Context
interface AppContextType {
  state: AppState;
  dispatch: React.Dispatch<AppAction>;
  
  // Convenience methods
  setLoading: (loading: boolean) => void;
  setError: (error: string | null) => void;
  setUserProfile: (profile: UserProfile) => void;
  updateSettings: (settings: Partial<AppSettings>) => void;
  updateHealthData: (data: Partial<HealthData>) => void;
  clearData: () => void;
  
  // Computed getters
  isLoggedIn: boolean;
  isInitialized: boolean;
}

const AppContext = createContext<AppContextType | undefined>(undefined);

// Provider component
interface AppProviderProps {
  children: ReactNode;
}

export const AppProvider: React.FC<AppProviderProps> = ({ children }) => {
  const [state, dispatch] = useReducer(appReducer, initialState);

  // Convenience methods
  const setLoading = (loading: boolean) => {
    dispatch({ type: 'SET_LOADING', payload: loading });
  };

  const setError = (error: string | null) => {
    dispatch({ type: 'SET_ERROR', payload: error });
  };

  const setUserProfile = (profile: UserProfile) => {
    dispatch({ type: 'SET_USER_PROFILE', payload: profile });
  };

  const updateSettings = (settings: Partial<AppSettings>) => {
    dispatch({ type: 'UPDATE_SETTINGS', payload: settings });
  };

  const updateHealthData = (data: Partial<HealthData>) => {
    dispatch({ type: 'UPDATE_HEALTH_DATA', payload: data });
  };

  const clearData = () => {
    dispatch({ type: 'CLEAR_DATA' });
  };

  // Computed getters
  const isLoggedIn = state.userProfile !== null;
  const isInitialized = state.settings !== null && state.healthData !== null;

  const contextValue: AppContextType = {
    state,
    dispatch,
    setLoading,
    setError,
    setUserProfile,
    updateSettings,
    updateHealthData,
    clearData,
    isLoggedIn,
    isInitialized,
  };

  return (
    <AppContext.Provider value={contextValue}>
      {children}
    </AppContext.Provider>
  );
};

// Hook to use the app context
export const useApp = (): AppContextType => {
  const context = useContext(AppContext);
  if (context === undefined) {
    throw new Error('useApp must be used within an AppProvider');
  }
  return context;
};

// Default settings
export const defaultSettings: AppSettings = {
  theme: 'auto',
  notifications: {
    enabled: true,
    cycleReminders: true,
    consultationReminders: true,
    healthTips: true,
    emergencyAlerts: true,
  },
  privacy: {
    shareHealthData: false,
    anonymousAnalytics: true,
  },
  units: {
    weight: 'kg',
    temperature: 'celsius',
    height: 'cm',
  },
};

// Default health data
export const defaultHealthData: HealthData = {
  cycleEntries: [],
  symptoms: [],
  medications: [],
  consultations: [],
  lastSync: new Date().toISOString(),
};

export default AppProvider;