## Admin ID : 
## Admin Password :

DB Scehma 

1. Customer Table

CREATE TABLE customers (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    gender VARCHAR(10) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password TEXT NOT NULL,
    phone VARCHAR(15) NOT NULL,
    address TEXT NOT NULL,
    national_id VARCHAR(20) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', 
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

2. Account Table

CREATE TABLE accounts (
    id BIGSERIAL PRIMARY KEY,
    account_number VARCHAR(20) NOT NULL UNIQUE,
    account_type VARCHAR(20) NOT NULL, -- Enum (SAVINGS, CURRENT)
    balance NUMERIC(15,2) NOT NULL DEFAULT 0.00,
    interest_rate NUMERIC(5,2) NOT NULL,
    accrued_interest NUMERIC(15,2) DEFAULT 0.00,
    last_interest_calculated TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', 
    customer_id BIGINT NOT NULL, 
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_customer FOREIGN KEY (customer_id) REFERENCES customers(id)
);

3. Transaction Table

CREATE TABLE transactions (
    id BIGSERIAL PRIMARY KEY,
    transaction_id VARCHAR(50) NOT NULL UNIQUE,
    type VARCHAR(30) NOT NULL, -- Enum (DEPOSIT, WITHDRAWAL, TRANSFER, INTEREST_CREDIT)
    amount NUMERIC(15,2) NOT NULL,
    description VARCHAR(200),
    from_account_id BIGINT, -- FK to accounts(id)
    to_account_id BIGINT,   -- FK to accounts(id)
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    CONSTRAINT fk_from_account FOREIGN KEY (from_account_id) REFERENCES accounts(id),
    CONSTRAINT fk_to_account FOREIGN KEY (to_account_id) REFERENCES accounts(id)
);
  
<img width="451" height="640" alt="image" src="https://github.com/user-attachments/assets/689a6d52-b0a7-482d-8710-99f16cff3a55" />

