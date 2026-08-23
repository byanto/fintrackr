package com.budiyanto.fintrackr.portfolio.application.service;

import com.budiyanto.fintrackr.brokerage.domain.model.BrokerAccount;
import com.budiyanto.fintrackr.brokerage.domain.model.FeeStructure;
import com.budiyanto.fintrackr.brokerage.domain.model.Percentage;
import com.budiyanto.fintrackr.portfolio.application.exception.PortfolioNotFoundException;
import com.budiyanto.fintrackr.portfolio.application.port.in.RecordBuyCommand;
import com.budiyanto.fintrackr.portfolio.domain.exception.InsufficientBalanceException;
import com.budiyanto.fintrackr.portfolio.domain.model.Portfolio;
import com.budiyanto.fintrackr.portfolio.domain.model.PortfolioId;
import com.budiyanto.fintrackr.shared.AssetId;
import com.budiyanto.fintrackr.shared.Money;
import com.budiyanto.fintrackr.shared.Quantity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("RecordBuy Tests")
class RecordBuyServiceTest {

    private final InMemoryPortfolioRepository inMemoryPortfolioRepository = new InMemoryPortfolioRepository();
    private final InMemoryBrokerageApi inMemoryBrokerageApi = new InMemoryBrokerageApi();
    private final Clock clock = Clock.fixed(Instant.parse("2026-08-23T00:00:00Z"), ZoneId.systemDefault());
    private final RecordBuyService recordBuyService =  new RecordBuyService(inMemoryPortfolioRepository, inMemoryBrokerageApi, clock);

    private Portfolio portfolio;
    private BrokerAccount brokerAccount;

    private final AssetId assetId = AssetId.of("ID1000109507"); // BBCA ISIN
    private final Quantity quantity = Quantity.ofShares(new BigDecimal("1000"));
    private final Money price = Money.of(new BigDecimal("5000"));
    private final Money fee = Money.of(new BigDecimal("1250"));
    private final Money initialDeposit = Money.of(new BigDecimal("6000000"));

    @BeforeEach
    void setup() {
        brokerAccount = BrokerAccount.create("Test Broker Account", FeeStructure.of(Percentage.of(new BigDecimal("0.0015")), Percentage.of(new BigDecimal("0.0025"))));
        brokerAccount.applyCashFlow(initialDeposit);
        inMemoryBrokerageApi.addBrokerAccount(brokerAccount);

        portfolio = Portfolio.create(brokerAccount.id(), "Test Portfolio");
        portfolio.recordDeposit(initialDeposit, LocalDate.now(), LocalDate.now());
        inMemoryPortfolioRepository.save(portfolio);
    }

    @Test
    @DisplayName("Reduce portfolio trading balance and broker account RDN when record buy is recorded")
    void should_reduceTradingBalanceAndRdn_when_recordBuy() {
        // Given
        var command = new RecordBuyCommand(portfolio.id(), assetId, quantity, price, fee, LocalDate.now());

        // When
        recordBuyService.handle(command);

        // Then
        assertThat(portfolio.tradingBalance()).isEqualTo(Money.of(new BigDecimal("998750"))); // 6000000 - (1000 * 5000 + 1250) = 998750
        assertThat(brokerAccount.rdn()).isEqualTo(Money.of(new BigDecimal("998750"))); // 6000000 - (1000 * 5000 + 1250) = 998.750
        assertThat(brokerAccount.rdn()).isEqualTo(portfolio.tradingBalance());
    }

    @Test
    @DisplayName("Have sum(tradingBalance) == rdn when record buy is recorded on multiple portfolios")
    void should_haveEqualRdnAndAllPortfolioTradingBalance_when_recordBuyInMultiplePortfolios() {
        // Given
        Money secondDeposit = Money.of(new BigDecimal("10000000"));
        brokerAccount.applyCashFlow(secondDeposit);

        var command1 = new RecordBuyCommand(portfolio.id(), assetId, quantity, price, fee, LocalDate.now());

        Portfolio portfolio2 = Portfolio.create(brokerAccount.id(), "Test Portfolio 2");
        portfolio2.recordDeposit(secondDeposit, LocalDate.now(), LocalDate.now());
        inMemoryPortfolioRepository.save(portfolio2);
        var command2 = new RecordBuyCommand(portfolio2.id(), assetId, quantity, price, fee, LocalDate.now());

        // When
        recordBuyService.handle(command1);
        recordBuyService.handle(command2);

        // Then
        assertThat(brokerAccount.rdn()).isEqualTo(portfolio.tradingBalance().add(portfolio2.tradingBalance()));
    }

    @Test
    @DisplayName("Use computed fee when no fee is given in command")
    void should_useComputedFee_when_noFeeIsGivenInCommand() {
        // Given
        var command = new RecordBuyCommand(portfolio.id(), assetId, quantity, price, null, LocalDate.now());

        // When
        recordBuyService.handle(command);

        // Then
        assertThat(portfolio.tradingBalance()).isEqualTo(Money.of(new BigDecimal("992500"))); // 6000000 - (1000 * 5000 + 7500) = 992500
        assertThat(brokerAccount.rdn()).isEqualTo(Money.of(new BigDecimal("992500"))); // 6000000 - (1000 * 5000 + 7500) = 992500
        assertThat(brokerAccount.rdn()).isEqualTo(portfolio.tradingBalance());
    }

    @Test
    @DisplayName("Reject a record buy when the portfolio is not found")
    void should_throwException_when_portfolioNotFound() {
        // Given
        var command = new RecordBuyCommand(PortfolioId.generate(), assetId, quantity, price, fee, LocalDate.now());

        // When & Assert
        assertThatThrownBy(() -> recordBuyService.handle(command))
                .isInstanceOf(PortfolioNotFoundException.class);
    }

    @Test
    @DisplayName("Reject a record buy when the balance is insufficient")
    void should_throwException_when_balanceIsInsufficient() {
        // Given
        var command = new RecordBuyCommand(portfolio.id(), assetId, Quantity.ofShares(new BigDecimal("10000")), price, fee, LocalDate.now());

        // When & Then
        assertThatThrownBy(() -> recordBuyService.handle(command))
                .isInstanceOf(InsufficientBalanceException.class);

        assertThat(portfolio.tradingBalance()).isEqualTo(initialDeposit);
        assertThat(brokerAccount.rdn()).isEqualTo(initialDeposit);
    }

}
