package com.budiyanto.fintrackr.investmentservice.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

import com.budiyanto.fintrackr.investmentservice.domain.InstrumentType;
import com.budiyanto.fintrackr.investmentservice.domain.TradeType;
import com.budiyanto.fintrackr.investmentservice.dto.CreateFeeRuleRequest;
import com.budiyanto.fintrackr.investmentservice.dto.FeeRuleResponse;
import com.budiyanto.fintrackr.investmentservice.dto.UpdateFeeRuleRequest;
import com.budiyanto.fintrackr.investmentservice.exception.FeeRuleNotFoundException;
import com.budiyanto.fintrackr.investmentservice.service.FeeRuleService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(FeeRuleController.class)
@DisplayName("FeeRuleController Tests")
class FeeRuleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;
    
    @MockitoBean
    private FeeRuleService feeRuleService;

    // Common constants for tests
    private static final Long FEE_RULE_ID = 1L;
    private static final Long BROKER_ACCOUNT_ID = 10L;
    private static final String ACCOUNT_NAME = "Test Broker Account";
    private static final String BROKER_NAME = "Broker A";
    private static final InstrumentType INSTRUMENT_TYPE = InstrumentType.STOCK;
    private static final TradeType TRADE_TYPE = TradeType.BUY;
    private static final BigDecimal FEE_PERCENTAGE = new BigDecimal("0.0018");
    private static final BigDecimal MIN_FEE = new BigDecimal("10000");

    @Nested
    @DisplayName("POST /api/fee-rules")
    class CreateFeeRule {
        
        @Test
        @DisplayName("should create fee rule")
        void should_createFeeRule() throws Exception {
            // Arrange
            Instant createdAt = Instant.now();
            CreateFeeRuleRequest request = new CreateFeeRuleRequest(BROKER_ACCOUNT_ID, INSTRUMENT_TYPE, TRADE_TYPE, FEE_PERCENTAGE, MIN_FEE);
            var brokerAccountDto = new FeeRuleResponse.BrokerAccountInFeeRuleResponse(BROKER_ACCOUNT_ID, ACCOUNT_NAME, BROKER_NAME);
            FeeRuleResponse response = new FeeRuleResponse(FEE_RULE_ID, brokerAccountDto, INSTRUMENT_TYPE, TRADE_TYPE, FEE_PERCENTAGE, MIN_FEE, createdAt);
    
            when(feeRuleService.createFeeRule(request)).thenReturn(response);

            // Act & Assert
            mockMvc.perform(post("/api/fee-rules")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            )
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(FEE_RULE_ID))
                    .andExpect(jsonPath("$.brokerAccount.id").value(BROKER_ACCOUNT_ID))
                    .andExpect(jsonPath("$.brokerAccount.name").value(ACCOUNT_NAME))
                    .andExpect(jsonPath("$.brokerAccount.brokerName").value(BROKER_NAME))
                    .andExpect(jsonPath("$.instrumentType").value(INSTRUMENT_TYPE.name()))
                    .andExpect(jsonPath("$.tradeType").value(TRADE_TYPE.name()))
                    .andExpect(jsonPath("$.feePercentage").value(FEE_PERCENTAGE.toPlainString()))
                    .andExpect(jsonPath("$.minFee").value(MIN_FEE.toPlainString()))
                    .andExpect(jsonPath("$.createdAt").value(createdAt.toString()));

            // Verify that the service method was called with the correct argument
            verify(feeRuleService).createFeeRule(request);
        }

        @ParameterizedTest
        @MethodSource("provideInvalidCreateFeeRuleRequests")
        @DisplayName("should return BadRequest when any field is invalid")
        void should_returnBadRequest_when_requestIsInvalid(CreateFeeRuleRequest invalidRequest) throws Exception {
            // Act & Assert for invalid requests
            mockMvc.perform(post("/api/fee-rules")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest))
                            )
                    .andExpect(status().isBadRequest());
        }

        private static Stream<CreateFeeRuleRequest> provideInvalidCreateFeeRuleRequests() {
            return Stream.of(
                // Not Null field
                new CreateFeeRuleRequest(null, INSTRUMENT_TYPE, TRADE_TYPE, FEE_PERCENTAGE, MIN_FEE),
                new CreateFeeRuleRequest(BROKER_ACCOUNT_ID, null, TRADE_TYPE, FEE_PERCENTAGE, MIN_FEE),
                new CreateFeeRuleRequest(BROKER_ACCOUNT_ID, INSTRUMENT_TYPE, null, FEE_PERCENTAGE, MIN_FEE),
                new CreateFeeRuleRequest(BROKER_ACCOUNT_ID, INSTRUMENT_TYPE, TRADE_TYPE, null, MIN_FEE),
                new CreateFeeRuleRequest(BROKER_ACCOUNT_ID, INSTRUMENT_TYPE, TRADE_TYPE, FEE_PERCENTAGE, null),

                // Positive or Zero field
                new CreateFeeRuleRequest(BROKER_ACCOUNT_ID, INSTRUMENT_TYPE, TRADE_TYPE, new BigDecimal(-1000), MIN_FEE),
                new CreateFeeRuleRequest(BROKER_ACCOUNT_ID, INSTRUMENT_TYPE, TRADE_TYPE, new BigDecimal(2), MIN_FEE),
                new CreateFeeRuleRequest(BROKER_ACCOUNT_ID, INSTRUMENT_TYPE, TRADE_TYPE, FEE_PERCENTAGE, new BigDecimal(-1000))
            );
        }
    }
    
    @Nested
    @DisplayName("GET /api/fee-rules/{id}")
    class RetrieveFeeRuleById {
        @Test
        @DisplayName("should return fee rule when ID exists")
        void should_returnFeeRule_when_idExists() throws Exception {
            // Arrange
            Instant createdAt = Instant.now();
            var brokerAccountDto = new FeeRuleResponse.BrokerAccountInFeeRuleResponse(BROKER_ACCOUNT_ID, ACCOUNT_NAME, BROKER_NAME);
            FeeRuleResponse response = new FeeRuleResponse(FEE_RULE_ID, brokerAccountDto, INSTRUMENT_TYPE, TRADE_TYPE, FEE_PERCENTAGE, MIN_FEE, createdAt);
            when(feeRuleService.retrieveFeeRuleById(FEE_RULE_ID)).thenReturn(response);

            // Act & Assert
            mockMvc.perform(get("/api/fee-rules/{id}", FEE_RULE_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(FEE_RULE_ID))
                    .andExpect(jsonPath("$.brokerAccount.id").value(BROKER_ACCOUNT_ID))
                    .andExpect(jsonPath("$.brokerAccount.name").value(ACCOUNT_NAME))
                    .andExpect(jsonPath("$.brokerAccount.brokerName").value(BROKER_NAME))
                    .andExpect(jsonPath("$.instrumentType").value(INSTRUMENT_TYPE.name()))
                    .andExpect(jsonPath("$.tradeType").value(TRADE_TYPE.name()))
                    .andExpect(jsonPath("$.feePercentage").value(FEE_PERCENTAGE.toPlainString()))
                    .andExpect(jsonPath("$.minFee").value(MIN_FEE.toPlainString()))
                    .andExpect(jsonPath("$.createdAt").value(createdAt.toString()));

            // Verify that the service method was called with the correct argument
            verify(feeRuleService).retrieveFeeRuleById(FEE_RULE_ID);
        }

        @Test
        @DisplayName("should return NotFound when ID does not exist")
        void should_returnNotFound_when_retrievingNonExistentFeeRule() throws Exception {
            // Arrange
            Long nonExistentId = 99L;
            when(feeRuleService.retrieveFeeRuleById(nonExistentId)).thenThrow(new FeeRuleNotFoundException(nonExistentId));

            // Act & Assert
            mockMvc.perform(get("/api/fee-rules/{id}", nonExistentId))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/fee-rules")
    class RetrieveAllFeeRules {
        @Test
        @DisplayName("should return a list of all fee rules")
        void should_returnAllFeeRules() throws Exception {
            // Arrange
            var brokerAccountDto = new FeeRuleResponse.BrokerAccountInFeeRuleResponse(BROKER_ACCOUNT_ID, ACCOUNT_NAME, BROKER_NAME);
            FeeRuleResponse response1 = new FeeRuleResponse(1L, brokerAccountDto, InstrumentType.STOCK, TradeType.BUY, FEE_PERCENTAGE, MIN_FEE, Instant.now());
            FeeRuleResponse response2 = new FeeRuleResponse(2L, brokerAccountDto, InstrumentType.STOCK, TradeType.SELL, FEE_PERCENTAGE, MIN_FEE, Instant.now());
            when(feeRuleService.retrieveAllFeeRules()).thenReturn(List.of(response1, response2));

            // Act & Assert
            mockMvc.perform(get("/api/fee-rules"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.size()").value(2))
                    .andExpect(jsonPath("$[0].id").value(1L))
                    .andExpect(jsonPath("$[1].id").value(2L));

            // Verify that the service method was called with the correct argument
            verify(feeRuleService).retrieveAllFeeRules();
        }
    }    
    
    @Nested
    @DisplayName("PUT /api/fee-rules/{id}")
    class UpdateFeeRule {
        @Test
        @DisplayName("should update fee rule when ID exists")
        void should_updateFeeRule_whenIdExists() throws Exception {
            // Arrange
            Instant createdAt = Instant.now();
            BigDecimal updatedFeePercentage = new BigDecimal("0.0025");
            BigDecimal updatedMinFee = new BigDecimal("15000");

            UpdateFeeRuleRequest request = new UpdateFeeRuleRequest(updatedFeePercentage, updatedMinFee);
            var brokerAccountDto = new FeeRuleResponse.BrokerAccountInFeeRuleResponse(BROKER_ACCOUNT_ID, ACCOUNT_NAME, BROKER_NAME);
            FeeRuleResponse response = new FeeRuleResponse(FEE_RULE_ID, brokerAccountDto, INSTRUMENT_TYPE, TRADE_TYPE, updatedFeePercentage, updatedMinFee, createdAt);
            when(feeRuleService.updateFeeRule(FEE_RULE_ID, request)).thenReturn(response);

            // Act & Assert
            mockMvc.perform(put("/api/fee-rules/{id}", FEE_RULE_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(FEE_RULE_ID))
                    .andExpect(jsonPath("$.feePercentage").value(updatedFeePercentage.toPlainString()))
                    .andExpect(jsonPath("$.minFee").value(updatedMinFee.toPlainString()));

            // Verify that the service method was called with the correct arguments
            verify(feeRuleService).updateFeeRule(FEE_RULE_ID, request);
        }

        @Test
        @DisplayName("should return NotFound when ID does not exist")
        void should_returnNotFound_when_updatingNonExistentFeeRule() throws Exception {
            // Arrange
            Long nonExistentId = 99L;
            UpdateFeeRuleRequest request = new UpdateFeeRuleRequest(FEE_PERCENTAGE, MIN_FEE);
            when(feeRuleService.updateFeeRule(eq(nonExistentId), any(UpdateFeeRuleRequest.class)))
                    .thenThrow(new FeeRuleNotFoundException(nonExistentId));

            // Act & Assert
            mockMvc.perform(put("/api/fee-rules/{id}", nonExistentId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            )
                    .andExpect(status().isNotFound());
        }

        @ParameterizedTest
        @MethodSource("provideInvalidUpdateFeeRuleRequests")
        @DisplayName("should return BadRequest when any field is invalid")
        void should_returnBadRequest_when_requestIsInvalid(UpdateFeeRuleRequest invalidRequest) throws Exception {
            // Act & Assert for invalid requests
            mockMvc.perform(put("/api/fee-rules/{id}", FEE_RULE_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest))
                            )
                    .andExpect(status().isBadRequest());
        }

        private static Stream<UpdateFeeRuleRequest> provideInvalidUpdateFeeRuleRequests() {
            return Stream.of(
                    new UpdateFeeRuleRequest(null, MIN_FEE),
                    new UpdateFeeRuleRequest(FEE_PERCENTAGE, null),
                    new UpdateFeeRuleRequest(new BigDecimal("-0.1"), MIN_FEE),
                    new UpdateFeeRuleRequest(FEE_PERCENTAGE, new BigDecimal("-100"))
            );
        }
    }

    @Nested
    @DisplayName("DELETE /api/fee-rules/{id}")
    class DeleteFeeRule {
        @Test
        @DisplayName("should delete fee rule")
        void should_deleteFeeRule() throws Exception {
            // Arrange
            doNothing().when(feeRuleService).deleteFeeRule(FEE_RULE_ID);

            // Act & Assert
            mockMvc.perform(delete("/api/fee-rules/{id}", FEE_RULE_ID))
                    .andExpect(status().isNoContent());

            // Verify that the service method was called with the correct argument
            verify(feeRuleService).deleteFeeRule(FEE_RULE_ID);
        }
    }
}
