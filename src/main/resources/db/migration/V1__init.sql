CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE TABLE courses (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    address_line1 VARCHAR(255),
    city VARCHAR(120),
    state VARCHAR(120),
    postal_code VARCHAR(30),
    timezone VARCHAR(80) NOT NULL,
    status VARCHAR(40) NOT NULL
);

CREATE TABLE course_members (
    id BIGSERIAL PRIMARY KEY,
    course_id BIGINT NOT NULL REFERENCES courses(id),
    user_id BIGINT NOT NULL REFERENCES users(id),
    role VARCHAR(40) NOT NULL
);

CREATE TABLE subscriptions (
    id BIGSERIAL PRIMARY KEY,
    course_id BIGINT NOT NULL UNIQUE REFERENCES courses(id),
    stripe_customer_id VARCHAR(255),
    stripe_subscription_id VARCHAR(255),
    status VARCHAR(40) NOT NULL,
    current_period_end TIMESTAMP WITH TIME ZONE
);

CREATE TABLE outing_events (
    id BIGSERIAL PRIMARY KEY,
    course_id BIGINT NOT NULL REFERENCES courses(id),
    title VARCHAR(255) NOT NULL,
    description VARCHAR(2000),
    start_time TIMESTAMP WITH TIME ZONE NOT NULL,
    signup_deadline TIMESTAMP WITH TIME ZONE NOT NULL,
    capacity INT NOT NULL,
    price_cents INT NOT NULL,
    currency VARCHAR(8) NOT NULL,
    status VARCHAR(40) NOT NULL
);

CREATE TABLE registrations (
    id BIGSERIAL PRIMARY KEY,
    event_id BIGINT NOT NULL REFERENCES outing_events(id),
    user_id BIGINT REFERENCES users(id),
    attendee_name VARCHAR(255) NOT NULL,
    attendee_email VARCHAR(255) NOT NULL,
    spots INT NOT NULL,
    status VARCHAR(40) NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    registration_id BIGINT NOT NULL UNIQUE REFERENCES registrations(id),
    stripe_checkout_session_id VARCHAR(255) UNIQUE,
    amount_cents INT NOT NULL,
    currency VARCHAR(8) NOT NULL,
    status VARCHAR(40) NOT NULL
);

CREATE TABLE stripe_events (
    id VARCHAR(255) PRIMARY KEY,
    type VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE INDEX idx_outing_event_course_start ON outing_events(course_id, start_time);
CREATE INDEX idx_registration_event_status ON registrations(event_id, status);
CREATE INDEX idx_course_member_course_user ON course_members(course_id, user_id);
CREATE INDEX idx_user_email ON users(email);
