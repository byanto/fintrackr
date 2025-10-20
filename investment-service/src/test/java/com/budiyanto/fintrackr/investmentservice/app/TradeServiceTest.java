package com.budiyanto.fintrackr.investmentservice.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.assertj.core.api.InstanceOfAssertFactories;
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
import com.budiyanto.fintrackr.investmentservice.app.exception.InstrumentNotFoundException;
import com.budiyanto.fintrackr.investmentservice.app.exception.PortfolioNotFoundException;
import com.budiyanto.fintrackr.investmentservice.app.exception.TradeNotFoundException;
import com.budiyanto.fintrackr.investmentservice.app.mapper.TradeMapper;
import com.budiyanto.fintrackr.investmentservice.domain.BrokerAccount;
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
    private HoldingService holdingService;

    @Mock
    private TradeMapper tradeMapper;

    @Mock
    private FeeService feeService;
    
    @InjectMocks
    private TradeService tradeService;

    private static final Long TRADE_ID = 1L;
    private static final TradeType TRADE_TYPE = TradeType.BUY;
    private static final BigDecimal TRADE_QUANTITY = new BigDecimal(100);
    private static final BigDecimal TRADE_PRICE = new BigDecimal(1520);
    private static final BigDecimal TRADE_FEE = new BigDecimal(1200);

    private static final Long PORTFOLIO_ID = 1L;
    private static final String PORTFOLIO_NAME = "Test Portfolio";

    private static final Long INSTRUMENT_ID = 1L;
    private static final InstrumentType INSTRUMENT_TYPE = InstrumentType.STOCK;
    private static final String INSTRUMENT_CODE = "BBCA";
    private static final String INSTRUMENT_NAME = "Bank Central Asia";
    private static final String INSTRUMENT_CURRENCY = "IDR";

    private static final Long BROKER_ACCOUNT_ID = 1L;
    private static final String BROKER_ACCOUNT_NAME = "Test Broker Account";
    private static final String BROKER_NAME = "Broker A";

    @Nested
    @DisplayName("createTrade method")
    class CreateTrade {
        
        @Test
        @DisplayName("should create trade and update holding")
        void should_createTradeAndUpdateHolding_when_portfolioAndInstrumentExists() {
            // Arrange
            Instant tradedAt = Instant.now();
            CreateTradeRequest request = new CreateTradeRequest(
                PORTFOLIO_ID,
                INSTRUMENT_ID,
                TRADE_TYPE,
                TRADE_QUANTITY,
                TRADE_PRICE,
                tradedAt
            );

            BrokerAccount brokerAccount = new BrokerAccount(BROKER_ACCOUNT_NAME, BROKER_NAME);
            ReflectionTestUtils.setField(brokerAccount, "id", BROKER_ACCOUNT_ID);
            ReflectionTestUtils.setField(brokerAccount, "createdAt", tradedAt);

            Portfolio portfolio = new Portfolio(PORTFOLIO_NAME, brokerAccount);
            ReflectionTestUtils.setField(portfolio, "id", PORTFOLIO_ID);
            when(portfolioRepository.findById(PORTFOLIO_ID)).thenReturn(Optional.of(portfolio));

            Instrument instrument = new Instrument(INSTRUMENT_TYPE, INSTRUMENT_CODE, INSTRUMENT_NAME, INSTRUMENT_CURRENCY);
            ReflectionTestUtils.setField(instrument, "id", INSTRUMENT_ID);
            when(instrumentRepository.findById(INSTRUMENT_ID)).thenReturn(Optional.of(instrument));

            when(feeService.calculateFee(brokerAccount, instrument, TRADE_TYPE, TRADE_QUANTITY, TRADE_PRICE)).thenReturn(TRADE_FEE);

            Trade savedTrade = new Trade(portfolio, instrument, TRADE_TYPE, TRADE_QUANTITY, TRADE_PRICE, TRADE_FEE, tradedAt);
            ReflectionTestUtils.setField(savedTrade, "id", TRADE_ID);
            when(tradeRepository.save(any(Trade.class))).thenReturn(savedTrade); // any() is fine here as we assert on the captor

            TradeResponse response = new TradeResponse(TRADE_ID, PORTFOLIO_ID, INSTRUMENT_ID, TRADE_TYPE, TRADE_QUANTITY, TRADE_PRICE, TRADE_FEE, tradedAt);
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
            assertThat(capturedTrade.getQuantity()).isEqualByComparingTo(TRADE_QUANTITY);
            assertThat(capturedTrade.getPrice()).isEqualByComparingTo(TRADE_PRICE);
            assertThat(capturedTrade.getFee()).isEqualByComparingTo(TRADE_FEE);
            assertThat(capturedTrade.getTradedAt()).isEqualTo(tradedAt);

            // Verify interactions
            verify(portfolioRepository).findById(PORTFOLIO_ID);
            verify(instrumentRepository).findById(INSTRUMENT_ID);
            verify(feeService).calculateFee(brokerAccount, instrument, TRADE_TYPE, TRADE_QUANTITY, TRADE_PRICE);
            verify(holdingService).processTrade(savedTrade);
            verify(tradeMapper).toResponseDto(savedTrade);
        }

        @Test
        @DisplayName("should throw exception when portfolio does not exist")
        void should_throwException_when_portfolioDoesNotExist() {
            // Arrange
            Long nonExistentPortfolioId = 99L;
            Instant tradedAt = Instant.now();
            CreateTradeRequest request = new CreateTradeRequest(
                nonExistentPortfolioId,
                INSTRUMENT_ID,
                TRADE_TYPE,
                TRADE_QUANTITY,
                TRADE_PRICE,
                tradedAt
            );

            when(portfolioRepository.findById(nonExistentPortfolioId)).thenReturn(Optional.empty());
            
            // Act & Assert
            assertThatThrownBy(() -> tradeService.createTrade(request))
                .isInstanceOf(PortfolioNotFoundException.class)
                .asInstanceOf(InstanceOfAssertFactories.type(PortfolioNotFoundException.class))
                .extracting(PortfolioNotFoundException::getId)
                .isEqualTo(nonExistentPortfolioId);

            // Verify that no further interactions occurred
            verify(instrumentRepository, never()).findById(any());
            verify(tradeRepository, never()).save(any());
            verify(holdingService, never()).processTrade(any());
            verify(tradeMapper, never()).toResponseDto(any());
        }

        @Test
        @DisplayName("should throw exception when instrument does not exist")
        void should_throwException_when_instrumentDoesNotExist() {
            // Arrange
            Long nonExistentInstrumentId = 99L;
            Instant tradedAt = Instant.now();
            CreateTradeRequest request = new CreateTradeRequest(
                PORTFOLIO_ID,
                nonExistentInstrumentId,
                TRADE_TYPE,
                TRADE_QUANTITY,
                TRADE_PRICE,
                tradedAt
            );

            BrokerAccount brokerAccount = new BrokerAccount(BROKER_ACCOUNT_NAME, BROKER_NAME);
            ReflectionTestUtils.setField(brokerAccount, "id", BROKER_ACCOUNT_ID);
            ReflectionTestUtils.setField(brokerAccount, "createdAt", tradedAt);

            Portfolio portfolio = new Portfolio(PORTFOLIO_NAME, brokerAccount);
            ReflectionTestUtils.setField(portfolio, "id", PORTFOLIO_ID);
            ReflectionTestUtils.setField(portfolio, "createdAt", tradedAt);
            when(portfolioRepository.findById(PORTFOLIO_ID)).thenReturn(Optional.of(portfolio));

            when(instrumentRepository.findById(nonExistentInstrumentId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> tradeService.createTrade(request))
                .isInstanceOf(InstrumentNotFoundException.class)
                .asInstanceOf(InstanceOfAssertFactories.type(InstrumentNotFoundException.class))
                .satisfies(ex -> {
                    assertThat(ex.getId()).isEqualTo(nonExistentInstrumentId);
                });                

            // Verify that no further interactions occurred
            verify(tradeRepository, never()).save(any());
            verify(holdingService, never()).processTrade(any());
            verify(tradeMapper, never()).toResponseDto(any());
        }
    } 
    
    @Nested
    @DisplayName("retrieveTradeById method")    
    class RetrieveTradeById {
        @Test
        @DisplayName("should return trade when ID exists")
        void should_returnTrade_when_idExists() {
            
            // Arrange
            Instant tradedAt = Instant.now();

            BrokerAccount brokerAccount = new BrokerAccount(BROKER_ACCOUNT_NAME, BROKER_NAME);
            ReflectionTestUtils.setField(brokerAccount, "id", BROKER_ACCOUNT_ID);
            ReflectionTestUtils.setField(brokerAccount, "createdAt", tradedAt);

            Portfolio portfolio = new Portfolio(PORTFOLIO_NAME, brokerAccount);
            ReflectionTestUtils.setField(portfolio, "id", PORTFOLIO_ID);
            ReflectionTestUtils.setField(portfolio, "createdAt", tradedAt);

            Instrument instrument = new Instrument(INSTRUMENT_TYPE, INSTRUMENT_CODE, INSTRUMENT_NAME, INSTRUMENT_CURRENCY);
            ReflectionTestUtils.setField(instrument, "id", INSTRUMENT_ID);
            ReflectionTestUtils.setField(instrument, "createdAt", tradedAt);
            
            Trade retrievedTrade = new Trade(
                portfolio,
                instrument,
                TRADE_TYPE,
                TRADE_QUANTITY,
                TRADE_PRICE,
                TRADE_FEE,
                tradedAt
            );
            ReflectionTestUtils.setField(retrievedTrade, "id", TRADE_ID);
            when(tradeRepository.findById(TRADE_ID)).thenReturn(Optional.of(retrievedTrade));

            TradeResponse response = new TradeResponse(
                TRADE_ID, 
                PORTFOLIO_ID, 
                INSTRUMENT_ID, 
                TRADE_TYPE, 
                TRADE_QUANTITY, 
                TRADE_PRICE,
                TRADE_FEE,
                tradedAt
            );
            when(tradeMapper.toResponseDto(retrievedTrade)).thenReturn(response);

            // Act
            TradeResponse result = tradeService.retrieveTradeById(TRADE_ID);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(response);

            // Verify interactions
            verify(tradeRepository).findById(TRADE_ID);
            verify(tradeMapper).toResponseDto(retrievedTrade);
        }

        @Test
        @DisplayName("should throw exception when ID does not exist")
        void should_throwException_when_retrievingNonExistentTrade() {
            // Arrange
            Long nonExistentId = 99L;
            when(tradeRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> tradeService.retrieveTradeById(nonExistentId))
                .isInstanceOf(TradeNotFoundException.class)
                .asInstanceOf(InstanceOfAssertFactories.type(TradeNotFoundException.class))
                .satisfies(ex -> {
                    assertThat(ex.getId()).isEqualTo(nonExistentId);
                });

            // Verify that no further interactions occurred
            verify(tradeMapper, never()).toResponseDto(any());
        }
    }

    @Nested
    @DisplayName("retrieveAllTrades method")
    class RetrieveAllTrades {
        @Test
        @DisplayName("should return a list of all trades")
        void should_returnAllTrades() {
            // Arrange
            Instant createdAt = Instant.now();

            BrokerAccount brokerAccount = new BrokerAccount(BROKER_ACCOUNT_NAME, BROKER_NAME);
            ReflectionTestUtils.setField(brokerAccount, "id", BROKER_ACCOUNT_ID);
            ReflectionTestUtils.setField(brokerAccount, "createdAt", createdAt);

            Portfolio portfolio1 = new Portfolio("Portfolio 1", brokerAccount);
            Instrument instrument1 = new Instrument(InstrumentType.STOCK, "CODE1", "Instrument 1", "IDR");
            Trade trade1 = new Trade(portfolio1, instrument1, TradeType.BUY, BigDecimal.ONE, BigDecimal.TEN, BigDecimal.TWO, createdAt);

            Portfolio portfolio2 = new Portfolio("Portfolio 2", brokerAccount);
            Instrument instrument2 = new Instrument(InstrumentType.BOND, "CODE2", "Instrument 2", "IDR");
            Trade trade2 = new Trade(portfolio2, instrument2, TradeType.SELL, BigDecimal.ONE, BigDecimal.TEN, BigDecimal.ZERO, createdAt);

            List<Trade> tradeList = List.of(trade1, trade2);
            when(tradeRepository.findAll()).thenReturn(tradeList);

            TradeResponse response1 = new TradeResponse(1L, 1L, 1L, TradeType.BUY, BigDecimal.ONE, BigDecimal.TEN, BigDecimal.TWO, createdAt);
            TradeResponse response2 = new TradeResponse(2L, 2L, 2L, TradeType.SELL, BigDecimal.ONE, BigDecimal.TEN, BigDecimal.ZERO, createdAt);
            List<TradeResponse> responseList = List.of(response1, response2);
            when(tradeMapper.toResponseDtoList(tradeList)).thenReturn(responseList);

            // Act
            List<TradeResponse> result = tradeService.retrieveAllTrades();

            // Assert
            assertThat(result).isNotNull();
            assertThat(result).hasSize(2);
            assertThat(result).containsExactlyInAnyOrder(response1, response2);

            // Verify interactions
            verify(tradeRepository).findAll();
            verify(tradeMapper).toResponseDtoList(tradeList);
        }
    }
}
