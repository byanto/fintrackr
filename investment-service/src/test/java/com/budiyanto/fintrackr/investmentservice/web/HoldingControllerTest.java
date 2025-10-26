package com.budiyanto.fintrackr.investmentservice.web;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.budiyanto.fintrackr.investmentservice.dto.HoldingResponse;
import com.budiyanto.fintrackr.investmentservice.exception.HoldingNotFoundException;
import com.budiyanto.fintrackr.investmentservice.exception.PortfolioNotFoundException;
import com.budiyanto.fintrackr.investmentservice.service.HoldingService;

@WebMvcTest(HoldingController.class)
@DisplayName("HoldingController Tests")
public class HoldingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private HoldingService holdingService;

    private static final Long HOLDING_ID = 1L;
    private static final Long PORTFOLIO_ID = 1L;
    private static final Long INSTRUMENT_ID = 1L;
    private static final String INSTRUMENT_CODE = "BBCA";
    private static final String INSTRUMENT_NAME = "Bank Central Asia";
    private static final String INSTRUMENT_CURRENCY = "IDR";
    private static final BigDecimal QUANTITY = new BigDecimal(1000);
    private static final BigDecimal AVERAGE_PRICE = new BigDecimal(1500);

    @Nested
    @DisplayName("retrieveHoldingById method")
    class RetrieveHoldingById {
        
        @Test
        @DisplayName("should return holding when ID exists")
        void should_returnHolding_when_idExists() throws Exception {
            // Arrange
            var instrumentDto = new HoldingResponse.InstrumentInHoldingResponse(INSTRUMENT_ID, INSTRUMENT_CODE, INSTRUMENT_NAME, INSTRUMENT_CURRENCY);
            HoldingResponse response = new HoldingResponse(HOLDING_ID, PORTFOLIO_ID, instrumentDto, QUANTITY, AVERAGE_PRICE, Instant.now());
            when(holdingService.retrieveHoldingById(HOLDING_ID)).thenReturn(response);

            // Act & Assert
            mockMvc.perform(get("/api/holdings/{id}", HOLDING_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(HOLDING_ID))
                .andExpect(jsonPath("$.portfolioId").value(PORTFOLIO_ID))
                .andExpect(jsonPath("$.instrument.id").value(INSTRUMENT_ID))
                .andExpect(jsonPath("$.instrument.code").value(INSTRUMENT_CODE))
                .andExpect(jsonPath("$.instrument.name").value(INSTRUMENT_NAME))
                .andExpect(jsonPath("$.quantity").value(QUANTITY.toPlainString()))
                .andExpect(jsonPath("$.averagePrice").value(AVERAGE_PRICE.toPlainString()));
            
            // Verify that the service method was called with the correct argument
            verify(holdingService).retrieveHoldingById(HOLDING_ID);
        }

        @Test
        @DisplayName("should return NotFound when ID does not exist")
        void should_returnNotFound_when_retrievingNonExistentHolding() throws Exception {
            // Arrange
            Long nonExistentId = 99L;
            when(holdingService.retrieveHoldingById(nonExistentId)).thenThrow(new HoldingNotFoundException(nonExistentId));

            // Act & Assert
            mockMvc.perform(get("/api/holdings/{id}", nonExistentId))
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("retrieveHoldingsByPortfolioId method")
    class RetrieveHoldingsByPortfolioId {

        @Test
        @DisplayName("should return holdings when portfolio exists")
        void should_returnHoldings_when_portfolioIdExists() throws Exception {
            // Arrange
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

            var instrumentResponseDto1 = new HoldingResponse.InstrumentInHoldingResponse(instrumentId1, instrumentCode1, instrumentName1, instrumentCurrency1);
            var instrumentResponseDto2 = new HoldingResponse.InstrumentInHoldingResponse(instrumentId2, instrumentCode2, instrumentName2, instrumentCurrency2);
            HoldingResponse response1 = new HoldingResponse(holdingId1, PORTFOLIO_ID, instrumentResponseDto1, holdingQuantity1, holdingAveragePrice1, Instant.now());
            HoldingResponse response2 = new HoldingResponse(holdingId2, PORTFOLIO_ID, instrumentResponseDto2, holdingQuantity2, holdingAveragePrice2, Instant.now());
            when(holdingService.retrieveHoldingsByPortfolioId(PORTFOLIO_ID)).thenReturn(List.of(response1, response2));
            
            // Act & Assert
            mockMvc.perform(get("/api/portfolios/{portfolioId}/holdings", PORTFOLIO_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].id").value(holdingId1))
                .andExpect(jsonPath("$[0].portfolioId").value(PORTFOLIO_ID))
                .andExpect(jsonPath("$[0].instrument.id").value(instrumentId1))
                .andExpect(jsonPath("$[0].instrument.code").value(instrumentCode1))
                .andExpect(jsonPath("$[0].instrument.name").value(instrumentName1))
                .andExpect(jsonPath("$[1].id").value(holdingId2))
                .andExpect(jsonPath("$[1].portfolioId").value(PORTFOLIO_ID))
                .andExpect(jsonPath("$[1].instrument.id").value(instrumentId2))
                .andExpect(jsonPath("$[1].instrument.code").value(instrumentCode2))
                .andExpect(jsonPath("$[1].instrument.name").value(instrumentName2));

            // Verify interactions
            verify(holdingService).retrieveHoldingsByPortfolioId(PORTFOLIO_ID);
        }

        @Test
        @DisplayName("should return NotFound when portfolio does not exist")
        void should_returnNotFound_when_retrievingNonExistentPortfolio() throws Exception {
            // Arrange
            Long nonExistentId = 99L;
            when(holdingService.retrieveHoldingsByPortfolioId(nonExistentId))
                .thenThrow(new PortfolioNotFoundException(nonExistentId));

            // Act & Assert
            mockMvc.perform(get("/api/portfolios/{portfolioId}/holdings", nonExistentId))
                .andExpect(status().isNotFound());
                        
        }
    }
}
