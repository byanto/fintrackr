CREATE TABLE portfolios (
    id UUID PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    broker_account_id UUID NOT NULL,
    name TEXT NOT NULL,
    trading_balance_amount NUMERIC(19, 0) NOT NULL,
    trading_balance_currency VARCHAR(3) NOT NULL,
    CONSTRAINT fk_portfolios_broker_account_id FOREIGN KEY (broker_account_id) REFERENCES broker_accounts(id),
    CONSTRAINT ck_portfolios_trading_balance_currency CHECK (trading_balance_currency = 'IDR')
);

CREATE TABLE acquisitions (
    id UUID PRIMARY KEY,
    portfolio_id UUID NOT NULL,
    asset_id TEXT NOT NULL,
    open_date DATE NOT NULL,
    open_price_amount NUMERIC(19, 0) NOT NULL,
    open_price_currency VARCHAR(3) NOT NULL,
    open_fee_amount NUMERIC(19, 0) NOT NULL,
    open_fee_currency VARCHAR(3) NOT NULL,
    initial_quantity NUMERIC NOT NULL,
    CONSTRAINT fk_acquisitions_portfolio_id FOREIGN KEY (portfolio_id) REFERENCES portfolios(id),
    CONSTRAINT ck_acquisitions_open_price_currency CHECK (open_price_currency = 'IDR'),
    CONSTRAINT ck_acquisitions_open_fee_currency CHECK (open_fee_currency = 'IDR'),
    CONSTRAINT ck_acquisitions_initial_quantity CHECK (scale(initial_quantity) IN (0, 4))
);

CREATE TABLE transactions (
    id UUID PRIMARY KEY,
    portfolio_id UUID NOT NULL,
    date DATE NOT NULL,
    type VARCHAR(20) NOT NULL,
    cash_amount NUMERIC(19, 0),
    cash_currency VARCHAR(3),
    asset_id TEXT,
    quantity NUMERIC,
    price_amount NUMERIC(19, 0),
    price_currency VARCHAR(3),
    fee_amount NUMERIC(19, 0),
    fee_currency VARCHAR(3),
    acquisition_id UUID UNIQUE,
    seq BIGINT GENERATED ALWAYS AS IDENTITY,
    CONSTRAINT fk_transactions_portfolio_id FOREIGN KEY (portfolio_id) REFERENCES portfolios(id),
    CONSTRAINT fk_transactions_acquisition_id FOREIGN KEY (acquisition_id) REFERENCES acquisitions(id),
    CONSTRAINT ck_transactions_cash_currency CHECK (cash_currency = 'IDR'),
    CONSTRAINT ck_transactions_price_currency CHECK (price_currency = 'IDR'),
    CONSTRAINT ck_transactions_fee_currency CHECK (fee_currency = 'IDR'),
    CONSTRAINT ck_transactions_quantity CHECK (scale(quantity) IN (0, 4)),
    CONSTRAINT ck_transactions_type CHECK (type IN ('DEPOSIT', 'BUY')),
    CONSTRAINT ck_transactions_columns_match_type
      CHECK (
        CASE type
          WHEN 'DEPOSIT' THEN (cash_amount IS NOT NULL AND cash_currency IS NOT NULL AND asset_id IS NULL AND quantity IS NULL AND price_amount IS NULL AND price_currency IS NULL AND fee_amount IS NULL AND fee_currency IS NULL AND acquisition_id IS NULL)
          WHEN 'BUY' THEN (cash_amount IS NULL AND cash_currency IS NULL AND asset_id IS NOT NULL AND quantity IS NOT NULL AND price_amount IS NOT NULL AND price_currency IS NOT NULL AND fee_amount IS NOT NULL AND fee_currency IS NOT NULL AND acquisition_id IS NOT NULL)
          ELSE FALSE
        END
      )
);

CREATE INDEX idx_acquisitions_portfolio_id ON acquisitions(portfolio_id);
CREATE INDEX idx_transactions_portfolio_id ON transactions(portfolio_id);
