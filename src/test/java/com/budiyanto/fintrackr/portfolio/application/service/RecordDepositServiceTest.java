package com.budiyanto.fintrackr.portfolio.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.budiyanto.fintrackr.brokerage.domain.model.BrokerAccount;
import com.budiyanto.fintrackr.brokerage.domain.model.FeeStructure;
import com.budiyanto.fintrackr.brokerage.domain.model.Percentage;
import com.budiyanto.fintrackr.portfolio.application.exception.PortfolioNotFoundException;
import com.budiyanto.fintrackr.portfolio.application.port.in.RecordDepositCommand;
import com.budiyanto.fintrackr.portfolio.domain.exception.FutureDatedTransactionException;
import com.budiyanto.fintrackr.portfolio.domain.model.Portfolio;
import com.budiyanto.fintrackr.portfolio.domain.model.PortfolioId;
import com.budiyanto.fintrackr.portfolio.domain.model.Transaction;
import com.budiyanto.fintrackr.portfolio.domain.model.TransactionId;
import com.budiyanto.fintrackr.shared.Money;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RecordDepositServiceTest {

    private final InMemoryPortfolioRepository inMemoryPortfolioRepository = new InMemoryPortfolioRepository();
    private final InMemoryBrokerageApi inMemoryBrokerageApi = new InMemoryBrokerageApi();
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-15T00:00:00Z"), ZoneOffset.UTC);
    private final RecordDepositService recordDepositService = new RecordDepositService(inMemoryPortfolioRepository, inMemoryBrokerageApi, CLOCK);

    private BrokerAccount brokerAccount;
    private Portfolio portfolio;

    private static final LocalDate TODAY = LocalDate.now(CLOCK);

    @BeforeEach
    void setup() {
        brokerAccount = BrokerAccount.create("Test Broker Account", FeeStructure.of(Percentage.of(new BigDecimal("0.0015")), Percentage.of(new BigDecimal("0.0025"))));
        inMemoryBrokerageApi.addBrokerAccount(brokerAccount);

        portfolio = Portfolio.create(brokerAccount.id(), "Test Portfolio");
        inMemoryPortfolioRepository.save(portfolio);
    }

    @Test
    @DisplayName("Increase trading balance and RDN by the amount when handle")
    void should_increaseTradingBalanceAndRdnByTheAmount_when_handle() {
        // Given
        Money amount = Money.of(new BigDecimal("1000000"));
        RecordDepositCommand command = new RecordDepositCommand(portfolio.id(), TODAY.minusDays(5), amount);

        // When
        recordDepositService.handle(command);

        // Then
        Portfolio loadedPortfolio = inMemoryPortfolioRepository.findById(portfolio.id()).orElseThrow();
        assertThat(loadedPortfolio.tradingBalance()).isEqualTo(amount);
        assertThat(brokerAccount.rdn()).isEqualTo(amount);
    }

    @Test
    @DisplayName("Return id of the recorded deposit when handle")
    void should_returnIdOfTheRecordedDeposit_when_handle() {
        // Given
        RecordDepositCommand command = new RecordDepositCommand(
                portfolio.id(), TODAY.minusDays(5), Money.of(new BigDecimal("100000")));

        // When
        TransactionId result = recordDepositService.handle(command);

        // Then
        Portfolio loadedPortfolio = inMemoryPortfolioRepository.findById(portfolio.id()).orElseThrow();
        List<Transaction> transactions = loadedPortfolio.transactions();
        assertThat(transactions).hasSize(1);
        assertThat(result).isEqualTo(transactions.getLast().id());
    }

    @Test
    @DisplayName("Throw PortfolioNotFoundException when portfolio does not exist")
    void should_throwPortfolioNotFoundException_when_portfolioDoesNotExist() {
        // Given
        RecordDepositCommand command = new RecordDepositCommand(
                PortfolioId.generate(), TODAY.minusDays(5), Money.of(new BigDecimal("100000")));

        // When & Then
        assertThatThrownBy(() -> recordDepositService.handle(command))
                .isInstanceOf(PortfolioNotFoundException.class)
                .hasMessageContaining("Portfolio not found");
    }

    @Test
    @DisplayName("Leave RDN unchanged when portfolio rejects deposit")
    void should_leaveRdnUnchanged_when_portfolioRejectsDeposit() {
        // Given
        RecordDepositCommand command = new RecordDepositCommand(
                portfolio.id(), TODAY.plusDays(10), Money.of(new BigDecimal("100000")));

        // When
        assertThatThrownBy(() -> recordDepositService.handle(command))
                .isInstanceOf(FutureDatedTransactionException.class);

        // Then
        assertThat(brokerAccount.rdn()).isEqualTo(Money.zero());
    }
}
