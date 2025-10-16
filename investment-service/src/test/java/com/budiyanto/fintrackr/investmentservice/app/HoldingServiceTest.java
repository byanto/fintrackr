package com.budiyanto.fintrackr.investmentservice.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Optional;

import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import com.budiyanto.fintrackr.investmentservice.app.exception.InsufficientHoldingsException;
import com.budiyanto.fintrackr.investmentservice.domain.Holding;
import com.budiyanto.fintrackr.investmentservice.domain.Instrument;
import com.budiyanto.fintrackr.investmentservice.domain.InstrumentType;
import com.budiyanto.fintrackr.investmentservice.domain.Portfolio;
import com.budiyanto.fintrackr.investmentservice.domain.Trade;
import com.budiyanto.fintrackr.investmentservice.domain.TradeType;
import com.budiyanto.fintrackr.investmentservice.repository.HoldingRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("HoldingService Tests")
class HoldingServiceTest {

    @Mock
    private HoldingRepository holdingRepository;

    @InjectMocks
    private HoldingService holdingService;

    private Portfolio portfolio;
    private Instrument instrument;
    private static final Long PORTFOLIO_ID = 1L;
    private static final Long INSTRUMENT_ID = 1L;
    private static final BigDecimal TRADE_QUANTITY = new BigDecimal(500);
    private static final BigDecimal TRADE_PRICE = new BigDecimal(1500);
    private static final Long HOLDING_ID = 1L;
    private static final BigDecimal HOLDING_QUANTITY = new BigDecimal(2000);
    private static final BigDecimal HOLDING_AVERAGE_PRICE = new BigDecimal(1800);


    @BeforeEach
    void setUp() {
        portfolio = new Portfolio("Test Portfolio");
        ReflectionTestUtils.setField(portfolio, "id", PORTFOLIO_ID);

        instrument = new Instrument(InstrumentType.STOCK, "BBCA", "Bank Central Asia", "IDR");
        ReflectionTestUtils.setField(instrument, "id", INSTRUMENT_ID);
        ReflectionTestUtils.setField(instrument, "createdAt", Instant.now());
    }

    @Nested
    @DisplayName("processTrade method")
    class ProcessTrade {
        @Test
        @DisplayName("should create new holding when first buy trade is processed")
        void should_createNewHolding_when_firstBuyTradeIsProcessed() {
            Trade trade = new Trade(portfolio, instrument, TradeType.BUY, TRADE_QUANTITY, TRADE_PRICE, Instant.now());
            when(holdingRepository.findByPortfolioIdAndInstrumentId(portfolio.getId(), instrument.getId())).thenReturn(Optional.empty());
            
            // Act
            holdingService.processTrade(trade);

            // Assert
            ArgumentCaptor<Holding> captor = ArgumentCaptor.forClass(Holding.class);
            verify(holdingRepository).save(captor.capture());
            Holding capturedHolding = captor.getValue();

            assertThat(capturedHolding).isNotNull();
            assertThat(capturedHolding.getPortfolio()).isEqualTo(portfolio);
            assertThat(capturedHolding.getInstrument()).isEqualTo(instrument);
            assertThat(capturedHolding.getQuantity()).isEqualByComparingTo(TRADE_QUANTITY);
            assertThat(capturedHolding.getAveragePrice()).isEqualByComparingTo(TRADE_PRICE);
            assertThat(capturedHolding.getUpdatedAt()).isNull();
        }

        @Test
        @DisplayName("should update existing holding when subsequent buy trade is processed")
        void should_updateExistingHolding_when_subsequentBuyTradeIsProcessed() {
            // Arrange
            Trade trade = new Trade(portfolio, instrument, TradeType.BUY, TRADE_QUANTITY, TRADE_PRICE, Instant.now());
            Holding existingHolding = new Holding(portfolio, instrument, HOLDING_QUANTITY, HOLDING_AVERAGE_PRICE);
            when(holdingRepository.findByPortfolioIdAndInstrumentId(portfolio.getId(), instrument.getId())).thenReturn(Optional.of(existingHolding));
            
            // Act
            holdingService.processTrade(trade);

            // Assert
            ArgumentCaptor<Holding> captor = ArgumentCaptor.forClass(Holding.class);
            verify(holdingRepository).save(captor.capture());
            Holding capturedHolding = captor.getValue();

            BigDecimal newQuantity = HOLDING_QUANTITY.add(TRADE_QUANTITY);
            BigDecimal newAveragePrice = HOLDING_QUANTITY.multiply(HOLDING_AVERAGE_PRICE).add(TRADE_QUANTITY.multiply(TRADE_PRICE)).divide(newQuantity, 4, RoundingMode.HALF_UP);
            assertThat(capturedHolding).isNotNull();
            assertThat(capturedHolding.getPortfolio()).isEqualTo(portfolio);
            assertThat(capturedHolding.getInstrument()).isEqualTo(instrument);
            assertThat(capturedHolding.getQuantity()).isEqualByComparingTo(newQuantity);
            assertThat(capturedHolding.getAveragePrice()).isEqualByComparingTo(newAveragePrice);
            assertThat(capturedHolding.getUpdatedAt()).isNull();
        }

        @Test
        @DisplayName("should update existing holding when sell trade is processed")
        void should_updateExistingHolding_when_sellTradeIsProcessed() {
            // Arrange
            Trade trade = new Trade(portfolio, instrument, TradeType.SELL, TRADE_QUANTITY, TRADE_PRICE, Instant.now());
            Holding existingHolding = new Holding(portfolio, instrument, HOLDING_QUANTITY, HOLDING_AVERAGE_PRICE);
            when(holdingRepository.findByPortfolioIdAndInstrumentId(portfolio.getId(), instrument.getId())).thenReturn(Optional.of(existingHolding));
            
            // Act
            holdingService.processTrade(trade);

            // Assert
            ArgumentCaptor<Holding> captor = ArgumentCaptor.forClass(Holding.class);
            verify(holdingRepository).save(captor.capture());
            Holding capturedHolding = captor.getValue();

            BigDecimal newQuantity = HOLDING_QUANTITY.subtract(TRADE_QUANTITY);
            assertThat(capturedHolding).isNotNull();
            assertThat(capturedHolding.getPortfolio()).isEqualTo(portfolio);
            assertThat(capturedHolding.getInstrument()).isEqualTo(instrument);
            assertThat(capturedHolding.getQuantity()).isEqualByComparingTo(newQuantity);
            assertThat(capturedHolding.getAveragePrice()).isEqualByComparingTo(HOLDING_AVERAGE_PRICE); // Average price must be unchanged on sell
            assertThat(capturedHolding.getUpdatedAt()).isNull();
        }

        @Test
        @DisplayName("should delete holding when quantity is zero after sell")
        void should_deleteHolding_when_quantityIsZeroAfterSell() {
            // Arrange
            Trade trade = new Trade(portfolio, instrument, TradeType.SELL, HOLDING_QUANTITY, TRADE_PRICE, Instant.now());
            Holding existingHolding = new Holding(portfolio, instrument, HOLDING_QUANTITY, HOLDING_AVERAGE_PRICE);
            ReflectionTestUtils.setField(existingHolding, "id", HOLDING_ID);
            when(holdingRepository.findByPortfolioIdAndInstrumentId(portfolio.getId(), instrument.getId())).thenReturn(Optional.of(existingHolding));
            
            // Act
            holdingService.processTrade(trade);

            // Assert
            verify(holdingRepository).delete(existingHolding);
            verify(holdingRepository, never()).save(any(Holding.class));
        }

        @Test
        @DisplayName("should throw exception when selling more than holding quantity")
        void should_throwException_when_sellingMoreThanHoldingQuantity() {
            // Arrange
            BigDecimal sellQuantity = HOLDING_QUANTITY.add(BigDecimal.TEN);
            Trade trade = new Trade(portfolio, instrument, TradeType.SELL, sellQuantity, TRADE_PRICE, Instant.now());
            Holding existingHolding = new Holding(portfolio, instrument, HOLDING_QUANTITY, HOLDING_AVERAGE_PRICE);
            ReflectionTestUtils.setField(existingHolding, "id", HOLDING_ID);
            when(holdingRepository.findByPortfolioIdAndInstrumentId(portfolio.getId(), instrument.getId())).thenReturn(Optional.of(existingHolding));
            
            // Act & Assert
            assertThatThrownBy(() -> holdingService.processTrade(trade))
                    .isInstanceOf(InsufficientHoldingsException.class)
                    .asInstanceOf(InstanceOfAssertFactories.type(InsufficientHoldingsException.class))
                    .satisfies(ex -> {
                        assertThat(ex.getAttemptedQuantity()).isEqualByComparingTo(sellQuantity);
                        assertThat(ex.getAvailableQuantity()).isEqualByComparingTo(HOLDING_QUANTITY);
                        assertThat(ex.getPortfolioId()).isEqualTo(PORTFOLIO_ID);
                        assertThat(ex.getInstrumentId()).isEqualTo(INSTRUMENT_ID);
                    });

            // Verify
            verify(holdingRepository, never()).delete(any(Holding.class));
            verify(holdingRepository, never()).save(any(Holding.class));
        }

        @Test
        @DisplayName("should throw exception when selling with no holding quantity")
        void should_throwException_when_sellingWithNoHoldingQuantity() {
            // Arrange
            Trade trade = new Trade(portfolio, instrument, TradeType.SELL, TRADE_QUANTITY, TRADE_PRICE, Instant.now());
            when(holdingRepository.findByPortfolioIdAndInstrumentId(portfolio.getId(), instrument.getId())).thenReturn(Optional.empty());
            
            // Act & Assert
            assertThatThrownBy(() -> holdingService.processTrade(trade))
                    .isInstanceOf(InsufficientHoldingsException.class)
                    .asInstanceOf(InstanceOfAssertFactories.type(InsufficientHoldingsException.class))
                    .satisfies(ex -> {
                        assertThat(ex.getAttemptedQuantity()).isEqualByComparingTo(TRADE_QUANTITY);
                        assertThat(ex.getAvailableQuantity()).isEqualByComparingTo(BigDecimal.ZERO);
                        assertThat(ex.getPortfolioId()).isEqualTo(PORTFOLIO_ID);
                        assertThat(ex.getInstrumentId()).isEqualTo(INSTRUMENT_ID);
                    });

            // Verify
            verify(holdingRepository, never()).delete(any(Holding.class));
            verify(holdingRepository, never()).save(any(Holding.class));
        }
    }
}
