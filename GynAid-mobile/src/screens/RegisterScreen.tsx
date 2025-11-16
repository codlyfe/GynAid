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
  SegmentedButtons,
} from 'react-native-paper';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useAuth } from '../services/AuthService';
import { theme, spacing } from '../utils/theme';

type UserRole = 'CLIENT' | 'PROVIDER_INDIVIDUAL' | 'PROVIDER_INSTITUTION';

const RegisterScreen = ({ navigation }: any) => {
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [role, setRole] = useState<UserRole>('CLIENT');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  
  const { register } = useAuth();

  const validateForm = () => {
    if (!firstName || !lastName || !email || !password || !confirmPassword) {
      setError('Please fill in all fields');
      return false;
    }

    if (password !== confirmPassword) {
      setError('Passwords do not match');
      return false;
    }

    if (password.length < 6) {
      setError('Password must be at least 6 characters long');
      return false;
    }

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email)) {
      setError('Please enter a valid email address');
      return false;
    }

    return true;
  };

  const handleRegister = async () => {
    if (!validateForm()) return;

    try {
      setLoading(true);
      setError('');
      await register(email, password, firstName, lastName, role);
      // Navigation will be handled by auth state change
    } catch (error: any) {
      setError(error.message || 'Registration failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const roleOptions = [
    { value: 'CLIENT', label: 'Client' },
    { value: 'PROVIDER_INDIVIDUAL', label: 'Provider' },
    { value: 'PROVIDER_INSTITUTION', label: 'Institution' },
  ];

  return (
    <SafeAreaView style={styles.container}>
      <KeyboardAvoidingView
        behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
        style={styles.keyboardView}
      >
        <ScrollView style={styles.scrollView} showsVerticalScrollIndicator={false}>
          <View style={styles.header}>
            <Title style={styles.title}>Create Account</Title>
            <Paragraph style={styles.subtitle}>
              Join the GynAid community
            </Paragraph>
          </View>

          <Card style={styles.card}>
            <Card.Content>
              {error ? (
                <Paragraph style={styles.errorText}>{error}</Paragraph>
              ) : null}

              <View style={styles.row}>
                <TextInput
                  label="First Name"
                  value={firstName}
                  onChangeText={setFirstName}
                  mode="outlined"
                  style={[styles.input, styles.halfInput]}
                  left={<TextInput.Icon icon="account" />}
                />

                <TextInput
                  label="Last Name"
                  value={lastName}
                  onChangeText={setLastName}
                  mode="outlined"
                  style={[styles.input, styles.halfInput]}
                />
              </View>

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

              <TextInput
                label="Confirm Password"
                value={confirmPassword}
                onChangeText={setConfirmPassword}
                mode="outlined"
                secureTextEntry
                style={styles.input}
                left={<TextInput.Icon icon="lock-check" />}
              />

              <Paragraph style={styles.label}>I am a:</Paragraph>
              <SegmentedButtons
                value={role}
                onValueChange={(value) => setRole(value as UserRole)}
                buttons={roleOptions}
                style={styles.segmentedButtons}
              />

              <Button
                mode="contained"
                onPress={handleRegister}
                style={styles.button}
                disabled={loading}
              >
                {loading ? (
                  <ActivityIndicator color="white" size="small" />
                ) : (
                  'Create Account'
                )}
              </Button>
            </Card.Content>
          </Card>

          <View style={styles.footer}>
            <Paragraph>
              Already have an account?{' '}
              <Paragraph
                style={styles.link}
                onPress={() => navigation.navigate('Login')}
              >
                Sign In
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
  row: {
    flexDirection: 'row',
    justifyContent: 'space-between',
  },
  halfInput: {
    width: '48%',
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
  label: {
    marginBottom: spacing.sm,
    color: theme.colors.onSurface,
  },
  segmentedButtons: {
    marginBottom: spacing.lg,
  },
});

export default RegisterScreen;