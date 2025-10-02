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

import com.budiyanto.fintrackr.investmentservice.domain.Instrument;
import com.budiyanto.fintrackr.investmentservice.domain.InstrumentType;
import com.budiyanto.fintrackr.investmentservice.domain.Portfolio;
import com.budiyanto.fintrackr.investmentservice.domain.Trade;
import com.budiyanto.fintrackr.investmentservice.domain.TradeType;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TradeRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    private final TradeRepository tradeRepository;
    private final PortfolioRepository portfolioRepository;
    private final InstrumentRepository instrumentRepository;

    @Autowired
    TradeRepositoryTest(TradeRepository tradeRepository, PortfolioRepository portfolioRepository,
            InstrumentRepository instrumentRepository) {
        this.tradeRepository = tradeRepository;
        this.portfolioRepository = portfolioRepository;
        this.instrumentRepository = instrumentRepository;  
    }

    @Test
    void shouldSaveAndRetrieveTrade() {
        // Arrange: Create a new Trade object
        Portfolio portfolio = new Portfolio();
        portfolio.setName("My Portfolio");
        Portfolio savedPortfolio = portfolioRepository.save(portfolio);

        Instrument instrument = new Instrument();
        instrument.setName("Bank Central Asia");
        instrument.setCode("BBCA");
        instrument.setInstrumentType(InstrumentType.STOCK);
        instrument.setCurrency("IDR");
        Instrument savedInstrument = instrumentRepository.save(instrument);

        Trade trade = new Trade();
        trade.setPortfolio(savedPortfolio);
        trade.setInstrument(savedInstrument);
        trade.setTradeType(TradeType.BUY);
        trade.setQuantity(new BigDecimal(100));
        trade.setPrice(new BigDecimal(1520));
        trade.setTradedAt(Instant.now());
        
        // Act: Save using the repository
        Trade savedTrade = tradeRepository.save(trade);

        // Assert: Verify that the trade was saved correctly and can be retrieved
        assertThat(savedTrade).isNotNull();
        assertThat(savedTrade.getId()).isGreaterThan(0);

        Trade retrievedTrade = tradeRepository.findById(savedTrade.getId()).orElse(null);
        assertThat(retrievedTrade.getPortfolio().getId()).isEqualTo(savedPortfolio.getId());
        assertThat(retrievedTrade.getInstrument().getId()).isEqualTo(savedInstrument.getId());
        assertThat(retrievedTrade.getTradeType()).isEqualTo(trade.getTradeType());
        assertThat(retrievedTrade.getQuantity()).isEqualTo(trade.getQuantity());
        assertThat(retrievedTrade.getPrice()).isEqualTo(trade.getPrice());
        assertThat(retrievedTrade.getTradedAt()).isNotNull();

    }


}
