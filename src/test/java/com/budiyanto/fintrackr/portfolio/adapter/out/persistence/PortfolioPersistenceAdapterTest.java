package com.budiyanto.fintrackr.portfolio.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.budiyanto.fintrackr.TestcontainersConfiguration;
import com.budiyanto.fintrackr.portfolio.domain.model.Buy;
import com.budiyanto.fintrackr.portfolio.domain.model.Portfolio;
import com.budiyanto.fintrackr.portfolio.domain.model.PortfolioId;
import com.budiyanto.fintrackr.portfolio.domain.model.Transaction;
import com.budiyanto.fintrackr.portfolio.domain.model.TransactionId;
import com.budiyanto.fintrackr.shared.AssetId;
import com.budiyanto.fintrackr.shared.BrokerAccountId;
import com.budiyanto.fintrackr.shared.Money;
import com.budiyanto.fintrackr.shared.Quantity;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

@DataJpaTest
@Import({TestcontainersConfiguration.class, PortfolioPersistenceAdapter.class, PortfolioPersistenceMapper.class})
class PortfolioPersistenceAdapterTest {

    private final PortfolioPersistenceAdapter adapter;
    private final TestEntityManager entityManager;
    private final JdbcTemplate jdbcTemplate;

    private static final String PORTFOLIO_NAME = "Test Portfolio";
    private static final BrokerAccountId BROKER_ACCOUNT_ID = BrokerAccountId.generate();

    private static final Money INITIAL_DEPOSIT = Money.of(new BigDecimal("5000000"));

    private static final AssetId ASSET_ID = AssetId.of("ID1000109507"); // BBCA ISIN
    private static final Quantity QUANTITY = Quantity.ofShares(new BigDecimal("1000"));
    private static final Money BUY_PRICE = Money.of(new BigDecimal("2000"));
    private static final Money BUY_FEE = Money.of(new BigDecimal("3000"));

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-18T00:00:00Z"), ZoneOffset.UTC);
    private static final LocalDate TODAY = LocalDate.now(CLOCK);
    private static final LocalDate DEPOSIT_DATE = TODAY.minusDays(10);
    private static final LocalDate BUY_DATE = TODAY.minusDays(5);

    @Autowired
    PortfolioPersistenceAdapterTest(PortfolioPersistenceAdapter adapter, TestEntityManager entityManager, JdbcTemplate jdbcTemplate) {
        this.adapter = adapter;
        this.entityManager = entityManager;
        this.jdbcTemplate = jdbcTemplate;
    }

    @BeforeEach
    void setup() {
        String insertBrokerAccountSql = "INSERT INTO broker_accounts(id, name, rdn_amount, rdn_currency, fee_structure_buy_rate, fee_structure_sell_rate) VALUES (?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(insertBrokerAccountSql, BROKER_ACCOUNT_ID.value(), "Test Broker Account", BigDecimal.ZERO, "IDR", new BigDecimal("0.0015"), new BigDecimal("0.0025"));
    }

    @Test
    @DisplayName("Write columns the schema expects when save")
    void should_writeColumnsTheSchemaExpects_when_save() {
        // Given
        Portfolio savedPortfolio = savedPortfolio();

        // When
        Map<String, Object> row = jdbcTemplate.queryForMap(
                "SELECT version, broker_account_id, name, trading_balance_amount, trading_balance_currency FROM portfolios WHERE id = ?",
                savedPortfolio.id().value()
        );

        // Then
        assertThat(row).containsEntry("version", 0L);
        assertThat(row).containsEntry("broker_account_id", BROKER_ACCOUNT_ID.value());
        assertThat(row).containsEntry("name", PORTFOLIO_NAME);
        assertThat(row).containsEntry("trading_balance_amount", new BigDecimal("2997000")); // 5000000 - (1000 * 2000) - 3000 = 2997000
        assertThat(row).containsEntry("trading_balance_currency", "IDR");
    }

    @Test
    @DisplayName("Write transaction rows the schema expects when save")
    void should_writeTransactionRowsTheSchemaExpects_when_save() {
        // Given
        Portfolio savedPortfolio = savedPortfolio();

        // When
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT id, date, type, cash_amount, cash_currency, asset_id, quantity, price_amount, price_currency, fee_amount, fee_currency, acquisition_id FROM transactions WHERE portfolio_id = ? ORDER BY seq",
                savedPortfolio.id().value()
        );

        // Then
        assertThat(rows).hasSize(2);
        Map<String, Object> depositRow = rows.get(0);
        Map<String, Object> buyRow = rows.get(1);

        assertThat(depositRow).containsEntry("type", "DEPOSIT");
        assertThat(depositRow).containsEntry("id", savedPortfolio.transactions().getFirst().id().value());
        assertThat(depositRow).containsEntry("date", Date.valueOf(DEPOSIT_DATE));
        assertThat(depositRow).containsEntry("cash_amount", INITIAL_DEPOSIT.amount());
        assertThat(depositRow).containsEntry("cash_currency", "IDR");
        assertThat(depositRow).containsEntry("asset_id", null);
        assertThat(depositRow).containsEntry("quantity", null);
        assertThat(depositRow).containsEntry("price_amount", null);
        assertThat(depositRow).containsEntry("price_currency", null);
        assertThat(depositRow).containsEntry("fee_amount", null);
        assertThat(depositRow).containsEntry("fee_currency", null);
        assertThat(depositRow).containsEntry("acquisition_id", null);

        assertThat(buyRow).containsEntry("type", "BUY");
        assertThat(buyRow).containsEntry("id", savedPortfolio.transactions().getLast().id().value());
        assertThat(buyRow).containsEntry("date", Date.valueOf(BUY_DATE));
        assertThat(buyRow).containsEntry("cash_amount", null);
        assertThat(buyRow).containsEntry("cash_currency", null);
        assertThat(buyRow).containsEntry("asset_id", ASSET_ID.value());
        assertThat(buyRow).containsEntry("quantity", QUANTITY.value());
        assertThat(buyRow).containsEntry("price_amount", BUY_PRICE.amount());
        assertThat(buyRow).containsEntry("price_currency", "IDR");
        assertThat(buyRow).containsEntry("fee_amount", BUY_FEE.amount());
        assertThat(buyRow).containsEntry("fee_currency", "IDR");
        assertThat(buyRow).containsEntry("acquisition_id", savedPortfolio.acquisitions().getFirst().id().value());
    }

    @Test
    @DisplayName("Write acquisition rows the schema expects when save")
    void should_writeAcquisitionRowsTheSchemaExpects_when_save() {
        // Given
        Portfolio savedPortfolio = savedPortfolio();

        // When
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT id, asset_id, open_date, open_price_amount, open_price_currency, open_fee_amount, open_fee_currency, initial_quantity FROM acquisitions WHERE portfolio_id = ?",
                savedPortfolio.id().value()
        );

        // Then
        assertThat(rows).hasSize(1);
        Map<String, Object> acquisitionRow = rows.getFirst();
        assertThat(acquisitionRow).containsEntry("id", savedPortfolio.acquisitions().getFirst().id().value());
        assertThat(acquisitionRow).containsEntry("asset_id", ASSET_ID.value());
        assertThat(acquisitionRow).containsEntry("open_date", Date.valueOf(BUY_DATE));
        assertThat(acquisitionRow).containsEntry("open_price_amount", BUY_PRICE.amount());
        assertThat(acquisitionRow).containsEntry("open_price_currency", "IDR");
        assertThat(acquisitionRow).containsEntry("open_fee_amount", BUY_FEE.amount());
        assertThat(acquisitionRow).containsEntry("open_fee_currency", "IDR");
        assertThat(acquisitionRow).containsEntry("initial_quantity", QUANTITY.value());
    }

    @Test
    @DisplayName("Return empty optional when id is unknown")
    void should_returnEmptyOptional_when_idIsUnknown() {
        // Given
        PortfolioId unknownId = PortfolioId.generate();

        // When
        Optional<Portfolio> result = adapter.findById(unknownId);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Reconstitute the portfolio fields when findById")
    void should_reconstitutePortfolioFields_when_findById() {
        // Given
        Portfolio savedPortfolio = savedPortfolio();

        // When
        Portfolio result = adapter.findById(savedPortfolio.id()).orElseThrow();

        // Then
        assertThat(result.id()).isEqualTo(savedPortfolio.id());
        assertThat(result.brokerAccountId()).isEqualTo(BROKER_ACCOUNT_ID);
        assertThat(result.name()).isEqualTo(PORTFOLIO_NAME);
        assertThat(result.tradingBalance()).isEqualTo(Money.of(new BigDecimal("2997000"))); // 5000000 - (1000 * 2000) - 3000 = 2997000
    }

    @Test
    @DisplayName("Reconstitute every transaction type when findById")
    void should_reconstituteEveryTransactionType_when_findById() {
        // Given
        Portfolio savedPortfolio = savedPortfolio();

        // When
        Portfolio result = adapter.findById(savedPortfolio.id()).orElseThrow();

        // Then
        assertThat(result.transactions()).hasSize(2);
        assertThat(result.transactions().getFirst()).isEqualTo(savedPortfolio.transactions().get(0));
        assertThat(result.transactions().getLast()).isEqualTo(savedPortfolio.transactions().get(1));
    }

    @Test
    @DisplayName("Reconstitute acquisitions when findById")
    void should_reconstituteAcquisitions_when_findById() {
        // Given
        Portfolio savedPortfolio = savedPortfolio();

        // When
        Portfolio result = adapter.findById(savedPortfolio.id()).orElseThrow();

        // Then
        assertThat(result.acquisitions()).hasSize(1);
        assertThat(result.acquisitions().getFirst()).usingRecursiveComparison().isEqualTo(savedPortfolio.acquisitions().getFirst());
    }

    @ParameterizedTest(name = "Quantity of \"{0}\"")
    @MethodSource("provideDifferentQuantityKinds")
    @DisplayName("Keep share and unit quantities apart when findById")
    void should_keepQuantityKindApart_when_findById(Quantity quantity) {
        // Given
        Portfolio savedPortfolio = savedPortfolio(quantity);

        // When
        Portfolio result = adapter.findById(savedPortfolio.id()).orElseThrow();

        // Then
        Buy buyTransaction = (Buy) result.transactions().get(1);
        assertThat(buyTransaction.quantity()).isEqualTo(quantity);
        assertThat(result.acquisitions().getFirst().initialQuantity()).isEqualTo(quantity);
    }

    private static Stream<Arguments> provideDifferentQuantityKinds() {
        return Stream.of(
                Arguments.of(Named.of("units", Quantity.ofUnits(new BigDecimal("100")))),
                Arguments.of(Named.of("shares", Quantity.ofShares(new BigDecimal("100"))))
        );
    }

    @Test
    @DisplayName("Persist the changed trading balance when an existing portfolio is saved")
    void should_persistChangedTradingBalance_when_existingPortfolioIsSaved() {
        // Given
        Portfolio savedPortfolio = savedPortfolio();

        // When
        Portfolio retrievedPortfolio = adapter.findById(savedPortfolio.id()).orElseThrow();
        retrievedPortfolio.recordDeposit(Money.of(new BigDecimal("1000000")), BUY_DATE.plusDays(1), TODAY); // 2997000 + 1000000 = 3997000
        adapter.save(retrievedPortfolio);

        entityManager.flush();
        entityManager.clear();

        // Then
        Portfolio result = adapter.findById(savedPortfolio.id()).orElseThrow();
        assertThat(result.tradingBalance()).isEqualTo(Money.of(new BigDecimal("3997000")));
    }

    @Test
    @DisplayName("Append only new ledger rows when an existing portfolio is saved")
    void should_appendOnlyNewLedgerRows_when_existingPortfolioIsSaved() {
        // Given
        Portfolio savedPortfolio = savedPortfolio();

        List<Map<String, Object>> initialRows = jdbcTemplate.queryForList(
                "SELECT id, seq FROM transactions WHERE portfolio_id = ? ORDER BY seq",
                savedPortfolio.id().value());

        // When
        Portfolio retrievedPortfolio = adapter.findById(savedPortfolio.id()).orElseThrow();
        retrievedPortfolio.recordDeposit(Money.of(new BigDecimal("500000")), BUY_DATE.plusDays(1), TODAY);
        adapter.save(retrievedPortfolio);

        entityManager.flush();
        entityManager.clear();

        // Then
        List<Map<String, Object>> updatedRows = jdbcTemplate.queryForList(
                "SELECT id, seq FROM transactions WHERE portfolio_id = ? ORDER BY seq",
                savedPortfolio.id().value());
        assertThat(updatedRows).hasSize(3);
        assertThat(updatedRows).containsAll(initialRows);
        assertThat(updatedRows.get(2)).containsEntry("id", retrievedPortfolio.transactions().get(2).id().value());
    }

    @Test
    @DisplayName("Reject save when the broker account does not exist")
    void should_rejectSave_when_brokerAccountDoesNotExist() {
        // Given
        BrokerAccountId unknownBrokerAccountId = BrokerAccountId.generate();
        Portfolio invalidPortfolio = Portfolio.create(unknownBrokerAccountId, PORTFOLIO_NAME);
        adapter.save(invalidPortfolio);

        // When & Then
        assertThatThrownBy(() -> entityManager.flush())
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("fk_portfolios_broker_account_id");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideDifferentLedgerRows")
    @DisplayName("Reject a ledger row when its columns do not match its type")
    void should_rejectLedgerRow_when_columnsDoNotMatchType(String sql) {
        // Given
        Portfolio savedPortfolio = savedPortfolio();

        // When & Then
        assertThatThrownBy(() -> jdbcTemplate.update(sql, UUID.randomUUID(), savedPortfolio.id().value(), TODAY))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("ck_transactions_columns_match_type");
    }

    private static Stream<Arguments> provideDifferentLedgerRows() {
        final UUID unknownAcquisitionId = UUID.randomUUID();
        return Stream.of(
                Arguments.of(Named.of("deposit with asset_id", "INSERT INTO transactions (id, portfolio_id, date, type, cash_amount, cash_currency, asset_id) VALUES (?, ?, ?, 'DEPOSIT', 100000, 'IDR', 'BBCA')")),
                Arguments.of(Named.of("deposit with cash_amount NULL", "INSERT INTO transactions (id, portfolio_id, date, type, cash_amount, cash_currency) VALUES (?, ?, ?, 'DEPOSIT', NULL, 'IDR')")),
                Arguments.of(Named.of("buy with cash_amount", "INSERT INTO transactions (id, portfolio_id, date, type, cash_amount, asset_id, quantity, price_amount, price_currency, fee_amount, fee_currency, acquisition_id) VALUES (?, ?, ?, 'BUY', 100000, 'BBCA', 100, 5000, 'IDR', 2500, 'IDR', '" + unknownAcquisitionId + "')")),
                Arguments.of(Named.of("buy with acquisition_id NULL", "INSERT INTO transactions (id, portfolio_id, date, type, asset_id, quantity, price_amount, price_currency, fee_amount, fee_currency, acquisition_id) VALUES (?, ?, ?, 'BUY', 'BBCA', 100, 5000, 'IDR', 2500, 'IDR', NULL)")),
                Arguments.of(Named.of("buy with fee_amount NULL", "INSERT INTO transactions (id, portfolio_id, date, type, asset_id, quantity, price_amount, price_currency, fee_amount, fee_currency, acquisition_id) VALUES (?, ?, ?, 'BUY', 'BBCA', 100, 5000, 'IDR', NULL, 'IDR', '" + unknownAcquisitionId + "')"))
        );
    }

    @Test
    @DisplayName("Return ledger in date order, recording order within a day, when findById")
    void should_returnLedgerInDateOrder_when_findById() {
        // Given
        Portfolio savedPortfolio = savedPortfolio();
        TransactionId firstDepositId = savedPortfolio.transactions().getFirst().id();
        TransactionId buyId = savedPortfolio.transactions().getLast().id();

        savedPortfolio.recordDeposit(Money.of(new BigDecimal("50000")), DEPOSIT_DATE.minusDays(5), TODAY);
        TransactionId backdatedDepositId = savedPortfolio.transactions().getLast().id();

        savedPortfolio.recordDeposit(Money.of(new BigDecimal("75000")), BUY_DATE, TODAY);
        TransactionId sameDayDepositId = savedPortfolio.transactions().getLast().id();

        adapter.save(savedPortfolio);
        entityManager.flush();
        entityManager.clear();

        // When
        Portfolio result = adapter.findById(savedPortfolio.id()).orElseThrow();

        // Then
        assertThat(result.transactions()).extracting(Transaction::id)
                .containsExactly(backdatedDepositId, firstDepositId, buyId, sameDayDepositId);
    }

    private Portfolio savedPortfolio(Quantity quantity) {
        Portfolio portfolio = Portfolio.create(BROKER_ACCOUNT_ID, PORTFOLIO_NAME);
        portfolio.recordDeposit(INITIAL_DEPOSIT, DEPOSIT_DATE, TODAY);
        portfolio.recordBuy(ASSET_ID, quantity, BUY_PRICE, BUY_FEE, BUY_DATE, TODAY);
        adapter.save(portfolio);

        entityManager.flush();
        entityManager.clear();

        return portfolio;
    }

    private Portfolio savedPortfolio() {
        return savedPortfolio(QUANTITY);
    }

}
