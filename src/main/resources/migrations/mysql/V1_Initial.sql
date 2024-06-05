CREATE TABLE IF NOT EXISTS balances (
    nickname VARCHAR(255) PRIMARY KEY,
    uuid VARCHAR(255),
    balance BIGINT,
    currency VARCHAR(255),
    UNIQUE KEY unique_nickname_currency (nickname, currency),
    UNIQUE KEY unique_uuid_currency (uuid,currency)
);
CREATE TABLE IF NOT EXISTS log_balance (
    id INT AUTO_INCREMENT PRIMARY KEY,
    unix BIGINT,
    nickname VARCHAR(255),
    uuid VARCHAR(255),
    type VARCHAR(64), # SET, INC, DEC, INIT, TRANSFER
    amount BIGINT,
    currency VARCHAR(255),
    data VARCHAR(255),
    initiator VARCHAR(255), # ADMIN || Player sending the balance
    via VARCHAR(255)
);