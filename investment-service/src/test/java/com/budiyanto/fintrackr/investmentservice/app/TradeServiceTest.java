package com.budiyanto.fintrackr.investmentservice.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.budiyanto.fintrackr.investmentservice.api.dto.CreateTradeRequest;
import com.budiyanto.fintrackr.investmentservice.api.dto.TradeResponse;
import com.budiyanto.fintrackr.investmentservice.app.mapper.TradeMapper;
import com.budiyanto.fintrackr.investmentservice.domain.Instrument;
import com.budiyanto.fintrackr.investmentservice.domain.InstrumentType;
import com.budiyanto.fintrackr.investmentservice.domain.Portfolio;
import com.budiyanto.fintrackr.investmentservice.domain.Trade;
import com.budiyanto.fintrackr.investmentservice.domain.TradeType;
import com.budiyanto.fintrackr.investmentservice.repository.InstrumentRepository;
import com.budiyanto.fintrackr.investmentservice.repository.PortfolioRepository;
import com.budiyanto.fintrackr.investmentservice.repository.TradeRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("TradeService Tests")
class TradeServiceTest {

    @Mock
    private TradeRepository tradeRepository;

    @Mock
    private PortfolioRepository portfolioRepository;

    @Mock
    private InstrumentRepository instrumentRepository;

    @Mock
    private TradeMapper tradeMapper;
    
    @InjectMocks
    private TradeService tradeService;

    private static final Long TRADE_ID = 1L;
    private static final Long PORTFOLIO_ID = 1L;
    private static final Long INSTRUMENT_ID = 1L;
    private static final TradeType TRADE_TYPE = TradeType.BUY;
    private static final BigDecimal QUANTITY = new BigDecimal(100);
    private static final BigDecimal PRICE = new BigDecimal(1520);

    @Nested
    @DisplayName("createTrade method")
    class CreateTrade {
        
        @Test
        @DisplayName("should create trade")
        void should_createTrade() {
            // Arrange
            Instant tradedAt = Instant.now();
            CreateTradeRequest request = new CreateTradeRequest(
                PORTFOLIO_ID,
                INSTRUMENT_ID,
                TRADE_TYPE,
                QUANTITY,
                PRICE,
                tradedAt
            );

            Portfolio portfolio = new Portfolio("Test Portfolio");
            ReflectionTestUtils.setField(portfolio, "id", PORTFOLIO_ID);
            when(portfolioRepository.findById(PORTFOLIO_ID)).thenReturn(Optional.of(portfolio));

            Instrument instrument = new Instrument(InstrumentType.STOCK, "BBCA", "Bank Central Asia", "IDR");
            ReflectionTestUtils.setField(instrument, "id", INSTRUMENT_ID);
            when(instrumentRepository.findById(INSTRUMENT_ID)).thenReturn(Optional.of(instrument));

            Trade savedTrade = new Trade(portfolio, instrument, TRADE_TYPE, QUANTITY, PRICE, tradedAt);
            ReflectionTestUtils.setField(savedTrade, "id", TRADE_ID);
            when(tradeRepository.save(any(Trade.class))).thenReturn(savedTrade);

            TradeResponse response = new TradeResponse(TRADE_ID, PORTFOLIO_ID, INSTRUMENT_ID, TRADE_TYPE, QUANTITY, PRICE, tradedAt);
            when(tradeMapper.toResponseDto(savedTrade)).thenReturn(response);

            // Act
            TradeResponse result = tradeService.createTrade(request);

            // Assert on the returned DTO (State-based test)
            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(response);

            // Assert on the interaction: verify the correct entity was passed to save()
            ArgumentCaptor<Trade> captor = ArgumentCaptor.forClass(Trade.class);
            verify(tradeRepository).save(captor.capture());

            Trade capturedTrade = captor.getValue();
            assertThat(capturedTrade.getId()).isNull();
            assertThat(capturedTrade.getPortfolio()).isSameAs(portfolio);
            assertThat(capturedTrade.getInstrument()).isSameAs(instrument);
            assertThat(capturedTrade.getTradeType()).isEqualTo(TRADE_TYPE);
            assertThat(capturedTrade.getQuantity()).isEqualByComparingTo(QUANTITY);
            assertThat(capturedTrade.getPrice()).isEqualByComparingTo(PRICE);
            assertThat(capturedTrade.getTradedAt()).isEqualTo(tradedAt);

            // Verify other interactions
            verify(portfolioRepository).findById(PORTFOLIO_ID);
            verify(instrumentRepository).findById(INSTRUMENT_ID);
            verify(tradeMapper).toResponseDto(savedTrade);
        
        }
    }   

}
