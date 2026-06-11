# Fintrackr — Domain Model v1

- **Status:** Draft
- **Date:** 2026-05-24
- **Author:** Budi Yanto

---

## 1. Purpose & scope

Fintrackr consolidates investment portfolios held across multiple Indonesian broker accounts into one platform, recording every cash and asset movement (buy, sell, dividend, deposit, withdrawal) so the owner can see aggregated holdings, per-acquisition performance, and total return including dividends.

- **Single currency:** Indonesian Rupiah (IDR). No FX in v1.
- **Single user:** personal-use; no multi-tenancy in v1.
- **Manual data entry** for v1. Importer/API integration deferred.

---

## 2. Ubiquitous Language

Terms in this section have *precise* meanings in code and conversation. Use them consistently.

| Term                               | Definition                                                                                                                                                                                                            |
|------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Broker Account**                 | A regulated account with an Indonesian brokerage (e.g., Stockbit, Mandiri Sekuritas). Holds exactly one RDN.                                                                                                          |
| **RDN**                            | *Rekening Dana Nasabah* — the regulated cash balance at a broker, held in a partner bank under OJK rules. One per Broker Account.                                                                                     |
| **Portfolio**                      | A logical grouping of investments inside a Broker Account (e.g., "Long-Term," "Trading"). Has its own *trading balance* — an allocated slice of the broker's RDN.                                                     |
| **Trading Balance**                | Cash available within a Portfolio for new investments. Sum of all Portfolio trading balances under a Broker Account equals the Broker Account's RDN.                                                                  |
| **Asset**                          | A tradable instrument: Stock, Mutual Fund, Bond, or Savings. Lives in the Asset Catalog context. Identified by AssetId (its ISIN).                                                                                    |
| **Asset Id**                       | The stable identity of an Asset: its ISIN (ISO 6166 — 12 chars, Luhn check digit). Equality is over the ISIN alone. Format-validated in its constructor; existence-validated only at system boundary.                 |
| **Short Code**                     | An Asset's human handle, a stock's IDX ticker (e.g., AADI) or an assigned fund code. A display/lookup label on the Asset, never the identity; not guaranteed stable across vendors.                                   |
| **Acquisition**                    | One immutable record of a single Buy decision: open date, open price, open fee, initial quantity. The unit of cost-basis tracking under Specific Identification.                                                      |
| **Holding**                        | Derived/cached view of all open Acquisitions for a single Symbol within one Portfolio. Displays aggregate quantity, weighted average cost (for display only), invested value, market value, total dividends received. |
| **Transaction**                    | An event that changes Portfolio state: Buy, Sell, Dividend, Deposit, or Withdrawal. Immutable once recorded; corrections happen by reversing transactions.                                                            |
| **Sell Allocation**                | Value object inside a Sell that specifies which Acquisition is being consumed and how many shares from it. A Sell has one or more.                                                                                    |
| **Dividend Allocation**            | Value object attached to a Acquisition recording the dividend received for that Acquisition at a given cum-date.                                                                                                      |
| **Cum-date**                       | Cumulative date — the last date an investor must hold the stock to be eligible for a declared dividend.                                                                                                               |
| **Acquisition Selection Strategy** | The rule that decides which Acquisition(s) a Sell consumes: FIFO, LIFO, Highest Cost, Lowest Cost, or Manual (user-specified).                                                                                        |
| **Fee Structure**                  | The buy and sell fee rates configured at the Broker Account level. Used to compute expected fees, which the user can override.                                                                                        |
| **Cost Basis**                     | The per-lot purchase price including fees, used to compute realized P&L on sells.                                                                                                                                     |
| **Realized P&L**                   | Profit or loss locked in by a Sell, computed per consumed Acquisition: `(sellPrice − acquisitionOpenPrice) × shares − allocatedFee`.                                                                                  |
| **Per-Acquisition Total Return**   | Capital gain plus dividends received during the acquisition's holding period — the user's preferred performance metric.                                                                                               |

---

## 3. Bounded contexts

Three contexts, each with a clear responsibility.

```
                 ┌───────────────────────┐
                 │   Asset Catalog       │   reference data
                 │   (Asset Ids, Stocks, │   lifecycle: external
                 │    Mutual Funds, etc.)│   referenced by AssetId
                 └───────────▲───────────┘
                             │ AssetId lookup
                             │ (boundary validation only)
                             │
┌────────────────────────┐   │    ┌─────────────────────────────────┐
│   Brokerage            │◀──┴─── │   Portfolio Management          │
│                        │        │                                 │
│   Broker Account       │        │   Portfolio (aggregate)         │
│   RDN                  │◀───────│  Acquisitions, Sells, Dividends │
│   Fee Structure        │ fee +  │   Holdings (derived)            │
│                        │ cash   │   Trading Balance               │
└────────────────────────┘        └─────────────────────────────────┘

Dependency direction: Portfolio Management → Brokerage, through Brokerage's
published API. PM's RecordBuyUseCase calls computeBuyFee + applyCashFlow in
one transaction (ADR-003, amended Session 004). Brokerage does NOT depend on
Portfolio Management.
```

- **Portfolio Management** — the heart of the system. Owns Acquisitions, Sells, Dividends, Holdings, Trading Balance. Where most of the business logic lives.
- **Brokerage** — owns the BrokerAccount, the RDN, and the fee configuration. Coordinates the cross-aggregate cash invariant.
- **Asset Catalog** — reference data. Knows what symbols exist, their names, types, and (eventually) current prices. Lifecycle is *external* — driven by market data, not by user actions.

---

## 4. Portfolio Management context

### 4.1 Aggregate: `Portfolio`

The single aggregate root in this context. **All mutation goes through Portfolio's methods** (Tell, Don't Ask). Direct setters on internal entities are not exposed.

```
Portfolio (aggregate root, entity)
├─ id
├─ brokerAccountId           ← reference to Brokerage context
├─ name (e.g., "Long-Term")
├─ tradingBalance: Money     ← allocated slice of broker RDN
├─ defaultAcquisitionSelectionStrategy: AcquisitionSelectionStrategy
├─ acquisitions: List<Acquisition>           ← immutable on creation
├─ transactions: List<Transaction>  ← append-only ledger (Buy, Sell, Dividend, Deposit, Withdrawal)
└─ holdings: Map<AssetId, Holding>   ← cached, derived from open Acquisitions (per ADR-004)
```

### 4.2 Entities

#### `Acquisition` (immutable after creation)

```
Acquisition
├─ id
├─ portfolioId
├─ assetId: AssetId
├─ openDate
├─ openPrice: Money
├─ openFee: Money
├─ initialQuantity: Quantity        ← never changes
├─ remainingQuantity (derived)      ← initialQuantity − Σ SellAllocations against this Acquisition
├─ status (derived)                 ← OPEN | PARTIALLY_CLOSED | CLOSED
└─ dividendAllocations: List<DividendAllocation>
```

Created by `recordBuy`. Mutated *only* by referenced SellAllocations and DividendAllocations — the Acquisition itself is never directly edited.

#### `Transaction` (sealed hierarchy)

```java
sealed interface Transaction
    permits Buy, Sell, Dividend, Deposit, Withdrawal { }
```

| Subtype | Attributes                                                                                                        |
|---|-------------------------------------------------------------------------------------------------------------------|
| `Buy` | id, portfolioId, assetId, date, quantity, price, fee, acquisitionId (the Acquisition it opened)                   |
| `Sell` | id, portfolioId, assetId, date, pricePerShare, totalFee, allocations: List\<SellAllocation\>                      |
| `Dividend` | id, portfolioId, assetId, cumDate, paymentDate, dps (dividend per share), allocations: List\<DividendAllocation\> |
| `Deposit` | id, portfolioId, amount, date, source (e.g., "RDN")                                                               |
| `Withdrawal` | id, portfolioId, amount, date, destination                                                                        |

All Transactions are immutable once recorded. Corrections are made by recording a reversing transaction, never by editing.

#### `Holding` (derived/cached aggregate view)

```
Holding (derived)
├─ portfolioId
├─ assetId
├─ totalQuantity         = Σ openAcquisitions.remainingQuantity
├─ averageCost (display) = weighted average of open acquisitions' openPrice (for display only — not the basis of accounting)
├─ totalInvested         = Σ openAcquisitions.remainingQuantity × openAcquisitions.openPrice + openFee proportion
├─ totalDividendsReceived = Σ dividendAllocations across this symbol's acquisitions
└─ openAcquisitions: List<Acquisition>
```

`Holding` is *not* an independent persisted entity in the domain sense; it is a cached aggregation of open Acquisitions for fast read. Per ADR-004, it stays in sync because all mutation goes through the `Portfolio` aggregate.

### 4.3 Value Objects

| Value Object                                                                     | Shape                                 | Validation |
|----------------------------------------------------------------------------------|---------------------------------------|---|
| `AssetId(String value)`                                                          | Typed asset identity wrapping an ISIN | 12 chars; uppercase alphanumeric; ISO-6166 Luhn check digit; malformed → `llegalArgumentException` (ADR-011); equality over the ISIN |
| `Money(BigDecimal amount, Currency currency)`                                    | All monetary values                   | amount ≥ 0 for balances; signed for deltas; rounding mode TBD (see ADR-007) |
| `Quantity(BigDecimal value)`                                                     | Shares / units                        | value > 0 for transactions; ≥ 0 for derived state |
| `Percentage(BigDecimal rate)`                                                    | Used for fee rates                    | 0 ≤ rate ≤ 1 |
| `SellAllocation(acquisitionId, sharesSoldFromAcquisition, feeAllocated)`         | Inside `Sell`                         | sharesSoldFromAcquisition > 0; sharesSoldFromAcquisition ≤ acquisition.remainingQuantity at sell time; feeAllocated proportional |
| `DividendAllocation(cumDate, paymentDate, dps, sharesEligibleAtCumDate, amount)` | Attached to `Acquisition`             | sharesEligibleAtCumDate > 0; amount = sharesEligibleAtCumDate × dps |
| `AcquisitionSelectionStrategy`                                                   | Sealed interface                      | See below |

#### `AcquisitionSelectionStrategy` (sealed)

```java
sealed interface AcquisitionSelectionStrategy
    permits Fifo, Lifo, HighestCost, LowestCost, Manual { }

public record Fifo()                                  implements AcquisitionSelectionStrategy {}
public record Lifo()                                  implements AcquisitionSelectionStrategy {}
public record HighestCost()                           implements AcquisitionSelectionStrategy {}
public record LowestCost()                            implements AcquisitionSelectionStrategy {}
public record Manual(List<SellAllocation> allocations) implements AcquisitionSelectionStrategy {}
```

Each Portfolio has a `defaultAcquisitionSelectionStrategy`. Every `recordSell` call can optionally override it (most commonly by passing `Manual`). Exhaustive switch pattern matching enforces compile-time handling of every variant.

### 4.4 Business methods (on `Portfolio` aggregate)

All in business language. Each enforces its invariants internally before changing state.

| Method                                                             | Purpose |
|--------------------------------------------------------------------|---|
| `recordBuy(assetId, quantity, price, fee, date)`                   | Opens a new Acquisition; updates Holding cache; decrements tradingBalance; **returns the cash delta moved (`quantity × price + fee` as `Money`)** so the orchestrating app service applies that exact value to RDN (single source of truth — no recomputation, no drift); emits `BuyRecorded` event |
| `recordSell(assetId, quantity, pricePerShare, fee, date, strategy)` | Resolves allocations via strategy; validates against open Acquisitions; updates referenced Acquisitions (derived state changes); updates Holding cache; increments tradingBalance; emits `SellRecorded` event |
| `recordDividend(assetId, dps, cumDate, paymentDate)`                | For every eligible Acquisition of `symbol`, appends a DividendAllocation; increments tradingBalance; emits `DividendReceived` event |
| `recordDeposit(amount, date, source)`                              | Increments tradingBalance from external cash inflow; emits `DepositRecorded` |
| `recordWithdrawal(amount, date, destination)`                      | Decrements tradingBalance; emits `WithdrawalRecorded` |
| `transferTo(targetPortfolioId, amount, date)`                      | Moves cash between portfolios under the same broker (preserves RDN total) |

### 4.5 Domain events emitted

Typed events (Spring Modulith `@ApplicationModuleListener` consumers):

- `BuyRecorded(portfolioId, acquisitionId, assetId, quantity, price, fee, date)`
- `SellRecorded(portfolioId, sellId, assetId, allocations, pricePerShare, fee, date)`
- `DividendReceived(portfolioId, assetId, allocations, paymentDate)`
- `DepositRecorded(portfolioId, amount, date, source)`
- `WithdrawalRecorded(portfolioId, amount, date, destination)`
- `TradingBalanceChanged(portfolioId, delta, newBalance)` ← consumed by Brokerage to sync RDN. The cash sync to BrokerAccount RDN is performed by application-service orchestration in one transaction per ADR-003, not by a domain event in v1. A `TradingBalanceChanged` integration event is deferred until a read-model consumer needs it or the modules are split into services.

---

## 5. Brokerage context

### 5.1 Aggregate: `BrokerAccount`

```
BrokerAccount (aggregate root, entity)
├─ id
├─ name (e.g., "Stockbit")
├─ rdn: Money                ← regulated cash balance
├─ feeStructure: FeeStructure
└─ portfolioIds: Set<PortfolioId>
```

### 5.2 Value Objects

```
FeeStructure (value object)
├─ buyRate:  Percentage   (e.g., 0.0015 for Stockbit)
└─ sellRate: Percentage   (e.g., 0.0025 for Stockbit)
```

### 5.3 Business methods

| Method | Purpose |
|---|---|
| `depositToBroker(amount, date)` | External cash inflow → increases RDN. User then allocates to a Portfolio via `Portfolio.recordDeposit`. |
| `withdrawFromBroker(amount, date)` | External cash outflow → decreases RDN. Originates from a Portfolio's `recordWithdrawal`. |
| `updateFeeStructure(newStructure)` | Changes fee rates. Affects only future transactions; historical fees are preserved as recorded (no FeeStructureHistory needed). |
| `applyCashFlow(delta, date)` | Applies a cash delta to RDN. Invoked by Portfolio Management's use-case application service (e.g., `RecordBuyUseCase`) through Brokerage's published API, within the same database transaction (per ADR-003, amended Session 004). Not event-driven in v1. |

### 5.4 Fee handling policy

- **Compute by default, allow override.** When a Buy is recorded with no explicit fee, the system computes `quantity × price × buyRate` and pre-fills it. Same for Sell (`quantity × price × sellRate`). The user may override.
- **Fee rate changes are not retroactive.** Updating `feeStructure` affects new transactions only. Historical transactions keep their recorded fees forever.

---

## 6. Asset Catalog context

Reference data. Lightweight in v1.

### 6.1 Aggregate: `Asset` (sealed hierarchy — see future ADR-006)

```java
sealed interface Asset permits Stock, MutualFund {
    AssetId id();
    String name();
    String shortCode();
}
```

- **Stock** — id, name, shortCode (IDX ticker), sector, currentPrice (eventually fed by external market data), priceAsOf
- **MutualFund** — id, name, shortCode (assigned fund code), currentNAV, navAsOf

V1 implementation: small in-memory list seeded manually (Stockbit's most common Indonesian tickers). Future: external feed integration.

### 6.2 Boundary validation

Portfolio Management receives an asset reference from outside (UI, API, importer), typically a `shortCode` the user typed, or an `AssetId`; it queries Asset Catalog: *"is this Symbol known?"* If not, the operation is rejected at the boundary; Inside the Portfolio aggregate, `AssetId` existence is trusted.

---

## 7. Cross-context invariants & communication

### 7.1 The cash invariant (per ADR-003)

> `sum(brokerAccount.portfolios.tradingBalance) == brokerAccount.rdn` must always hold.

Enforced by application-service orchestration in a single database transaction (per ADR-003). A use-case application service loads the affected Portfolio and its BrokerAccount, records the transaction on the Portfolio (changing its `tradingBalance`), applies the matching cash-flow delta to the BrokerAccount's RDN, and commits both in one transaction. The cash math lives inside the aggregates; the application service only orchestrates and owns the transaction boundary. Cross-module calls go through each module's public API (enforced by Spring Modulith).

Domain events (`BuyRecorded`, `SellRecorded`, …) may be emitted as a record of what happened and consumed by read models or audit, but they do not drive the RDN sync in v1. The invariant does not depend on event delivery.
Migration to event-driven eventual consistency (transactional outbox + listener) is a future option if Brokerage and Portfolio Management are split into separate services — at which point a single ACID transaction across two databases is no longer possible, and a saga/outbox replaces the orchestrated transaction.

### 7.2 Asset existence validation

Boundary check only: Portfolio Management consults Asset Catalog when accepting a new asset reference (`shortCode`/`AssetId`) from outside. Internal references trust the data.

---

## 8. Per-aggregate invariants (consolidated)

### `Portfolio`
- `tradingBalance ≥ 0`
- Cannot record a Buy if `(quantity × price + fee) > tradingBalance` (v1 — ignore T+2 settlement)
- Cannot record a Sell with allocations whose total exceeds available open lot quantities
- For every SellAllocation: `sharesSoldFromAcquisition ≤ acquisition.remainingQuantity` at sell time
- For every SellAllocation: `sharesSoldFromAcquisition > 0`
- Sum of `sharesSoldFromAcquisition` across a Sell's allocations equals the Sell's total quantity
- All transaction dates `≤ today` (no future-dated transactions)
- All quantities `> 0`, prices `> 0`, fees `≥ 0`
- Dividend allocations only on Acquisitions where `openDate ≤ cumDate AND sharesEligibleAtCumDate > 0`
- Acquisitions are immutable after creation (only derived `remainingQuantity` and `status` change)
- At most one active Holding per AssetId per Portfolio

### `BrokerAccount`
- `rdn ≥ 0`
- `feeStructure.buyRate` and `feeStructure.sellRate` both in `[0, 1]`
- Cross-aggregate (see §7.1): `sum(portfolios.tradingBalance) == rdn`

### `Asset` (catalog)
- `AssetId` is unique within the catalog
- `shortCode` is unique within the catalog so user input resolves unambiguously

---

## 9. Tactical guidance — patterns to follow

- **Tell, Don't Ask.** Methods on aggregates use business language (`recordBuy`, not `setHoldings`). No public setters on internal entities. Callers express intent; aggregates enforce rules.
- **Primitive obsession is forbidden.** No raw `String` identifiers, no raw `BigDecimal` money, no raw `int` quantities. Every primitive concept gets a typed value object with validation in its constructor.
- **Immutability bias.** Acquisitions, transactions, value objects are immutable once created. Mutations happen by appending new events (Sells, DividendAllocations) that reference the original.
- **Boundary validation.** Validate at the edges. Trust data inside the domain.
- **Domain events are typed and module-internal** (Spring Modulith `@ApplicationModuleListener`). No string-named topics in v1.

---

## 10. Out of scope (v1) — recap

- Multi-currency / FX
- Real-time market price feeds
- Multi-user / multi-tenant
- Tax reporting
- Brokerage API integration (manual entry only)
- Mobile app (REST API + minimal web UI is enough)
- T+2 settlement timing (assume immediate cash settlement)
- Fee structure rate history (current rates apply to new transactions only)

---

## 11. Open questions / TODOs

These are deliberate parking lots. Track in `backlog.md`; revisit when implementing.

- **BigDecimal rounding mode** (HALF_UP vs HALF_EVEN / banker's rounding) — Resolved in ADR-007
- **Asset sealed hierarchy details** (exact attributes per subtype, especially Mutual Fund NAV refresh policy) — Resolved in ADR-006
- **Closed-acquisitions archival** — at what point do fully closed Acquisitions move out of the active aggregate footprint?
- **Database strategy** — single schema, schema-per-module, or database-per-module — future ADR candidate
- **Asset Catalog seeding** — manual list vs CSV vs external feed
- **Sync vs async events between modules** — start synchronous; revisit if needed

---

## 12. References

- ADR-001 — Modular monolith with Spring Modulith
- ADR-002 — Testing strategy
- ADR-003 — Cross-aggregate cash invariant
- ADR-004 — Transaction ledger as source of truth; Holding cached
- ADR-005 — Specific Identification cost basis with per-lot dividend attribution
- ADR-006 — Asset model and family split
- ADR-012 — Asset identity via ISIN
- Fintrackr Vision (`01-fintrackr-vision.md`)
- Eric Evans, *Domain-Driven Design*
- Vaughn Vernon, *Implementing Domain-Driven Design* — aggregate design chapters
