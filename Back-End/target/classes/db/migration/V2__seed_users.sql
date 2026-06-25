-- ============================================================
-- V2: Seed initial users
-- ============================================================
-- Passwords are stored as plain text for this mock/dev service.
-- In production, use BCrypt or similar hashing.

INSERT INTO users (id, email, password, display_name, role, avatar_url, status, created_at)
VALUES
    ('', 'lslingkesh@gmail.com', 'foo123', 'Lingkesh Admin', 'admin', NULL, 'active', NOW()),
    ('', 'lingkeshra.rajendram@gmail.com', 'foo123', 'Lingkesh Seller', 'seller', NULL, 'active', NOW()),
    ('', 'lingkeshra@gmail.com', 'foo123', 'Lingkesh Bidder', 'buyer', NULL, 'active', NOW());
