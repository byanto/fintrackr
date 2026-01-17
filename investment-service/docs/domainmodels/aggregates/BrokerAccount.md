# Aggregate: `BrokerAccount`

A `BrokerAccount` represents an individual's investment account held with a brokerage firm.
It aggregates information about the account itself, the associated brokerage,
details of the RDN (Rekening Dana Nasabah - Customer Fund Account),
and a list of portfolios managed within this account.

```java
class BrokerAccount {
    private BrokerAccountId id;
    private String name; // min length: 3, max length: 255
    private Broker broker;
    private Rdn rdn; // balance must be positive
    private List<PortfolioId> portfolioIds;
    
    // TODO: Add methods here
}
```

```json5
{
  "id": {"value":  "ccc2ca2c-f311-426b-ac43-aed46e5394d5"},
  "name": "Stockbit - Budi",
  "broker": {
    "name": "Stockbit",
    "url": "https://stockbit.com"
  },
  "RDN": {
    "bank": "Bank Mandiri",
    "accountNumber": "1234567",
    "balance": {"amount": 100000000, "currency":  "IDR"}
  },
  "portfolios": [
    {"value": "eea81abe-3752-45ff-ba76-faac069ce08c"},
    {"value": "9970d07b-718d-42eb-9afa-43199152322e"}  
  ],
  "createdAt": "2026-01-01T00:00:00.0000000Z",
  "updatedAt": "2026-01-20T00:00:00.0000000Z"
}
```

```yaml
table: 
  - name: broker_account
    columns: 
      - id: [UUID, PRIMARY KEY]
      - name: [VARCHAR(255), NOT NULL]
      - broker_name: [VARCHAR(100), NOT NULL]
      - broker_url: [VARCHAR(255), NOT NULL]
      - rdn_bank: [VARCHAR(100), NOT NULL]
      - rdn_account_number: [VARCHAR(20), NOT NULL]
      - rdn_balance: [DECIMAL(19, 2), NOT NULL]
      - rdn_balance_currency: [VARCHAR(3), NOT NULL]
      - created_at: [DATETIME, NOT NULL]
      - updated_at: [DATETIME, NOT NULL]

  - name: broker_account_portfolios
    columns:
      - portfolio_id: [UUID, UNIQUE, NOT NULL]
      - broker_account_id: [UUID, FOREIGN KEY, NOT NULL]
      - id: [PRIMARY KEY, UNIQUE, NOT NULL]
```
