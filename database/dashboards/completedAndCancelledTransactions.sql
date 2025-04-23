SELECT
    status,
    COUNT(*) AS transaction_count,
    ROUND(COUNT(*) * 100.0 / SUM(COUNT(*)) OVER (), 2) AS percentage
FROM
    transactions
WHERE
    user_id = :user_id
    AND transaction_date BETWEEN :start_date AND :end_date
GROUP BY
    status
ORDER BY
    transaction_count DESC;

SELECT
    transaction_type,
    SUM(CASE WHEN status = 'completed' THEN 1 ELSE 0 END) AS completed_count,
    SUM(CASE WHEN status = 'cancelled' THEN 1 ELSE 0 END) AS cancelled_count,
    SUM(CASE WHEN status = 'pending' THEN 1 ELSE 0 END) AS pending_count,
    ROUND(
        SUM(CASE WHEN status = 'cancelled' THEN 1 ELSE 0 END) * 100.0 / 
        NULLIF(COUNT(*), 0),
        2
    ) AS cancellation_rate
FROM
    transactions
WHERE
    user_id = :user_id
    AND transaction_date BETWEEN :start_date AND :end_date
GROUP BY
    transaction_type
ORDER BY
    cancellation_rate DESC;
