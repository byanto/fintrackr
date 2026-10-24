package com.budiyanto.fintrackr.portfolio.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.budiyanto.fintrackr.TestcontainersConfiguration;
import com.budiyanto.fintrackr.brokerage.domain.exception.InsufficientRdnException;
import com.budiyanto.fintrackr.portfolio.application.port.in.RecordBuyCommand;
import com.budiyanto.fintrackr.portfolio.application.port.in.RecordBuyUseCase;
import com.budiyanto.fintrackr.portfolio.application.port.out.PortfolioRepository;
import com.budiyanto.fintrackr.portfolio.domain.model.Portfolio;
import com.budiyanto.fintrackr.shared.AssetId;
import com.budiyanto.fintrackr.shared.BrokerAccountId;
import com.budiyanto.fintrackr.shared.Money;
import com.budiyanto.fintrackr.shared.Quantity;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@DisplayName("RecordBuyService Transaction Tests")
class RecordBuyServiceTransactionTest {

    private final RecordBuyUseCase recordBuyUseCase;
    private final PortfolioRepository portfolioRepository;
    private final JdbcTemplate jdbcTemplate;
    private final Clock clock;

    @Autowired
    RecordBuyServiceTransactionTest(RecordBuyUseCase recordBuyUseCase, PortfolioRepository portfolioRepository, JdbcTemplate jdbcTemplate, Clock clock) {
        this.recordBuyUseCase = recordBuyUseCase;
        this.portfolioRepository = portfolioRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.clock = clock;
    }

    @Test
    @DisplayName("Leave ledger and balance unchanged when cash flow is rejected")
    void should_leaveLedgerAndBalanceUnchanged_when_cashFlowIsRejected() {
        // Given
        LocalDate today = LocalDate.now(clock);
        BigDecimal initialRdnAmount = new BigDecimal("50000");
        BrokerAccountId brokerAccountId = BrokerAccountId.generate();
        jdbcTemplate.update("INSERT INTO broker_accounts (id, name, rdn_amount, rdn_currency, fee_structure_buy_rate, fee_structure_sell_rate) VALUES (?, ?, ?, ?, ?, ?)",
                brokerAccountId.value(), "Test Broker Account", initialRdnAmount, "IDR", new BigDecimal("0.0015"), new BigDecimal("0.0025"));

        BigDecimal initialTradingBalance = new BigDecimal("1000000");
        Portfolio portfolio = Portfolio.create(brokerAccountId, "Test Portfolio");
        portfolio.recordDeposit(Money.of(initialTradingBalance), today.minusDays(3), today);
        portfolioRepository.save(portfolio);

        RecordBuyCommand command = new RecordBuyCommand(
                portfolio.id(), AssetId.of("ID1000109507"), Quantity.ofShares(new BigDecimal("100")),
                Money.of(new BigDecimal("5000")), Money.of(new BigDecimal("3000")), today.minusDays(2));

        // When
        assertThatThrownBy(() -> recordBuyUseCase.handle(command))
                .isInstanceOf(InsufficientRdnException.class)
                .hasMessageContaining("Apply cash flow rejected: would drive RDN below zero.");

        // Then
        Long acquisitions = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM acquisitions WHERE portfolio_id = ?", Long.class, portfolio.id().value());
        assertThat(acquisitions).isEqualTo(0L);

        Long transactions = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM transactions WHERE portfolio_id = ?", Long.class, portfolio.id().value());
        assertThat(transactions).isEqualTo(1L);

        BigDecimal tradingBalance = jdbcTemplate.queryForObject("SELECT trading_balance_amount FROM portfolios WHERE id = ?", BigDecimal.class, portfolio.id().value());
        assertThat(tradingBalance).isEqualByComparingTo(initialTradingBalance);

        BigDecimal rdnAmount = jdbcTemplate.queryForObject("SELECT rdn_amount FROM broker_accounts WHERE id = ?", BigDecimal.class, brokerAccountId.value());
        assertThat(rdnAmount).isEqualByComparingTo(initialRdnAmount);
    }

    @Test
    @DisplayName("Persist buy and debit balance when buy succeeds")
    void should_persistBuyAndDebitBalance_when_buySucceeds() {
        // Given
        LocalDate today = LocalDate.now(clock);
        BigDecimal initialBalance = new BigDecimal("1000000");
        BrokerAccountId brokerAccountId = BrokerAccountId.generate();
        jdbcTemplate.update("INSERT INTO broker_accounts (id, name, rdn_amount, rdn_currency, fee_structure_buy_rate, fee_structure_sell_rate) VALUES (?, ?, ?, ?, ?, ?)",
                brokerAccountId.value(), "Test Broker Account", initialBalance, "IDR", new BigDecimal("0.0015"), new BigDecimal("0.0025"));

        Portfolio portfolio = Portfolio.create(brokerAccountId, "Test Portfolio");
        portfolio.recordDeposit(Money.of(initialBalance), today.minusDays(3), today);
        portfolioRepository.save(portfolio);

        BigDecimal endBalance = new BigDecimal("497000"); // 1000000 - (100 * 5000) - 3000 = 497000
        RecordBuyCommand command = new RecordBuyCommand(
                portfolio.id(), AssetId.of("ID1000109507"), Quantity.ofShares(new BigDecimal("100")),
                Money.of(new BigDecimal("5000")), Money.of(new BigDecimal("3000")), today.minusDays(2));

        // When
        recordBuyUseCase.handle(command);

        // Then
        Long acquisitions = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM acquisitions WHERE portfolio_id = ?", Long.class, portfolio.id().value());
        assertThat(acquisitions).isEqualTo(1L);

        Long transactions = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM transactions WHERE portfolio_id = ?", Long.class, portfolio.id().value());
        assertThat(transactions).isEqualTo(2L);

        BigDecimal tradingBalance = jdbcTemplate.queryForObject("SELECT trading_balance_amount FROM portfolios WHERE id = ?", BigDecimal.class, portfolio.id().value());
        assertThat(tradingBalance).isEqualByComparingTo(endBalance);

        BigDecimal rdnAmount = jdbcTemplate.queryForObject("SELECT rdn_amount FROM broker_accounts WHERE id = ?", BigDecimal.class, brokerAccountId.value());
        assertThat(rdnAmount).isEqualByComparingTo(endBalance);
    }

}
