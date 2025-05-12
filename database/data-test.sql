-- Генерация тестовых транзакций на 2 пользователя (пароли для тестовых юзеров - 123)

INSERT INTO users (email, password_hash, full_name) VALUES
('test@yandex.ru', '$2a$10$.0TZlsKnckkri2.WSok3beh2.4W/oGjzEDCoZAxPs0fKV2Yn3poDa', 'Петров Петр Петрович'),	
('test2@yandex.ru', '$2a$10$a2EZrIaB/WIyhnHxIDLaq.qKvIlnAFe4mUdSQD086EnpztDvrHoSu', 'Иванов Иван Иванович');

INSERT INTO transactions (
    type_id,
    sender_bank_id,
    recipient_bank_id,
    amount,
    comment,
    status_id,
    created_at,
    user_id,
    account_number,
    recipient_number,
    legal_type_id,
    transaction_date,
    recipient_tin,
    category_id,
    recipient_phone
)
SELECT
    (RANDOM() * 1 + 1)::INT,
    (RANDOM() * 4 + 1)::INT,
    (RANDOM() * 4 + 1)::INT,
    (RANDOM() * 50000 + 1000)::NUMERIC(12, 2),
    'Автотестовая транзакция',
    (RANDOM() * 6 + 1)::INT,
    d.day + (RANDOM() * INTERVAL '23 hours'),
    FLOOR(RANDOM() * 2 + 1)::INT,  -- случайный user_id от 1 до 2
    '40817810' || LPAD(FLOOR(RANDOM() * 1000000000)::TEXT, 12, '0'),
    '40817810' || LPAD(FLOOR(RANDOM() * 1000000000)::TEXT, 12, '0'),
    (RANDOM() * 1 + 1)::INT,
    d.day + (RANDOM() * INTERVAL '23 hours'),
    FLOOR(RANDOM() * 89999999999 + 10000000000)::BIGINT,
    (RANDOM() * 11 + 1)::INT,
    CASE WHEN RANDOM() < 0.5 THEN '+7' ELSE '8' END || LPAD(FLOOR(RANDOM() * 10000000000)::TEXT, 10, '0')
FROM (
    SELECT generate_series(CURRENT_DATE - INTERVAL '1.5 years', CURRENT_DATE, INTERVAL '1 day') AS day
) d,
LATERAL generate_series(1, FLOOR(RANDOM() * 3 + 1)::INT) AS g(n);

SELECT *
FROM transactions t
JOIN users u ON t.user_id = u.user_id
ORDER BY t.transaction_date DESC;