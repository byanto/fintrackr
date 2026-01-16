# Aggregate: `Portfolio`

## Class

```java
class Portfolio {
    // TODO: Add methods here
    void addTransaction();
}
```

## Attributes

```json5
{
  "id": {"value":  "eea81abe-3752-45ff-ba76-faac069ce0"}, 
  "name": "My Investment Portfolio",
  "cashBalance": {"amount": 150000, "currency":  "IDR"},
  "investedValue": {"amount": 85000000, "currency":  "IDR"},
  "marketValue": {"amount": 90000000, "currency":  "IDR"},
  "profitLoss": {"amount": 5000000, "currency":  "IDR"},
  "totalEquity": {"amount": 90500000, "currency":  "IDR"},
  "brokerAccountId": {"value": "ccc2ca2c-f311-426b-ac43-aed46e5394d5"},
  "holdings": [
    {
      "currentPrice": {"amount": 10000, "currency":  "IDR"},
      "quantity": 500,
      "averagePrice": {"amount": 9765, "currency":  "IDR"},
      "amount": {"amount": 4882500, "currency":  "IDR"}, // quantity * average price
      "fee": {"amount": 9277, "currency":  "IDR"}, 
      "investedValue": {"amount": 4891777, "currency":  "IDR"}, // (quantity * average price) + fee 
      "marketValue":  {"amount": 5000000, "currency":  "IDR"}, // quantity * current price
      "profitLoss": {"amount": 108223, "currency":  "IDR"}, // marketValue - investedValue
      "profitLossPercentage": 0.0221,
      "profitLossPercentageText": "2.21%",
      "portfolioAllocation": 0.1058,
      "portfolioAllocationText": "10.58%",
      "stockId": {"value": "dc78aa50-f93a-4220-a8d4-fbceedd67bfe"},
      "holdingItems": [
        {
          "id": {"value": "971a91da-ab37-49d8-93ea-dfc6efcc9def"},
          "buyingDate": "2024-12-02T00:00:00.0000000Z",
          "quantity": 200,
          "buyingPrice": {"amount": 9825, "currency":  "IDR"},
          "amount": {"amount": 1965000, "currency":  "IDR"}, // quantity * buyingPrice
          "buyingFee": {"amount": 3734, "currency":  "IDR"},
          "investedValue": {"amount": 1968734, "currency":  "IDR"}, // amount + buyingFee
          "investedPrice": {"amount": 9844, "currency":  "IDR"}, // investedValue / quantity
          "dividendReceived": {"amount": 61000, "currency":  "IDR"},
          "dividendReceivedPercentage": 0.0310, // totalDividend / investedValue
          "dividendReceivedPercentageText": "3.10%",
          "dividends": [
            {"dividendId": {"value": "dc78aa50-f93a-4220-a8d4-fbceedd67bfe"}},
            {"dividendId": {"value": "61df26b5-929e-4eb2-8a36-b05b6d8a0062"}}
          ],
          "transactions": [
            {
              "date": "2024-12-02T00:00:00.0000000Z",
              "type": "BUY",
              "quantity": 200,
              "price": {"amount": 9825, "currency":  "IDR"},
              "amount": {"amount": 1965000, "currency":  "IDR"}, // quantity * price
              "fee": {"amount": 3734, "currency":  "IDR"},
              "netAmount": {"amount": 1968734, "currency":  "IDR"}, // amount + fee
              "createdAt": "2024-12-02T00:00:00.0000000Z"
            }
          ],
          "createdAt": "2024-12-02T00:00:00.0000000Z",
          "updatedAt": "2024-12-02T00:00:00.0000000Z"
        },
        {
          "id": {"value": "efe06ef9-8957-41bf-84b9-ae76fff16d10"},
          "buyingDate": "2024-12-19T00:00:00.0000000Z",
          "quantity": 300,
          "buyingPrice": {"amount": 9725, "currency":  "IDR"},
          "amount": {"amount": 2917500, "currency":  "IDR"}, // quantity * buyingPrice
          "buyingFee": {"amount": 5543, "currency":  "IDR"},
          "investedValue": {"amount": 2923043, "currency":  "IDR"}, // amount + buyingFee
          "investedPrice": {"amount": 9743, "currency":  "IDR"}, // investedValue / quantity
          "dividendReceived": {"amount": 91500, "currency":  "IDR"},
          "dividendReceivedPercentage": 0.3130, // totalDividend / investedValue
          "dividendReceivedPercentageText": "3.13%",
          "dividends": [
            {"dividendId": {"value": "dc78aa50-f93a-4220-a8d4-fbceedd67bfe"}},
            {"dividendId": {"value": "61df26b5-929e-4eb2-8a36-b05b6d8a0062"}}
          ],
          "transactions": [
            {
              "date": "2024-12-19T00:00:00.0000000Z",
              "type": "BUY",
              "quantity": 300,
              "price": {"amount": 9725, "currency":  "IDR"},
              "amount": {"amount": 2917500, "currency":  "IDR"}, // quantity * price
              "fee": {"amount": 5543, "currency":  "IDR"},
              "netAmount": {"amount": 2923043, "currency":  "IDR"}, // amount + fee
              "createdAt": "2024-12-19T00:00:00.0000000Z"
            }
          ],
          "createdAt": "2024-12-19T00:00:00.0000000Z",
          "updatedAt": "2024-12-19T00:00:00.0000000Z"
        }
      ],
      "createdAt": "2024-12-02T00:00:00.0000000Z",
      "updatedAt": "2024-12-19T00:00:00.0000000Z"
    },
    {
      "currentPrice": {"amount": 10000, "currency":  "IDR"},
      "quantity": 500,
      "averagePrice": {"amount": 9765, "currency":  "IDR"},
      "amount": {"amount": 4882500, "currency":  "IDR"}, // quantity * average price
      "fee": {"amount": 9277, "currency":  "IDR"},
      "investedValue": {"amount": 4891777, "currency":  "IDR"}, // (quantity * average price) + fee 
      "marketValue":  {"amount": 5000000, "currency":  "IDR"}, // quantity * current price
      "profitLoss": {"amount": 108223, "currency":  "IDR"}, // marketValue - investedValue
      "profitLossPercentage": 0.0221,
      "profitLossPercentageText": "2.21%",
      "portfolioAllocation": 0.1058,
      "portfolioAllocationText": "10.58%",
      "stockId": {"value": "dc78aa50-f93a-4220-a8d4-fbceedd67bfe"},
      "holdingItems": [
        {
          "id": {"value": "eacfb1dd-c8b3-4f45-bd5c-03876d835c57"},
          "buyingDate": "2025-01-13T00:00:00.0000000Z",
          "quantity": 500,
          "buyingPrice": {"amount": 3900, "currency":  "IDR"},
          "amount": {"amount": 1950000, "currency":  "IDR"}, // quantity * buyingPrice
          "buyingFee": {"amount": 3705, "currency":  "IDR"},
          "investedValue": {"amount": 1953705, "currency":  "IDR"}, // amount + buyingFee
          "investedPrice": {"amount": 3907, "currency":  "IDR"}, // investedValue / quantity
          "dividendReceived": {"amount": 172700, "currency":  "IDR"},
          "dividendReceivedPercentage": 0.0884, // totalDividend / investedValue
          "dividendReceivedPercentageText": "8.84%",
          "dividends": [
            {"dividendId": {"value": "dc78aa50-f93a-4220-a8d4-fbceedd67bfe"}},
            {"dividendId": {"value": "61df26b5-929e-4eb2-8a36-b05b6d8a0062"}}
          ],
          "transactions": [
            {
              "date": "2025-01-13T00:00:00.0000000Z",
              "type": "BUY",
              "quantity": 500,
              "price": {"amount": 3900, "currency":  "IDR"},
              "amount": {"amount": 1950000, "currency":  "IDR"}, // quantity * price
              "fee": {"amount": 3705, "currency":  "IDR"},
              "netAmount": {"amount": 1953705, "currency":  "IDR"}, // amount + fee
              "createdAt": "2025-01-13T00:00:00.0000000Z"
            }
          ],
          "createdAt": "2025-01-13T00:00:00.0000000Z",
          "updatedAt": "2025-01-13T00:00:00.0000000Z"
        },
        {
          "id": {"value": "108c9bc7-a857-483d-8e0a-cec0dbe62733"},
          "buyingDate": "2025-01-14T00:00:00.0000000Z",
          "quantity": 500,
          "buyingPrice": {"amount": 3830, "currency":  "IDR"},
          "amount": {"amount": 1915000, "currency":  "IDR"}, // quantity * buyingPrice
          "buyingFee": {"amount": 3639, "currency":  "IDR"},
          "investedValue": {"amount": 1918639, "currency":  "IDR"}, // amount + buyingFee
          "investedPrice": {"amount": 3837, "currency":  "IDR"}, // investedValue / quantity
          "dividendReceived": {"amount": 172700, "currency":  "IDR"},
          "dividendReceivedPercentage": 0.900, // totalDividend / investedValue
          "dividendReceivedPercentageText": "9.00%",
          "dividends": [
            {"dividendId": {"value": "dc78aa50-f93a-4220-a8d4-fbceedd67bfe"}},
            {"dividendId": {"value": "61df26b5-929e-4eb2-8a36-b05b6d8a0062"}}
          ],
          "transactions": [
            {
              "date": "2025-01-14T00:00:00.0000000Z",
              "type": "BUY",
              "quantity": 500,
              "price": {"amount": 3830, "currency":  "IDR"},
              "amount": {"amount": 1915000, "currency":  "IDR"}, // quantity * price
              "fee": {"amount": 3639, "currency":  "IDR"},
              "netAmount": {"amount": 1918639, "currency":  "IDR"}, // amount + fee
              "createdAt": "2025-01-13T00:00:00.0000000Z"
            }
          ],
          "createdAt": "2025-01-14T00:00:00.0000000Z",
          "updatedAt": "2025-01-14T00:00:00.0000000Z"
        }
      ],
      "createdAt": "2025-01-13T00:00:00.0000000Z",
      "updatedAt": "2025-01-14T00:00:00.0000000Z"
    }
  ],
  "createdAt": "2024-12-01T00:00:00.0000000Z",
  "updatedAt": "2025-01-16T00:00:00.0000000Z"
}
```

## Database Tables
```yaml
table: 
  - name: portfolios
    columns:
      - id: [UUID, PRIMARY KEY, UNIQUE, NOT NULL]
      - name: [VARCHAR(100), NOT NULL]
      - cash_balance: [DECIMAL(19,2), NOT NULL]
      - cash_balance_currency: [VARCHAR(3), NOT NULL]
      - broker_account_id: [UUID, FOREIGN KEY, NOT NULL]
      - created_at: [DATETIME, NOT NULL]
      - updated_at: [DATETIME, NOT NULL]
        
  - name: portfolio_holdings
    columns:
      - id: [UNIQUE, NOT NULL, AUTO_INCREMENT]
      - portfolio_id: [UUID, FOREIGN KEY, NOT NULL]
      - stock_id: [UUID, FOREIGN KEY, NOT NULL]
      - PRIMARY KEY: [portfolio_id, stock_id]
  
  - name: portfolio_holding_items
    columns:
        - id: [UUID, PRIMARY KEY, UNIQUE, NOT NULL]
        - buying_date: [DATETIME, NOT NULL]
        - quantity: [NUMBER, NOT NULL]
        - buying_price: [DECIMAL(19,2), NOT NULL]
        - buying_price_currency: [VARCHAR(3), NOT NULL]
        - holding_id: [UNIQUE, FOREIGN KEY, NOT NULL]
      
- name: portfolio_holding_item_dividends
  columns:
    - id: [UNIQUE, NOT NULL, AUTO INCREMENT] 
    - holding_item_id: [UUID, FOREIGN KEY, UNIQUE, NOT NULL]
    - dividend_id: [FOREIGN KEY, UNIQUE, NOT NULL]
    - PRIMARY KEY: [holding_item_id, dividend_id]

- name: portfolio_holding_item_transactions
  columns:
    - id: [UNIQUE, NOT NULL, AUTO INCREMENT]
    - holding_item_id: [UUID, FOREIGN KEY, UNIQUE, NOT NULL]
    - transaction_id: [FOREIGN KEY, UNIQUE, NOT NULL]
    - PRIMARY KEY: [holding_item_id, transaction_id]
```

## Domain Events
```yaml
```