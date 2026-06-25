-- ============================================================
-- V1: Create all tables for the Furniture Bid application
-- ============================================================

-- Users table
CREATE TABLE users (
    id          VARCHAR(36)  PRIMARY KEY,
    email       VARCHAR(255) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    display_name VARCHAR(50) NOT NULL,
    role        VARCHAR(20)  NOT NULL DEFAULT 'buyer',
    avatar_url  VARCHAR(2048),
    status      VARCHAR(20)  NOT NULL DEFAULT 'active',
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_status ON users(status);

-- Furniture listings table
CREATE TABLE furniture_listings (
    id                  VARCHAR(36)    PRIMARY KEY,
    title               VARCHAR(100)   NOT NULL,
    description         TEXT,
    category            VARCHAR(30)    NOT NULL,
    condition           VARCHAR(20)    NOT NULL,
    brand               VARCHAR(100),
    material            VARCHAR(100),
    dimension_width     DOUBLE,
    dimension_height    DOUBLE,
    dimension_length    DOUBLE,
    weight              DOUBLE,
    location            VARCHAR(255),
    starting_price      DECIMAL(12,2)  NOT NULL,
    reserve_price       DECIMAL(12,2),
    current_bid         DECIMAL(12,2),
    bid_count           INT            NOT NULL DEFAULT 0,
    auction_end_date    TIMESTAMP      NOT NULL,
    status              VARCHAR(20)    NOT NULL DEFAULT 'active',
    seller_id           VARCHAR(36)    NOT NULL,
    seller_display_name VARCHAR(50),
    seller_rating       DOUBLE         DEFAULT 0.0,
    created_at          TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_listing_seller FOREIGN KEY (seller_id) REFERENCES users(id)
);

CREATE INDEX idx_listings_status ON furniture_listings(status);
CREATE INDEX idx_listings_category ON furniture_listings(category);
CREATE INDEX idx_listings_seller ON furniture_listings(seller_id);
CREATE INDEX idx_listings_end_date ON furniture_listings(auction_end_date);

-- Listing images (separate table for the List<String> images)
CREATE TABLE listing_images (
    id          VARCHAR(36)  PRIMARY KEY,
    listing_id  VARCHAR(36)  NOT NULL,
    image_url   VARCHAR(2048) NOT NULL,
    sort_order  INT          NOT NULL DEFAULT 0,
    CONSTRAINT fk_image_listing FOREIGN KEY (listing_id) REFERENCES furniture_listings(id) ON DELETE CASCADE
);

CREATE INDEX idx_images_listing ON listing_images(listing_id);

-- Bids table
CREATE TABLE bids (
    id           VARCHAR(36)   PRIMARY KEY,
    auction_id   VARCHAR(36)   NOT NULL,
    bidder_id    VARCHAR(36)   NOT NULL,
    bidder_alias VARCHAR(50),
    amount       DECIMAL(12,2) NOT NULL,
    timestamp    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_bid_auction FOREIGN KEY (auction_id) REFERENCES furniture_listings(id),
    CONSTRAINT fk_bid_bidder FOREIGN KEY (bidder_id) REFERENCES users(id)
);

CREATE INDEX idx_bids_auction ON bids(auction_id);
CREATE INDEX idx_bids_bidder ON bids(bidder_id);
CREATE INDEX idx_bids_timestamp ON bids(timestamp DESC);

-- Watchlist (user <-> listing many-to-many)
CREATE TABLE watchlist (
    user_id    VARCHAR(36) NOT NULL,
    listing_id VARCHAR(36) NOT NULL,
    added_at   TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, listing_id),
    CONSTRAINT fk_watchlist_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_watchlist_listing FOREIGN KEY (listing_id) REFERENCES furniture_listings(id) ON DELETE CASCADE
);

-- Notifications table
CREATE TABLE notifications (
    id         VARCHAR(36)  PRIMARY KEY,
    user_id    VARCHAR(36)  NOT NULL,
    type       VARCHAR(30)  NOT NULL,
    title      VARCHAR(255) NOT NULL,
    message    TEXT,
    auction_id VARCHAR(36),
    is_read    BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_notifications_user ON notifications(user_id);
CREATE INDEX idx_notifications_created ON notifications(created_at DESC);

-- Payments table
CREATE TABLE payments (
    id            VARCHAR(36)   PRIMARY KEY,
    user_id       VARCHAR(36)   NOT NULL,
    auction_id    VARCHAR(36)   NOT NULL,
    client_secret VARCHAR(255)  NOT NULL,
    amount        DECIMAL(12,2) NOT NULL,
    currency      VARCHAR(3)    NOT NULL DEFAULT 'USD',
    status        VARCHAR(30)   NOT NULL DEFAULT 'requires_payment_method',
    created_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_payment_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_payment_auction FOREIGN KEY (auction_id) REFERENCES furniture_listings(id)
);

CREATE INDEX idx_payments_user ON payments(user_id);
CREATE INDEX idx_payments_created ON payments(created_at DESC);

-- Auto-bid configuration table
CREATE TABLE auto_bids (
    user_id    VARCHAR(36)   NOT NULL,
    auction_id VARCHAR(36)   NOT NULL,
    max_amount DECIMAL(12,2) NOT NULL,
    created_at TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, auction_id),
    CONSTRAINT fk_autobid_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_autobid_auction FOREIGN KEY (auction_id) REFERENCES furniture_listings(id) ON DELETE CASCADE
);

-- Listing reports table
CREATE TABLE listing_reports (
    id                    VARCHAR(36)  PRIMARY KEY,
    listing_id            VARCHAR(36)  NOT NULL,
    reason                VARCHAR(500) NOT NULL,
    reporter_id           VARCHAR(36)  NOT NULL,
    reporter_display_name VARCHAR(50),
    report_date           TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_report_listing FOREIGN KEY (listing_id) REFERENCES furniture_listings(id) ON DELETE CASCADE,
    CONSTRAINT fk_report_reporter FOREIGN KEY (reporter_id) REFERENCES users(id)
);

CREATE INDEX idx_reports_listing ON listing_reports(listing_id);
CREATE INDEX idx_reports_date ON listing_reports(report_date DESC);
