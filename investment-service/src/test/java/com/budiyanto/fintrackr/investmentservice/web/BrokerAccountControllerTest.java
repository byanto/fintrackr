package com.budiyanto.fintrackr.investmentservice.web;

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

import com.budiyanto.fintrackr.investmentservice.dto.BrokerAccountResponse;
import com.budiyanto.fintrackr.investmentservice.dto.CreateBrokerAccountRequest;
import com.budiyanto.fintrackr.investmentservice.dto.UpdateBrokerAccountRequest;
import com.budiyanto.fintrackr.investmentservice.exception.BrokerAccountNotFoundException;
import com.budiyanto.fintrackr.investmentservice.service.BrokerAccountService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(BrokerAccountController.class)
@DisplayName("BrokerAccountController Tests")
class BrokerAccountControllerTest {

    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockitoBean
    private BrokerAccountService brokerAccountService;

    private static final Long BROKER_ACCOUNT_ID = 1L;
    private static final String ACCOUNT_NAME = "Test Broker Account";
    private static final String BROKER_NAME = "Broker A";


    @Nested
    @DisplayName("createBrokerAccount method")
    class CreateBrokerAccount {
        @Test
        @DisplayName("should create broker account when request is valid")
        void should_createBrokerAccount_when_requestIsValid() throws Exception {
            // Arrange
            Instant createdAt = Instant.now();
            CreateBrokerAccountRequest request = new CreateBrokerAccountRequest(ACCOUNT_NAME, BROKER_NAME);
            BrokerAccountResponse response = new BrokerAccountResponse(BROKER_ACCOUNT_ID, ACCOUNT_NAME, BROKER_NAME, createdAt);
            when(brokerAccountService.createBrokerAccount(request)).thenReturn(response);
            
            // Act
            mockMvc.perform(post("/api/broker-accounts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(BROKER_ACCOUNT_ID))
                    .andExpect(jsonPath("$.name").value(ACCOUNT_NAME))
                    .andExpect(jsonPath("$.brokerName").value(BROKER_NAME))
                    .andExpect(jsonPath("$.createdAt").value(createdAt.toString()));

            // Verify interactions
            verify(brokerAccountService).createBrokerAccount(request);
        }

        @ParameterizedTest
        @MethodSource("provideInvalidCreateBrokerAccountRequests")
        @DisplayName("should return BadRequest when any field is invalid")
        void should_returnBadRequest_when_requestIsInvalid(CreateBrokerAccountRequest invalidRequest) throws Exception {
            // Act & Assert for invalid requests
            mockMvc.perform(post("/api/broker-accounts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());
        }

        private static Stream<CreateBrokerAccountRequest> provideInvalidCreateBrokerAccountRequests() {
            return Stream.of(
                new CreateBrokerAccountRequest("   ", BROKER_NAME),
                new CreateBrokerAccountRequest(ACCOUNT_NAME, "   ")                
            );
        }
    }

    @Nested
    @DisplayName("retrieveBrokerAccountById method")
    class RetrieveBrokerAccountById {
        @Test
        @DisplayName("should return broker account when ID exists")
        void should_returnBrokerAccount_when_idExists() throws Exception {
            // Arrange
            Instant createdAt = Instant.now();
            BrokerAccountResponse response = new BrokerAccountResponse(BROKER_ACCOUNT_ID, ACCOUNT_NAME, BROKER_NAME, createdAt);
            when(brokerAccountService.retrieveBrokerAccountById(BROKER_ACCOUNT_ID)).thenReturn(response);
            
            // Act & Assert
            mockMvc.perform(get("/api/broker-accounts/{id}", BROKER_ACCOUNT_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(BROKER_ACCOUNT_ID))
                    .andExpect(jsonPath("$.name").value(ACCOUNT_NAME))
                    .andExpect(jsonPath("$.brokerName").value(BROKER_NAME))
                    .andExpect(jsonPath("$.createdAt").value(createdAt.toString()));

            // Verify interactions
            verify(brokerAccountService).retrieveBrokerAccountById(BROKER_ACCOUNT_ID);
        }

        @Test
        @DisplayName("should return NotFound when ID does not exist")
        void should_returnNotFound_when_retrievingNonExistentBrokerAccount() throws Exception {
            // Arrange
            Long nonExistentId = 99L;
            when(brokerAccountService.retrieveBrokerAccountById(nonExistentId))
                .thenThrow(new BrokerAccountNotFoundException(nonExistentId));

            // Act & Assert
            mockMvc.perform(get("/api/broker-accounts/{id}", nonExistentId))
                    .andExpect(status().isNotFound());

        }
    }

    @Nested
    @DisplayName("retrieveAllBrokerAccounts method")
    class RetrieveAllBrokerAccounts {
        @Test
        @DisplayName("should return a list of all broker accounts")
        void should_returnAllBrokerAccounts() throws Exception {
            // Arrange
            Instant createdAt = Instant.now();

            Long brokerAccountId1 = 1L;
            String accountName1 = "Test Broker Account 1";
            String brokerName1 = "Broker A";
            BrokerAccountResponse brokerAccountResponse1 = new BrokerAccountResponse(brokerAccountId1, accountName1, brokerName1, createdAt);

            Long brokerAccountId2 = 2L;
            String accountName2 = "Test Broker Account 2";
            String brokerName2 = "Broker B";
            BrokerAccountResponse brokerAccountResponse2 = new BrokerAccountResponse(brokerAccountId2, accountName2, brokerName2, createdAt);

            List<BrokerAccountResponse> responseList = List.of(brokerAccountResponse1, brokerAccountResponse2);
            when(brokerAccountService.retrieveAllBrokerAccounts()).thenReturn(responseList);

            // Act & Assert
            mockMvc.perform(get("/api/broker-accounts"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.size()").value(2))
                    .andExpect(jsonPath("$[0].id").value(brokerAccountId1))
                    .andExpect(jsonPath("$[0].name").value(accountName1))
                    .andExpect(jsonPath("$[0].brokerName").value(brokerName1))
                    .andExpect(jsonPath("$[0].createdAt").value(createdAt.toString()))
                    .andExpect(jsonPath("$[1].id").value(brokerAccountId2))
                    .andExpect(jsonPath("$[1].name").value(accountName2))
                    .andExpect(jsonPath("$[1].brokerName").value(brokerName2))
                    .andExpect(jsonPath("$[1].createdAt").value(createdAt.toString()));
                    
            // Verify interactions
            verify(brokerAccountService).retrieveAllBrokerAccounts();
        }
    }

    @Nested
    @DisplayName("updateBrokerAccount method")
    class UpdateBrokerAccount {
        @Test
        @DisplayName("should update broker account when ID exists")
        void should_updateBrokerAccount_when_idExists() throws Exception {
            // Arrange
            Instant createdAt = Instant.now();
            String updatedAccountName = "Updated Account Name";
            String updatedBrokerName = "Updated Broker Name";

            UpdateBrokerAccountRequest request = new UpdateBrokerAccountRequest(updatedAccountName, updatedBrokerName);
            BrokerAccountResponse response = new BrokerAccountResponse(BROKER_ACCOUNT_ID, updatedAccountName, updatedBrokerName, createdAt);
            when(brokerAccountService.updateBrokerAccount(BROKER_ACCOUNT_ID, request)).thenReturn(response);
            
            // Act & Assert
            mockMvc.perform(put("/api/broker-accounts/{id}", BROKER_ACCOUNT_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(BROKER_ACCOUNT_ID))
                    .andExpect(jsonPath("$.name").value(updatedAccountName))
                    .andExpect(jsonPath("$.brokerName").value(updatedBrokerName))
                    .andExpect(jsonPath("$.createdAt").value(createdAt.toString()));
                    
            // Verify interactions
            verify(brokerAccountService).updateBrokerAccount(BROKER_ACCOUNT_ID, request);
        }

        @Test
        @DisplayName("should return NotFound when ID does not exist")
        void should_returnNotFound_when_updatingNonExistentBrokerAccount() throws Exception {
            // Arrange
            Long nonExistentId = 99L;
            UpdateBrokerAccountRequest request = new UpdateBrokerAccountRequest("Updated Account Name", "Updated Broker Name");

            // Mock the service to throw the expected exception when called
            when(brokerAccountService.updateBrokerAccount(nonExistentId, request))
                .thenThrow(new BrokerAccountNotFoundException(nonExistentId));

            // Act & Assert
            mockMvc.perform(put("/api/broker-accounts/{id}", nonExistentId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());

        }

        @ParameterizedTest
        @MethodSource("provideInvalidUpdateBrokerAccountRequests")
        @DisplayName("should return BadRequest when any field is invalid")
        void should_returnBadRequest_when_requestIsInvalid(UpdateBrokerAccountRequest invalidRequest) throws Exception {
            // Act & Assert for invalid requests
            mockMvc.perform(put("/api/broker-accounts/{id}", BROKER_ACCOUNT_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());
        }

        private static Stream<UpdateBrokerAccountRequest> provideInvalidUpdateBrokerAccountRequests() {
            return Stream.of(
                new UpdateBrokerAccountRequest("   ", BROKER_NAME),
                new UpdateBrokerAccountRequest(ACCOUNT_NAME, "   ")                
            );
        }
    }

    @Nested
    @DisplayName("deleteBrokerAccountById method")
    class DeleteBrokerAccountById {
        @Test
        @DisplayName("should delete broker account")
        void should_deleteBrokerAccount() throws Exception {
            // Arrange
            doNothing().when(brokerAccountService).deleteBrokerAccountById(BROKER_ACCOUNT_ID);

            // Act & Assert
            mockMvc.perform(delete("/api/broker-accounts/{id}", BROKER_ACCOUNT_ID))
                .andExpect(status().isNoContent());
        
            // Verify that the service method was called with the correct argument
            verify(brokerAccountService).deleteBrokerAccountById(BROKER_ACCOUNT_ID);
        }
    }
}
