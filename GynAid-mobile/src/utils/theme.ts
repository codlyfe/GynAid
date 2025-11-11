import { DefaultTheme } from 'react-native-paper';

export const theme = {
  ...DefaultTheme,
  colors: {
    ...DefaultTheme.colors,
    primary: '#E91E63',
    secondary: '#9C27B0',
    accent: '#FF5722',
    background: '#FFFFFF',
    surface: '#F5F5F5',
    error: '#F44336',
    success: '#4CAF50',
    warning: '#FF9800',
    info: '#2196F3',
    text: '#212121',
    onSurface: '#757575',
    disabled: '#BDBDBD',
    placeholder: '#9E9E9E',
    backdrop: 'rgba(0, 0, 0, 0.5)',
  },
  fonts: {
    ...DefaultTheme.fonts,
    regular: {
      fontFamily: 'System',
      fontWeight: '400' as const,
    },
    medium: {
      fontFamily: 'System',
      fontWeight: '500' as const,
    },
    light: {
      fontFamily: 'System',
      fontWeight: '300' as const,
    },
    thin: {
      fontFamily: 'System',
      fontWeight: '100' as const,
    },
  },
  roundness: 8,
  animation: {
    scale: 1.0,
  },
};

export const spacing = {
  xs: 4,
  sm: 8,
  md: 16,
  lg: 24,
  xl: 32,
  xxl: 48,
};

export const typography = {
  h1: {
    fontSize: 32,
    fontWeight: 'bold' as const,
    lineHeight: 40,
  },
  h2: {
    fontSize: 28,
    fontWeight: 'bold' as const,
    lineHeight: 36,
  },
  h3: {
    fontSize: 24,
    fontWeight: '600' as const,
    lineHeight: 32,
  },
  h4: {
    fontSize: 20,
    fontWeight: '600' as const,
    lineHeight: 28,
  },
  body1: {
    fontSize: 16,
    fontWeight: '400' as const,
    lineHeight: 24,
  },
  body2: {
    fontSize: 14,
    fontWeight: '400' as const,
    lineHeight: 20,
  },
  caption: {
    fontSize: 12,
    fontWeight: '400' as const,
    lineHeight: 16,
  },
};
