CREATE TABLE businesses (
    phone_number VARCHAR(32) NOT NULL,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(320) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (phone_number)
);

CREATE TABLE transactions (
    id CHAR(36) CHARACTER SET ascii COLLATE ascii_bin NOT NULL DEFAULT (UUID()),
    business_id VARCHAR(32) NOT NULL,
    price DECIMAL(19, 2) NOT NULL,
    tip DECIMAL(19, 2) NULL,
    name VARCHAR(255) NULL,
    business_date DATE NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_transactions_business
        FOREIGN KEY (business_id) REFERENCES businesses (phone_number),
    INDEX idx_transactions_business_date (business_id, business_date)
);

CREATE TABLE expenses (
    id CHAR(36) CHARACTER SET ascii COLLATE ascii_bin NOT NULL DEFAULT (UUID()),
    business_id VARCHAR(32) NOT NULL,
    cost DECIMAL(19, 2) NOT NULL,
    reason VARCHAR(255) NULL,
    business_date DATE NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_expenses_business
        FOREIGN KEY (business_id) REFERENCES businesses (phone_number),
    INDEX idx_expenses_business_date (business_id, business_date)
);

CREATE TABLE payouts (
    id CHAR(36) CHARACTER SET ascii COLLATE ascii_bin NOT NULL DEFAULT (UUID()),
    business_id VARCHAR(32) NOT NULL,
    amount DECIMAL(19, 2) NOT NULL,
    business_date DATE NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_payouts_business
        FOREIGN KEY (business_id) REFERENCES businesses (phone_number),
    INDEX idx_payouts_business_date (business_id, business_date)
);

CREATE TABLE reports (
    id CHAR(36) CHARACTER SET ascii COLLATE ascii_bin NOT NULL DEFAULT (UUID()),
    business_id VARCHAR(32) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_reports_business
        FOREIGN KEY (business_id) REFERENCES businesses (phone_number),
    INDEX idx_reports_business (business_id)
);

CREATE TABLE `undos` (
    id CHAR(36) CHARACTER SET ascii COLLATE ascii_bin NOT NULL DEFAULT (UUID()),
    business_id VARCHAR(32) NOT NULL,
    entry_type VARCHAR(16) NOT NULL,
    entry_id CHAR(36) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_undos_business
        FOREIGN KEY (business_id) REFERENCES businesses (phone_number),
    CONSTRAINT chk_undos_entry_type
        CHECK (entry_type IN ('TRANSACTION', 'EXPENSE', 'PAYOUT')),
    INDEX idx_undos_business (business_id),
    INDEX idx_undos_entry (entry_type, entry_id)
);
