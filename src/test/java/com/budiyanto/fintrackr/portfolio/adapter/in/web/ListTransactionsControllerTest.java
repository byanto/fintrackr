package com.budiyanto.fintrackr.portfolio.adapter.in.web;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.budiyanto.fintrackr.portfolio.application.exception.PortfolioNotFoundException;
import com.budiyanto.fintrackr.portfolio.application.port.in.ListTransactionsUseCase;
import com.budiyanto.fintrackr.portfolio.domain.model.Portfolio;
import com.budiyanto.fintrackr.portfolio.domain.model.PortfolioId;
import com.budiyanto.fintrackr.shared.AssetId;
import com.budiyanto.fintrackr.shared.BrokerAccountId;
import com.budiyanto.fintrackr.shared.Money;
import com.budiyanto.fintrackr.shared.Quantity;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ListTransactionsController.class)
@DisplayName("ListTransactionsController Tests")
class ListTransactionsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ListTransactionsUseCase listTransactionsUseCase;

    private static final String URI = "/api/portfolios/{pid}/transactions";
    private static final BigDecimal BALANCE = new BigDecimal("1000000");
    private static final LocalDate TODAY = LocalDate.of(2026, 10, 15);

    @Test
    @DisplayName("Return 200 with deposits and buys when portfolio has transactions")
    void should_return200WithDepositsAndBuys_when_portfolioHasTransactions() throws Exception {
        // Given
        Portfolio portfolio = createPortfolio();

        when(listTransactionsUseCase.handle(portfolio.id())).thenReturn(portfolio.transactions());

        // When & Then
        mockMvc.perform(get(URI, portfolio.id().value()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.items").value(hasSize(3)))
                .andExpect(jsonPath("$.items[*].type").value(contains("DEPOSIT", "BUY", "DEPOSIT")))
                .andExpect(jsonPath("$.items[*].date").value(contains(
                        TODAY.minusDays(10).toString(),
                        TODAY.minusDays(5).toString(),
                        TODAY.minusDays(2).toString())));
    }

    @Test
    @DisplayName("Return 200 with empty items when portfolio has no transactions")
    void should_return200WithEmptyItems_when_portfolioHasNoTransactions() throws Exception {
        // Given
        Portfolio portfolio = Portfolio.create(BrokerAccountId.generate(), "Test Portfolios");

        when(listTransactionsUseCase.handle(portfolio.id())).thenReturn(portfolio.transactions());

        // When & Then
        mockMvc.perform(get(URI, portfolio.id().value()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.items").value(empty()));
    }

    @Test
    @DisplayName("Return 404 problem detail when portfolio does not exist")
    void should_return404ProblemDetail_when_portfolioDoesNotExist() throws Exception {
        // Given
        PortfolioId portfolioId = PortfolioId.generate();
        PortfolioNotFoundException exception = new PortfolioNotFoundException(portfolioId);

        when(listTransactionsUseCase.handle(portfolioId)).thenThrow(exception);

        // When & Then
        mockMvc.perform(get(URI, portfolioId.value()))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Portfolio Not Found"))
                .andExpect(jsonPath("$.detail").value(exception.getMessage()));
    }

    @Test
    @DisplayName("Return 400 problem detail when portfolio id is not UUID")
    void should_return400ProblemDetail_when_portfolioIdIsNotUuid() throws Exception {
        // When & Then
        mockMvc.perform(get(URI, "Invalid UUID"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Bad Request"));

        verifyNoInteractions(listTransactionsUseCase);
    }

    private Portfolio createPortfolio() {
        Portfolio portfolio = Portfolio.create(BrokerAccountId.generate(), "Test Portfolios");
        portfolio.recordDeposit(Money.of(BALANCE), TODAY.minusDays(10), TODAY);
        portfolio.recordBuy(
                AssetId.of("ID1000109507"),
                Quantity.ofShares(new BigDecimal("100")),
                Money.of(new BigDecimal("2000")),
                Money.of(new BigDecimal("3500")),
                TODAY.minusDays(5),
                TODAY
        );
        portfolio.recordDeposit(Money.of(BALANCE.multiply(BigDecimal.TWO)), TODAY.minusDays(2), TODAY);
        return portfolio;
    }

}
