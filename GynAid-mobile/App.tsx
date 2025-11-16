import React, { useEffect, useState } from 'react';
import { StatusBar } from 'expo-status-bar';
import { View, Text, StyleSheet } from 'react-native';
import { NavigationContainer } from '@react-navigation/native';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import { SafeAreaProvider } from 'react-native-safe-area-context';
import { GestureHandlerRootView } from 'react-native-gesture-handler';
import { Ionicons } from '@expo/vector-icons';
import * as SplashScreen from 'expo-splash-screen';

// Screens
import DashboardScreen from './src/screens/DashboardScreen';
import LoginScreen from './src/screens/LoginScreen';
import RegisterScreen from './src/screens/RegisterScreen';
import ChatScreen from './src/screens/ChatScreen';
import CycleTrackerScreen from './src/screens/CycleTrackerScreen';

// Services
import { AuthProvider, useAuth } from './src/services/AuthService';
import { AppProvider } from './src/hooks/useAppState';
import { ErrorBoundary } from './src/components';
import { NotificationService } from './src/services/NotificationService';
import { theme } from './src/utils/theme';

const Stack = createNativeStackNavigator();
const Tab = createBottomTabNavigator();

// Keep the splash screen visible while we fetch resources
SplashScreen.preventAutoHideAsync();

// Auth Navigator
function AuthNavigator() {
  return (
    <ErrorBoundary>
      <Stack.Navigator screenOptions={{ headerShown: false }}>
        <Stack.Screen name="Login" component={LoginScreen} />
        <Stack.Screen name="Register" component={RegisterScreen} />
      </Stack.Navigator>
    </ErrorBoundary>
  );
}

// Main App Tabs
function MainTabNavigator() {
  return (
    <ErrorBoundary>
      <Tab.Navigator
        screenOptions={({ route }) => ({
          tabBarIcon: ({ focused, color, size }) => {
            let iconName: keyof typeof Ionicons.glyphMap;

            switch (route.name) {
              case 'Dashboard':
                iconName = focused ? 'home' : 'home-outline';
                break;
              case 'Cycle':
                iconName = focused ? 'calendar' : 'calendar-outline';
                break;
              case 'Chat':
                iconName = focused ? 'chatbubbles' : 'chatbubbles-outline';
                break;
              case 'Profile':
                iconName = focused ? 'person' : 'person-outline';
                break;
              default:
                iconName = 'home-outline';
            }

            return <Ionicons name={iconName} size={size} color={color} />;
          },
          tabBarActiveTintColor: theme.colors.primary,
          tabBarInactiveTintColor: theme.colors.onSurface,
          headerShown: false,
        })}
      >
        <Tab.Screen name="Dashboard" component={DashboardScreen} />
        <Tab.Screen name="Cycle" component={CycleTrackerScreen} />
        <Tab.Screen name="Chat" component={ChatScreen} />
        <Tab.Screen
          name="Profile"
          component={() => <View style={{ flex: 1, justifyContent: 'center', alignItems: 'center' }}>
            <Text>Profile Screen (Coming Soon)</Text>
          </View>}
        />
      </Tab.Navigator>
    </ErrorBoundary>
  );
}

// Main Stack Navigator
function MainNavigator() {
  return (
    <ErrorBoundary>
      <Stack.Navigator screenOptions={{ headerShown: false }}>
        <Stack.Screen name="MainTabs" component={MainTabNavigator} />
        <Stack.Screen name="Chat" component={ChatScreen} />
      </Stack.Navigator>
    </ErrorBoundary>
  );
}

// Root Navigator with Auth State
function AppNavigator() {
  const { user, isLoading } = useAuth();

  if (isLoading) {
    return (
      <View style={styles.loadingContainer}>
        <Text style={styles.loadingText}>Loading GynAid...</Text>
      </View>
    );
  }

  return (
    <ErrorBoundary>
      <NavigationContainer>
        <Stack.Navigator screenOptions={{ headerShown: false }}>
          {user ? (
            <Stack.Screen name="Main" component={MainNavigator} />
          ) : (
            <Stack.Screen name="Auth" component={AuthNavigator} />
          )}
        </Stack.Navigator>
      </NavigationContainer>
    </ErrorBoundary>
  );
}

export default function App() {
  const [appIsReady, setAppIsReady] = useState(false);

  useEffect(() => {
    async function prepare() {
      try {
        // Initialize notification service
        await NotificationService.initialize();
        
        // Pre-load any fonts, make API calls here if needed
        await new Promise(resolve => setTimeout(resolve, 2000));
      } catch (e) {
        console.warn(e);
      } finally {
        setAppIsReady(true);
      }
    }

    prepare();
  }, []);

  useEffect(() => {
    if (appIsReady) {
      SplashScreen.hideAsync();
    }
  }, [appIsReady]);

  if (!appIsReady) {
    return (
      <View style={styles.loadingContainer}>
        <Text style={styles.loadingText}>Loading GynAid...</Text>
      </View>
    );
  }

  return (
    <GestureHandlerRootView style={{ flex: 1 }}>
      <SafeAreaProvider>
        <AuthProvider>
          <AppProvider>
            <AppNavigator />
            <StatusBar style="auto" />
          </AppProvider>
        </AuthProvider>
      </SafeAreaProvider>
    </GestureHandlerRootView>
  );
}

const styles = StyleSheet.create({
  loadingContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: theme.colors.background,
  },
  loadingText: {
    fontSize: 16,
    color: theme.colors.onSurface,
  },
});
