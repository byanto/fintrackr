-- V2__create_broker_account_table.sql

-- Broker Account Table: Represents a financial institution holding investments
CREATE TABLE broker_account (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    broker_name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW()
);

/*
-- Alter Portfolio Table: Add reference to broker account
ALTER TABLE portfolio 
ADD COLUMN broker_account_id BIGINT NOT NULL REFERENCES broker_account(id);

-- Alter Trade Table: Add fee column
ALTER TABLE trade 
ADD COLUMNN fee DECIMAL(19, 4) NOT NULL DEFAULT 0.00;
*/

-- Add comments for clarity
COMMENT ON TABLE broker_account IS 'Stores information about brokerage accounts or bank accounts holding investments.';
COMMENT ON COLUMN broker_account.name IS 'User-defined nickname for the account.';
COMMENT ON COLUMN broker_account.broker_name IS 'Official name of the financial institution.';
COMMENT ON COLUMN broker_account.created_at IS 'Timestamp when the account was created.';