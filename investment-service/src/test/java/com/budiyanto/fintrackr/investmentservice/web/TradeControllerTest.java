package com.budiyanto.fintrackr.investmentservice.web;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.budiyanto.fintrackr.investmentservice.domain.TradeType;
import com.budiyanto.fintrackr.investmentservice.dto.CreateTradeRequest;
import com.budiyanto.fintrackr.investmentservice.dto.TradeResponse;
import com.budiyanto.fintrackr.investmentservice.exception.InstrumentNotFoundException;
import com.budiyanto.fintrackr.investmentservice.exception.PortfolioNotFoundException;
import com.budiyanto.fintrackr.investmentservice.exception.TradeNotFoundException;
import com.budiyanto.fintrackr.investmentservice.service.TradeService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(TradeController.class)
@DisplayName("TradeController Tests")
class TradeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TradeService tradeService;

    private static final Long TRADE_ID = 1L;
    private static final Long PORTFOLIO_ID = 1L;
    private static final Long INSTRUMENT_ID = 1L;
    private static final TradeType TRADE_TYPE = TradeType.BUY;
    private static final BigDecimal QUANTITY = new BigDecimal(100);
    private static final BigDecimal PRICE = new BigDecimal(1520);
    private static final BigDecimal FEE = new BigDecimal(1200);

    @Nested
    @DisplayName("createTrade method")
    class CreateTrade {
        @Test
        @DisplayName("should create trade")
        void should_createTrade_when_requestIsValid() throws Exception {
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

            TradeResponse response = new TradeResponse(
                TRADE_ID,
                PORTFOLIO_ID,
                INSTRUMENT_ID,
                TRADE_TYPE,
                QUANTITY,
                PRICE,
                FEE,
                tradedAt
            );
            when(tradeService.createTrade(request)).thenReturn(response);

            // Act & Assert
            mockMvc.perform(post("/api/trades")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(TRADE_ID))
                    .andExpect(jsonPath("$.portfolioId").value(PORTFOLIO_ID))
                    .andExpect(jsonPath("$.instrumentId").value(INSTRUMENT_ID))
                    .andExpect(jsonPath("$.tradeType").value(TRADE_TYPE.toString()))
                    .andExpect(jsonPath("$.quantity").value(QUANTITY.toPlainString()))
                    .andExpect(jsonPath("$.price").value(PRICE.toPlainString()))
                    .andExpect(jsonPath("$.fee").value(FEE.toPlainString()))
                    .andExpect(jsonPath("$.tradedAt").value(tradedAt.toString()));

            // Verify that the service method was called with the correct argument
            verify(tradeService).createTrade(request);
        }

        @ParameterizedTest
        @MethodSource("provideInvalidCreateTradeRequests")
        @DisplayName("should return BadRequest when any field is invalid")
        void should_returnBadRequest_when_requestIsInvalid(CreateTradeRequest invalidRequest) throws Exception {
            // Act & Assert
            mockMvc.perform(post("/api/trades")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());
        }

        private static Stream<CreateTradeRequest> provideInvalidCreateTradeRequests() {
            Instant tradedAt = Instant.now();
            return Stream.of(
                // Null fields
                new CreateTradeRequest(null, INSTRUMENT_ID, TRADE_TYPE, QUANTITY, PRICE, tradedAt),
                new CreateTradeRequest(PORTFOLIO_ID, null, TRADE_TYPE, QUANTITY, PRICE, tradedAt),
                new CreateTradeRequest(PORTFOLIO_ID, INSTRUMENT_ID, null, QUANTITY, PRICE, tradedAt),
                new CreateTradeRequest(PORTFOLIO_ID, INSTRUMENT_ID, TRADE_TYPE, null, PRICE, tradedAt),
                new CreateTradeRequest(PORTFOLIO_ID, INSTRUMENT_ID, TRADE_TYPE, QUANTITY, null, tradedAt),
                new CreateTradeRequest(PORTFOLIO_ID, INSTRUMENT_ID, TRADE_TYPE, QUANTITY, PRICE, null),
                // Non-positive quantity/price
                new CreateTradeRequest(PORTFOLIO_ID, INSTRUMENT_ID, TRADE_TYPE, BigDecimal.ZERO, PRICE, tradedAt),
                new CreateTradeRequest(PORTFOLIO_ID, INSTRUMENT_ID, TRADE_TYPE, new BigDecimal("-10"), PRICE, tradedAt),
                new CreateTradeRequest(PORTFOLIO_ID, INSTRUMENT_ID, TRADE_TYPE, QUANTITY, BigDecimal.ZERO, tradedAt),
                new CreateTradeRequest(PORTFOLIO_ID, INSTRUMENT_ID, TRADE_TYPE, QUANTITY, new BigDecimal("-1500"), tradedAt)
            );
        }

        @Test
        @DisplayName("should return NotFound when portfolio does not exist")
        void should_returnNotFound_when_portfolioDoesNotExist() throws Exception {
            // Arrange
            Long nonExistentPortfolioId = 99L;
            CreateTradeRequest request = new CreateTradeRequest(nonExistentPortfolioId, INSTRUMENT_ID, TRADE_TYPE, QUANTITY, PRICE, Instant.now());

            when(tradeService.createTrade(request))
                .thenThrow(new PortfolioNotFoundException(nonExistentPortfolioId));

            // Act & Assert
            mockMvc.perform(post("/api/trades")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("should return NotFound when instrument does not exist")
        void should_returnNotFound_when_instrumentDoesNotExist() throws Exception {
            // Arrange
            Long nonExistentInstrumentId = 99L;
            CreateTradeRequest request = new CreateTradeRequest(PORTFOLIO_ID, nonExistentInstrumentId, TRADE_TYPE, QUANTITY, PRICE, Instant.now());

            when(tradeService.createTrade(request))
                .thenThrow(new InstrumentNotFoundException(nonExistentInstrumentId));

            // Act & Assert
            mockMvc.perform(post("/api/trades")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("retrieveTradeById method")
    class RetrieveTradeById {
        @Test
        @DisplayName("should return trade when ID exists")
        void should_returnTrade_when_idExists() throws Exception {
            // Arrange
            Instant tradedAt = Instant.now();
            TradeResponse response = new TradeResponse(
                TRADE_ID,
                PORTFOLIO_ID, 
                INSTRUMENT_ID, 
                TRADE_TYPE, 
                QUANTITY, 
                PRICE,
                FEE,
                tradedAt
            );
            when(tradeService.retrieveTradeById(TRADE_ID)).thenReturn(response);

            // Act & Assert
            mockMvc.perform(get("/api/trades/{id}", TRADE_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(TRADE_ID))
                    .andExpect(jsonPath("$.portfolioId").value(PORTFOLIO_ID))
                    .andExpect(jsonPath("$.instrumentId").value(INSTRUMENT_ID))
                    .andExpect(jsonPath("$.tradeType").value(TRADE_TYPE.toString()))
                    .andExpect(jsonPath("$.quantity").value(QUANTITY.toPlainString()))
                    .andExpect(jsonPath("$.price").value(PRICE.toPlainString()))
                    .andExpect(jsonPath("$.fee").value(FEE.toPlainString()))
                    .andExpect(jsonPath("$.tradedAt").value(tradedAt.toString()));

            // Verify that the service method was called with the correct argument
            verify(tradeService).retrieveTradeById(TRADE_ID);
        }

        @Test
        @DisplayName("should return NotFound when ID does not exist")
        void should_returnNotFound_when_retrievingNonExistentTrade() throws Exception {
            // Arrange
            Long nonExistentId = 99L;
            when(tradeService.retrieveTradeById(nonExistentId))
                    .thenThrow(new TradeNotFoundException(nonExistentId));

            // Act & Assert
            mockMvc.perform(get("/api/trades/{id}", nonExistentId))
                    .andExpect(status().isNotFound());
        }   
    }

    @Nested
    @DisplayName("retrieveAllTrades method")
    class RetrieveAllTrades {
        @Test
        @DisplayName("should return a list of all trades")
        void should_returnAllTrades() throws Exception {
            // Arrange
            Long id1 = 1L;
            Long id2 = 2L;
            Long portfolioId1 = 1L;
            Long portfolioId2 = 2L;
            Long instrumentId1 = 1L;
            Long instrumentId2 = 2L;
            TradeType tradeType1 = TradeType.BUY;
            TradeType tradeType2 = TradeType.SELL;
            BigDecimal quantity1 = new BigDecimal(100);
            BigDecimal quantity2 = new BigDecimal(200);
            BigDecimal price1 = new BigDecimal(1500);
            BigDecimal price2 = new BigDecimal(1800);
            BigDecimal fee1 = new BigDecimal(1200);
            BigDecimal fee2 = new BigDecimal(1800);
            Instant tradedAt1 = Instant.now();
            Instant tradedAt2 = Instant.now();

            TradeResponse response1 = new TradeResponse(id1, portfolioId1, instrumentId1, tradeType1, quantity1, price1, fee1, tradedAt1);
            TradeResponse response2 = new TradeResponse(id2, portfolioId2, instrumentId2, tradeType2, quantity2, price2, fee2, tradedAt2);

            when(tradeService.retrieveAllTrades()).thenReturn(List.of(response1, response2));

            mockMvc.perform(get("/api/trades"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.size()").value(2))
                    .andExpect(jsonPath("$[0].id").value(id1))
                    .andExpect(jsonPath("$[0].portfolioId").value(portfolioId1))
                    .andExpect(jsonPath("$[0].instrumentId").value(instrumentId1))
                    .andExpect(jsonPath("$[0].tradeType").value(tradeType1.toString()))
                    .andExpect(jsonPath("$[0].quantity").value(quantity1.toPlainString()))
                    .andExpect(jsonPath("$[0].price").value(price1.toPlainString()))
                    .andExpect(jsonPath("$[0].fee").value(fee1.toPlainString()))
                    .andExpect(jsonPath("$[0].tradedAt").value(tradedAt1.toString()))
                    .andExpect(jsonPath("$[1].id").value(id2))
                    .andExpect(jsonPath("$[1].portfolioId").value(portfolioId2))
                    .andExpect(jsonPath("$[1].instrumentId").value(instrumentId2))
                    .andExpect(jsonPath("$[1].tradeType").value(tradeType2.toString()))
                    .andExpect(jsonPath("$[1].quantity").value(quantity2.toPlainString()))
                    .andExpect(jsonPath("$[1].price").value(price2.toPlainString()))
                    .andExpect(jsonPath("$[1].fee").value(fee2.toPlainString()))
                    .andExpect(jsonPath("$[1].tradedAt").value(tradedAt2.toString()));

            // Verify that the service method was called with the correct argument
            verify(tradeService).retrieveAllTrades();
        }
    }


}
