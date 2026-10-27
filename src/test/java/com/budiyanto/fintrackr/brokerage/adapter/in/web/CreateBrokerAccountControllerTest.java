package com.budiyanto.fintrackr.brokerage.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.budiyanto.fintrackr.brokerage.application.port.in.CreateBrokerAccountCommand;
import com.budiyanto.fintrackr.brokerage.application.port.in.CreateBrokerAccountUseCase;
import com.budiyanto.fintrackr.brokerage.domain.model.FeeStructure;
import com.budiyanto.fintrackr.brokerage.domain.model.Percentage;
import com.budiyanto.fintrackr.shared.BrokerAccountId;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CreateBrokerAccountController.class)
class CreateBrokerAccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreateBrokerAccountUseCase createBrokerAccountUseCase;

    private static final String URI = "/api/broker-accounts";

    @Nested
    @DisplayName("Valid Request Tests")
    class ValidRequestTest {

        @Test
        @DisplayName("Return 201 with location when request is valid")
        void should_return201WithLocation_when_requestIsValid() throws Exception {
            // Given
            CreateBrokerAccountRequest request = validRequest();

            BrokerAccountId brokerAccountId = BrokerAccountId.generate();
            when(createBrokerAccountUseCase.handle(any(CreateBrokerAccountCommand.class))).thenReturn(brokerAccountId);

            // When & Then
            mockMvc.perform(post(URI)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", "/api/broker-accounts/" + brokerAccountId.value()))
                    .andExpect(content().string(""));

            verify(createBrokerAccountUseCase).handle(any(CreateBrokerAccountCommand.class));
        }

        @Test
        @DisplayName("Return 201 when fee rate is zero")
        void should_return201_when_feeRateIsZero() throws Exception {
            // Given
            CreateBrokerAccountRequest request = new CreateBrokerAccountRequest(
                    "Test Broker Account", BigDecimal.ZERO, new BigDecimal("0.0025"));

            BrokerAccountId brokerAccountId = BrokerAccountId.generate();
            when(createBrokerAccountUseCase.handle(any(CreateBrokerAccountCommand.class))).thenReturn(brokerAccountId);

            // When & Then
            mockMvc.perform(post(URI)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());

            verify(createBrokerAccountUseCase).handle(any(CreateBrokerAccountCommand.class));
        }

        @Test
        @DisplayName("Pass name and fee rates to use case when request is valid")
        void should_passNameAndFeeRatesToUseCase_when_requestIsValid() throws Exception {
            // Given
            CreateBrokerAccountRequest request = validRequest();

            BrokerAccountId brokerAccountId = BrokerAccountId.generate();
            when(createBrokerAccountUseCase.handle(any(CreateBrokerAccountCommand.class))).thenReturn(brokerAccountId);

            // When
            mockMvc.perform(post(URI)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)));

            // Then
            ArgumentCaptor<CreateBrokerAccountCommand> captor = ArgumentCaptor.forClass(CreateBrokerAccountCommand.class);
            verify(createBrokerAccountUseCase).handle(captor.capture());
            assertThat(captor.getValue().name()).isEqualTo(request.name());
            assertThat(captor.getValue().feeStructure()).isEqualTo(FeeStructure.of(Percentage.of(request.buyRate()), Percentage.of(request.sellRate())));
        }
    }

    @Nested
    @DisplayName("Invalid Input Tests")
    class InvalidInputTest {

        @Test
        @DisplayName("Return 400 problem detail with field errors when name is blank")
        void should_return400ProblemDetailWithFieldErrors_when_nameIsBlank() throws Exception {
            // Given
            CreateBrokerAccountRequest request = new CreateBrokerAccountRequest("", new BigDecimal("0.0015"), new BigDecimal("0.0025"));

            // When & Then
            mockMvc.perform(post(URI)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.title").value("Bad Request"))
                    .andExpect(jsonPath("$.errors", hasSize(1)))
                    .andExpect(jsonPath("$.errors[0].field").value("name"));

            verify(createBrokerAccountUseCase, never()).handle(any(CreateBrokerAccountCommand.class));
        }

        @Test
        @DisplayName("Return 400 problem detail with field errors when fee rate is missing")
        void should_return400ProblemDetailWithFieldErrors_when_feeRateIsMissing() throws Exception {
            // Given
            String invalidBody = """
                        {"name": "Test Broker Account", "sellRate": 0.0025}
                    """;

            // When & Then
            mockMvc.perform(post(URI)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidBody))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.title").value("Bad Request"))
                    .andExpect(jsonPath("$.errors", hasSize(1)))
                    .andExpect(jsonPath("$.errors[0].field").value("buyRate"));

            verify(createBrokerAccountUseCase, never()).handle(any(CreateBrokerAccountCommand.class));
        }

        @Test
        @DisplayName("Return 400 when fee rate is above 100 percent")
        void should_return400_when_feeRateIsAbove100Percent() throws Exception {
            // Given
            CreateBrokerAccountRequest request = new CreateBrokerAccountRequest("Test Broker Account", new BigDecimal("1.5"), new BigDecimal("0.0025"));

            // When & Then
            mockMvc.perform(post(URI)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.title").value("Invalid Request"));

            verify(createBrokerAccountUseCase, never()).handle(any(CreateBrokerAccountCommand.class));
        }
    }

    private CreateBrokerAccountRequest validRequest() {
        return new CreateBrokerAccountRequest(
                "Test Broker Account", new BigDecimal("0.0015"), new BigDecimal("0.0025")
        );
    }
}
