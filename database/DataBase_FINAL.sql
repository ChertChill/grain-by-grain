-- Таблица пользователей
CREATE TABLE users (
    user_id SERIAL PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$'),
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL
);

-- Типы транзакций
CREATE TABLE transaction_types (
    type_id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);

-- Статусы транзакций
CREATE TABLE transaction_status (
    status_id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    is_final BOOLEAN DEFAULT FALSE
);

-- Категории получателей
CREATE TABLE categories (
    category_id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);

-- Банки
CREATE TABLE banks (
    bank_id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    bic VARCHAR(9) NOT NULL CHECK (bic ~ '^[0-9]{9}$'),
    address VARCHAR(255)
);

-- Типы юридических лиц
CREATE TABLE legal_types (
    legal_type_id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);

CREATE TABLE transactions (
    transaction_id SERIAL PRIMARY KEY,
    type_id INT NOT NULL REFERENCES transaction_types(type_id),
    bank_id INT NOT NULL REFERENCES banks (bank_id)
    amount DECIMAL(12, 2) NOT NULL,
    comment TEXT,
    status_id INT NOT NULL REFERENCES transaction_status(status_id),
    created_at TIMESTAMP NOT NULL,
    user_id BIGINT NOT NULL REFERENCES users(user_id),               
    account_number VARCHAR(50) NOT NULL,                             
    recipient_number VARCHAR(50) NOT NULL,                           
    legal_type_id INT NOT NULL REFERENCES legal_types(legal_type_id),
    transaction_date DATE NOT NULL,                                             
    recipient_tin BIGINT NOT NULL CHECK (recipient_tin >= 10000000000 AND recipient_tin <= 99999999999), 
    category_id INT REFERENCES categories(category_id),              
    recipient_phone VARCHAR(16) CHECK (recipient_phone ~ '^(\+7|8)[0-9]{10}$')           
);

CREATE TABLE audit_logs (
    audit_log_id SERIAL PRIMARY KEY
);

CREATE TABLE integration_logs (
    integration_log_id SERIAL PRIMARY KEY
);