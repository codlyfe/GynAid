import React, { useState } from 'react';
import {
  View,
  StyleSheet,
  KeyboardAvoidingView,
  Platform,
  ScrollView,
} from 'react-native';
import {
  TextInput,
  Button,
  Title,
  Paragraph,
  Card,
  ActivityIndicator,
} from 'react-native-paper';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useAuth } from '../services/AuthService';
import { theme, spacing } from '../utils/theme';

const LoginScreen = ({ navigation }: any) => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  
  const { login } = useAuth();

  const handleLogin = async () => {
    if (!email || !password) {
      setError('Please fill in all fields');
      return;
    }

    try {
      setLoading(true);
      setError('');
      await login(email, password);
      // Navigation will be handled by auth state change
    } catch (error: any) {
      setError(error.message || 'Login failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <SafeAreaView style={styles.container}>
      <KeyboardAvoidingView
        behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
        style={styles.keyboardView}
      >
        <ScrollView style={styles.scrollView} showsVerticalScrollIndicator={false}>
          <View style={styles.header}>
            <Title style={styles.title}>Welcome Back</Title>
            <Paragraph style={styles.subtitle}>
              Sign in to your GynAid account
            </Paragraph>
          </View>

          <Card style={styles.card}>
            <Card.Content>
              {error ? (
                <Paragraph style={styles.errorText}>{error}</Paragraph>
              ) : null}

              <TextInput
                label="Email"
                value={email}
                onChangeText={setEmail}
                mode="outlined"
                keyboardType="email-address"
                autoCapitalize="none"
                style={styles.input}
                left={<TextInput.Icon icon="email" />}
              />

              <TextInput
                label="Password"
                value={password}
                onChangeText={setPassword}
                mode="outlined"
                secureTextEntry
                style={styles.input}
                left={<TextInput.Icon icon="lock" />}
              />

              <Button
                mode="contained"
                onPress={handleLogin}
                style={styles.button}
                disabled={loading}
              >
                {loading ? (
                  <ActivityIndicator color="white" size="small" />
                ) : (
                  'Sign In'
                )}
              </Button>
            </Card.Content>
          </Card>

          <View style={styles.footer}>
            <Paragraph>
              Don't have an account?{' '}
              <Paragraph
                style={styles.link}
                onPress={() => navigation.navigate('Register')}
              >
                Sign Up
              </Paragraph>
            </Paragraph>
          </View>
        </ScrollView>
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: theme.colors.background,
  },
  keyboardView: {
    flex: 1,
  },
  scrollView: {
    flex: 1,
    padding: spacing.lg,
  },
  header: {
    alignItems: 'center',
    marginBottom: spacing.xl,
    marginTop: spacing.lg,
  },
  title: {
    fontSize: 32,
    fontWeight: 'bold',
    color: theme.colors.primary,
    marginBottom: spacing.sm,
  },
  subtitle: {
    fontSize: 16,
    color: theme.colors.onSurface,
    textAlign: 'center',
  },
  card: {
    marginBottom: spacing.lg,
  },
  input: {
    marginBottom: spacing.md,
    backgroundColor: theme.colors.background,
  },
  button: {
    marginTop: spacing.md,
    paddingVertical: spacing.xs,
  },
  errorText: {
    color: theme.colors.error,
    marginBottom: spacing.md,
    textAlign: 'center',
  },
  footer: {
    alignItems: 'center',
    marginTop: spacing.xl,
  },
  link: {
    color: theme.colors.primary,
    fontWeight: 'bold',
  },
});

export default LoginScreen;