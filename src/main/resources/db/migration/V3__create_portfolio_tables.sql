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
    CONSTRAINT fk_transactions_portfolio_id FOREIGN KEY (portfolio_id) REFERENCES portfolios(id),
    CONSTRAINT uq_transactions_id_type UNIQUE (id, type),
    CONSTRAINT ck_transactions_type CHECK (type in ('DEPOSIT', 'BUY'))
);

CREATE TABLE buys (
    id UUID PRIMARY KEY,
    asset_id TEXT NOT NULL,
    quantity NUMERIC NOT NULL,
    price_amount NUMERIC(19, 0) NOT NULL,
    price_currency VARCHAR(3) NOT NULL,
    fee_amount NUMERIC(19, 0) NOT NULL,
    fee_currency VARCHAR(3) NOT NULL,
    acquisition_id UUID UNIQUE NOT NULL,
    type VARCHAR(20) NOT NULL DEFAULT 'BUY',
    CONSTRAINT fk_buys_id_type FOREIGN KEY (id, type) REFERENCES transactions(id, type),
    CONSTRAINT fk_buys_acquisition_id FOREIGN KEY (acquisition_id) REFERENCES acquisitions(id),
    CONSTRAINT ck_buys_price_currency CHECK (price_currency = 'IDR'),
    CONSTRAINT ck_buys_fee_currency CHECK (fee_currency = 'IDR'),
    CONSTRAINT ck_buys_quantity CHECK (scale(quantity) IN (0, 4)),
    CONSTRAINT ck_buys_type CHECK (type = 'BUY')
);

CREATE TABLE deposits (
    id UUID PRIMARY KEY,
    amount_amount NUMERIC(19, 0) NOT NULL,
    amount_currency VARCHAR(3) NOT NULL,
    type VARCHAR(20) NOT NULL DEFAULT 'DEPOSIT',
    CONSTRAINT fk_deposits_id_type FOREIGN KEY (id, type) REFERENCES transactions(id, type),
    CONSTRAINT ck_deposits_amount_currency CHECK (amount_currency = 'IDR'),
    CONSTRAINT ck_deposits_type CHECK (type = 'DEPOSIT')
);

CREATE INDEX idx_acquisitions_portfolio_id ON acquisitions(portfolio_id);
CREATE INDEX idx_transactions_portfolio_id ON transactions(portfolio_id);
