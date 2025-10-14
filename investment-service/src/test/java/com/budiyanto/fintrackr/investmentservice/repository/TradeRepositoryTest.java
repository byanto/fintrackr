package com.budiyanto.fintrackr.investmentservice.repository;


import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.budiyanto.fintrackr.investmentservice.domain.Instrument;
import com.budiyanto.fintrackr.investmentservice.domain.InstrumentType;
import com.budiyanto.fintrackr.investmentservice.domain.Portfolio;
import com.budiyanto.fintrackr.investmentservice.domain.Trade;
import com.budiyanto.fintrackr.investmentservice.domain.TradeType;

@DisplayName("TradeRepository Tests")
class TradeRepositoryTest extends AbstractRepositoryTest{

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
    @DisplayName("should save and retrieve trade")
    void should_saveAndRetrieveTrade() {
        // Arrange: Create a new Trade object
        Portfolio portfolio = new Portfolio("My Portfolio");
        Portfolio savedPortfolio = portfolioRepository.save(portfolio);

        Instrument instrument = new Instrument(InstrumentType.STOCK, "BBCA", "Bank Central Asia", "IDR");
        Instrument savedInstrument = instrumentRepository.save(instrument);

        Trade trade = new Trade(savedPortfolio, savedInstrument, TradeType.BUY, new BigDecimal(100), new BigDecimal(1520), Instant.now());
        
        // Act: Save using the repository
        Trade savedTrade = tradeRepository.save(trade);

        // Assert: Verify that the trade was saved correctly and can be retrieved
        assertThat(savedTrade).isNotNull();
        assertThat(savedTrade.getId()).isGreaterThan(0);

        Trade retrievedTrade = tradeRepository.findById(savedTrade.getId()).orElse(null);
        assertThat(retrievedTrade.getPortfolio().getId()).isEqualTo(savedPortfolio.getId());
        assertThat(retrievedTrade.getInstrument().getId()).isEqualTo(savedInstrument.getId());
        assertThat(retrievedTrade.getTradeType()).isEqualTo(trade.getTradeType());
        assertThat(retrievedTrade.getQuantity()).isEqualByComparingTo(trade.getQuantity());
        assertThat(retrievedTrade.getPrice()).isEqualByComparingTo(trade.getPrice());
        assertThat(retrievedTrade.getTradedAt()).isNotNull();

    }


}
