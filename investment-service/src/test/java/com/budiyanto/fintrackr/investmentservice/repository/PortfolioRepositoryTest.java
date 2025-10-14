package com.budiyanto.fintrackr.investmentservice.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.budiyanto.fintrackr.investmentservice.domain.Portfolio;

@DisplayName("PortfolioRepository Tests")
class PortfolioRepositoryTest extends AbstractRepositoryTest{

    private final PortfolioRepository portfolioRepository;

    @Autowired
    PortfolioRepositoryTest(PortfolioRepository portfolioRepository) {
        this.portfolioRepository = portfolioRepository;
    }

    @Test
    @DisplayName("should save and retrieve portfolio")
    void should_saveAndRetrievePortfolio() {
        // Arrange: Create a new Portfolio object
        Portfolio portfolio = new Portfolio("My Portfolio");

        // Act: Save the portfolio using the repository
        Portfolio savedPortfolio = portfolioRepository.save(portfolio);

        // Assert: Verify that the portfolio was saved correctly and can be retrieved
        assertThat(savedPortfolio).isNotNull();
        assertThat(savedPortfolio.getId()).isGreaterThan(0);

        Portfolio retrievedPortfolio = portfolioRepository.findById(savedPortfolio.getId()).orElse(null);
        assertThat(retrievedPortfolio).isNotNull();
        assertThat(retrievedPortfolio.getName()).isEqualTo(portfolio.getName());
        assertThat(retrievedPortfolio.getCreatedAt()).isNotNull();

    }
}
