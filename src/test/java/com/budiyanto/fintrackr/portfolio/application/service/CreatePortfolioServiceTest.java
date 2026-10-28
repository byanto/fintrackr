package com.budiyanto.fintrackr.portfolio.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.budiyanto.fintrackr.brokerage.domain.model.BrokerAccount;
import com.budiyanto.fintrackr.brokerage.domain.model.FeeStructure;
import com.budiyanto.fintrackr.brokerage.domain.model.Percentage;
import com.budiyanto.fintrackr.portfolio.application.exception.UnknownBrokerAccountException;
import com.budiyanto.fintrackr.portfolio.application.port.in.CreatePortfolioCommand;
import com.budiyanto.fintrackr.portfolio.domain.model.Portfolio;
import com.budiyanto.fintrackr.portfolio.domain.model.PortfolioId;
import com.budiyanto.fintrackr.shared.BrokerAccountId;
import com.budiyanto.fintrackr.shared.Money;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("CreatePortfolioService Tests")
class CreatePortfolioServiceTest {

    private final InMemoryPortfolioRepository inMemoryPortfolioRepository = new InMemoryPortfolioRepository();
    private final InMemoryBrokerageApi inMemoryBrokerageApi = new InMemoryBrokerageApi();
    private final CreatePortfolioService createPortfolioService = new CreatePortfolioService(inMemoryPortfolioRepository, inMemoryBrokerageApi);

    @Test
    @DisplayName("Save portfolio when broker account exists")
    void should_savePortfolio_when_brokerAccountExists() {
        // Given
        BrokerAccount brokerAccount = BrokerAccount.create(
                "Test Broker Account",
                new FeeStructure(Percentage.of(new BigDecimal("0.0015")), Percentage.of(new BigDecimal("0.0025")))
        );
        inMemoryBrokerageApi.addBrokerAccount(brokerAccount);
        CreatePortfolioCommand command = new CreatePortfolioCommand(brokerAccount.id(), "Test Portfolio");

        // When
        PortfolioId result = createPortfolioService.handle(command);

        // Then
        Portfolio loadedPortfolio = inMemoryPortfolioRepository.findById(result).orElseThrow();
        assertThat(loadedPortfolio.brokerAccountId()).isEqualTo(brokerAccount.id());
        assertThat(loadedPortfolio.name()).isEqualTo("Test Portfolio");
    }

    @Test
    @DisplayName("Save portfolio with zero trading balance when broker account exists")
    void should_savePortfolioWithZeroTradingBalance_when_brokerAccountExists() {
        // Given
        BrokerAccount brokerAccount = BrokerAccount.create(
                "Test Broker Account",
                new FeeStructure(Percentage.of(new BigDecimal("0.0015")), Percentage.of(new BigDecimal("0.0025")))
        );
        inMemoryBrokerageApi.addBrokerAccount(brokerAccount);
        CreatePortfolioCommand command = new CreatePortfolioCommand(brokerAccount.id(), "Test Portfolio");

        // When
        PortfolioId result = createPortfolioService.handle(command);

        // Then
        Portfolio loadedPortfolio = inMemoryPortfolioRepository.findById(result).orElseThrow();
        assertThat(loadedPortfolio.tradingBalance()).isEqualTo(Money.zero());
    }

    @Test
    @DisplayName("Throw unknown broker account and save nothing when broker account is unknown")
    void should_throwUnknownBrokerAccountAndSaveNothing_when_brokerAccountIsUnknown() {
        // Given
        BrokerAccountId unknownBrokerAccountId = BrokerAccountId.generate();
        CreatePortfolioCommand command = new CreatePortfolioCommand(unknownBrokerAccountId, "Test Portfolio");

        // When & Then
        assertThatThrownBy(() -> createPortfolioService.handle(command))
                .isInstanceOf(UnknownBrokerAccountException.class);

        assertThat(inMemoryPortfolioRepository.isEmpty()).isTrue();
    }
}
