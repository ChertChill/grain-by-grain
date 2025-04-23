WITH sender_stats AS (
    SELECT 
        b.bank_name,
        COUNT(*) AS sent_count,
        SUM(t.amount) AS sent_amount
    FROM 
        transactions t
    JOIN 
        accounts a ON t.account_id = a.account_id
    JOIN 
        banks b ON a.bank_id = b.bank_id
    WHERE 
        t.user_id = :user_id
        AND t.transaction_type = 'expense'
        AND t.transaction_date BETWEEN :start_date AND :end_date
    GROUP BY 
        b.bank_name
),
receiver_stats AS (
    SELECT 
        b.bank_name,
        COUNT(*) AS received_count,
        SUM(t.amount) AS received_amount
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
)

SELECT 
    COALESCE(s.bank_name, r.bank_name) AS bank_name,
    COALESCE(s.sent_count, 0) AS sent_count,
    COALESCE(s.sent_amount, 0) AS sent_amount,
    COALESCE(r.received_count, 0) AS received_count,
    COALESCE(r.received_amount, 0) AS received_amount,
    COALESCE(r.received_amount, 0) - COALESCE(s.sent_amount, 0) AS net_flow
FROM 
    sender_stats s
FULL OUTER JOIN 
    receiver_stats r ON s.bank_name = r.bank_name
ORDER BY 
    ABS(COALESCE(r.received_amount, 0) - COALESCE(s.sent_amount, 0)) DESC;
