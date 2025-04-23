SELECT 
    b.bank_name,
    COUNT(*) AS received_transfers_count,
    SUM(t.amount) AS received_amount,
    AVG(t.amount) AS avg_received_amount
FROM 
    transfers tr
JOIN 
    transactions t ON tr.receiver_transaction_id = t.transaction_id
JOIN 
    accounts a ON t.account_id = a.account_id
JOIN 
    banks b ON a.bank_id = b.bank_id
WHERE 
    tr.sender_user_id = :user_id
    AND t.transaction_date BETWEEN :start_date AND :end_date
GROUP BY 
    b.bank_name
ORDER BY 
    received_amount DESC;
