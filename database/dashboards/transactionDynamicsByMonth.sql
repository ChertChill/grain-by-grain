SELECT 
    DATE_TRUNC('month', transaction_date) AS month_start,
    COUNT(*) AS transaction_count,
    SUM(CASE WHEN transaction_type = 'income' THEN 1 ELSE 0 END) AS income_count,
    SUM(CASE WHEN transaction_type = 'expense' THEN 1 ELSE 0 END) AS expense_count,
    SUM(CASE WHEN transaction_type = 'transfer' THEN 1 ELSE 0 END) AS transfer_count,
    EXTRACT(YEAR FROM transaction_date) AS year,
    EXTRACT(MONTH FROM transaction_date) AS month
FROM 
    transactions
WHERE 
    user_id = :user_id
    AND transaction_date BETWEEN :start_date AND :end_date
GROUP BY 
    DATE_TRUNC('month', transaction_date),
    EXTRACT(YEAR FROM transaction_date),
    EXTRACT(MONTH FROM transaction_date)
ORDER BY 
    month_start;