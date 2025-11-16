import React from 'react';
import { View, Text, StyleSheet, TouchableOpacity, ViewStyle } from 'react-native';
import { theme, spacing } from '../utils/theme';

interface PrimaryButtonProps {
  title: string;
  onPress: () => void;
  disabled?: boolean;
  loading?: boolean;
  style?: ViewStyle;
  variant?: 'contained' | 'outlined';
}

const PrimaryButton: React.FC<PrimaryButtonProps> = ({
  title,
  onPress,
  disabled = false,
  loading = false,
  style,
  variant = 'contained',
}) => {
  const buttonStyle = [
    styles.button,
    variant === 'contained' ? styles.contained : styles.outlined,
    disabled && styles.disabled,
    style,
  ];

  const textStyle = [
    styles.text,
    variant === 'contained' ? styles.textContained : styles.textOutlined,
    disabled && styles.textDisabled,
  ];

  return (
    <TouchableOpacity
      style={buttonStyle}
      onPress={onPress}
      disabled={disabled || loading}
      activeOpacity={0.8}
    >
      <Text style={textStyle}>
        {loading ? 'Loading...' : title}
      </Text>
    </TouchableOpacity>
  );
};

const styles = StyleSheet.create({
  button: {
    paddingVertical: spacing.md,
    paddingHorizontal: spacing.lg,
    borderRadius: 8,
    alignItems: 'center',
    justifyContent: 'center',
    minHeight: 48,
  },
  contained: {
    backgroundColor: theme.colors.primary,
  },
  outlined: {
    backgroundColor: 'transparent',
    borderWidth: 1,
    borderColor: theme.colors.primary,
  },
  disabled: {
    backgroundColor: theme.colors.disabled,
    borderColor: theme.colors.disabled,
  },
  text: {
    fontSize: 16,
    fontWeight: '600',
  },
  textContained: {
    color: 'white',
  },
  textOutlined: {
    color: theme.colors.primary,
  },
  textDisabled: {
    color: theme.colors.disabled,
  },
});

export default PrimaryButton;