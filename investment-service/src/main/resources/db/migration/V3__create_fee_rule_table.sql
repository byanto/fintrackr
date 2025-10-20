-- V3__create_fee_rule_table.sql

-- Fee Rule Table: Defines fee structures for brokers
CREATE TABLE fee_rule (
    id BIGSERIAL PRIMARY KEY,
    broker_account_id BIGINT NOT NULL REFERENCES broker_account(id) ON DELETE CASCADE,
    instrument_type VARCHAR(255) NOT NULL,
    trade_type VARCHAR(255) NOT NULL,
    fee_percentage DECIMAL(5, 4) NOT NULL,
    min_fee DECIMAL(19, 2) NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    UNIQUE (broker_account_id, instrument_type, trade_type)
);

-- Alter Trade Table: Add fee column
ALTER TABLE trade
ADD COLUMN fee DECIMAL(19, 4) NOT NULL DEFAULT 0.00;

-- Add comments for clarity
COMMENT ON TABLE fee_rule IS 'Stores fee calculation rules based on broker, instrument type, and trade type.';
COMMENT ON COLUMN fee_rule.fee_percentage IS 'Fee as a decimal (e.g., 0.0018 = 0.18%).';
COMMENT ON COLUMN fee_rule.min_fee IS 'Minimum fee amount for a transaction.';
COMMENT ON COLUMN trade.fee IS 'The calculated fee for the trade.';