SELECT
    c.category_id,
    c.category_name,
    c.category_type,
    SUM(CASE WHEN t.transaction_type = 'income' THEN t.amount ELSE 0 END) AS income_amount,
    SUM(CASE WHEN t.transaction_type = 'expense' THEN t.amount ELSE 0 END) AS expense_amount,
    COUNT(CASE WHEN t.transaction_type = 'income' THEN 1 END) AS income_count,
    COUNT(CASE WHEN t.transaction_type = 'expense' THEN 1 END) AS expense_count,
    ROUND(
        SUM(CASE WHEN t.transaction_type = 'income' THEN t.amount ELSE 0 END) * 100.0 / 
        NULLIF(SUM(SUM(CASE WHEN t.transaction_type = 'income' THEN t.amount ELSE 0 END)) OVER (), 0),
        2
    ) AS income_percentage,
    ROUND(
        SUM(CASE WHEN t.transaction_type = 'expense' THEN t.amount ELSE 0 END) * 100.0 / 
        NULLIF(SUM(SUM(CASE WHEN t.transaction_type = 'expense' THEN t.amount ELSE 0 END)) OVER (), 0),
        2
    ) AS expense_percentage
FROM
    categories c
LEFT JOIN
    transactions t ON c.category_id = t.category_id
    AND t.user_id = :user_id
    AND t.transaction_date BETWEEN :start_date AND :end_date
    AND t.status = 'completed'
WHERE
    c.category_type IN ('income', 'expense')
GROUP BY
    c.category_id, c.category_name, c.category_type
ORDER BY
    CASE WHEN c.category_type = 'income' THEN 0 ELSE 1 END,
    COALESCE(SUM(CASE WHEN t.transaction_type = c.category_type THEN t.amount ELSE 0 END), 0) DESC;
