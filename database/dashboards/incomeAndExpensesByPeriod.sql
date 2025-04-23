SELECT
    SUM(CASE WHEN transaction_type = 'income' THEN amount ELSE 0 END) AS total_income,
    SUM(CASE WHEN transaction_type = 'expense' THEN amount ELSE 0 END) AS total_expense,
    SUM(CASE WHEN transaction_type = 'income' THEN amount ELSE 0 END) - 
    SUM(CASE WHEN transaction_type = 'expense' THEN amount ELSE 0 END) AS balance,
    ROUND(
        SUM(CASE WHEN transaction_type = 'income' THEN amount ELSE 0 END) * 100.0 / 
        NULLIF(SUM(CASE WHEN transaction_type = 'expense' THEN amount ELSE 0 END), 0),
        2
    ) AS income_to_expense_ratio,
    COUNT(CASE WHEN transaction_type = 'income' THEN 1 END) AS income_count,
    COUNT(CASE WHEN transaction_type = 'expense' THEN 1 END) AS expense_count
FROM
    transactions
WHERE
    user_id = :user_id
    AND transaction_date BETWEEN :start_date AND :end_date;
