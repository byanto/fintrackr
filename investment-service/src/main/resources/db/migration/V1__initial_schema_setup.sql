-- V1__initial_schema_setup.sql

-- Portfolio Table: Represents a collection of investments.
CREATE TABLE portfolio (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW()
);

-- Instrument Table: Represents a financial instrument (e.g., a specific stock or mutual fund).
CREATE TABLE instrument (
    id BIGSERIAL PRIMARY KEY,
    instrument_type VARCHAR(50) NOT NULL, -- e.g., 'STOCK', 'MUTUAL_FUND'
    code VARCHAR(50) UNIQUE NOT NULL, -- e.g., 'BBCA', '001-MF-XYZ'
    name VARCHAR(255) NOT NULL,
    currency VARCHAR(10) NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW()
);

-- Trade Table: Records every buy or sell transaction. This is an immutable event log.
CREATE TABLE trade (
    id BIGSERIAL PRIMARY KEY,
    portfolio_id BIGINT NOT NULL REFERENCES portfolio(id) ON DELETE CASCADE,
    instrument_id BIGINT NOT NULL REFERENCES instrument(id) ON DELETE RESTRICT,
    trade_type VARCHAR(10) NOT NULL, -- 'BUY' or 'SELL'
    quantity DECIMAL(19, 4) NOT NULL,
    price DECIMAL(19, 4) NOT NULL,    
    traded_at TIMESTAMP WITHOUT TIME ZONE NOT NULL
);

-- Holding Table: A materialized view of the current position for an instrument in a portfolio.
CREATE TABLE holding (
    id BIGSERIAL PRIMARY KEY,
    portfolio_id BIGINT NOT NULL REFERENCES portfolio(id) ON DELETE CASCADE,
    instrument_id BIGINT NOT NULL REFERENCES instrument(id) ON DELETE RESTRICT,
    quantity DECIMAL(19, 4) NOT NULL,
    average_price DECIMAL(19, 4) NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    UNIQUE (portfolio_id, instrument_id)
);

-- Add comments for clarity
COMMENT ON TABLE trade IS 'An immutable log of all buy/sell transactions.';
COMMENT ON TABLE holding IS 'Represents the current aggregated position of an instrument within a portfolio.';