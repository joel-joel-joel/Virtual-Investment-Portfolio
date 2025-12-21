import React, { useState } from 'react';
import {
  View,
  Text,
  TextInput,
  TouchableOpacity,
  StyleSheet,
  useColorScheme,
  Alert,
  KeyboardAvoidingView,
  Platform,
  ScrollView,
  ActivityIndicator,
} from 'react-native';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { useTheme } from '@/src/context/ThemeContext';
import { login as apiLogin, register as apiRegister } from '@/src/services/authService';
import { useAuth } from '@/src/context/AuthContext';

export default function LoginScreen() {
  const {Colors} = useTheme();
  const { login } = useAuth();

  const [isLogin, setIsLogin] = useState(true);
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [loading, setLoading] = useState(false);


    const handleSubmit = async () => {
        // Basic validation
        if (!email || !password || (!isLogin && (!firstName || !lastName))) {
            Alert.alert('Error', 'Please fill in all required fields');
            return;
        }

        // Email format validation
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(email)) {
            Alert.alert('Error', 'Please enter a valid email address');
            return;
        }

        // Password length validation (match backend)
        if (password.length < 8) {
            Alert.alert('Error', 'Password must be at least 8 characters long');
            return;
        }

        // Confirm password for registration
        if (!isLogin && password !== confirmPassword) {
            Alert.alert('Error', 'Passwords do not match');
            return;
        }

        setLoading(true);

        try {
            if (isLogin) {
                // Login
                const response = await apiLogin({ email, password });
                await login(response.token); // updates context & SecureStore
            } else {
                // Register
                const fullName = `${firstName} ${lastName}`.trim();
                const username = email.split('@')[0]; // simple username generation

                const response = await apiRegister({
                    email,
                    username,
                    password,
                    fullName,
                });

                await login(response.token); // auto-login after registration
            }
        } catch (error: any) {
            Alert.alert('Error', error.message || 'An error occurred. Please try again.');
        } finally {
            setLoading(false);
        }
    };


    const toggleMode = () => {
    setIsLogin(!isLogin);
    // Clear form
    setEmail('');
    setPassword('');
    setConfirmPassword('');
    setFirstName('');
    setLastName('');
  };

  return (
    <KeyboardAvoidingView
      style={[styles.container, { backgroundColor: Colors.background }]}
      behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
    >
      <ScrollView
        contentContainerStyle={styles.scrollContent}
        showsVerticalScrollIndicator={false}
        keyboardShouldPersistTaps="handled"
      >
        {/* Logo/Header */}
        <View style={styles.header}>
          <View style={[styles.logoContainer, { backgroundColor: Colors.tint }]}>
            <MaterialCommunityIcons
              name="chart-line"
              size={48}
              color="white"
            />
          </View>
          <Text style={[styles.title, { color: Colors.text }]}>
            Pegasus
          </Text>
          <Text style={[styles.subtitle, { color: Colors.text, opacity: 0.6 }]}>
            {isLogin ? 'Welcome back!' : 'Create your account'}
          </Text>
        </View>

        {/* Form */}
        <View style={styles.formContainer}>
          {/* Email */}
          <View style={styles.inputContainer}>
            <Text style={[styles.label, { color: Colors.text }]}>Email</Text>
            <View
              style={[
                styles.inputWrapper,
                { backgroundColor: Colors.card, borderColor: Colors.border },
              ]}
            >
              <MaterialCommunityIcons
                name="email-outline"
                size={20}
                color={Colors.text}
                style={{ opacity: 0.5 }}
              />
              <TextInput
                style={[styles.input, { color: Colors.text }]}
                placeholder="Enter your email"
                placeholderTextColor={Colors.text + '80'}
                value={email}
                onChangeText={setEmail}
                autoCapitalize="none"
                keyboardType="email-address"
                editable={!loading}
              />
            </View>
          </View>

          {/* First Name - Register only */}
          {!isLogin && (
            <View style={styles.inputContainer}>
              <Text style={[styles.label, { color: Colors.text }]}>
                First Name
              </Text>
              <View
                style={[
                  styles.inputWrapper,
                  { backgroundColor: Colors.card, borderColor: Colors.border },
                ]}
              >
                <MaterialCommunityIcons
                  name="account-outline"
                  size={20}
                  color={Colors.text}
                  style={{ opacity: 0.5 }}
                />
                <TextInput
                  style={[styles.input, { color: Colors.text }]}
                  placeholder="Enter your first name"
                  placeholderTextColor={Colors.text + '80'}
                  value={firstName}
                  onChangeText={setFirstName}
                  editable={!loading}
                />
              </View>
            </View>
          )}

          {/* Last Name - Register only */}
          {!isLogin && (
            <View style={styles.inputContainer}>
              <Text style={[styles.label, { color: Colors.text }]}>
                Last Name
              </Text>
              <View
                style={[
                  styles.inputWrapper,
                  { backgroundColor: Colors.card, borderColor: Colors.border },
                ]}
              >
                <MaterialCommunityIcons
                  name="account-outline"
                  size={20}
                  color={Colors.text}
                  style={{ opacity: 0.5 }}
                />
                <TextInput
                  style={[styles.input, { color: Colors.text }]}
                  placeholder="Enter your last name"
                  placeholderTextColor={Colors.text + '80'}
                  value={lastName}
                  onChangeText={setLastName}
                  editable={!loading}
                />
              </View>
            </View>
          )}

          {/* Password */}
          <View style={styles.inputContainer}>
            <Text style={[styles.label, { color: Colors.text }]}>Password</Text>
            <View
              style={[
                styles.inputWrapper,
                { backgroundColor: Colors.card, borderColor: Colors.border },
              ]}
            >
              <MaterialCommunityIcons
                name="lock-outline"
                size={20}
                color={Colors.text}
                style={{ opacity: 0.5 }}
              />
              <TextInput
                style={[styles.input, { color: Colors.text }]}
                placeholder="Enter your password"
                placeholderTextColor={Colors.text + '80'}
                value={password}
                onChangeText={setPassword}
                secureTextEntry={!showPassword}
                autoCapitalize="none"
                autoComplete="off"
                editable={!loading}
              />
              <TouchableOpacity
                onPress={() => setShowPassword(!showPassword)}
                disabled={loading}
              >
                <MaterialCommunityIcons
                  name={showPassword ? 'eye-off-outline' : 'eye-outline'}
                  size={20}
                  color={Colors.text}
                  style={{ opacity: 0.5 }}
                />
              </TouchableOpacity>
            </View>
          </View>

          {/* Confirm Password - Register only */}
          {!isLogin && (
            <View style={styles.inputContainer}>
              <Text style={[styles.label, { color: Colors.text }]}>
                Confirm Password
              </Text>
              <View
                style={[
                  styles.inputWrapper,
                  { backgroundColor: Colors.card, borderColor: Colors.border },
                ]}
              >
                <MaterialCommunityIcons
                  name="lock-check-outline"
                  size={20}
                  color={Colors.text}
                  style={{ opacity: 0.5 }}
                />
                <TextInput
                  style={[styles.input, { color: Colors.text }]}
                  placeholder="Confirm your password"
                  placeholderTextColor={Colors.text + '80'}
                  value={confirmPassword}
                  onChangeText={setConfirmPassword}
                  secureTextEntry={!showConfirmPassword}
                  autoCapitalize="none"
                  editable={!loading}
                />
                <TouchableOpacity
                  onPress={() => setShowConfirmPassword(!showConfirmPassword)}
                  disabled={loading}
                >
                  <MaterialCommunityIcons
                    name={
                      showConfirmPassword ? 'eye-off-outline' : 'eye-outline'
                    }
                    size={20}
                    color={Colors.text}
                    style={{ opacity: 0.5 }}
                  />
                </TouchableOpacity>
              </View>
            </View>
          )}

          {/* Forgot Password - Login only */}
          {isLogin && (
            <TouchableOpacity
              onPress={() => Alert.alert('Forgot Password', 'Coming soon!')}
              disabled={loading}
            >
              <Text
                style={[
                  styles.forgotPassword,
                  { color: Colors.tint, textAlign: 'right' },
                ]}
              >
                Forgot Password?
              </Text>
            </TouchableOpacity>
          )}

          {/* Submit Button */}
          <TouchableOpacity
            style={[
              styles.submitButton,
              { backgroundColor: Colors.tint },
              loading && { opacity: 0.7 },
            ]}
            onPress={handleSubmit}
            disabled={loading}
          >
            {loading ? (
              <ActivityIndicator color="white" />
            ) : (
              <>
                <Text style={styles.submitButtonText}>
                  {isLogin ? 'Login' : 'Create Account'}
                </Text>
                <MaterialCommunityIcons
                  name="arrow-right"
                  size={20}
                  color="white"
                />
              </>
            )}
          </TouchableOpacity>

          {/* Toggle Mode */}
          <View style={styles.toggleContainer}>
            <Text style={[styles.toggleText, { color: Colors.text }]}>
              {isLogin ? "Don't have an account?" : 'Already have an account?'}
            </Text>
            <TouchableOpacity onPress={toggleMode} disabled={loading}>
              <Text style={[styles.toggleButton, { color: Colors.tint }]}>
                {isLogin ? 'Register' : 'Login'}
              </Text>
            </TouchableOpacity>
          </View>
        </View>
      </ScrollView>
    </KeyboardAvoidingView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  scrollContent: {
    flexGrow: 1,
    paddingHorizontal: 24,
    paddingTop: 60,
    paddingBottom: 40,
  },
  header: {
    alignItems: 'center',
    marginBottom: 40,
  },
  logoContainer: {
    width: 80,
    height: 80,
    borderRadius: 20,
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: 20,
  },
  title: {
    fontSize: 28,
    fontWeight: '800',
    fontStyle: 'italic',
    marginBottom: 8,
  },
  subtitle: {
    fontSize: 15,
    fontWeight: '500',
  },
  formContainer: {
    gap: 20,
  },
  inputContainer: {
    gap: 8,
  },
  label: {
    fontSize: 13,
    fontWeight: '600',
    paddingLeft: 4,
  },
  inputWrapper: {
    flexDirection: 'row',
    alignItems: 'center',
    borderWidth: 1,
    borderRadius: 12,
    paddingHorizontal: 16,
    gap: 12,
    height: 52,
  },
  input: {
    flex: 1,
    fontSize: 14,
    fontWeight: '500',
  },
  forgotPassword: {
    fontSize: 12,
    fontWeight: '600',
    marginTop: -8,
  },
  submitButton: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 8,
    paddingVertical: 16,
    borderRadius: 12,
    marginTop: 12,
  },
  submitButtonText: {
    color: 'white',
    fontSize: 15,
    fontWeight: '700',
  },
  toggleContainer: {
    flexDirection: 'row',
    justifyContent: 'center',
    alignItems: 'center',
    gap: 6,
    marginTop: 12,
  },
  toggleText: {
    fontSize: 13,
    fontWeight: '500',
  },
  toggleButton: {
    fontSize: 13,
    fontWeight: '700',
  },
});
