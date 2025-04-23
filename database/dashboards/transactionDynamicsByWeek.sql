SELECT 
    DATE_TRUNC('week', transaction_date) AS week_start,
    COUNT(*) AS transaction_count,
    SUM(CASE WHEN transaction_type = 'income' THEN 1 ELSE 0 END) AS income_count,
    SUM(CASE WHEN transaction_type = 'expense' THEN 1 ELSE 0 END) AS expense_count,
    SUM(CASE WHEN transaction_type = 'transfer' THEN 1 ELSE 0 END) AS transfer_count
FROM 
    transactions
WHERE 
    user_id = :user_id
    AND transaction_date BETWEEN :start_date AND :end_date
GROUP BY 
    DATE_TRUNC('week', transaction_date)
ORDER BY 
    week_start;