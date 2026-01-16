# Entity: `Stock`

```java
class Stock {
    // TODO: Add methods here
}
```

```json5
{
  "id": "dc78aa50-f93a-4220-a8d4-fbceedd67bfe",
  "symbol": "BBCA",
  "name": "Bank Central Asia Tbk.",
  "currentPrice": {"amount": 100000000, "currency":  "IDR"},
  "priceUpdatedAt": "2026-01-20T00:00:00.0000000Z",
  "createdAt": "2026-01-01T00:00:00.0000000Z",
  "updatedAt": "2026-01-20T00:00:00.0000000Z"
}
```

```yaml
table
  - name: stocks
    columns:
      - id: [UUID, UNIQUE, PRIMARY KEY]
      - symbol: [VARCHAR(12), UNIQUE, NOT NULL]
      - name: [VARCHAR(100), UNIQUE, NOT NULL]
      - current_price: [DECIMAL(19, 2), NOT NULL] # Current price of the stock
      - current_price_currency: [VARCHAR(3), NOT NULL] # Currency of the current price
      - price_updated_at: [TIMESTAMP, NOT NULL] # Timestamp of the last price update,
      - created_at: [TIMESTAMP, NOT NULL] # Timestamp of when the stock was first added,
      - updated_at: [TIMESTAMP, NOT NULL] # Timestamp of the last update to the stock record
```