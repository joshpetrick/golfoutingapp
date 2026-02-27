export type AuthResponse = { token: string };

export type EventResponse = {
  id: number;
  courseId: number;
  title: string;
  description: string | null;
  startTime: string;
  signupDeadline: string;
  capacity: number;
  priceCents: number;
  currency: string;
  status: 'DRAFT' | 'PUBLISHED' | 'CANCELLED';
};

export type RegistrationCheckoutResponse = {
  registrationId: number;
  checkoutSessionId: string;
  checkoutUrl: string;
};

export type ApiClient = ReturnType<typeof createApiClient>;

export function createApiClient(baseUrl: string) {
  const apiFetch = async <T>(path: string, init: RequestInit = {}): Promise<T> => {
    const response = await fetch(`${baseUrl}${path}`, {
      ...init,
      headers: {
        'Content-Type': 'application/json',
        ...(init.headers ?? {}),
      },
    });

    if (!response.ok) {
      const message = await response.text();
      throw new Error(message || `HTTP ${response.status}`);
    }

    return response.json() as Promise<T>;
  };

  return {
    login: (email: string, password: string) =>
      apiFetch<AuthResponse>('/api/auth/login', {
        method: 'POST',
        body: JSON.stringify({ email, password }),
      }),
    listEvents: () => apiFetch<EventResponse[]>('/api/events'),
    createRegistration: (eventId: number, attendeeName: string, attendeeEmail: string, spots: number) =>
      apiFetch<RegistrationCheckoutResponse>(`/api/events/${eventId}/registrations`, {
        method: 'POST',
        body: JSON.stringify({ attendeeName, attendeeEmail, spots }),
      }),
  };
}
