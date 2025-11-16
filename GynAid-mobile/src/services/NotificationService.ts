import { Platform } from 'react-native';
import AsyncStorage from '@react-native-async-storage/async-storage';
import * as Notifications from 'expo-notifications';
import * as Device from 'expo-device';
import { ApiService } from './ApiService';

// Configure notification behavior
Notifications.setNotificationHandler({
  handleNotification: async () => ({
    shouldShowAlert: true,
    shouldPlaySound: true,
    shouldSetBadge: false,
  }),
});

// Enhanced notification interface
export interface NotificationData {
  id: string;
  title: string;
  body: string;
  data?: any;
  category?: string;
  priority?: 'min' | 'low' | 'default' | 'high' | 'max';
  sound?: boolean;
}

// Notification channels for Android
export interface NotificationChannel {
  id: string;
  name: string;
  description?: string;
  importance?: 'min' | 'low' | 'default' | 'high' | 'max';
}

// Full-featured notification service
class NotificationServiceClass {
  private initialized: boolean = false;
  private expoPushToken: string | null = null;

  constructor() {
    this.initialize();
  }

  async initialize() {
    try {
      if (!this.initialized) {
        await this.setupNotifications();
        await this.registerForPushNotificationsAsync();
        this.initialized = true;
        console.log('NotificationService initialized successfully');
      }
    } catch (error) {
      console.error('Error initializing NotificationService:', error);
    }
  }

  private async setupNotifications() {
    if (Platform.OS === 'android') {
      await Notifications.setNotificationChannelAsync('gynaid-default', {
        name: 'GynAid Notifications',
        importance: Notifications.AndroidImportance.DEFAULT,
        vibrationPattern: [0, 250, 250, 250],
        lightColor: '#FF231F7C',
      });

      await Notifications.setNotificationChannelAsync('gynaid-reminders', {
        name: 'Health Reminders',
        description: 'Cycle and health tracking reminders',
        importance: Notifications.AndroidImportance.HIGH,
        vibrationPattern: [0, 250, 250, 250],
        lightColor: '#FF231F7C',
      });

      await Notifications.setNotificationChannelAsync('gynaid-emergency', {
        name: 'Emergency Notifications',
        description: 'Important health alerts and emergency notifications',
        importance: Notifications.AndroidImportance.MAX,
        vibrationPattern: [0, 100, 200, 100, 200, 100],
        lightColor: '#FF231F7C',
      });
    }
  }

  private async registerForPushNotificationsAsync() {
    try {
      if (!Device.isDevice) {
        console.log('Must use physical device for Push Notifications');
        return;
      }

      const { status: existingStatus } = await Notifications.getPermissionsAsync();
      let finalStatus = existingStatus;

      if (existingStatus !== 'granted') {
        const { status } = await Notifications.requestPermissionsAsync();
        finalStatus = status;
      }

      if (finalStatus !== 'granted') {
        console.log('Failed to get push token for push notification!');
        return;
      }

      this.expoPushToken = (await Notifications.getExpoPushTokenAsync()).data;
      console.log('Push token:', this.expoPushToken);

      // Store token for backend communication
      if (this.expoPushToken) {
        await AsyncStorage.setItem('expo_push_token', this.expoPushToken);
      }

      if (Platform.OS === 'android') {
        await Notifications.setNotificationChannelAsync('default', {
          name: 'default',
          importance: Notifications.AndroidImportance.MAX,
          vibrationPattern: [0, 250, 250, 250],
          lightColor: '#FF231F7C',
        });
      }

      return this.expoPushToken;
    } catch (error) {
      console.error('Error getting push token:', error);
      return null;
    }
  }

  async scheduleNotification(
    notification: NotificationData, 
    trigger: Date | number | any
  ): Promise<string> {
    try {
      const identifier = await Notifications.scheduleNotificationAsync({
        content: {
          title: notification.title,
          body: notification.body,
          data: notification.data,
          categoryIdentifier: notification.category,
          sound: notification.sound !== false,
          priority: notification.priority || 'default',
        },
        trigger: trigger as any,
      });

      console.log('Notification scheduled with ID:', identifier);
      return identifier;
    } catch (error) {
      console.error('Error scheduling notification:', error);
      throw error;
    }
  }

  async scheduleCycleReminder(daysBeforePeriod: number = 3) {
    try {
      const trigger = {
        date: this.calculateNextReminderDate(daysBeforePeriod),
      };

      return await this.scheduleNotification(
        {
          id: 'cycle-reminder',
          title: 'Cycle Reminder',
          body: `Your period is coming up in ${daysBeforePeriod} days. Time to prepare!`,
          category: 'health-reminder',
          priority: 'high',
        },
        trigger
      );
    } catch (error) {
      console.error('Error scheduling cycle reminder:', error);
      throw error;
    }
  }

  async scheduleConsultationReminder(consultationDate: Date) {
    try {
      const reminderTime = new Date(consultationDate.getTime() - 30 * 60 * 1000); // 30 minutes before
      
      return await this.scheduleNotification(
        {
          id: 'consultation-reminder',
          title: 'Upcoming Consultation',
          body: 'You have a consultation in 30 minutes',
          category: 'appointment',
          priority: 'high',
          sound: true,
        },
        reminderTime
      );
    } catch (error) {
      console.error('Error scheduling consultation reminder:', error);
      throw error;
    }
  }

  async cancelNotification(notificationId: string) {
    try {
      await Notifications.cancelScheduledNotificationAsync(notificationId);
      console.log('Notification cancelled:', notificationId);
    } catch (error) {
      console.error('Error cancelling notification:', error);
    }
  }

  async cancelAllNotifications() {
    try {
      await Notifications.cancelAllScheduledNotificationsAsync();
      console.log('All notifications cancelled');
    } catch (error) {
      console.error('Error cancelling all notifications:', error);
    }
  }

  async getAllNotifications() {
    try {
      return await Notifications.getAllScheduledNotificationsAsync();
    } catch (error) {
      console.error('Error getting scheduled notifications:', error);
      return [];
    }
  }

  async setBadgeCount(count: number) {
    try {
      if (Platform.OS === 'ios') {
        await Notifications.setBadgeCountAsync(count);
      }
      console.log('Badge count set to:', count);
    } catch (error) {
      console.error('Error setting badge count:', error);
    }
  }

  async sendLocalNotification(notification: NotificationData) {
    try {
      await Notifications.scheduleNotificationAsync({
        content: {
          title: notification.title,
          body: notification.body,
          data: notification.data,
          categoryIdentifier: notification.category,
          sound: notification.sound !== false,
        },
        trigger: null, // Immediate notification
      });
    } catch (error) {
      console.error('Error sending local notification:', error);
    }
  }

  private calculateNextReminderDate(daysBeforePeriod: number): Date {
    const now = new Date();
    const nextCycle = new Date(now.getTime() + 28 * 24 * 60 * 60 * 1000); // Assume 28-day cycle
    const reminderDate = new Date(nextCycle.getTime() - daysBeforePeriod * 24 * 60 * 60 * 1000);
    return reminderDate;
  }

  // Health-specific notification methods
  async scheduleMedicationReminder(medicationName: string, time: string) {
    const [hours, minutes] = time.split(':').map(Number);
    
    return await this.scheduleNotification(
      {
        id: `medication-${medicationName}`,
        title: 'Medication Reminder',
        body: `Time to take ${medicationName}`,
        category: 'medication',
        priority: 'high',
      },
      {
        hour: hours,
        minute: minutes,
        repeats: true,
      } as any
    );
  }

  async scheduleFertileWindowReminder() {
    return await this.scheduleNotification(
      {
        id: 'fertile-window',
        title: 'Fertile Window Alert',
        body: 'You are in your fertile window. Consider tracking cervical mucus.',
        category: 'fertility',
        priority: 'default',
      },
      {
        hour: 9,
        minute: 0,
        repeats: true,
      } as any
    );
  }

  // Get push token for backend registration
  async getPushToken(): Promise<string | null> {
    return this.expoPushToken;
  }

  // Send push token to backend
  async registerWithBackend() {
    try {
      const token = await this.getPushToken();
      if (token) {
        await ApiService.post('/api/notifications/register', { expoPushToken: token });
        console.log('Push token registered with backend');
      }
    } catch (error) {
      console.error('Error registering push token with backend:', error);
    }
  }

  // Add notification listener
  addNotificationReceivedListener(callback: (notification: Notifications.Notification) => void) {
    return Notifications.addNotificationReceivedListener(callback);
  }

  // Add notification response listener
  addNotificationResponseReceivedListener(
    callback: (response: Notifications.NotificationResponse) => void
  ) {
    return Notifications.addNotificationResponseReceivedListener(callback);
  }
}

export const NotificationService = new NotificationServiceClass();
