INSERT INTO users(name, email, password_hash) VALUES
('Demo Staff', 'staff@demo.com', '$2a$10$jE6f4dCZij6Vf6NAAzjYkeuM66zCFAaj6MqFmoVoeAQMtk0iA0W2y'); -- password123

INSERT INTO courses(name, timezone, status, address_line1, city, state, postal_code) VALUES
('Pine Valley Demo Course', 'America/New_York', 'ACTIVE', '123 Fairway Ln', 'Pineville', 'NJ', '08001');

INSERT INTO course_members(course_id, user_id, role) VALUES (1, 1, 'COURSE_ADMIN');
INSERT INTO subscriptions(course_id, status) VALUES (1, 'INCOMPLETE');

INSERT INTO outing_events(course_id, title, description, start_time, signup_deadline, capacity, price_cents, currency, status)
VALUES
(1, 'Spring Charity Scramble', 'Fundraiser event', now() + interval '30 day', now() + interval '25 day', 80, 15000, 'usd', 'PUBLISHED'),
(1, 'Member Guest Classic', 'Invite-only pairs event', now() + interval '45 day', now() + interval '40 day', 48, 20000, 'usd', 'DRAFT');
