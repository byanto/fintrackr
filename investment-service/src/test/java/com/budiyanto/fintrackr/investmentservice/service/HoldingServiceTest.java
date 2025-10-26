package com.budiyanto.fintrackr.investmentservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
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

import com.budiyanto.fintrackr.investmentservice.domain.BrokerAccount;
import com.budiyanto.fintrackr.investmentservice.domain.Holding;
import com.budiyanto.fintrackr.investmentservice.domain.Instrument;
import com.budiyanto.fintrackr.investmentservice.domain.InstrumentType;
import com.budiyanto.fintrackr.investmentservice.domain.Portfolio;
import com.budiyanto.fintrackr.investmentservice.domain.Trade;
import com.budiyanto.fintrackr.investmentservice.domain.TradeType;
import com.budiyanto.fintrackr.investmentservice.dto.HoldingResponse;
import com.budiyanto.fintrackr.investmentservice.exception.HoldingNotFoundException;
import com.budiyanto.fintrackr.investmentservice.exception.InsufficientHoldingsException;
import com.budiyanto.fintrackr.investmentservice.exception.PortfolioNotFoundException;
import com.budiyanto.fintrackr.investmentservice.mapper.HoldingMapper;
import com.budiyanto.fintrackr.investmentservice.repository.HoldingRepository;
import com.budiyanto.fintrackr.investmentservice.repository.PortfolioRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("HoldingService Tests")
class HoldingServiceTest {

    @Mock
    private HoldingRepository holdingRepository;

    @Mock
    private PortfolioRepository portfolioRepository;

    @Mock
    private HoldingMapper holdingMapper;

    @InjectMocks
    private HoldingService holdingService;

    private Portfolio portfolio;
    private Instrument instrument;
    private static final Long PORTFOLIO_ID = 1L;
    private static final Long INSTRUMENT_ID = 1L;
    private static final BigDecimal TRADE_QUANTITY = new BigDecimal(500);
    private static final BigDecimal TRADE_PRICE = new BigDecimal(1500);
    private static final BigDecimal TRADE_FEE = new BigDecimal("150");
    private static final Long HOLDING_ID = 1L;
    private static final BigDecimal HOLDING_QUANTITY = new BigDecimal(2000);
    private static final BigDecimal HOLDING_AVERAGE_PRICE = new BigDecimal(1800);

    @BeforeEach
    void setUp() {
        Instant createdAt = Instant.now();
        BrokerAccount brokerAccount = new BrokerAccount("Test Broker Account", "Broker A");
        portfolio = new Portfolio("Test Portfolio", brokerAccount);
        ReflectionTestUtils.setField(portfolio, "id", PORTFOLIO_ID);
        ReflectionTestUtils.setField(portfolio, "createdAt", createdAt);

        instrument = new Instrument(InstrumentType.STOCK, "BBCA", "Bank Central Asia", "IDR");
        ReflectionTestUtils.setField(instrument, "id", INSTRUMENT_ID);
        ReflectionTestUtils.setField(instrument, "createdAt", createdAt);
    }

    @Nested
    @DisplayName("processTrade method")
    class ProcessTrade {
        @Test
        @DisplayName("should create new holding with fee when first buy trade is processed")
        void should_createNewHolding_when_firstBuyTradeIsProcessed() {
            
            Trade trade = new Trade(portfolio, instrument, TradeType.BUY, TRADE_QUANTITY, TRADE_PRICE, TRADE_FEE, Instant.now());
            when(holdingRepository.findByPortfolioIdAndInstrumentId(portfolio.getId(), instrument.getId())).thenReturn(Optional.empty());
            
            // Act
            holdingService.processTrade(trade);

            // Assert
            ArgumentCaptor<Holding> captor = ArgumentCaptor.forClass(Holding.class);
            verify(holdingRepository).save(captor.capture());
            Holding capturedHolding = captor.getValue();

            // Total cost = (500 * 1500) + 150 = 750150. Avg Price = 750150 / 500 = 1500.3
            BigDecimal expectedAveragePrice = TRADE_QUANTITY.multiply(TRADE_PRICE).add(TRADE_FEE)
                    .divide(TRADE_QUANTITY, 4, RoundingMode.HALF_UP);

            assertThat(capturedHolding).isNotNull();
            assertThat(capturedHolding.getPortfolio()).isEqualTo(portfolio);
            assertThat(capturedHolding.getInstrument()).isEqualTo(instrument);
            assertThat(capturedHolding.getQuantity()).isEqualByComparingTo(TRADE_QUANTITY);
            assertThat(capturedHolding.getAveragePrice()).isEqualByComparingTo(expectedAveragePrice);
            assertThat(capturedHolding.getUpdatedAt()).isNull();
        }

        @Test
        @DisplayName("should update existing holding with fee when subsequent buy trade is processed")
        void should_updateExistingHolding_when_subsequentBuyTradeIsProcessed() {
            // Arrange
            Trade trade = new Trade(portfolio, instrument, TradeType.BUY, TRADE_QUANTITY, TRADE_PRICE, TRADE_FEE, Instant.now());
            Holding existingHolding = new Holding(portfolio, instrument, HOLDING_QUANTITY, HOLDING_AVERAGE_PRICE);
            when(holdingRepository.findByPortfolioIdAndInstrumentId(portfolio.getId(), instrument.getId())).thenReturn(Optional.of(existingHolding));
            
            // Act
            holdingService.processTrade(trade);

            // Assert
            ArgumentCaptor<Holding> captor = ArgumentCaptor.forClass(Holding.class);
            verify(holdingRepository).save(captor.capture());
            Holding capturedHolding = captor.getValue();

            BigDecimal newQuantity = HOLDING_QUANTITY.add(TRADE_QUANTITY);
            BigDecimal newAveragePrice = HOLDING_QUANTITY.multiply(HOLDING_AVERAGE_PRICE)
                    .add(TRADE_QUANTITY.multiply(TRADE_PRICE)).add(TRADE_FEE)
                    .divide(newQuantity, 4, RoundingMode.HALF_UP);
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
            Trade trade = new Trade(portfolio, instrument, TradeType.SELL, TRADE_QUANTITY, TRADE_PRICE, TRADE_FEE, Instant.now());
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
            Trade trade = new Trade(portfolio, instrument, TradeType.SELL, HOLDING_QUANTITY, TRADE_PRICE, TRADE_FEE, Instant.now());
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
            Trade trade = new Trade(portfolio, instrument, TradeType.SELL, sellQuantity, TRADE_PRICE, TRADE_FEE, Instant.now());
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
            Trade trade = new Trade(portfolio, instrument, TradeType.SELL, TRADE_QUANTITY, TRADE_PRICE, TRADE_FEE, Instant.now());
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

    @Nested
    @DisplayName("retrieveHoldingById method")
    class RetrieveHoldingById {

        @Test
        @DisplayName("should return holding when ID exists")
        void should_returnHolding_when_idExists() {
            // Arrange
            Holding existingHolding = new Holding(portfolio, instrument, HOLDING_QUANTITY, HOLDING_AVERAGE_PRICE);
            when(holdingRepository.findById(HOLDING_ID)).thenReturn(Optional.of(existingHolding));
            
            var instrumentDto = new HoldingResponse.InstrumentInHoldingResponse(INSTRUMENT_ID, "BBCA", "Bank Central Asia", "IDR");
            HoldingResponse response = new HoldingResponse(HOLDING_ID, PORTFOLIO_ID, instrumentDto, HOLDING_QUANTITY, HOLDING_AVERAGE_PRICE, Instant.now());
            when(holdingMapper.toResponseDto(existingHolding)).thenReturn(response);

            // Act
            HoldingResponse result = holdingService.retrieveHoldingById(HOLDING_ID);
            
            // Assert
            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(response);

            // Verify interactions
            verify(holdingRepository).findById(HOLDING_ID);
            verify(holdingMapper).toResponseDto(existingHolding);
        }

        @Test
        @DisplayName("should throw exception when ID not exists")
        void should_throwException_when_idNotExists() {
            // Arrange
            Long nonExistentId = 99L;
            when(holdingRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> holdingService.retrieveHoldingById(nonExistentId))
                .isInstanceOf(HoldingNotFoundException.class)
                .asInstanceOf(InstanceOfAssertFactories.type(HoldingNotFoundException.class))
                .satisfies(ex -> {
                    assertThat(ex.getId()).isEqualTo(nonExistentId);
                });

            // Verify that no further interactions occurred
            verify(holdingMapper, never()).toResponseDto(any());
        }

        @Nested
        @DisplayName("retrieveHoldingsByPortfolioId method")
        class RetrieveHoldingsByPortfolioId {
            @Test
            @DisplayName("should return holdings when portfolio exists")
            void should_returnHolding_when_portfolioExists() {
                // Arrange
                when(portfolioRepository.existsById(PORTFOLIO_ID)).thenReturn(true);

                Long holdingId1 = 1L;
                Long instrumentId1 = 1L;
                String instrumentCode1 = "BBCA";
                String instrumentName1 = "Bank Central Asia";
                String instrumentCurrency1 = "IDR";
                BigDecimal holdingQuantity1 = new BigDecimal(1000);
                BigDecimal holdingAveragePrice1 = new BigDecimal(1250);

                Long holdingId2 = 2L;
                Long instrumentId2 = 2L;
                String instrumentCode2 = "BBRI";
                String instrumentName2 = "Bank Rakyat Indonesia";
                String instrumentCurrency2 = "IDR";
                BigDecimal holdingQuantity2 = new BigDecimal(2000);
                BigDecimal holdingAveragePrice2 = new BigDecimal(2250);

                Instrument instrument1 = new Instrument(InstrumentType.STOCK, instrumentCode1, instrumentName1, instrumentCurrency1);
                Instrument instrument2 = new Instrument(InstrumentType.STOCK, instrumentCode2, instrumentName2, instrumentCurrency2);
                Holding holding1 = new Holding(portfolio, instrument1, holdingQuantity1, holdingAveragePrice1);
                Holding holding2 = new Holding(portfolio, instrument2, holdingQuantity2, holdingAveragePrice2);
                when(holdingRepository.findByPortfolioId(PORTFOLIO_ID)).thenReturn(List.of(holding1, holding2));

                var instrumentResponseDto1 = new HoldingResponse.InstrumentInHoldingResponse(instrumentId1, instrumentCode1, instrumentName1, instrumentCurrency1);
                var instrumentResponseDto2 = new HoldingResponse.InstrumentInHoldingResponse(instrumentId2, instrumentCode2, instrumentName2, instrumentCurrency2);
                HoldingResponse response1 = new HoldingResponse(holdingId1, PORTFOLIO_ID, instrumentResponseDto1, holdingQuantity1, holdingAveragePrice1, Instant.now());
                HoldingResponse response2 = new HoldingResponse(holdingId2, PORTFOLIO_ID, instrumentResponseDto2, holdingQuantity2, holdingAveragePrice2, Instant.now());
                when(holdingMapper.toResponseDtoList(List.of(holding1, holding2))).thenReturn(List.of(response1, response2));

                // Act
                List<HoldingResponse> result = holdingService.retrieveHoldingsByPortfolioId(PORTFOLIO_ID);
    
                // Assert
                assertThat(result).isNotNull();
                assertThat(result).hasSize(2);
                assertThat(result).containsExactlyInAnyOrder(response1, response2);
    
                // Verify interactions
                verify(portfolioRepository).existsById(PORTFOLIO_ID);
                verify(holdingRepository).findByPortfolioId(PORTFOLIO_ID);
                verify(holdingMapper).toResponseDtoList(List.of(holding1, holding2));
            }

            @Test
            @DisplayName("should throw exception when non existent portfolio")
            void should_throwException_when_nonExistentPortfolio() {
                // Arrange
                Long nonExistentId = 99L;
                when(portfolioRepository.existsById(nonExistentId)).thenReturn(false);

                // Act & Assert
                assertThatThrownBy(() -> holdingService.retrieveHoldingsByPortfolioId(nonExistentId))
                    .isInstanceOf(PortfolioNotFoundException.class)
                    .asInstanceOf(InstanceOfAssertFactories.type(PortfolioNotFoundException.class))
                    .satisfies(ex -> {
                        assertThat(ex.getId()).isEqualTo(nonExistentId);
                    });
            }
        }   
    }
}
