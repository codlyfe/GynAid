import React, { useEffect, useState } from 'react';
import { StatusBar } from 'expo-status-bar';
import { NavigationContainer } from '@react-navigation/native';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import { Provider as PaperProvider } from 'react-native-paper';
import { SafeAreaProvider } from 'react-native-safe-area-context';
import * as SplashScreen from 'expo-splash-screen';
import * as Font from 'expo-font';
import { Ionicons } from '@expo/vector-icons';

// Screens
import LoginScreen from './src/screens/LoginScreen';
import RegisterScreen from './src/screens/RegisterScreen';
import DashboardScreen from './src/screens/DashboardScreen';
import ChatBotScreen from './src/screens/ChatBotScreen';
import CycleTrackerScreen from './src/screens/CycleTrackerScreen';
import ConsultationsScreen from './src/screens/ConsultationsScreen';
import HealthTipsScreen from './src/screens/HealthTipsScreen';
import NotificationsScreen from './src/screens/NotificationsScreen';
import EmergencyScreen from './src/screens/EmergencyScreen';
import ProfileScreen from './src/screens/ProfileScreen';

// Services
import { AuthProvider, useAuth } from './src/services/AuthService';
import { NotificationService } from './src/services/NotificationService';
import { theme } from './src/utils/theme';

const Stack = createNativeStackNavigator();
const Tab = createBottomTabNavigator();

// Keep the splash screen visible while we fetch resources
SplashScreen.preventAutoHideAsync();

function TabNavigator() {
  return (
    <Tab.Navigator
      screenOptions={({ route }) => ({
        tabBarIcon: ({ focused, color, size }) => {
          let iconName: keyof typeof Ionicons.glyphMap;

          if (route.name === 'Dashboard') {
            iconName = focused ? 'home' : 'home-outline';
          } else if (route.name === 'Chat') {
            iconName = focused ? 'chatbubble' : 'chatbubble-outline';
          } else if (route.name === 'Cycle') {
            iconName = focused ? 'calendar' : 'calendar-outline';
          } else if (route.name === 'Consult') {
            iconName = focused ? 'videocam' : 'videocam-outline';
          } else if (route.name === 'Profile') {
            iconName = focused ? 'person' : 'person-outline';
          } else {
            iconName = 'ellipse-outline';
          }

          return <Ionicons name={iconName} size={size} color={color} />;
        },
        tabBarActiveTintColor: theme.colors.primary,
        tabBarInactiveTintColor: 'gray',
        headerShown: false,
      })}
    >
      <Tab.Screen name="Dashboard" component={DashboardScreen} />
      <Tab.Screen name="Chat" component={ChatBotScreen} />
      <Tab.Screen name="Cycle" component={CycleTrackerScreen} />
      <Tab.Screen name="Consult" component={ConsultationsScreen} />
      <Tab.Screen name="Profile" component={ProfileScreen} />
    </Tab.Navigator>
  );
}

function AuthStack() {
  return (
    <Stack.Navigator screenOptions={{ headerShown: false }}>
      <Stack.Screen name="Login" component={LoginScreen} />
      <Stack.Screen name="Register" component={RegisterScreen} />
    </Stack.Navigator>
  );
}

function AppStack() {
  return (
    <Stack.Navigator screenOptions={{ headerShown: false }}>
      <Stack.Screen name="Main" component={TabNavigator} />
      <Stack.Screen name="HealthTips" component={HealthTipsScreen} />
      <Stack.Screen name="Notifications" component={NotificationsScreen} />
      <Stack.Screen name="Emergency" component={EmergencyScreen} />
    </Stack.Navigator>
  );
}

function AppContent() {
  const { user, isLoading } = useAuth();

  if (isLoading) {
    return null; // Show splash screen
  }

  return (
    <NavigationContainer>
      {user ? <AppStack /> : <AuthStack />}
    </NavigationContainer>
  );
}

export default function App() {
  const [appIsReady, setAppIsReady] = useState(false);

  useEffect(() => {
    async function prepare() {
      try {
        // Pre-load fonts, make any API calls you need to do here
        await Font.loadAsync({
          ...Ionicons.font,
        });

        // Initialize notification service
        await NotificationService.initialize();
      } catch (e) {
        console.warn(e);
      } finally {
        // Tell the application to render
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
    return null;
  }

  return (
    <SafeAreaProvider>
      <PaperProvider theme={theme}>
        <AuthProvider>
          <AppContent />
          <StatusBar style="auto" />
        </AuthProvider>
      </PaperProvider>
    </SafeAreaProvider>
  );
}
