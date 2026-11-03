package com.budiyanto.fintrackr.portfolio.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.budiyanto.fintrackr.portfolio.application.exception.PortfolioNotFoundException;
import com.budiyanto.fintrackr.portfolio.application.port.in.PortfolioDetails;
import com.budiyanto.fintrackr.portfolio.domain.model.Portfolio;
import com.budiyanto.fintrackr.portfolio.domain.model.PortfolioId;
import com.budiyanto.fintrackr.shared.BrokerAccountId;
import com.budiyanto.fintrackr.shared.Money;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("GetPortfolioService Tests")
class GetPortfolioServiceTest {

    private final InMemoryPortfolioRepository inMemoryPortfolioRepository = new InMemoryPortfolioRepository();
    private final GetPortfolioService getPortfolioService = new GetPortfolioService(inMemoryPortfolioRepository);

    private static final BigDecimal BALANCE = new BigDecimal("100000");

    @Test
    @DisplayName("Return portfolio details when portfolio exists")
    void should_returnPortfolioDetails_when_portfolioExists() {
        // Given
        Portfolio portfolio = savedPortfolio();

        // When
        PortfolioDetails result = getPortfolioService.handle(portfolio.id());

        // Then
        assertThat(result.id()).isEqualTo(portfolio.id().value());
        assertThat(result.brokerAccountId()).isEqualTo(portfolio.brokerAccountId().value());
        assertThat(result.name()).isEqualTo(portfolio.name());
        assertThat(result.tradingBalance()).isEqualTo(BALANCE);
    }

    @Test
    @DisplayName("Throw portfolio not found when portfolio does not exist")
    void should_throwPortfolioNotFound_when_portfolioDoesNotExist() {
        // Given
        PortfolioId portfolioId = PortfolioId.generate();

        // When & Then
        assertThatThrownBy(() -> getPortfolioService.handle(portfolioId))
                .isInstanceOf(PortfolioNotFoundException.class)
                .hasMessageContaining("Portfolio not found");
    }

    private Portfolio savedPortfolio() {
        Portfolio portfolio = Portfolio.create(BrokerAccountId.generate(), "Test Portfolios");
        portfolio.recordDeposit(
                Money.of(BALANCE),
                LocalDate.of(2026, 10, 5),
                LocalDate.of(2026, 10, 15));
        inMemoryPortfolioRepository.save(portfolio);
        return portfolio;
    }
}
