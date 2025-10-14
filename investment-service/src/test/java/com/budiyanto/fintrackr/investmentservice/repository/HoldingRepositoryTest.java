package com.budiyanto.fintrackr.investmentservice.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.budiyanto.fintrackr.investmentservice.domain.Holding;
import com.budiyanto.fintrackr.investmentservice.domain.Instrument;
import com.budiyanto.fintrackr.investmentservice.domain.InstrumentType;
import com.budiyanto.fintrackr.investmentservice.domain.Portfolio;

@DisplayName("HoldingRepository Tests")
class HoldingRepositoryTest extends AbstractRepositoryTest {

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
    @DisplayName("should save and retrieve holding")
    void should_saveAndRetrieveHolding() {
        // Arrange: Create a new Holding object
        Portfolio portfolio = new Portfolio("My Portfolio");
        Portfolio savedPortfolio = portfolioRepository.save(portfolio);     
    
        Instrument instrument = new Instrument(InstrumentType.STOCK, "BBCA", "Bank Central Asia", "IDR");
        Instrument savedInstrument = instrumentRepository.save(instrument);

        Holding holding = new Holding(savedPortfolio, savedInstrument, new BigDecimal(3000), new BigDecimal(2780)); 

        // Act: Save using the repository
        Holding savedHolding = holdingRepository.save(holding);

        // Assert: Verify that the holding was saved correctly and can be retrieved
        assertThat(savedHolding).isNotNull();
        assertThat(savedHolding.getId()).isGreaterThan(0);

        Holding retrievedHolding = holdingRepository.findById(savedHolding.getId()).orElse(null);
        assertThat(retrievedHolding.getPortfolio().getId()).isEqualTo(savedPortfolio.getId());
        assertThat(retrievedHolding.getInstrument().getId()).isEqualTo(savedInstrument.getId());
        assertThat(retrievedHolding.getQuantity()).isEqualByComparingTo(holding.getQuantity());
        assertThat(retrievedHolding.getAveragePrice()).isEqualByComparingTo(holding.getAveragePrice());
        assertThat(retrievedHolding.getUpdatedAt()).isNotNull();

    }

}
