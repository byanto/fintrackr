package com.budiyanto.fintrackr.portfolio.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED;

import com.budiyanto.fintrackr.TestcontainersConfiguration;
import com.budiyanto.fintrackr.portfolio.domain.model.Portfolio;
import com.budiyanto.fintrackr.portfolio.domain.model.Transaction;
import com.budiyanto.fintrackr.shared.AssetId;
import com.budiyanto.fintrackr.shared.BrokerAccountId;
import com.budiyanto.fintrackr.shared.Money;
import com.budiyanto.fintrackr.shared.Quantity;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@DataJpaTest
@Import({TestcontainersConfiguration.class, PortfolioPersistenceAdapter.class, PortfolioPersistenceMapper.class})
@Transactional(propagation = NOT_SUPPORTED)
class PortfolioOptimisticLockingTest {

    private final PortfolioPersistenceAdapter adapter;
    private final PlatformTransactionManager transactionManager;
    private final JdbcTemplate jdbcTemplate;

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-18T00:00:00Z"), ZoneOffset.UTC);
    private static final LocalDate TODAY = LocalDate.now(CLOCK);
    private static final LocalDate DEPOSIT_DATE = TODAY.minusDays(10);

    @Autowired
    PortfolioOptimisticLockingTest(PortfolioPersistenceAdapter adapter, PlatformTransactionManager transactionManager, JdbcTemplate jdbcTemplate) {
        this.adapter = adapter;
        this.transactionManager = transactionManager;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Test
    @DisplayName("Reject a stale save when a concurrent transaction updated the portfolio first")
    void should_rejectStaleSave_when_concurrentTransactionUpdatedPortfolioFirst() {
        // Given
        Portfolio savedPortfolio = savedPortfolio();

        // When
        TransactionTemplate templateA = new TransactionTemplate(transactionManager);
        TransactionTemplate templateB = new TransactionTemplate(transactionManager);
        templateB.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        AtomicReference<Transaction> portfolioBTransaction = new AtomicReference<>();
        assertThatThrownBy(() -> templateA.execute(statusA -> {
            Portfolio portfolioA = adapter.findById(savedPortfolio.id()).orElseThrow();
            templateB.execute(statusB -> {
                Portfolio portfolioB = adapter.findById(savedPortfolio.id()).orElseThrow();
                portfolioB.recordDeposit(Money.of(new BigDecimal("100000")), DEPOSIT_DATE, TODAY);
                portfolioBTransaction.set(portfolioB.transactions().getLast());
                adapter.save(portfolioB);
                return null;
            });
            portfolioA.recordDeposit(Money.of(new BigDecimal("5000000")), DEPOSIT_DATE, TODAY);
            adapter.save(portfolioA);
            return null;
        }))
                .isInstanceOf(OptimisticLockingFailureException.class);

        // Then
        Portfolio result = adapter.findById(savedPortfolio.id()).orElseThrow();
        assertThat(result.tradingBalance()).isEqualTo(Money.of(new BigDecimal("100000")));
        assertThat(result.transactions()).containsExactly(portfolioBTransaction.get());
    }

    @Test
    @DisplayName("Load ledger when called without surrounding transaction")
    void should_loadLedger_when_calledWithoutSurroundingTransaction() {
        // Given
        Portfolio savedPortfolio = savedPortfolio();
        savedPortfolio.recordDeposit(Money.of(new BigDecimal("1000000")), DEPOSIT_DATE, TODAY);
        savedPortfolio.recordBuy(AssetId.of("ID1000109507"), Quantity.ofShares(new BigDecimal("100")), Money.of(new BigDecimal("1000")), Money.of(new BigDecimal("150")), DEPOSIT_DATE.plusDays(1), TODAY);
        adapter.save(savedPortfolio);

        // When
        Portfolio result = adapter.findById(savedPortfolio.id()).orElseThrow();

        // Then
        assertThat(result.transactions()).containsExactly(savedPortfolio.transactions().getFirst(), savedPortfolio.transactions().getLast());
        assertThat(result.acquisitions()).containsExactly(savedPortfolio.acquisitions().getLast());
    }

    private Portfolio savedPortfolio() {
        BrokerAccountId brokerAccountId = BrokerAccountId.generate();
        String insertBrokerAccountSql = "INSERT INTO broker_accounts(id, name, rdn_amount, rdn_currency, fee_structure_buy_rate, fee_structure_sell_rate) VALUES (?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(insertBrokerAccountSql, brokerAccountId.value(), "Test Broker Account", BigDecimal.ZERO, "IDR", new BigDecimal("0.0015"), new BigDecimal("0.0025"));

        Portfolio portfolio = Portfolio.create(brokerAccountId, "Test Portfolio");
        return adapter.save(portfolio);
    }

}
