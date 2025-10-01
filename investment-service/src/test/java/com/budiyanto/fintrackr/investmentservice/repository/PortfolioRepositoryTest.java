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

    @Autowired
    private PortfolioRepository portfolioRepository;

    @Test
    void shouldSaveAndRetrievePortfolio() {
        // Arrange: Create a new Portfolio object
        Portfolio portfolio = new Portfolio();
        String portfolioName = "My Portfolio";
        portfolio.setName(portfolioName);

        // Act: Save the portfolio using the repository
        portfolio.setName("My Portfolio");

        // Act: Save the portfolio using the repository
        Portfolio savedPortfolio = portfolioRepository.save(portfolio);

        // Assert: Verify that the portfolio was saved correctly and can be retrieved
        assertThat(savedPortfolio).isNotNull();
        assertThat(savedPortfolio.getId()).isGreaterThan(0);

        Portfolio foundPortfolio = portfolioRepository.findById(savedPortfolio.getId()).orElse(null);
        assertThat(foundPortfolio).isNotNull();
        assertThat(foundPortfolio.getName()).isEqualTo(portfolioName);
        assertThat(foundPortfolio.getCreatedAt()).isNotNull();

    }
}
