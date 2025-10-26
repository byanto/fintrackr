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

import com.budiyanto.fintrackr.investmentservice.dto.CreatePortfolioRequest;
import com.budiyanto.fintrackr.investmentservice.dto.PortfolioResponse;
import com.budiyanto.fintrackr.investmentservice.dto.UpdatePortfolioRequest;
import com.budiyanto.fintrackr.investmentservice.exception.BrokerAccountNotFoundException;
import com.budiyanto.fintrackr.investmentservice.exception.PortfolioNotFoundException;
import com.budiyanto.fintrackr.investmentservice.service.PortfolioService;
import com.fasterxml.jackson.databind.ObjectMapper;


@WebMvcTest(PortfolioController.class)
@DisplayName("PortfolioController Tests")
class PortfolioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PortfolioService portfolioService;

    private static final Long PORTFOLIO_ID = 1L;
    private static final String PORTFOLIO_NAME = "Test Portfolio";
    private static final Long BROKER_ACCOUNT_ID = 1L;
    private static final String ACCOUNT_NAME = "Test Broker Account";
    private static final String BROKER_NAME = "Broker A";

    @Nested
    @DisplayName("createPortfolio method")
    class CreatePortfolio {
        @Test
        @DisplayName("should create portfolio when request is valid")
        void should_createPortfolio_when_requestIsValid() throws Exception {
            // Arrange
            Instant createdAt = Instant.now();
            CreatePortfolioRequest request = new CreatePortfolioRequest(PORTFOLIO_NAME, BROKER_ACCOUNT_ID);
            var brokerAccountDto = new PortfolioResponse.BrokerAccountInPortfolioResponse(BROKER_ACCOUNT_ID, ACCOUNT_NAME, BROKER_NAME);
            PortfolioResponse response = new PortfolioResponse(PORTFOLIO_ID, PORTFOLIO_NAME, brokerAccountDto, createdAt);
    
            when(portfolioService.createPortfolio(request)).thenReturn(response);

            // Act & Assert
            mockMvc.perform(post("/api/portfolios")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(PORTFOLIO_ID))
                    .andExpect(jsonPath("$.name").value(PORTFOLIO_NAME))
                    .andExpect(jsonPath("$.brokerAccount.id").value(BROKER_ACCOUNT_ID))
                    .andExpect(jsonPath("$.brokerAccount.name").value(ACCOUNT_NAME))
                    .andExpect(jsonPath("$.brokerAccount.brokerName").value(BROKER_NAME))
                    .andExpect(jsonPath("$.createdAt").value(createdAt.toString()));

            // Verify that the service method was called with the correct argument
            verify(portfolioService).createPortfolio(request);
        }

        @ParameterizedTest
        @MethodSource("provideInvalidCreatePortfolioRequests")
        @DisplayName("should return BadRequest when any field is invalid")
        void should_returnBadRequest_when_requestIsInvalid(CreatePortfolioRequest invalidRequest) throws Exception {
            // Act & Assert for invalid requests
            mockMvc.perform(post("/api/portfolios")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());
        }

        private static Stream<CreatePortfolioRequest> provideInvalidCreatePortfolioRequests() {
            return Stream.of(
                new CreatePortfolioRequest("   ", BROKER_ACCOUNT_ID),
                new CreatePortfolioRequest(PORTFOLIO_NAME, null)                
            );
        }

        @Test
        @DisplayName("should return NotFound when broker account not found")
        void should_returnNotFound_when_creatingPortfolioWithNonExistentBrokerAccount() throws Exception {
            // Arrange
            Long nonExistentId = 99L;
            CreatePortfolioRequest request = new CreatePortfolioRequest(PORTFOLIO_NAME, nonExistentId);
            when(portfolioService.createPortfolio(request))
                .thenThrow(new BrokerAccountNotFoundException(nonExistentId));
            
            // Act & Assert
            mockMvc.perform(post("/api/portfolios")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }
    }
   
    @Nested
    @DisplayName("retrievePortfolioById method")
    class RetrievePortfolioById {
        @Test
        @DisplayName("should return portfolio when ID exists")
        void should_returnPortfolio_when_idExists() throws Exception{
            // Arrange
            Instant createdAt = Instant.now();
            
            var brokerAccountDto = new PortfolioResponse.BrokerAccountInPortfolioResponse(BROKER_ACCOUNT_ID, ACCOUNT_NAME, BROKER_NAME);
            PortfolioResponse response = new PortfolioResponse(PORTFOLIO_ID, PORTFOLIO_NAME, brokerAccountDto, createdAt);
            when(portfolioService.retrievePortfolioById(PORTFOLIO_ID)).thenReturn(response);
        
            // Act & Assert
            mockMvc.perform(get("/api/portfolios/{id}", PORTFOLIO_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(PORTFOLIO_ID))
                    .andExpect(jsonPath("$.name").value(PORTFOLIO_NAME))
                    .andExpect(jsonPath("$.brokerAccount.id").value(BROKER_ACCOUNT_ID))
                    .andExpect(jsonPath("$.brokerAccount.name").value(ACCOUNT_NAME))
                    .andExpect(jsonPath("$.brokerAccount.brokerName").value(BROKER_NAME))
                    .andExpect(jsonPath("$.createdAt").value(createdAt.toString()));

            // Verify that the service method was called with the correct argument
            verify(portfolioService).retrievePortfolioById(PORTFOLIO_ID);
        }

        @Test
        @DisplayName("should return NotFound when ID does not exist")
        void should_returnNotFound_when_retrievingNonExistentPortfolio() throws Exception {
            // Arrange
            Long nonExistentId = 99L;
            when(portfolioService.retrievePortfolioById(nonExistentId))
                .thenThrow(new PortfolioNotFoundException(nonExistentId));
            
            // Act & Assert
            mockMvc.perform(get("/api/portfolios/{id}", nonExistentId))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("retrieveAllPortfolios method")
    class RetrieveAllPortfolios {
        @Test
        @DisplayName("should return a list of all portfolios")
        void should_returnAllPortfolios() throws Exception {
            // Arrange
            var brokerAccountDto = new PortfolioResponse.BrokerAccountInPortfolioResponse(BROKER_ACCOUNT_ID, ACCOUNT_NAME, BROKER_NAME);
            Long portfolioId1 = 1L;
            Long portfolioId2 = 2L;
            String portfolioName1 = "Test Portfolio 1";
            String portfolioName2 = "Test Portfolio 2";
            PortfolioResponse response1 = new PortfolioResponse(portfolioId1, portfolioName1, brokerAccountDto, Instant.now());
            PortfolioResponse response2 = new PortfolioResponse(portfolioId2, portfolioName2, brokerAccountDto, Instant.now());
            when(portfolioService.retrieveAllPortfolios()).thenReturn(List.of(response1, response2));

            // Act & Assert
            mockMvc.perform(get("/api/portfolios"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.size()").value(2))
                    .andExpect(jsonPath("$[0].id").value(portfolioId1))
                    .andExpect(jsonPath("$[0].name").value(portfolioName1))
                    .andExpect(jsonPath("$[0].brokerAccount.id").value(BROKER_ACCOUNT_ID))
                    .andExpect(jsonPath("$[1].id").value(portfolioId2))
                    .andExpect(jsonPath("$[1].name").value(portfolioName2))
                    .andExpect(jsonPath("$[1].brokerAccount.id").value(BROKER_ACCOUNT_ID));

            // Verify that the service method was called with the correct argument
            verify(portfolioService).retrieveAllPortfolios();
        }
    }

    @Nested
    @DisplayName("updatePortfolio method")
    class UpdatePortfolio {
        @Test
        @DisplayName("should update portfolio when ID exists")
        void should_updatePortfolio_whenIdExists() throws Exception {
            // Arrange
            Instant createdAt = Instant.now();
            String updatedPortfolioName = "Updated Portfolio Name";

            UpdatePortfolioRequest request = new UpdatePortfolioRequest(updatedPortfolioName);
            var brokerAccountDto = new PortfolioResponse.BrokerAccountInPortfolioResponse(BROKER_ACCOUNT_ID, ACCOUNT_NAME, BROKER_NAME);
            PortfolioResponse response = new PortfolioResponse(PORTFOLIO_ID, updatedPortfolioName, brokerAccountDto, createdAt);
            when(portfolioService.updatePortfolio(PORTFOLIO_ID, request)).thenReturn(response);

            // Act & Assert
            mockMvc.perform(put("/api/portfolios/{id}", PORTFOLIO_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(PORTFOLIO_ID))
                    .andExpect(jsonPath("$.name").value(updatedPortfolioName))
                    .andExpect(jsonPath("$.brokerAccount.id").value(BROKER_ACCOUNT_ID))
                    .andExpect(jsonPath("$.createdAt").value(createdAt.toString()));

            // Verify that the service method was called with the correct arguments
            verify(portfolioService).updatePortfolio(PORTFOLIO_ID, request);
        }

        @Test
        @DisplayName("should return NotFound when ID does not exist")
        void should_returnNotFound_when_updatingNonExistentPortfolio() throws Exception {
            // Arrange
            Long nonExistentId = 99L;
            UpdatePortfolioRequest request = new UpdatePortfolioRequest("Updated Portfolio Name");
            
            // Mock the service to throw the expected exception when called
            when(portfolioService.updatePortfolio(eq(nonExistentId), any(UpdatePortfolioRequest.class)))
                .thenThrow(new PortfolioNotFoundException(nonExistentId));

            // Act & Assert
            mockMvc.perform(put("/api/portfolios/{id}", nonExistentId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("should return BadRequest when name is blank")
        void should_returnBadRequest_when_nameIsBlank() throws Exception {
            // Arrange
            // Create a request with a blank name
            UpdatePortfolioRequest request = new UpdatePortfolioRequest("   ");

            // Act & Assert
            mockMvc.perform(put("/api/portfolios/{id}", PORTFOLIO_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("deletePortfolioById method")
    class DeletePortfolio {
        @Test
        @DisplayName("should delete portfolio")
        void should_deletePortfolio() throws Exception {
            // Arrange
            doNothing().when(portfolioService).deletePortfolioById(PORTFOLIO_ID);

            // Act & Assert
            mockMvc.perform(delete("/api/portfolios/{id}", PORTFOLIO_ID))
                .andExpect(status().isNoContent());
        
            // Verify that the service method was called with the correct argument
            verify(portfolioService).deletePortfolioById(PORTFOLIO_ID);
        }
    }
}
