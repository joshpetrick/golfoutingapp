import { StatusBar } from 'expo-status-bar';
import { useMemo, useState } from 'react';
import {
  ActivityIndicator,
  Alert,
  FlatList,
  Linking,
  SafeAreaView,
  StyleSheet,
  Text,
  TextInput,
  TouchableOpacity,
  View,
} from 'react-native';
import { EventResponse, createApiClient } from './src/api';

const API_BASE_URL = 'http://10.0.2.2:8080';

export default function App() {
  const api = useMemo(() => createApiClient(API_BASE_URL), []);
  const [email, setEmail] = useState('staff@demo.com');
  const [password, setPassword] = useState('password123');
  const [token, setToken] = useState<string | null>(null);
  const [events, setEvents] = useState<EventResponse[]>([]);
  const [loading, setLoading] = useState(false);

  const login = async () => {
    setLoading(true);
    try {
      const response = await api.login(email, password);
      setToken(response.token);
      Alert.alert('Logged in', 'JWT received. You can now browse and register for events.');
    } catch (error) {
      Alert.alert('Login failed', String(error));
    } finally {
      setLoading(false);
    }
  };

  const loadEvents = async () => {
    setLoading(true);
    try {
      const publishedEvents = await api.listEvents();
      setEvents(publishedEvents);
    } catch (error) {
      Alert.alert('Failed to load events', String(error));
    } finally {
      setLoading(false);
    }
  };

  const registerForEvent = async (eventId: number) => {
    setLoading(true);
    try {
      const checkout = await api.createRegistration(eventId, 'Mobile User', email, 1);
      await Linking.openURL(checkout.checkoutUrl);
    } catch (error) {
      Alert.alert('Registration failed', String(error));
    } finally {
      setLoading(false);
    }
  };

  return (
    <SafeAreaView style={styles.container}>
      <StatusBar style="auto" />
      <Text style={styles.heading}>Golf Outing Mobile</Text>
      <Text style={styles.subheading}>API: {API_BASE_URL}</Text>

      <View style={styles.card}>
        <Text style={styles.cardTitle}>Login (staff demo user)</Text>
        <TextInput style={styles.input} autoCapitalize="none" value={email} onChangeText={setEmail} />
        <TextInput style={styles.input} secureTextEntry value={password} onChangeText={setPassword} />
        <TouchableOpacity style={styles.button} onPress={login}>
          <Text style={styles.buttonLabel}>Sign in</Text>
        </TouchableOpacity>
        {token ? <Text style={styles.tokenState}>JWT stored in memory ✅</Text> : null}
      </View>

      <TouchableOpacity style={styles.button} onPress={loadEvents}>
        <Text style={styles.buttonLabel}>Load published events</Text>
      </TouchableOpacity>

      {loading ? <ActivityIndicator size="large" style={styles.loading} /> : null}

      <FlatList
        data={events}
        keyExtractor={(item) => item.id.toString()}
        renderItem={({ item }) => (
          <View style={styles.eventCard}>
            <Text style={styles.eventTitle}>{item.title}</Text>
            <Text>{new Date(item.startTime).toLocaleString()}</Text>
            <Text>
              ${(item.priceCents / 100).toFixed(2)} {item.currency.toUpperCase()}
            </Text>
            <TouchableOpacity style={styles.secondaryButton} onPress={() => registerForEvent(item.id)}>
              <Text style={styles.buttonLabel}>Register + Stripe Checkout</Text>
            </TouchableOpacity>
          </View>
        )}
      />
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f4f7fb',
    padding: 16,
    gap: 10,
  },
  heading: {
    fontSize: 28,
    fontWeight: '700',
  },
  subheading: {
    color: '#667085',
    marginBottom: 8,
  },
  card: {
    backgroundColor: '#fff',
    borderRadius: 8,
    padding: 12,
    gap: 8,
  },
  cardTitle: {
    fontWeight: '600',
    fontSize: 16,
  },
  input: {
    borderColor: '#d0d5dd',
    borderWidth: 1,
    borderRadius: 6,
    padding: 10,
    backgroundColor: '#fff',
  },
  button: {
    backgroundColor: '#1d4ed8',
    borderRadius: 8,
    padding: 12,
    alignItems: 'center',
    marginTop: 4,
  },
  secondaryButton: {
    backgroundColor: '#2563eb',
    borderRadius: 8,
    padding: 10,
    alignItems: 'center',
    marginTop: 8,
  },
  buttonLabel: {
    color: '#fff',
    fontWeight: '600',
  },
  tokenState: {
    color: '#067647',
    fontWeight: '600',
  },
  loading: {
    marginVertical: 8,
  },
  eventCard: {
    backgroundColor: '#fff',
    marginTop: 8,
    padding: 12,
    borderRadius: 8,
    gap: 4,
  },
  eventTitle: {
    fontWeight: '700',
    fontSize: 16,
  },
});
