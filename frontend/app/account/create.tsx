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
import { useRouter } from 'expo-router';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { getThemeColors } from '@/src/constants/colors';
import { createAccount } from '@/src/services/portfolioService';

export default function CreateAccountScreen() {
  const colorScheme = useColorScheme();
  const Colors = getThemeColors(colorScheme);
  const router = useRouter();

  const [accountName, setAccountName] = useState('');
  const [initialBalance, setInitialBalance] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async () => {
    // Validation
    if (!accountName.trim()) {
      Alert.alert('Error', 'Please enter an account name');
      return;
    }

    const balance = initialBalance ? parseFloat(initialBalance) : 0;

    if (initialBalance && (isNaN(balance) || balance < 0)) {
      Alert.alert('Error', 'Please enter a valid initial balance');
      return;
    }

    setLoading(true);

    try {
      const newAccount = await createAccount({
        accountName: accountName.trim(),
        cashBalance: balance,
      });

      Alert.alert('Success', `Account "${accountName}" created successfully!`, [
        {
          text: 'OK',
          onPress: () => {
            // Navigate back to profile
            router.back();
          },
        },
      ]);
    } catch (error: any) {
      Alert.alert(
        'Error',
        error.message || 'Failed to create account. Please try again.'
      );
    } finally {
      setLoading(false);
    }
  };

  const formatCurrency = (value: string) => {
    // Remove non-numeric characters except decimal point
    const cleaned = value.replace(/[^0-9.]/g, '');

    // Ensure only one decimal point
    const parts = cleaned.split('.');
    if (parts.length > 2) {
      return parts[0] + '.' + parts.slice(1).join('');
    }

    return cleaned;
  };

  const handleBalanceChange = (text: string) => {
    const formatted = formatCurrency(text);
    setInitialBalance(formatted);
  };

  return (
    <KeyboardAvoidingView
      style={[styles.container, { backgroundColor: Colors.background }]}
      behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
    >
      {/* Header */}
      <View style={[styles.header, { borderBottomColor: Colors.border }]}>
        <TouchableOpacity
          onPress={() => router.back()}
          style={[
            styles.backButton,
            { backgroundColor: Colors.card, borderColor: Colors.border },
          ]}
          disabled={loading}
        >
          <MaterialCommunityIcons
            name="chevron-left"
            size={28}
            color={Colors.text}
          />
        </TouchableOpacity>
        <Text style={[styles.title, { color: Colors.text }]}>
          Create New Account
        </Text>
        <View style={{ width: 44 }} />
      </View>

      <ScrollView
        contentContainerStyle={styles.scrollContent}
        showsVerticalScrollIndicator={false}
        keyboardShouldPersistTaps="handled"
      >
        {/* Info Card */}
        <View
          style={[
            styles.infoCard,
            { backgroundColor: Colors.tint + '15', borderColor: Colors.tint + '30' },
          ]}
        >
          <MaterialCommunityIcons
            name="information-outline"
            size={20}
            color={Colors.tint}
          />
          <Text style={[styles.infoText, { color: Colors.tint }]}>
            Create multiple accounts to track different investment portfolios separately
          </Text>
        </View>

        {/* Form */}
        <View style={styles.formContainer}>
          {/* Account Name */}
          <View style={styles.inputContainer}>
            <Text style={[styles.label, { color: Colors.text }]}>
              Account Name <Text style={{ color: '#C62828' }}>*</Text>
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
                placeholder="e.g., Main Portfolio, Retirement Account"
                placeholderTextColor={Colors.text + '80'}
                value={accountName}
                onChangeText={setAccountName}
                editable={!loading}
                maxLength={50}
              />
            </View>
            <Text style={[styles.helperText, { color: Colors.text, opacity: 0.5 }]}>
              Choose a name to identify this account
            </Text>
          </View>

          {/* Initial Balance */}
          <View style={styles.inputContainer}>
            <Text style={[styles.label, { color: Colors.text }]}>
              Initial Cash Balance (Optional)
            </Text>
            <View
              style={[
                styles.inputWrapper,
                { backgroundColor: Colors.card, borderColor: Colors.border },
              ]}
            >
              <Text style={[styles.currencySymbol, { color: Colors.text }]}>
                A$
              </Text>
              <TextInput
                style={[styles.input, { color: Colors.text }]}
                placeholder="0.00"
                placeholderTextColor={Colors.text + '80'}
                value={initialBalance}
                onChangeText={handleBalanceChange}
                keyboardType="decimal-pad"
                editable={!loading}
              />
            </View>
            <Text style={[styles.helperText, { color: Colors.text, opacity: 0.5 }]}>
              Enter the starting cash balance for this account
            </Text>
          </View>

          {/* Account Types - Visual Guide */}
          <View style={styles.examplesContainer}>
            <Text style={[styles.examplesTitle, { color: Colors.text }]}>
              Account Examples:
            </Text>
            <View style={styles.examplesList}>
              {[
                { icon: 'chart-line', name: 'Main Portfolio', desc: 'Your primary investment account' },
                { icon: 'piggy-bank', name: 'Retirement Account', desc: 'Long-term savings and investments' },
                { icon: 'briefcase', name: 'Trading Account', desc: 'Active trading and day trading' },
                { icon: 'home', name: 'Real Estate', desc: 'Property and REIT investments' },
              ].map((example, index) => (
                <View
                  key={index}
                  style={[
                    styles.exampleCard,
                    { backgroundColor: Colors.card, borderColor: Colors.border },
                  ]}
                >
                  <View
                    style={[
                      styles.exampleIcon,
                      { backgroundColor: Colors.tint + '20' },
                    ]}
                  >
                    <MaterialCommunityIcons
                      name={example.icon as any}
                      size={20}
                      color={Colors.tint}
                    />
                  </View>
                  <View style={styles.exampleInfo}>
                    <Text style={[styles.exampleName, { color: Colors.text }]}>
                      {example.name}
                    </Text>
                    <Text
                      style={[
                        styles.exampleDesc,
                        { color: Colors.text, opacity: 0.6 },
                      ]}
                    >
                      {example.desc}
                    </Text>
                  </View>
                </View>
              ))}
            </View>
          </View>
        </View>

        {/* Create Button */}
        <TouchableOpacity
          style={[
            styles.createButton,
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
              <MaterialCommunityIcons
                name="plus-circle"
                size={20}
                color="white"
              />
              <Text style={styles.createButtonText}>Create Account</Text>
            </>
          )}
        </TouchableOpacity>

        <View style={{ height: 40 }} />
      </ScrollView>
    </KeyboardAvoidingView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: 12,
    paddingTop: 60,
    paddingBottom: 16,
    borderBottomWidth: 1,
  },
  backButton: {
    width: 44,
    height: 44,
    borderRadius: 10,
    borderWidth: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  title: {
    fontSize: 18,
    fontWeight: '800',
    fontStyle: 'italic',
  },
  scrollContent: {
    paddingHorizontal: 24,
    paddingTop: 24,
  },
  infoCard: {
    flexDirection: 'row',
    alignItems: 'flex-start',
    gap: 12,
    padding: 16,
    borderRadius: 12,
    borderWidth: 1,
    marginBottom: 24,
  },
  infoText: {
    flex: 1,
    fontSize: 12,
    fontWeight: '600',
    lineHeight: 18,
  },
  formContainer: {
    gap: 24,
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
  currencySymbol: {
    fontSize: 14,
    fontWeight: '700',
    opacity: 0.7,
  },
  helperText: {
    fontSize: 11,
    fontWeight: '500',
    paddingLeft: 4,
  },
  examplesContainer: {
    gap: 12,
    marginTop: 8,
  },
  examplesTitle: {
    fontSize: 13,
    fontWeight: '700',
    paddingLeft: 4,
  },
  examplesList: {
    gap: 10,
  },
  exampleCard: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 12,
    padding: 12,
    borderRadius: 10,
    borderWidth: 1,
  },
  exampleIcon: {
    width: 40,
    height: 40,
    borderRadius: 10,
    alignItems: 'center',
    justifyContent: 'center',
  },
  exampleInfo: {
    flex: 1,
    gap: 2,
  },
  exampleName: {
    fontSize: 12,
    fontWeight: '700',
  },
  exampleDesc: {
    fontSize: 10,
    fontWeight: '500',
  },
  createButton: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 8,
    paddingVertical: 16,
    borderRadius: 12,
    marginTop: 32,
  },
  createButtonText: {
    color: 'white',
    fontSize: 15,
    fontWeight: '700',
  },
});
