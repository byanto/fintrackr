CREATE TABLE broker_accounts (
    id UUID PRIMARY KEY,
    name TEXT NOT NULL,
    rdn_amount NUMERIC(19, 0) NOT NULL,
    rdn_currency VARCHAR(3) NOT NULL,
    fee_structure_buy_rate NUMERIC(7, 6) NOT NULL,
    fee_structure_sell_rate NUMERIC(7, 6) NOT NULL,
    CONSTRAINT ck_broker_accounts_rdn_currency CHECK (rdn_currency = 'IDR')
);
