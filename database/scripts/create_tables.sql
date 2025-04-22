-- Создание таблицы пользователей
CREATE TABLE users (
    user_id SERIAL PRIMARY KEY,
    login VARCHAR(20) NOT NULL UNIQUE CHECK (length(login) >= 4 AND length(login) <= 20),
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    username VARCHAR(50) NOT NULL,
	full_name VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE
);

-- Создание таблицы справочника типов лиц
CREATE TABLE user_types (
    user_type_id SERIAL PRIMARY KEY,
    type_name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT
);

-- Создание таблицы справочника типов транзакций
CREATE TABLE transaction_types (
    transaction_type_id SERIAL PRIMARY KEY,
    type_name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT
);

-- Создание таблицы справочника статусов операций
CREATE TABLE operation_statuses (
    status_id SERIAL PRIMARY KEY,
    status_name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    is_editable BOOLEAN DEFAULT TRUE,
    is_deletable BOOLEAN DEFAULT TRUE
);

-- Создание таблицы справочника категорий
CREATE TABLE categories (
    category_id SERIAL PRIMARY KEY,
	user_id INTEGER NOT NULL REFERENCES users(user_id),
    category_name VARCHAR(100) NOT NULL,
    description TEXT,
    is_income BOOLEAN NOT NULL, -- TRUE для доходов, FALSE для расходов
    UNIQUE(category_name, is_income)
);

-- Создание таблицы банков
CREATE TABLE banks (
    bank_id SERIAL PRIMARY KEY,
    bank_name VARCHAR(100) NOT NULL,
    bik VARCHAR(9),
    correspondent_account VARCHAR(20),
    address TEXT,
    UNIQUE(bank_name, bik)
);

-- Создание таблицы счетов пользователей
CREATE TABLE user_accounts (
    account_id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(user_id),
    bank_id INTEGER NOT NULL REFERENCES banks(bank_id),
    account_number VARCHAR(20) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'RUB',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(bank_id, account_number),
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- Создание таблицы транзакций
CREATE TABLE transactions (
    transaction_id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(user_id),
    user_type_id INTEGER NOT NULL REFERENCES user_types(user_type_id),
    operation_date DATE NOT NULL CHECK (
        TO_CHAR(operation_date, 'DD.MM.YYYY') ~ '^(0[1-9]|[12][0-9]|3[01])\.(0[1-9]|1[0-2])\.\d{4}$'
    ),
    transaction_type_id INTEGER NOT NULL REFERENCES transaction_types(transaction_type_id),
    amount NUMERIC(15,5) NOT NULL,
    status_id INTEGER NOT NULL REFERENCES operation_statuses(status_id),
    sender_bank_id INTEGER REFERENCES banks(bank_id),
    sender_account_id INTEGER REFERENCES user_accounts(account_id),
    receiver_bank_id INTEGER REFERENCES banks(bank_id),
    receiver_inn VARCHAR(5) NOT NULL CHECK (receiver_inn ~ '^[0-9]{5}$'),
    receiver_account VARCHAR(20),
    receiver_phone VARCHAR(20) NOT NULL CHECK (
        REGEXP_REPLACE(receiver_phone, '[^0-9+]', '', 'g') ~ '^(\+7|8)[0-9]{10}$'
    ),
    category_id INTEGER REFERENCES categories(category_id),
    comment TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE,
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (user_type_id) REFERENCES user_types(user_type_id),
    FOREIGN KEY (transaction_type_id) REFERENCES transaction_types(transaction_type_id),
    FOREIGN KEY (status_id) REFERENCES operation_statuses(status_id),
    FOREIGN KEY (sender_bank_id) REFERENCES banks(bank_id),
    FOREIGN KEY (sender_account_id) REFERENCES user_accounts(account_id),
    FOREIGN KEY (receiver_bank_id) REFERENCES banks(bank_id),
    FOREIGN KEY (category_id) REFERENCES categories(category_id)
);

-- Создание таблицы для аудита изменений транзакций
CREATE TABLE transaction_audit (
    audit_id SERIAL PRIMARY KEY,
    transaction_id INTEGER NOT NULL,
    changed_by INTEGER NOT NULL REFERENCES users(user_id),
    change_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    field_name VARCHAR(50) NOT NULL,
    old_value TEXT,
    new_value TEXT,
    FOREIGN KEY (transaction_id) REFERENCES transactions(transaction_id),
    FOREIGN KEY (changed_by) REFERENCES users(user_id)
);

-- Создание таблицы для хранения сгенерированных отчетов
CREATE TABLE reports (
    report_id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(user_id),
    report_name VARCHAR(100) NOT NULL,
    report_type VARCHAR(50) NOT NULL,
    parameters JSONB,
    file_path VARCHAR(255),
    generated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- Создание таблицы для хранения иных настроек
CREATE TABLE Settings (
	setting_id SERIAL PRIMARY KEY,
	user_id INTEGER NOT NULL REFERENCES users(user_id),
	key VARCHAR(50) NOT NULL,
	value VARCHAR(100) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);