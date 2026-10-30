package com.budiyanto.fintrackr.portfolio.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.budiyanto.fintrackr.brokerage.domain.model.BrokerAccount;
import com.budiyanto.fintrackr.brokerage.domain.model.FeeStructure;
import com.budiyanto.fintrackr.brokerage.domain.model.Percentage;
import com.budiyanto.fintrackr.portfolio.application.exception.PortfolioNotFoundException;
import com.budiyanto.fintrackr.portfolio.application.port.in.RecordBuyCommand;
import com.budiyanto.fintrackr.portfolio.domain.exception.InsufficientBalanceException;
import com.budiyanto.fintrackr.portfolio.domain.model.Portfolio;
import com.budiyanto.fintrackr.portfolio.domain.model.PortfolioId;
import com.budiyanto.fintrackr.portfolio.domain.model.TransactionId;
import com.budiyanto.fintrackr.shared.AssetId;
import com.budiyanto.fintrackr.shared.Money;
import com.budiyanto.fintrackr.shared.Quantity;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("RecordBuyService Tests")
class RecordBuyServiceTest {

    private final InMemoryPortfolioRepository inMemoryPortfolioRepository = new InMemoryPortfolioRepository();
    private final InMemoryBrokerageApi inMemoryBrokerageApi = new InMemoryBrokerageApi();
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-08-23T00:00:00Z"), ZoneOffset.UTC);
    private final RecordBuyService recordBuyService = new RecordBuyService(inMemoryPortfolioRepository, inMemoryBrokerageApi, CLOCK);

    private Portfolio portfolio;
    private BrokerAccount brokerAccount;

    private static final AssetId ASSET_ID = AssetId.of("ID1000109507"); // BBCA ISIN
    private static final Quantity QUANTITY = Quantity.ofShares(new BigDecimal("1000"));
    private static final Money PRICE = Money.of(new BigDecimal("5000"));
    private static final Money FEE = Money.of(new BigDecimal("1250"));
    private static final Money INITIAL_DEPOSIT = Money.of(new BigDecimal("6000000"));

    private static final LocalDate TODAY = LocalDate.now(CLOCK);
    private static final LocalDate DEPOSIT_DATE = TODAY.minusDays(10);
    private static final LocalDate BUY_DATE = TODAY.minusDays(5);

    @BeforeEach
    void setup() {
        brokerAccount = BrokerAccount.create("Test Broker Account", FeeStructure.of(Percentage.of(new BigDecimal("0.0015")), Percentage.of(new BigDecimal("0.0025"))));
        brokerAccount.applyCashFlow(INITIAL_DEPOSIT);
        inMemoryBrokerageApi.addBrokerAccount(brokerAccount);

        portfolio = Portfolio.create(brokerAccount.id(), "Test Portfolio");
        portfolio.recordDeposit(INITIAL_DEPOSIT, DEPOSIT_DATE, TODAY);
        inMemoryPortfolioRepository.save(portfolio);
    }

    @Test
    @DisplayName("Return id of the recorded buy when handle")
    void should_returnIdOfRecordedBuy_when_handle() {
        // Given
        var command = new RecordBuyCommand(portfolio.id(), ASSET_ID, QUANTITY, PRICE, FEE, BUY_DATE);

        // When
        TransactionId result = recordBuyService.handle(command);

        // Then
        Portfolio loadedPortfolio = inMemoryPortfolioRepository.findById(portfolio.id()).orElseThrow();
        assertThat(result).isEqualTo(loadedPortfolio.transactions().getLast().id());
    }

    @Test
    @DisplayName("Reduce portfolio trading balance and broker account RDN when record buy is recorded")
    void should_reduceTradingBalanceAndRdn_when_recordBuy() {
        // Given
        var command = new RecordBuyCommand(portfolio.id(), ASSET_ID, QUANTITY, PRICE, FEE, BUY_DATE);

        // When
        recordBuyService.handle(command);

        // Then
        Portfolio loadedPortfolio = inMemoryPortfolioRepository.findById(portfolio.id()).orElseThrow();
        assertThat(loadedPortfolio.tradingBalance()).isEqualTo(Money.of(new BigDecimal("998750"))); // 6000000 - (1000 * 5000 + 1250) = 998750
        assertThat(brokerAccount.rdn()).isEqualTo(Money.of(new BigDecimal("998750"))); // 6000000 - (1000 * 5000 + 1250) = 998.750
        assertThat(brokerAccount.rdn()).isEqualTo(loadedPortfolio.tradingBalance());
    }

    @Test
    @DisplayName("Have sum(tradingBalance) == rdn when record buy is recorded on multiple portfolios")
    void should_haveEqualRdnAndAllPortfolioTradingBalance_when_recordBuyInMultiplePortfolios() {
        // Given
        Money secondDeposit = Money.of(new BigDecimal("10000000"));
        brokerAccount.applyCashFlow(secondDeposit);

        var command1 = new RecordBuyCommand(portfolio.id(), ASSET_ID, QUANTITY, PRICE, FEE, BUY_DATE);

        Portfolio portfolio2 = Portfolio.create(brokerAccount.id(), "Test Portfolio 2");
        portfolio2.recordDeposit(secondDeposit, DEPOSIT_DATE, TODAY);
        inMemoryPortfolioRepository.save(portfolio2);
        var command2 = new RecordBuyCommand(portfolio2.id(), ASSET_ID, QUANTITY, PRICE, FEE, BUY_DATE);

        // When
        recordBuyService.handle(command1);
        recordBuyService.handle(command2);

        // Then
        Portfolio loadedPortfolio1 = inMemoryPortfolioRepository.findById(portfolio.id()).orElseThrow();
        Portfolio loadedPortfolio2 = inMemoryPortfolioRepository.findById(portfolio2.id()).orElseThrow();
        assertThat(brokerAccount.rdn()).isEqualTo(loadedPortfolio1.tradingBalance().add(loadedPortfolio2.tradingBalance()));
    }

    @Test
    @DisplayName("Use computed fee when no fee is given in command")
    void should_useComputedFee_when_noFeeIsGivenInCommand() {
        // Given
        var command = new RecordBuyCommand(portfolio.id(), ASSET_ID, QUANTITY, PRICE, null, BUY_DATE);

        // When
        recordBuyService.handle(command);

        // Then
        Portfolio loadedPortfolio = inMemoryPortfolioRepository.findById(portfolio.id()).orElseThrow();
        assertThat(loadedPortfolio.tradingBalance()).isEqualTo(Money.of(new BigDecimal("992500"))); // 6000000 - (1000 * 5000 + 7500) = 992500
        assertThat(brokerAccount.rdn()).isEqualTo(Money.of(new BigDecimal("992500"))); // 6000000 - (1000 * 5000 + 7500) = 992500
        assertThat(brokerAccount.rdn()).isEqualTo(loadedPortfolio.tradingBalance());
    }

    @Test
    @DisplayName("Reject a record buy when the portfolio is not found")
    void should_throwException_when_portfolioNotFound() {
        // Given
        var command = new RecordBuyCommand(PortfolioId.generate(), ASSET_ID, QUANTITY, PRICE, FEE, BUY_DATE);

        // When & Assert
        assertThatThrownBy(() -> recordBuyService.handle(command))
                .isInstanceOf(PortfolioNotFoundException.class);
    }

    @Test
    @DisplayName("Reject a record buy when the balance is insufficient")
    void should_throwException_when_balanceIsInsufficient() {
        // Given
        var command = new RecordBuyCommand(portfolio.id(), ASSET_ID, Quantity.ofShares(new BigDecimal("10000")), PRICE, FEE, BUY_DATE);

        // When & Then
        assertThatThrownBy(() -> recordBuyService.handle(command))
                .isInstanceOf(InsufficientBalanceException.class);

        Portfolio loadedPortfolio = inMemoryPortfolioRepository.findById(portfolio.id()).orElseThrow();
        assertThat(loadedPortfolio.tradingBalance()).isEqualTo(INITIAL_DEPOSIT);
        assertThat(brokerAccount.rdn()).isEqualTo(INITIAL_DEPOSIT);
    }

}
