# Value Object: `Dividend`

```java
record Dividend (
    UUID stockId,
    LocalDate cumDate,
    LocalDate exDate,
    LocalDate recordingDate,
    LocalDate paymentDate,
    MonetaryAmount amount
) {}
```

```json5
{
  "stockId": "dc78aa50-f93a-4220-a8d4-fbceedd67bfe",
  "cumDate": "2026-01-20", // LocalDate
  "exDate": "2026-01-20", // LocalDate
  "recordingDate": "2026-01-20", // LocalDate
  "paymentDate": "2026-01-20", // LocalDate
  "amount": { "amount": 120.00, "currency": "IDR" }
}
```