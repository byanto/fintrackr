package com.budiyanto.fintrackr.investmentservice.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.budiyanto.fintrackr.investmentservice.TestcontainersConfiguration;
import com.budiyanto.fintrackr.investmentservice.domain.BrokerAccount;
import com.budiyanto.fintrackr.investmentservice.domain.Portfolio;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
@DisplayName("PortfolioRepository Tests")
class PortfolioRepositoryTest {

    private final PortfolioRepository portfolioRepository;
    private final BrokerAccountRepository brokerAccountRepository;

    @Autowired
    PortfolioRepositoryTest(PortfolioRepository portfolioRepository, BrokerAccountRepository brokerAccountRepository) {
        this.portfolioRepository = portfolioRepository;
        this.brokerAccountRepository = brokerAccountRepository;
    }

    @Test
    @DisplayName("should save and retrieve portfolio")
    void should_saveAndRetrievePortfolio() {
        // Arrange: Create a new BrokerAccount and Portfolio object
        BrokerAccount brokerAccount = new BrokerAccount("Test Broker Account", "Broker A");
        BrokerAccount savedBrokerAccount = brokerAccountRepository.save(brokerAccount);
        Portfolio portfolio = new Portfolio("My Portfolio", savedBrokerAccount);

        // Act: Save the portfolio using the repository
        Portfolio savedPortfolio = portfolioRepository.save(portfolio);

        // Assert: Verify that the portfolio was saved correctly
        assertThat(savedPortfolio).isNotNull();
        assertThat(savedPortfolio.getId()).isGreaterThan(0);

        // Act: Retrieve the saved portfolio
        Portfolio retrievedPortfolio = portfolioRepository.findById(savedPortfolio.getId()).orElse(null);

        // Assert: Verify that the portfolio can be retrieved
        assertThat(retrievedPortfolio).isNotNull();
        assertThat(retrievedPortfolio.getName()).isEqualTo(portfolio.getName());
        assertThat(retrievedPortfolio.getBrokerAccount().getId()).isEqualTo(savedBrokerAccount.getId());
        assertThat(retrievedPortfolio.getBrokerAccount().getName()).isEqualTo(savedBrokerAccount.getName());
        assertThat(retrievedPortfolio.getBrokerAccount().getBrokerName()).isEqualTo(savedBrokerAccount.getBrokerName());
        assertThat(retrievedPortfolio.getCreatedAt()).isNotNull();
    }
}
