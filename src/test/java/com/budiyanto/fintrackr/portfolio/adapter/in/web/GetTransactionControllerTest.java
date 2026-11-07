package com.budiyanto.fintrackr.portfolio.adapter.in.web;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.budiyanto.fintrackr.portfolio.application.exception.PortfolioNotFoundException;
import com.budiyanto.fintrackr.portfolio.application.exception.TransactionNotFoundException;
import com.budiyanto.fintrackr.portfolio.application.port.in.GetTransactionUseCase;
import com.budiyanto.fintrackr.portfolio.domain.model.Buy;
import com.budiyanto.fintrackr.portfolio.domain.model.Deposit;
import com.budiyanto.fintrackr.portfolio.domain.model.Portfolio;
import com.budiyanto.fintrackr.portfolio.domain.model.PortfolioId;
import com.budiyanto.fintrackr.portfolio.domain.model.TransactionId;
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

@WebMvcTest(GetTransactionController.class)
@DisplayName("GetTransactionController Tests")
class GetTransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetTransactionUseCase getTransactionUseCase;

    private static final String URI = "/api/portfolios/{pid}/transactions/{tid}";
    private static final LocalDate TODAY = LocalDate.of(2026, 10, 15);

    @Test
    @DisplayName("Return 200 with deposit fields when transaction is deposit")
    void should_return200WithDepositFields_when_transactionIsDeposit() throws Exception {
        // Given
        Portfolio portfolio = Portfolio.create(BrokerAccountId.generate(), "Test Portfolio");
        Deposit deposit = portfolio.recordDeposit(Money.of(new BigDecimal("100000")), TODAY.minusDays(5), TODAY);

        when(getTransactionUseCase.handle(portfolio.id(), deposit.id())).thenReturn(deposit);

        // When & Then
        mockMvc.perform(get(URI, portfolio.id().value(), deposit.id().value()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(deposit.id().value().toString()))
                .andExpect(jsonPath("$.portfolioId").value(portfolio.id().value().toString()))
                .andExpect(jsonPath("$.type").value("DEPOSIT"))
                .andExpect(jsonPath("$.date").value(TODAY.minusDays(5).toString()))
                .andExpect(jsonPath("$.amount").value(100000))
                .andExpect(jsonPath("$.quantity").doesNotExist())
                .andExpect(jsonPath("$.isin").doesNotExist())
                .andExpect(jsonPath("$.price").doesNotExist())
                .andExpect(jsonPath("$.fee").doesNotExist());
    }

    @Test
    @DisplayName("Return 200 with buy fields when transaction is buy")
    void should_return200WithBuyFields_when_transactionIsBuy() throws Exception {
        // Given
        Portfolio portfolio = Portfolio.create(BrokerAccountId.generate(), "Test Portfolio");
        portfolio.recordDeposit(Money.of(new BigDecimal("1000000")), TODAY.minusDays(5), TODAY);
        Buy buy = portfolio.recordBuy(AssetId.of("ID1000109507"), Quantity.ofShares(new BigDecimal("100")),
                Money.of(new BigDecimal("1500")), Money.of(new BigDecimal("3000")),
                TODAY.minusDays(3), TODAY);

        when(getTransactionUseCase.handle(portfolio.id(), buy.id())).thenReturn(buy);

        // When & Then
        mockMvc.perform(get(URI, portfolio.id().value(), buy.id().value()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(buy.id().value().toString()))
                .andExpect(jsonPath("$.portfolioId").value(portfolio.id().value().toString()))
                .andExpect(jsonPath("$.type").value("BUY"))
                .andExpect(jsonPath("$.date").value(TODAY.minusDays(3).toString()))
                .andExpect(jsonPath("$.isin").value("ID1000109507"))
                .andExpect(jsonPath("$.quantity").value(100))
                .andExpect(jsonPath("$.price").value(1500))
                .andExpect(jsonPath("$.fee").value(3000))
                .andExpect(jsonPath("$.amount").doesNotExist());
    }

    @Test
    @DisplayName("Return 404 problem detail when transaction does not exist")
    void should_return404ProblemDetail_when_transactionDoesNotExist() throws Exception {
        // Given
        PortfolioId portfolioId = PortfolioId.generate();
        TransactionId transactionId = TransactionId.generate();
        TransactionNotFoundException exception = new TransactionNotFoundException(portfolioId, transactionId);

        when(getTransactionUseCase.handle(portfolioId, transactionId)).thenThrow(exception);

        // When & Then
        mockMvc.perform(get(URI, portfolioId.value(), transactionId.value()))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Transaction Not Found"))
                .andExpect(jsonPath("$.detail").value(exception.getMessage()));
    }

    @Test
    @DisplayName("Return 404 problem detail when portfolio does not exist")
    void should_return404ProblemDetail_when_portfolioDoesNotExist() throws Exception {
        // Given
        PortfolioId portfolioId = PortfolioId.generate();
        TransactionId transactionId = TransactionId.generate();
        PortfolioNotFoundException exception = new PortfolioNotFoundException(portfolioId);

        when(getTransactionUseCase.handle(portfolioId, transactionId)).thenThrow(exception);

        // When & Then
        mockMvc.perform(get(URI, portfolioId.value(), transactionId.value()))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Portfolio Not Found"))
                .andExpect(jsonPath("$.detail").value(exception.getMessage()));
    }

    @Test
    @DisplayName("Return 400 problem detail when transaction id is not UUID")
    void should_return400ProblemDetail_when_transactionIdIsNotUuid() throws Exception {
        // When & Then
        mockMvc.perform(get(URI, PortfolioId.generate().value(), "Invalid UUID"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Bad Request"));

        verifyNoInteractions(getTransactionUseCase);
    }

}
