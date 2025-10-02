package com.budiyanto.fintrackr.investmentservice.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.budiyanto.fintrackr.investmentservice.domain.Holding;
import com.budiyanto.fintrackr.investmentservice.domain.Instrument;
import com.budiyanto.fintrackr.investmentservice.domain.InstrumentType;
import com.budiyanto.fintrackr.investmentservice.domain.Portfolio;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class HoldingRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    private final HoldingRepository holdingRepository;
    private final PortfolioRepository portfolioRepository;
    private final InstrumentRepository instrumentRepository;

    @Autowired
    HoldingRepositoryTest(HoldingRepository holdingRepository, PortfolioRepository portfolioRepository,
            InstrumentRepository instrumentRepository) {
        this.holdingRepository = holdingRepository;
        this.portfolioRepository = portfolioRepository;
        this.instrumentRepository = instrumentRepository;    
    }

    @Test
    void shouldSaveAndRetrieveHolding() {
        // Arrange: Create a new Holding object
        Portfolio portfolio = new Portfolio();
        portfolio.setName("My Portfolio");
        Portfolio savedPortfolio = portfolioRepository.save(portfolio);     
    
        Instrument instrument = new Instrument();
        instrument.setName("Bank Central Asia");
        instrument.setCode("BBCA");
        instrument.setInstrumentType(InstrumentType.STOCK);
        instrument.setCurrency("IDR");
        Instrument savedInstrument = instrumentRepository.save(instrument);

        Holding holding = new Holding();
        holding.setPortfolio(savedPortfolio);
        holding.setInstrument(savedInstrument);
        holding.setQuantity(new BigDecimal(3000));
        holding.setAveragePrice(new BigDecimal(2780));
        holding.setUpdatedAt(Instant.now()); 

        // Act: Save using the repository
        Holding savedHolding = holdingRepository.save(holding);

        // Assert: Verify that the holding was saved correctly and can be retrieved
        assertThat(savedHolding).isNotNull();
        assertThat(savedHolding.getId()).isGreaterThan(0);

        Holding retrievedHolding = holdingRepository.findById(savedHolding.getId()).orElse(null);
        assertThat(retrievedHolding.getPortfolio().getId()).isEqualTo(savedPortfolio.getId());
        assertThat(retrievedHolding.getInstrument().getId()).isEqualTo(savedInstrument.getId());
        assertThat(retrievedHolding.getQuantity()).isEqualTo(retrievedHolding.getQuantity());
        assertThat(retrievedHolding.getAveragePrice()).isEqualTo(retrievedHolding.getAveragePrice());
        assertThat(retrievedHolding.getUpdatedAt()).isNotNull();

    }



}
