WITH
sender_bank_id AS (SELECT bank_id FROM banks WHERE bank_name = :sender_bank_name), 

reciever_bank_id AS (SELECT bank_id FROM banks WHERE bank_name = :reciever_bank_name), 

sender_inn AS (SELECT inn FROM users WHERE user_name = :user_name), 

new_phone_number AS (SELECT phone_number FROM users WHERE user_name = :user_name), 

new_category_id AS (SELECT category_id FROM categories WHERE category_name = :category_name) 

UPDATE transactions SET

user_type = :user_type, 

transaction_date =: transaction_date, 

comment = :comment, 

transaction_amount = :transaction_amount, 

transaction_status = :transaction_status,

send_from_bank_id = sender_bank_id, 

send_to_bank_id = reciever_bank_id, 

inn = sender_inn, 

transaction_category_id = :new_category_id, 

phone_number = :new_phone_number

WHERE transaction_id = :transaction_id;
