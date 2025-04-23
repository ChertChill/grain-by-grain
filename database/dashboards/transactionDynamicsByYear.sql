SELECT 
    DATE_TRUNC('year', transaction_date) AS year_start,
    COUNT(*) AS transaction_count,
    SUM(CASE WHEN transaction_type = 'income' THEN 1 ELSE 0 END) AS income_count,
    SUM(CASE WHEN transaction_type = 'expense' THEN 1 ELSE 0 END) AS expense_count,
    SUM(CASE WHEN transaction_type = 'transfer' THEN 1 ELSE 0 END) AS transfer_count,
    EXTRACT(YEAR FROM transaction_date) AS year
FROM 
    transactions
WHERE 
    user_id = :user_id
    AND transaction_date BETWEEN :start_date AND :end_date
GROUP BY 
    DATE_TRUNC('year', transaction_date),
    EXTRACT(YEAR FROM transaction_date)
ORDER BY 
    year_start;