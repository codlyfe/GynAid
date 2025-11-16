import React, { Component, ReactNode } from 'react';
import { View, StyleSheet, Text } from 'react-native';
import { Button, Card } from 'react-native-paper';
import { theme, spacing } from '../utils/theme';

interface Props {
  children: ReactNode;
  fallback?: ReactNode;
}

interface State {
  hasError: boolean;
  error?: Error;
}

class ErrorBoundary extends Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = { hasError: false };
  }

  static getDerivedStateFromError(error: Error): State {
    return { hasError: true, error };
  }

  componentDidCatch(error: Error, errorInfo: any) {
    console.error('ErrorBoundary caught an error:', error, errorInfo);
  }

  handleRetry = () => {
    this.setState({ hasError: false, error: undefined });
  };

  render() {
    if (this.state.hasError) {
      if (this.props.fallback) {
        return this.props.fallback;
      }

      return (
        <View style={styles.container}>
          <Card style={styles.errorCard}>
            <Card.Content style={styles.errorContent}>
              <Text style={styles.errorTitle}>Something went wrong</Text>
              <Text style={styles.errorMessage}>
                {this.state.error?.message || 'An unexpected error occurred'}
              </Text>
              <Button
                mode="contained"
                onPress={this.handleRetry}
                style={styles.retryButton}
              >
                Try Again
              </Button>
            </Card.Content>
          </Card>
        </View>
      );
    }

    return this.props.children;
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: spacing.lg,
    backgroundColor: theme.colors.background,
  },
  errorCard: {
    width: '100%',
    maxWidth: 300,
  },
  errorContent: {
    alignItems: 'center',
  },
  errorTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    color: theme.colors.error,
    marginBottom: spacing.md,
    textAlign: 'center',
  },
  errorMessage: {
    fontSize: 14,
    color: theme.colors.onSurface,
    textAlign: 'center',
    marginBottom: spacing.lg,
  },
  retryButton: {
    paddingVertical: spacing.xs,
  },
});

export default ErrorBoundary;