CREATE TABLE IF NOT EXISTS balances (
    nickname VARCHAR(255) PRIMARY KEY,
    uuid VARCHAR(255),
    balance BIGINT,
    currency VARCHAR(255),
    UNIQUE KEY unique_nickname_currency (nickname, currency),
    UNIQUE KEY unique_uuid_currency (uuid,currency)
);
CREATE TABLE IF NOT EXISTS balance_logs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    time BIGINT,
    nickname VARCHAR(255),
    uuid VARCHAR(255),
    type VARCHAR(64), # SET, INC, DEC
    amount INT,
    currency VARCHAR(255),
    admin VARCHAR(255),
    via VARCHAR(255)
);
