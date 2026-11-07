package com.budiyanto.fintrackr.portfolio.adapter.in.web;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.budiyanto.fintrackr.portfolio.application.exception.PortfolioNotFoundException;
import com.budiyanto.fintrackr.portfolio.application.port.in.GetPortfolioUseCase;
import com.budiyanto.fintrackr.portfolio.application.port.in.PortfolioDetails;
import com.budiyanto.fintrackr.portfolio.domain.model.PortfolioId;
import com.budiyanto.fintrackr.shared.BrokerAccountId;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(GetPortfolioController.class)
@DisplayName("GetPortfolioController Tests")
class GetPortfolioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetPortfolioUseCase getPortfolioUseCase;

    private static final String URI = "/api/portfolios/{id}";

    @Test
    @DisplayName("Return 200 with portfolio fields when portfolio exists")
    void should_return200WithPortfolioFields_when_portfolioExists() throws Exception {
        // Given
        PortfolioId portfolioId = PortfolioId.generate();
        PortfolioDetails details = new PortfolioDetails(
                portfolioId.value(),
                BrokerAccountId.generate().value(),
                "Test Portfolios",
                new BigDecimal("100000")
        );

        when(getPortfolioUseCase.handle(portfolioId)).thenReturn(details);

        // When & Then
        mockMvc.perform(get(URI, portfolioId.value()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(portfolioId.value().toString()))
                .andExpect(jsonPath("$.brokerAccountId").value(details.brokerAccountId().toString()))
                .andExpect(jsonPath("$.name").value(details.name()))
                .andExpect(jsonPath("$.tradingBalance").value(details.tradingBalance()));
    }

    @Test
    @DisplayName("Return 404 problem detail when portfolio does not exist")
    void should_return404ProblemDetail_when_portfolioDoesNotExist() throws Exception {
        // Given
        PortfolioId portfolioId = PortfolioId.generate();
        PortfolioNotFoundException exception = new PortfolioNotFoundException(portfolioId);

        when(getPortfolioUseCase.handle(portfolioId)).thenThrow(exception);

        // When & Then
        mockMvc.perform(get(URI, portfolioId.value()))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title", containsString("Portfolio Not Found")))
                .andExpect(jsonPath("$.detail", containsString("Portfolio not found")));
    }

    @Test
    @DisplayName("Return 400 when id is not a UUID")
    void should_return400_when_idIsNotAUuid() throws Exception {
        // When & Then
        mockMvc.perform(get(URI, "Invalid UUID"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Bad Request"));

        verifyNoInteractions(getPortfolioUseCase);
    }
}
