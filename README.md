# Golf Outing Platform API (Spring Boot 3 MVP)

Multi-tenant B2B SaaS backend where golf courses manage outing events, players register/pay, and courses subscribe via Stripe Billing.

## Stack
- Java 21, Spring Boot 3
- Spring Web/Security/Data JPA
- PostgreSQL + Flyway
- JWT auth
- Stripe Checkout + webhooks

## Run locally
1. Start Postgres:
   ```bash
   docker compose up -d
   ```
2. Configure env:
   ```bash
   cp .env.example .env
   export $(grep -v '^#' .env | xargs)
   ```
3. Run app:
   ```bash
   ./mvnw spring-boot:run
   ```

## Seed data
- Staff user: `staff@demo.com` / `password123`
- Demo course + memberships + 2 events.

## Key business rules
- Course staff can create/edit course events only within their tenant.
- Publish requires course subscription status `ACTIVE`.
- Registration creates `PENDING` registration and `CREATED` payment.
- `checkout.session.completed` webhook confirms payment and registration only if capacity still allows.
- Duplicate webhooks are ignored using `stripe_events` idempotency table.

## Important endpoints
- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/courses/{courseId}/events`
- `POST /api/courses/{courseId}/events/{eventId}/publish`
- `GET /api/events`
- `POST /api/events/{eventId}/registrations`
- `POST /api/courses/{courseId}/billing/checkout`
- `POST /api/webhooks/stripe`

## cURL examples
Register:
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"name":"Alice","email":"alice@example.com","password":"password123"}'
```

Login:
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"staff@demo.com","password":"password123"}'
```

Create event (staff):
```bash
curl -X POST http://localhost:8080/api/courses/1/events \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"title":"Summer Scramble","description":"charity","startTime":"2026-07-01T12:00:00Z","signupDeadline":"2026-06-20T00:00:00Z","capacity":100,"priceCents":12000,"currency":"usd"}'
```

Create registration checkout:
```bash
curl -X POST http://localhost:8080/api/events/1/registrations \
  -H 'Content-Type: application/json' \
  -d '{"attendeeName":"Guest Player","attendeeEmail":"guest@example.com","spots":2}'
```

Stripe webhook (local testing via Stripe CLI):
```bash
stripe listen --forward-to localhost:8080/api/webhooks/stripe
```

## Notes
- The subscription checkout currently expects a Stripe Price ID from env `STRIPE_SUBSCRIPTION_PRICE_ID` in Stripe account.
- For a production deployment add refresh tokens, stricter RBAC/authority model, and observability/metrics.


## Mobile apps (Android/iOS)
A cross-platform Expo React Native starter app is available in `mobile-app/` and already wired to these endpoints:
- `POST /api/auth/login`
- `GET /api/events`
- `POST /api/events/{eventId}/registrations`

See `mobile-app/README.md` for setup and emulator/device URL notes.
