
-- Таблица пользователей
CREATE TABLE users (
    user_id SERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$'),
    password_hash VARCHAR(255) NOT NULL,
    user_name VARCHAR(100) NOT NULL
);

-- Таблица аккаунтов
CREATE TABLE accounts (
    account_id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES users(user_id),
    num_id INT NOT NULL,
    number VARCHAR(50) NOT NULL
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
    name VARCHAR(255) NOT NULL,
    bic VARCHAR(9) NOT NULL CHECK (bic ~ '^[0-9]{9}$'),
    address VARCHAR(255)
);

-- Типы юридических лиц
CREATE TABLE legal_types (
    legal_type_id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);

-- Получатели
CREATE TABLE recipients (
    recipient_id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    category_id INT NOT NULL REFERENCES categories(category_id),
    bank_id INT NOT NULL REFERENCES banks(bank_id),
    legal_type_id INT NOT NULL REFERENCES legal_types(legal_type_id),
    inn VARCHAR(11) NOT NULL CHECK (inn ~ '^[0-9]{11}$'),
    payment_account VARCHAR(50),
    phone VARCHAR(16) CHECK (phone ~ '^(\+7|8)[0-9]{10}$')
);

-- Транзакции
CREATE TABLE transactions (
    transaction_id SERIAL PRIMARY KEY,
    type_id INT NOT NULL REFERENCES transaction_types(type_id),
    amount DECIMAL(12, 2) NOT NULL,
    comment TEXT,
    status_id INT NOT NULL REFERENCES transaction_status(status_id),
    created_at TIMESTAMP NOT NULL,
    user_id INT NOT NULL REFERENCES users(user_id),
    account_id INT NOT NULL REFERENCES accounts(account_id),
    recipient_id INT NOT NULL REFERENCES recipients(recipient_id)
);
