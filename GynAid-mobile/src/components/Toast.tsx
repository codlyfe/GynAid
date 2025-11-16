import React, { useState, useEffect } from 'react';
import { View, StyleSheet } from 'react-native';
import { Snackbar } from 'react-native-paper';
import { theme, spacing } from '../utils/theme';

type SnackbarType = 'success' | 'error' | 'info' | 'warning';

interface ToastProps {
  visible: boolean;
  message: string;
  type?: SnackbarType;
  duration?: number;
  onDismiss: () => void;
}

const Toast: React.FC<ToastProps> = ({ 
  visible, 
  message, 
  type = 'info', 
  duration = 3000,
  onDismiss 
}) => {
  const getSnackbarColor = () => {
    switch (type) {
      case 'success':
        return theme.colors.success;
      case 'error':
        return theme.colors.error;
      case 'warning':
        return theme.colors.warning;
      default:
        return theme.colors.info;
    }
  };

  return (
    <Snackbar
      visible={visible}
      onDismiss={onDismiss}
      duration={duration}
      style={[styles.snackbar, { backgroundColor: getSnackbarColor() }]}
      action={{
        label: 'Dismiss',
        onPress: onDismiss,
      }}
    >
      {message}
    </Snackbar>
  );
};

const styles = StyleSheet.create({
  snackbar: {
    margin: spacing.md,
    borderRadius: 8,
  },
});

export default Toast;

// Toast hook for easy usage
interface ToastState {
  message: string;
  type: SnackbarType;
  visible: boolean;
}

let toastQueue: ToastState[] = [];
let toastListeners: Array<(toast: ToastState | null) => void> = [];

const showToast = (message: string, type: SnackbarType = 'info') => {
  const newToast: ToastState = { message, type, visible: true };
  toastQueue.push(newToast);
  
  if (toastListeners.length > 0) {
    toastListeners.forEach(listener => listener(newToast));
  }
};

export const useToast = () => {
  const [toast, setToast] = useState<ToastState | null>(null);

  useEffect(() => {
    const listener = (newToast: ToastState | null) => {
      setToast(newToast);
    };

    toastListeners.push(listener);

    return () => {
      toastListeners = toastListeners.filter(l => l !== listener);
    };
  }, []);

  const hideToast = () => {
    setToast(null);
    toastQueue = [];
  };

  return {
    toast,
    hideToast,
    success: (message: string) => showToast(message, 'success'),
    error: (message: string) => showToast(message, 'error'),
    warning: (message: string) => showToast(message, 'warning'),
    info: (message: string) => showToast(message, 'info'),
  };
};