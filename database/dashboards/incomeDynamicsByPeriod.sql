WITH income_dynamics AS (
    SELECT
        DATE_TRUNC(:period, transaction_date) AS period_start,
        COUNT(*) AS income_count,
        SUM(amount) AS income_amount,
        AVG(amount) AS avg_income,
        MAX(amount) AS max_income,
        MIN(amount) AS min_income
    FROM 
        transactions
    WHERE 
        user_id = :user_id
        AND transaction_type = 'income'
        AND transaction_date BETWEEN :start_date AND :end_date
    GROUP BY 
        DATE_TRUNC(:period, transaction_date)
)

SELECT 
    period_start,
    income_count,
    income_amount,
    avg_income,
    max_income,
    min_income,
    LAG(income_count, 1) OVER (ORDER BY period_start) AS prev_period_count,
    LAG(income_amount, 1) OVER (ORDER BY period_start) AS prev_period_amount,
    ROUND(
        (income_count - LAG(income_count, 1) OVER (ORDER BY period_start)) * 100.0 / 
        NULLIF(LAG(income_count, 1) OVER (ORDER BY period_start), 0), 
    2) AS count_change_percent,
    ROUND(
        (income_amount - LAG(income_amount, 1) OVER (ORDER BY period_start)) * 100.0 / 
        NULLIF(LAG(income_amount, 1) OVER (ORDER BY period_start), 0), 
    2) AS amount_change_percent
FROM 
    income_dynamics
ORDER BY 
    period_start;
