-- Таблица пользователей
CREATE TABLE users (
    user_id SERIAL PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$'),
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL
);

-- Типы транзакций
CREATE TABLE transaction_types (
    type_id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);

-- Статусы транзакций
CREATE TABLE transaction_status (
    status_id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    is_final BOOLEAN DEFAULT FALSE
);

-- Категории получателей
CREATE TABLE categories (
    category_id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);

-- Банки
CREATE TABLE banks (
    bank_id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    bic VARCHAR(9) UNIQUE NOT NULL CHECK (bic ~ '^[0-9]{9}$'),
    address VARCHAR(255)
);

-- Типы юридических лиц
CREATE TABLE legal_types (
    legal_type_id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);

CREATE TABLE transactions (
    transaction_id SERIAL PRIMARY KEY,
    type_id INT NOT NULL REFERENCES transaction_types(type_id),
    sender_bank_id INT NOT NULL REFERENCES banks (bank_id),
    recipient_bank_id INT NOT NULL REFERENCES banks (bank_id),
    amount DECIMAL(12, 2) NOT NULL,
    comment TEXT,
    status_id INT NOT NULL REFERENCES transaction_status(status_id),
    created_at TIMESTAMP NOT NULL,
    user_id BIGINT NOT NULL REFERENCES users(user_id),               
    account_number VARCHAR(50) NOT NULL,                             
    recipient_number VARCHAR(50) NOT NULL,                           
    legal_type_id INT NOT NULL REFERENCES legal_types(legal_type_id),
    transaction_date TIMESTAMP NOT NULL,                                             
    recipient_tin BIGINT NOT NULL CHECK (recipient_tin >= 10000000000 AND recipient_tin <= 99999999999), 
    category_id INT REFERENCES categories(category_id),              
    recipient_phone VARCHAR(16) CHECK (recipient_phone ~ '^(\+7|8)[0-9]{10}$')           
);

/*CREATE TABLE audit_logs (
    audit_log_id SERIAL PRIMARY KEY
);

CREATE TABLE integration_logs (
    integration_log_id SERIAL PRIMARY KEY
);*/


-- Логи для таблицы USERS 
CREATE TABLE users_audit (
    audit_id SERIAL PRIMARY KEY,
    operation_type VARCHAR(10) NOT NULL,
    user_id INT NOT NULL REFERENCES users(user_id),
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, 
    changed_by INT NOT NULL REFERENCES users(user_id),
    old_email VARCHAR(100),
    new_email VARCHAR(100) NOT NULL,
    old_password_hash VARCHAR(255),
    new_password_hash VARCHAR(255) NOT NULL,
    old_full_name VARCHAR(100),
    new_full_name VARCHAR(100) NOT NULL
);


-- Триггерная функция на оператор INSERT
-- для таблицы USERS
CREATE OR REPLACE FUNCTION audit_users_insert()
RETURNS TRIGGER AS $$
BEGIN
    -- Вставляем запись в таблицу аудита для операции INSERT
    INSERT INTO users_audit (
        operation_type,
        user_id,
        changed_by,
        old_email,
        new_email,
        old_password_hash,
        new_password_hash,
        old_full_name,
        new_full_name
    ) VALUES (
        'INSERT',
        NEW.user_id,
        NEW.user_id, 	-- Предполагаем, что пользователь создает сам себя, иначе нужно передать ID текущего пользователя
        NULL, 			-- Для INSERT старые значения пустые
        NEW.email,
        NULL, 			-- Для INSERT старые значения пустые
        NEW.password_hash,
        NULL, 			-- Для INSERT старые значения пустые
        NEW.full_name
    );
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;


-- Триггер на оператор INSERT
-- для таблицы USERS
CREATE OR REPLACE TRIGGER new_user_audit
    AFTER INSERT
    ON public.users
    FOR EACH ROW
    EXECUTE FUNCTION public.audit_users_insert();
	
	
-- Триггерная функция на оператор UPDATE
-- для таблицы USERS
CREATE OR REPLACE FUNCTION audit_users_update()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'UPDATE' AND (OLD.* IS DISTINCT FROM NEW.*) THEN
        INSERT INTO users_audit (
            operation_type,
			user_id,
			changed_by,
			old_email,
			new_email,
			old_password_hash,
			new_password_hash,
			old_full_name,
			new_full_name
        ) VALUES (
            'UPDATE',
            OLD.user_id,
			NEW.user_id,
            OLD.email,
			NEW.email,
            OLD.password_hash,
			NEW.password_hash,
            OLD.full_name,
			NEW.full_name
        );
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Триггер на оператор UPDATE
-- для таблицы USERS
CREATE OR REPLACE TRIGGER update_user_audit
    AFTER UPDATE
    ON public.users
    FOR EACH ROW
    EXECUTE FUNCTION public.audit_users_update();



-- Логи для таблицы TRANSACTIONS
CREATE TABLE transactions_audit (
    
	-- Поля таблицы для аудита
	audit_id SERIAL PRIMARY KEY,
    operation_type VARCHAR(10) NOT NULL,
	
	
	-- Поля таблицы transactions, которые не меняются
	transaction_id INT NOT NULL REFERENCES transactions(transaction_id),
	type_id INT NOT NULL REFERENCES transaction_types(type_id),
	user_id BIGINT NOT NULL REFERENCES users(user_id),
	created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	changed_by INT NOT NULL REFERENCES users(user_id),
	account_number VARCHAR(50) NOT NULL,
	recipient_number VARCHAR(50) NOT NULL,
	
	
	-- Параметры, доступные для редактирования (старые значения)
	amount DECIMAL(12, 2),
	comment TEXT,
	status_id INT REFERENCES transaction_status(status_id),
	legal_type_id INT REFERENCES legal_types(legal_type_id),
	transaction_date DATE ,  
	sender_bank_id INT REFERENCES banks (bank_id),
	recipient_bank_id INT REFERENCES banks (bank_id),
	recipient_tin VARCHAR(11),
	category_id INT REFERENCES categories(category_id),
	recipient_phone VARCHAR(16),
	
	
	--Поля для сохранения новых значений
	new_legal_type_id INT REFERENCES legal_types(legal_type_id),
	new_transaction_date DATE,  
	new_comment TEXT,
	new_amount DECIMAL(12, 2),
	new_status_id INT REFERENCES transaction_status(status_id),
	new_sender_bank_id INT REFERENCES banks (bank_id),
	new_recipient_bank_id INT REFERENCES banks (bank_id),
	new_recipient_tin VARCHAR(11),
	new_category_id INT REFERENCES categories(category_id),
	new_recipient_phone VARCHAR(16)
	
);

-- Триггерная функция на оператор INSERT
-- для таблицы TRANSACTIONS
CREATE OR REPLACE FUNCTION audit_transactions_insert()
RETURNS TRIGGER AS $$
BEGIN
    -- Вставляем запись в таблицу аудита для операции INSERT
    INSERT INTO transactions_audit (
	
        operation_type,
		transaction_id,
		type_id,
		user_id,
		--created_at,
		changed_by, -- Предполагаем, что пользователь СОЗДАЕТ сам себя, иначе нужно передать ID текущего пользователя
		account_number,
		recipient_number,
		
		-- Для INSERT старым значениям присваивается NULL
		amount,
		comment,
		status_id,
		legal_type_id,
		transaction_date,  
		sender_bank_id,
		recipient_bank_id,
		recipient_tin,
		category_id,
		recipient_phone,

        -- Блок новых значений
		new_amount,
		new_comment,
		new_status_id,
		new_legal_type_id,
		new_transaction_date,  
		new_sender_bank_id,
		new_recipient_bank_id,
		new_recipient_tin,
		new_category_id,
		new_recipient_phone
	
	) VALUES (
        'INSERT',
        NEW.transaction_id,
		NEW.type_id,
        NEW.user_id,
		--created_at,
		NEW.user_id,
		NEW.account_number,
		NEW.recipient_number,

        -- Блок старых значений
        -- Для INSERT старым значениям присваивается NULL
		NULL,	--amount
		NULL,	--comment
		NULL,	--status_id
		NULL,	--legal_type_id
		NULL,	--transaction_date
		NULL,	--sender_bank_id
		NULL,	--recipient_bank_id
		NULL,	--recipient_tin
		NULL,	--category_id
		NULL,	--recipient_phone

		-- Блок новых значений
		NEW.amount,				--new_amount
		NEW.comment,			--new_comment
		NEW.status_id,			--new_status_id
		NEW.legal_type_id,		--new_legal_type_id
		NEW.transaction_date,	--new_transaction_date
		NEW.sender_bank_id,		--new_sender_bank_id
		NEW.recipient_bank_id,	--new_recipient_bank_id
		NEW.recipient_tin,		--new_recipient_tin
		NEW.category_id,		--new_category_id
		NEW.recipient_phone		--new_recipient_phone

    );
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;


-- Триггер на оператор INSERT
-- для таблицы TRANSACTIONS
CREATE OR REPLACE TRIGGER new_transaction_audit
    AFTER INSERT
    ON public.transactions
    FOR EACH ROW
    EXECUTE FUNCTION public.audit_transactions_insert();



-- Триггерная функция на оператор UPDATE
-- для таблицы TRANSACTIONS
CREATE OR REPLACE FUNCTION audit_transactions_update()
RETURNS TRIGGER AS $$
BEGIN
    
	IF TG_OP = 'UPDATE' AND (OLD.* IS DISTINCT FROM NEW.*) THEN
		-- Вставляем запись в таблицу аудита для операции UPDATE
		INSERT INTO transactions_audit (
		
			operation_type,
			transaction_id,
			type_id,
			user_id,
			--created_at,
			changed_by, -- Предполагаем, что пользователь ОБНОВЛЯЕТ сам себя, иначе нужно передать ID текущего пользователя
			account_number,
			recipient_number,
			
			-- Для UPDATE сохраняем ва этот блок старые значения
			amount,
			comment,
			status_id,
			legal_type_id,
			transaction_date,  
			sender_bank_id,
            recipient_bank_id,
            recipient_tin,
			category_id,
			phone,

			-- Блок для новых значений
			new_amount,
			new_comment,
			new_status_id,
			new_legal_type_id,
			new_transaction_date,  
			new_sender_bank_id,
            new_recipient_bank_id,
			new_recipient_tin,
			new_category_id,
			new_phone
		
		) VALUES (
			'UPDATE',
			NEW.transaction_id,
			NEW.type_id,
			NEW.user_id,
			--created_at,
			NEW.user_id,
			NEW.account_number,
			NEW.recipient_number,

            -- Блок старых значений
			OLD.amount,				--amount
			OLD.comment,			--comment
			OLD.status_id,			--status_id
			OLD.legal_type_id,		--legal_type_id
			OLD.transaction_date,	--transaction_date
			OLD.sender_bank_id,		--sender_bank_id
			OLD.recipient_bank_id,	--recipient_bank_id
			OLD.recipient_tin,		--recipient_tin
			OLD.category_id,		--category_id
			OLD.phone,				--phone

			-- Блок новых значений
			NEW.amount,				--new_amount
			NEW.comment,			--new_comment
			NEW.status_id,			--new_status_id
			NEW.legal_type_id,		--new_legal_type_id
			NEW.transaction_date,	--new_transaction_date
			NEW.sender_bank_id,		--new_sender_bank_id
			NEW.recipient_bank_id,	--new_recipient_bank_id
			NEW.recipient_tin,		--new_recipient_tin
			NEW.category_id,		--new_category_id
			NEW.phone				--new_phone

		);
    END IF;
	
	RAISE NOTICE E'TRIGGER update\n Value of transaction_id : %', NEW.transaction_id;
	
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;


-- Триггер на оператор UPDATE
-- для таблицы TRANSACTIONS
CREATE OR REPLACE TRIGGER update_transaction_audit
    AFTER UPDATE
    ON public.transactions
    FOR EACH ROW
    EXECUTE FUNCTION public.audit_transactions_update();
	

-- Триггерная функция на оператор DELETE
-- для таблицы TRANSACTIONS
CREATE OR REPLACE FUNCTION audit_transactions_delete()
RETURNS TRIGGER AS $$
BEGIN
    
	UPDATE transactions
	SET status_id = 6
	WHERE transaction_id = OLD.transaction_id;
	
	INSERT INTO transactions_audit (
	
		operation_type,
		transaction_id,
		type_id,
		user_id,
		--created_at,
		changed_by, -- Предполагаем, что пользователь УДАЛЯЕТ свои транзакции, иначе нужно передать ID текущего пользователя
		account_number,
		recipient_number,
		
		-- Для DELETE старым значениям присваиваются текущие значения
		amount,
		comment,
		status_id,
		legal_type_id,
		transaction_date,  
		sender_bank_id,
        recipient_bank_id,
		recipient_tin,
		category_id,
		recipient_phone,
		
		-- Для DELETE новым значениям присваивается NULL		
		new_amount,
		new_comment,
		new_status_id,
		new_legal_type_id,
		new_transaction_date,  
        new_sender_bank_id,
        new_recipient_bank_id,
		new_recipient_tin,
		new_category_id,
		new_recipient_phone
	
	) VALUES (
		'DELETE',
		OLD.transaction_id,
		OLD.type_id,
		OLD.user_id,
		--created_at,
		OLD.user_id,
		OLD.account_number,
		OLD.recipient_number,

        -- Блок старых значений
        -- Старые значения не изменяются
        -- За исключением статуса транзакции
		OLD.amount,				--amount
		OLD.comment,			--comment
		-- OLD.status_id,		--status_id
		6, -- Платеж отменен
		OLD.legal_type_id,		--legal_type_id
		OLD.transaction_date,	--transaction_date
		OLD.sender_bank_id,		--sender_bank_id
		OLD.recipient_bank_id,	--recipient_bank_id
		OLD.recipient_tin,		--recipient_tin
		OLD.category_id,		--category_id
		OLD.recipient_phone,	--phone

		-- При удалении новые значения не устанавливаются
		-- Блок новых значений заполняем NULL
		NULL,	--new_amount
		NULL,	--new_comment
		NULL,	--new_status_id
		NULL,	--new_legal_type_id
		NULL,	--new_transaction_date
		NULL,	--new_sender_bank_id
		NULL,	--new_recipient_bank_id
		NULL,	--new_recipient_tin
		NULL,	--new_category_id
		NULL	--new_phone
	);
	
	RAISE NOTICE E'Trigger DELETE\nValue of transaction_id : %', OLD.transaction_id;
	
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

	
-- Триггер на оператор DELETE
-- для таблицы TRANSACTIONS
CREATE OR REPLACE TRIGGER delete_transactions_audit
    BEFORE DELETE
    ON public.transactions
    FOR EACH ROW
    EXECUTE FUNCTION public.audit_transactions_delete();
	
-- Тпблица для логирования сведений о выполнении HTTP-запросов к приложению
CREATE TABLE http_logs (
    id SERIAL PRIMARY KEY,
	user_id INT,
    request_method VARCHAR(10) NOT NULL,
    request_path VARCHAR(255) NOT NULL,
    request_ip VARCHAR(45) NOT NULL,
    request_user_agent TEXT,
    request_timestamp TIMESTAMP NOT NULL,
    response_status INT,
    response_time_ms BIGINT,
    error_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


