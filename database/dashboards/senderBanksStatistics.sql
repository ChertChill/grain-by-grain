SELECT 
    b.bank_name,
    COUNT(*) AS total_transactions,
    SUM(CASE WHEN t.transaction_type = 'expense' THEN 1 ELSE 0 END) AS outgoing_count,
    SUM(CASE WHEN t.transaction_type = 'expense' THEN t.amount ELSE 0 END) AS outgoing_amount,
    SUM(CASE WHEN t.transaction_type = 'income' THEN 1 ELSE 0 END) AS incoming_count,
    SUM(CASE WHEN t.transaction_type = 'income' THEN t.amount ELSE 0 END) AS incoming_amount
FROM 
    transactions t
JOIN 
    accounts a ON t.account_id = a.account_id
JOIN 
    banks b ON a.bank_id = b.bank_id
WHERE 
    t.user_id = :user_id
    AND t.transaction_date BETWEEN :start_date AND :end_date
GROUP BY 
    b.bank_name
ORDER BY 
    outgoing_amount DESC;
