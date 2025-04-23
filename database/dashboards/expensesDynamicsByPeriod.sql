WITH expense_dynamics AS (
    SELECT
        DATE_TRUNC(:period, transaction_date) AS period_start,
        COUNT(*) AS expense_count,
        SUM(amount) AS expense_amount,
        AVG(amount) AS avg_expense,
        MAX(amount) AS max_expense,
        MIN(amount) AS min_expense
    FROM 
        transactions
    WHERE 
        user_id = :user_id
        AND transaction_type = 'expense'
        AND transaction_date BETWEEN :start_date AND :end_date
    GROUP BY 
        DATE_TRUNC(:period, transaction_date)
)

SELECT 
    period_start,
    expense_count,
    expense_amount,
    avg_expense,
    max_expense,
    min_expense,
    LAG(expense_count, 1) OVER (ORDER BY period_start) AS prev_period_count,
    LAG(expense_amount, 1) OVER (ORDER BY period_start) AS prev_period_amount,
    ROUND(
        (expense_count - LAG(expense_count, 1) OVER (ORDER BY period_start)) * 100.0 / 
        NULLIF(LAG(expense_count, 1) OVER (ORDER BY period_start), 0), 
    2) AS count_change_percent,
    ROUND(
        (expense_amount - LAG(expense_amount, 1) OVER (ORDER BY period_start)) * 100.0 / 
        NULLIF(LAG(expense_amount, 1) OVER (ORDER BY period_start), 0), 
    2) AS amount_change_percent
FROM 
    expense_dynamics
ORDER BY 
    period_start;
