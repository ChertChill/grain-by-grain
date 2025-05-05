CREATE TABLE http_logs (
    id BIGINT PRIMARY KEY,
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