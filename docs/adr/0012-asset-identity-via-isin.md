# ADR-012: Asset Identity via ISIN; Ticker/Code Demoted to a Display Label

- **Status:** Accepted
- **Date:** 2026-06-11
- **Deciders:** Budi Yanto

## Context

We are about to implement the asset-identity value object and the Asset Catalog, so the identifier model must be settled now — it is the key for `Portfolio.holdings`, the reference on every `Acquisition` and `Transaction`, and the Asset Catalog's lookup key.

The inherited model (domain model §4.3; ADR-006) used `Symbol` — a typed ticker validated as `[A-Z]{4}` for IDX — and ADR-006 declared MutualFund's identifier "modelled as a `Symbol` so the `Asset` contract holds uniformly." That uniformity was assumed, not verified.

Investigation against real source data overturned the premise:

- Indonesian **mutual funds have no ticker**. They are identified by name + investment manager; broker product codes (e.g., `KISI-FIPLUS`) are non-uniform and broker-specific, and KSEI "short codes" run 4–16 characters with no common format.
- Fund **names are unstable** across sources — one fund (ISIN `IDN000053402`) appears as both "Reksa Dana ABF Indonesia Bond Index Fund" and "RD INDEKS ABF IBI FUND."
- **Both stocks and funds carry a KSEI-issued ISIN** (ISO 6166: 2-char ISO-3166 country + 9-char NSIN + 1 Luhn check digit). In the investigation, the ISIN was the *only* identifier consistent across every source, and it is uniform across both v1 asset families.
- A stock's 4-letter "Kode" (e.g., `AADI`) is a **label**, not an identity: it is exchange/vendor-decorated (`BBRI` vs Yahoo's `BBRI.JK`) and conceptually separate from the security.

Constraints in force: correctness above all (this is money); ubiquitous language; and the v1 scope discipline of ADR-006 (defer with *known slots* rather than build speculatively).

## Decision

**An Asset's identity is its ISIN.**

- Rename the identity value object `Symbol` → **`AssetId`**. In v1 it wraps and validates an ISIN: 12 chars, uppercase alphanumeric, ISO-6166 Luhn check digit. A malformed value throws `IllegalArgumentException` (a structural precondition, per ADR-011); `DomainException` is reserved for policy violations.
- Because ISIN format is **uniform across Stock and MutualFund**, `AssetId` needs **no family-specific factories**.
- `AssetId` equality is over the **ISIN alone** — a single-component `record`, so generated `equals`/`hashCode` are correct with no override.
- The human handle (stock ticker / assigned fund code) is **demoted from the identity to a `shortCode` `String` attribute on `Asset`**, beside `name`. The catalog guarantees one `shortCode` per ISIN. `shortCode` is deliberately a plain validated `String`, not a value object.
- The `Asset` contract becomes `AssetId id()`, `String name()`, `String shortCode()`.
- The name is **concept-level (`AssetId`, not `Isin`)** so a future asset lacking an ISIN can be accommodated by broadening one type rather than renaming across `Acquisition`, `Holding`, and `Transaction`. v1 validation remains ISIN-strict; "an asset with no ISIN" is a **known slot**, not built now.
- The ISIN remains directly available via `assetId.value()` for future broker-statement reconciliation and for display.

## Alternatives Considered

- **(A) Keep `Symbol` as the ticker/short code; store ISIN as an attribute** (the inherited model).
    - Pros: matches the ubiquitous term for stocks; ergonomic to type.
    - Cons: funds have no ticker, forcing an arbitrary user-assigned code *as identity*; the ticker is vendor-decorated and occasionally reassigned — a weak identity for a money app; reintroduces a family-specific factory split.
    - Why rejected: identity must be stable and uniform; a ticker is neither, and funds can't supply one.

- **(B) `Symbol{shortCode, ISIN}` with `equals` over the ISIN only.**
    - Pros: keeps the human handle close to the identity in one object.
    - Cons: a value object whose `equals` ignores a component breaks interchangeability — two "equal" instances can carry different `shortCode`s, so the `shortCode` inside a `Map` key is arbitrary and non-deterministic; forces a hand-written `equals` that fights the record; duplicates the label's home (drift risk).
    - Why rejected: the equals-on-ISIN instinct was *correct* — and it proves `shortCode` is not part of the value, so it belongs on `Asset`. Making the ISIN the *whole* value is the clean expression of the same idea.

- **(C) A minted surrogate internal id as identity; ticker/ISIN/KSEI code as attributes.**
    - Pros: decoupled from every external scheme; never collides; uniform.
    - Cons: discards the external anchoring of a perfectly good natural key (ISIN) to solve a multi-source reconciliation problem that does not exist in a single-user, hand-seeded v1; sacrifices ubiquitous-language-as-identity; pure ceremony at this scale.
    - Why rejected: premature (YAGNI). Recorded as the option to revisit only if external identifiers ever prove unstable.

- **(D) `AssetId` content correct, but name the type `Isin`.**
    - Pros: maximally honest about what it holds today.
    - Cons: names the implementation, not the concept; a future no-ISIN asset forces a rename ripple through `Acquisition`/`Holding`/`Transaction`.
    - Why rejected: the concept-level name costs nothing now and saves that ripple later.

- **(E) KSEI short code (e.g., `R-ABFII`) as identity.**
    - Pros: KSEI-issued; shorter than an ISIN.
    - Cons: non-uniform format (4–16 chars), national-scope only, and the long machine codes (`GR003MMSLKDSYA00`) aren't human-usable; not the cross-source-stable field ISIN is.
    - Why rejected: fails the uniformity and stability tests ISIN passes.

## Consequences

### Positive
- One uniform, globally-unique, stable identity across both v1 asset families — and the only field shown consistent across sources.
- `AssetId` is a clean single-component value object: record `equals`/`hashCode` correct for free.
- Identity and human label cleanly separated, in different homes.
- Richer validation than `[A-Z]{4}` — a real ISO-6166 Luhn check digit, a high-value TDD target for a money app.
- Eliminates a bug class: the catalog guarantees one `shortCode` per ISIN, so a mislabeled identity can't be constructed (unlike Alternative B, where `equals` couldn't see it).
- Simplifies persistence (ADR-009): a single `VARCHAR(12)` column.
- The `ofStock`/`ofFund` factory split implied by ADR-008's pattern **dissolves** for the identifier — the design got simpler as it got more correct.

### Negative (costs we explicitly accept)
- The internal key is not the term the investor types. The boundary must resolve `shortCode` → `AssetId`; mitigated because the Asset Catalog already performs boundary resolution (§6.2), so the cost is contained in one place.
- A 12-char ISIN is less friendly to read/type than a ticker; mitigated by `shortCode`-based entry and display at the edge.
- ISIN-strict v1 validation means a (currently hypothetical) no-ISIN asset cannot be recorded until `AssetId` is broadened — accepted as a known slot.

### Neutral / Open Questions
- Whether to enforce more ISIN structure than "12 uppercase-alphanumeric + Luhn" (e.g., first two chars are ISO-3166 letters). Resist hard-coding `ID` (over-fits v1). Decide when implementing.
- `shortCode` validation kept loose (non-blank, trimmed, length cap); the strictness lives on `AssetId`.

## References
- ADR-006 — Asset model & family split (amended 2026-06-11; identity decision lifted here)
- ADR-008 — Quantity/Percentage factory-method pattern (which *dissolves* for `AssetId`)
- ADR-009 — Pure domain + separate JPA model (`AssetId` is a single-column record VO)
- ADR-011 — Exception strategy (malformed ISIN → `IllegalArgumentException`)
- Fintrackr Domain Model §2, §4.3, §6
- ISO 6166 (ISIN); ISO 3166-1 (country codes)