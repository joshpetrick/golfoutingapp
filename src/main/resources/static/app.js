const flash = document.getElementById('flash');
const authState = document.getElementById('auth-state');
const tokenInput = document.getElementById('token-input');
const eventsList = document.getElementById('events-list');
const checkoutResult = document.getElementById('checkout-result');

const tokenStorageKey = 'golfouting-token';
const savedToken = localStorage.getItem(tokenStorageKey);
if (savedToken) {
  tokenInput.value = savedToken;
  authState.textContent = 'Token loaded from previous session.';
}

function toIsoFromLocalDateTime(value) {
  return new Date(value).toISOString();
}

function setFlash(message, type = 'success') {
  flash.className = type;
  flash.textContent = message;
}

async function api(path, options = {}) {
  const includeAuth = options.includeAuth !== false;
  const headers = {
    'Content-Type': 'application/json',
    ...(options.headers || {})
  };
  const token = tokenInput.value.trim();
  if (includeAuth && token) headers.Authorization = `Bearer ${token}`;

  const { includeAuth: _includeAuth, ...fetchOptions } = options;
  const response = await fetch(path, { ...fetchOptions, headers });
  if (!response.ok) {
    let message = `Request failed (${response.status})`;
    try {
      const body = await response.json();
      message = body.message || message;
    } catch (_) {}
    throw new Error(message);
  }
  if (response.status === 204) return null;
  return response.json();
}

function persistToken(token) {
  tokenInput.value = token;
  localStorage.setItem(tokenStorageKey, token);
  authState.textContent = 'Authenticated token saved to browser storage.';
}

document.getElementById('register-form').addEventListener('submit', async (event) => {
  event.preventDefault();
  const form = new FormData(event.target);
  try {
    const response = await api('/api/auth/register', {
      method: 'POST',
      body: JSON.stringify(Object.fromEntries(form.entries()))
    });
    persistToken(response.token);
    setFlash('Registration complete. You are now signed in.');
  } catch (error) {
    setFlash(error.message, 'error');
  }
});

document.getElementById('login-form').addEventListener('submit', async (event) => {
  event.preventDefault();
  const form = new FormData(event.target);
  try {
    const response = await api('/api/auth/login', {
      method: 'POST',
      body: JSON.stringify(Object.fromEntries(form.entries()))
    });
    persistToken(response.token);
    setFlash('Welcome back.');
  } catch (error) {
    setFlash(error.message, 'error');
  }
});

document.getElementById('course-form').addEventListener('submit', async (event) => {
  event.preventDefault();
  const form = new FormData(event.target);
  try {
    const response = await api('/api/courses', {
      method: 'POST',
      body: JSON.stringify(Object.fromEntries(form.entries()))
    });
    setFlash(`Course created with ID ${response.id}.`);
  } catch (error) {
    setFlash(error.message, 'error');
  }
});

document.getElementById('event-form').addEventListener('submit', async (event) => {
  event.preventDefault();
  const form = Object.fromEntries(new FormData(event.target).entries());
  const courseId = form.courseId;
  const payload = {
    title: form.title,
    description: form.description,
    startTime: toIsoFromLocalDateTime(form.startTime),
    signupDeadline: toIsoFromLocalDateTime(form.signupDeadline),
    capacity: Number(form.capacity),
    priceCents: Number(form.priceCents),
    currency: form.currency
  };

  try {
    const response = await api(`/api/courses/${courseId}/events`, {
      method: 'POST',
      body: JSON.stringify(payload)
    });
    setFlash(`Event created with ID ${response.id}. Publish from your API workflow when ready.`);
  } catch (error) {
    setFlash(error.message, 'error');
  }
});

async function loadEvents() {
  try {
    const events = await api('/api/events', { includeAuth: false });
    if (!events.length) {
      eventsList.innerHTML = '<p class="muted">No published events yet.</p>';
      return;
    }

    eventsList.innerHTML = events
      .map((evt) => `
      <article class="event-item">
        <h4>${evt.title}</h4>
        <p class="event-meta">Event #${evt.id} · Course #${evt.courseId}</p>
        <p class="event-meta">Starts ${new Date(evt.startTime).toLocaleString()} · Capacity ${evt.capacity} · ${(evt.priceCents / 100).toLocaleString(undefined, { style: 'currency', currency: evt.currency.toUpperCase() })}</p>
      </article>`)
      .join('');
  } catch (error) {
    setFlash(`Could not load events: ${error.message}`, 'error');
  }
}

document.getElementById('refresh-events').addEventListener('click', loadEvents);

document.getElementById('registration-form').addEventListener('submit', async (event) => {
  event.preventDefault();
  const form = Object.fromEntries(new FormData(event.target).entries());
  const payload = {
    attendeeName: form.attendeeName,
    attendeeEmail: form.attendeeEmail,
    spots: Number(form.spots)
  };

  try {
    const response = await api(`/api/events/${form.eventId}/registrations`, {
      method: 'POST',
      body: JSON.stringify(payload)
    });
    checkoutResult.innerHTML = `Registration #${response.registrationId}. Checkout session: <strong>${response.checkoutSessionId || 'n/a'}</strong>${response.checkoutUrl ? ` · <a href="${response.checkoutUrl}" target="_blank" rel="noreferrer">Open checkout</a>` : ''}`;
    setFlash('Registration created successfully.');
  } catch (error) {
    setFlash(error.message, 'error');
  }
});

loadEvents();
