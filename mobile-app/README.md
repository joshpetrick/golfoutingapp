# Golf Outing Mobile (Expo)

A starter React Native app for Android + iOS that consumes the backend endpoints already available in this repository.

## Features included
- Login against `POST /api/auth/login`
- Load published events from `GET /api/events`
- Create event registration via `POST /api/events/{eventId}/registrations`
- Open Stripe checkout URL in mobile browser

## Setup
1. Install dependencies:
   ```bash
   cd mobile-app
   npm install
   ```
2. Run backend API from repo root on port 8080.
3. Start Expo:
   ```bash
   npm run start
   ```
4. Press `a` for Android emulator or `i` for iOS simulator.

## API base URL
The app currently points to:
- `http://10.0.2.2:8080` (works for Android emulator to reach host machine)

If you run on a physical device or iOS simulator, update `API_BASE_URL` in `App.tsx`.

## Notes
- JWT is fetched and stored in memory in this MVP scaffold.
- Staff-only endpoints (like event creation/publish) are not wired yet; this app focuses on player-facing browsing/registration flow.
