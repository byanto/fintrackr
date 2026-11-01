package com.budiyanto.fintrackr.brokerage.adapter.in.web;

import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.budiyanto.fintrackr.brokerage.application.exception.BrokerAccountNotFoundException;
import com.budiyanto.fintrackr.brokerage.application.port.in.BrokerAccountDetails;
import com.budiyanto.fintrackr.brokerage.application.port.in.GetBrokerAccountUseCase;
import com.budiyanto.fintrackr.shared.BrokerAccountId;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(GetBrokerAccountController.class)
@DisplayName("GetBrokerAccountController Tests")
class GetBrokerAccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetBrokerAccountUseCase getBrokerAccountUseCase;

    private final static String URI = "/api/broker-accounts/{id}";

    @Test
    @DisplayName("Return 200 with account fields when account exists")
    void should_return200WithAccountFields_when_accountExists() throws Exception {
        // Given
        BrokerAccountDetails details = validBrokerAccountDetails();

        when(getBrokerAccountUseCase.handle(BrokerAccountId.of(details.id()))).thenReturn(details);

        // When & Then
        mockMvc.perform(get(URI, details.id()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(details.id().toString()))
                .andExpect(jsonPath("$.name").value(details.name()))
                .andExpect(jsonPath("$.rdn").value(details.rdn()))
                .andExpect(jsonPath("$.buyRate").value(details.buyRate()))
                .andExpect(jsonPath("$.sellRate").value(details.sellRate()));
    }

    @Test
    @DisplayName("Return 404 problem detail when account does not exist")
    void should_return404ProblemDetail_when_accountDoesNotExist() throws Exception {
        // Given
        BrokerAccountId brokerAccountId = BrokerAccountId.generate();

        BrokerAccountNotFoundException exception = new BrokerAccountNotFoundException(brokerAccountId);
        when(getBrokerAccountUseCase.handle(brokerAccountId)).thenThrow(exception);

        // When & Then
        mockMvc.perform(get(URI, brokerAccountId.value()))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Broker Account Not Found"))
                .andExpect(jsonPath("$.detail", containsStringIgnoringCase("Broker account not found")));
    }

    @Test
    @DisplayName("Return 400 when id is not a UUID")
    void should_return400_when_idIsNotAUuid() throws Exception {
        // When & Then
        mockMvc.perform(get(URI, "Invalid UUID"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Bad Request"));

        verify(getBrokerAccountUseCase, never()).handle(any(BrokerAccountId.class));
    }

    private BrokerAccountDetails validBrokerAccountDetails() {
        return new BrokerAccountDetails(
                BrokerAccountId.generate().value(),
                "Test Broker Account",
                new BigDecimal("100000"),
                new BigDecimal("0.0015"),
                new BigDecimal("0.0025")
        );
    }
}
