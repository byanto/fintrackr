package com.budiyanto.fintrackr.investmentservice.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.budiyanto.fintrackr.investmentservice.domain.Portfolio;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PortfolioRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    private final PortfolioRepository portfolioRepository;

    @Autowired
    PortfolioRepositoryTest(PortfolioRepository portfolioRepository) {
        this.portfolioRepository = portfolioRepository;
    }

    @Test
    void shouldSaveAndRetrievePortfolio() {
        // Arrange: Create a new Portfolio object
        Portfolio portfolio = new Portfolio();
        portfolio.setName("My Portfolio");

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
