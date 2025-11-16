import React from 'react';
import { View, ActivityIndicator, StyleSheet, ViewStyle, Text } from 'react-native';
import { theme, spacing } from '../utils/theme';

interface LoadingSpinnerProps {
  size?: 'small' | 'large';
  color?: string;
  style?: ViewStyle;
  text?: string;
}

const LoadingSpinner: React.FC<LoadingSpinnerProps> = ({
  size = 'large',
  color = theme.colors.primary,
  style,
  text
}) => {
  return (
    <View style={[styles.container, style]}>
      <ActivityIndicator size={size} color={color} />
      {text && (
        <Text style={styles.loadingText}>{text}</Text>
      )}
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: spacing.lg,
  },
  loadingText: {
    marginTop: spacing.md,
    fontSize: 16,
    color: theme.colors.onSurface,
    textAlign: 'center',
  },
});

export default LoadingSpinner;