import * as Notifications from 'expo-notifications';
import * as Device from 'expo-device';
import { Platform } from 'react-native';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { ApiService } from './ApiService';

// Configure notification behavior
Notifications.setNotificationHandler({
  handleNotification: async () => ({
    shouldShowAlert: true,
    shouldPlaySound: true,
    shouldSetBadge: true,
  }),
});

export interface NotificationData {
  id: string;
  title: string;
  body: string;
  data?: any;
  categoryId?: string;
  priority?: 'low' | 'normal' | 'high' | 'max';
}

class NotificationServiceClass {
  private expoPushToken: string | null = null;

  async initialize() {
    if (!Device.isDevice) {
      console.log('Must use physical device for Push Notifications');
      return;
    }

    // Request permissions
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

    // Get push token
    try {
      this.expoPushToken = (await Notifications.getExpoPushTokenAsync()).data;
      console.log('Expo Push Token:', this.expoPushToken);
      
      // Send token to backend
      await this.registerPushToken();
    } catch (error) {
      console.error('Error getting push token:', error);
    }

    // Configure notification categories
    await this.setupNotificationCategories();

    // Set up notification listeners
    this.setupNotificationListeners();

    // Schedule daily health reminders
    await this.scheduleDailyReminders();
  }

  private async registerPushToken() {
    if (!this.expoPushToken) return;

    try {
      await ApiService.post('/api/notifications/register-token', {
        token: this.expoPushToken,
        platform: Platform.OS,
      });
    } catch (error) {
      console.error('Error registering push token:', error);
    }
  }

  private async setupNotificationCategories() {
    await Notifications.setNotificationCategoryAsync('health_reminder', [
      {
        identifier: 'mark_done',
        buttonTitle: 'Mark as Done',
        options: { opensAppToForeground: false },
      },
      {
        identifier: 'snooze',
        buttonTitle: 'Remind Later',
        options: { opensAppToForeground: false },
      },
    ]);

    await Notifications.setNotificationCategoryAsync('appointment', [
      {
        identifier: 'join_call',
        buttonTitle: 'Join Call',
        options: { opensAppToForeground: true },
      },
      {
        identifier: 'reschedule',
        buttonTitle: 'Reschedule',
        options: { opensAppToForeground: true },
      },
    ]);

    await Notifications.setNotificationCategoryAsync('emergency', [
      {
        identifier: 'call_emergency',
        buttonTitle: 'Call 999',
        options: { opensAppToForeground: false },
      },
      {
        identifier: 'view_guide',
        buttonTitle: 'View Guide',
        options: { opensAppToForeground: true },
      },
    ]);
  }

  private setupNotificationListeners() {
    // Handle notification received while app is in foreground
    Notifications.addNotificationReceivedListener(notification => {
      console.log('Notification received:', notification);
    });

    // Handle notification tapped
    Notifications.addNotificationResponseReceivedListener(response => {
      console.log('Notification response:', response);
      this.handleNotificationResponse(response);
    });
  }

  private handleNotificationResponse(response: Notifications.NotificationResponse) {
    const { actionIdentifier, notification } = response;
    const { data } = notification.request.content;

    switch (actionIdentifier) {
      case 'mark_done':
        this.markReminderDone(data?.reminderId);
        break;
      case 'snooze':
        this.snoozeReminder(data?.reminderId);
        break;
      case 'join_call':
        this.joinVideoCall(data?.consultationId);
        break;
      case 'call_emergency':
        this.callEmergency();
        break;
      default:
        // Handle default tap
        this.navigateToScreen(data?.screen, data?.params);
    }
  }

  // Schedule local notifications
  async scheduleNotification(notification: NotificationData, trigger: Notifications.NotificationTriggerInput) {
    try {
      const id = await Notifications.scheduleNotificationAsync({
        content: {
          title: notification.title,
          body: notification.body,
          data: notification.data,
          categoryIdentifier: notification.categoryId,
          priority: this.mapPriority(notification.priority),
        },
        trigger,
      });
      
      return id;
    } catch (error) {
      console.error('Error scheduling notification:', error);
      return null;
    }
  }

  // Schedule daily health reminders
  private async scheduleDailyReminders() {
    // Cycle tracking reminder
    await this.scheduleNotification(
      {
        id: 'cycle_reminder',
        title: 'Cycle Tracker',
        body: 'Don\'t forget to log your cycle data today!',
        categoryId: 'health_reminder',
        data: { screen: 'CycleTracker', reminderId: 'cycle_reminder' },
      },
      {
        hour: 20, // 8 PM
        minute: 0,
        repeats: true,
      }
    );

    // Medication reminder (if applicable)
    const medicationReminders = await AsyncStorage.getItem('medication_reminders');
    if (medicationReminders) {
      const reminders = JSON.parse(medicationReminders);
      for (const reminder of reminders) {
        await this.scheduleNotification(
          {
            id: reminder.id,
            title: 'Medication Reminder',
            body: `Time to take your ${reminder.medication}`,
            categoryId: 'health_reminder',
            data: { screen: 'Medications', reminderId: reminder.id },
          },
          {
            hour: reminder.hour,
            minute: reminder.minute,
            repeats: true,
          }
        );
      }
    }
  }

  // Schedule appointment reminders
  async scheduleAppointmentReminder(consultation: any) {
    const appointmentTime = new Date(consultation.scheduledAt);
    const reminderTime = new Date(appointmentTime.getTime() - 30 * 60 * 1000); // 30 minutes before

    await this.scheduleNotification(
      {
        id: `appointment_${consultation.id}`,
        title: 'Upcoming Consultation',
        body: `Your consultation with ${consultation.providerName} starts in 30 minutes`,
        categoryId: 'appointment',
        priority: 'high',
        data: { 
          screen: 'Consultations', 
          consultationId: consultation.id,
          params: { consultationId: consultation.id }
        },
      },
      { date: reminderTime }
    );
  }

  // Send emergency notification
  async sendEmergencyNotification(type: string, message: string) {
    await this.scheduleNotification(
      {
        id: `emergency_${Date.now()}`,
        title: 'Health Emergency Alert',
        body: message,
        categoryId: 'emergency',
        priority: 'max',
        data: { screen: 'Emergency', type },
      },
      { seconds: 1 }
    );
  }

  // MOH notification
  async sendMOHNotification(notification: any) {
    await this.scheduleNotification(
      {
        id: `moh_${notification.id}`,
        title: notification.title,
        body: notification.content,
        priority: notification.priority.toLowerCase(),
        data: { 
          screen: 'Notifications', 
          notificationId: notification.id 
        },
      },
      { seconds: 1 }
    );
  }

  // Utility methods
  private mapPriority(priority?: string): Notifications.AndroidNotificationPriority {
    switch (priority) {
      case 'low': return Notifications.AndroidNotificationPriority.LOW;
      case 'high': return Notifications.AndroidNotificationPriority.HIGH;
      case 'max': return Notifications.AndroidNotificationPriority.MAX;
      default: return Notifications.AndroidNotificationPriority.DEFAULT;
    }
  }

  private async markReminderDone(reminderId: string) {
    try {
      await AsyncStorage.setItem(`reminder_${reminderId}_done`, new Date().toISOString());
    } catch (error) {
      console.error('Error marking reminder done:', error);
    }
  }

  private async snoozeReminder(reminderId: string) {
    // Reschedule for 1 hour later
    const snoozeTime = new Date(Date.now() + 60 * 60 * 1000);
    await this.scheduleNotification(
      {
        id: `${reminderId}_snoozed`,
        title: 'Health Reminder (Snoozed)',
        body: 'Don\'t forget to complete your health task!',
        categoryId: 'health_reminder',
        data: { reminderId },
      },
      { date: snoozeTime }
    );
  }

  private joinVideoCall(consultationId: string) {
    // This would navigate to the video call screen
    this.navigateToScreen('VideoCall', { consultationId });
  }

  private callEmergency() {
    // This would initiate an emergency call
    console.log('Calling emergency services...');
  }

  private navigateToScreen(screen: string, params?: any) {
    // This would be handled by the navigation service
    console.log(`Navigate to ${screen}`, params);
  }

  // Cancel notification
  async cancelNotification(notificationId: string) {
    await Notifications.cancelScheduledNotificationAsync(notificationId);
  }

  // Cancel all notifications
  async cancelAllNotifications() {
    await Notifications.cancelAllScheduledNotificationsAsync();
  }

  // Get scheduled notifications
  async getScheduledNotifications() {
    return await Notifications.getAllScheduledNotificationsAsync();
  }

  // Badge management
  async setBadgeCount(count: number) {
    await Notifications.setBadgeCountAsync(count);
  }

  async clearBadge() {
    await Notifications.setBadgeCountAsync(0);
  }
}

export const NotificationService = new NotificationServiceClass();
