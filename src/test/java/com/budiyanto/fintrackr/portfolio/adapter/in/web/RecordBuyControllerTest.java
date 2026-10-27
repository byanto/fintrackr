package com.budiyanto.fintrackr.portfolio.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.containsStringIgnoringCase;
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

import com.budiyanto.fintrackr.brokerage.domain.exception.InsufficientRdnException;
import com.budiyanto.fintrackr.portfolio.application.exception.PortfolioNotFoundException;
import com.budiyanto.fintrackr.portfolio.application.port.in.RecordBuyCommand;
import com.budiyanto.fintrackr.portfolio.application.port.in.RecordBuyUseCase;
import com.budiyanto.fintrackr.portfolio.domain.exception.InsufficientBalanceException;
import com.budiyanto.fintrackr.portfolio.domain.model.PortfolioId;
import com.budiyanto.fintrackr.portfolio.domain.model.TransactionId;
import com.budiyanto.fintrackr.shared.AssetId;
import com.budiyanto.fintrackr.shared.Money;
import com.budiyanto.fintrackr.shared.Quantity;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(RecordBuyController.class)
class RecordBuyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RecordBuyUseCase recordBuyUseCase;

    private static final String BUY_URI = "/api/portfolios/{pid}/buys";

    @Nested
    @DisplayName("Valid Request Tests")
    class ValidRequestTest {

        @Test
        @DisplayName("Return 201 with location of the new transaction when buy is valid")
        void should_return201WithLocationOfTheNewTransaction_when_buyIsValid() throws Exception {
            // Given
            PortfolioId portfolioId = PortfolioId.generate();
            RecordBuyRequest request = validRequest();

            TransactionId transactionId = TransactionId.generate();

            when(recordBuyUseCase.handle(any(RecordBuyCommand.class))).thenReturn(transactionId);

            // When & Then
            mockMvc.perform(post(BUY_URI, portfolioId.value())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", "/api/portfolios/" + portfolioId.value() + "/transactions/" + transactionId.value()))
            .andExpect(content().string(""));

            verify(recordBuyUseCase).handle(any(RecordBuyCommand.class));
        }

        @Test
        @DisplayName("Pass command with domain types when request is valid")
        void should_passCommandWithDomainTypes_when_requestIsValid() throws Exception {
            // Given
            PortfolioId portfolioId = PortfolioId.generate();
            RecordBuyRequest request = validRequest();

            when(recordBuyUseCase.handle(any(RecordBuyCommand.class))).thenReturn(TransactionId.generate());

            // When
            mockMvc.perform(post(BUY_URI, portfolioId.value())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // Then
            verify(recordBuyUseCase).handle(new RecordBuyCommand(
                    portfolioId,
                    AssetId.of("ID1000109507"),
                    Quantity.ofShares(new BigDecimal("100")),
                    Money.of(new BigDecimal("2000")),
                    Money.of(new BigDecimal("1500")),
                    LocalDate.of(2026, 10, 25)
            ));
        }

        @Test
        @DisplayName("Pass no fee when fee is null")
        void should_passNoFee_when_feeIsNull() throws Exception {
            // Given
            RecordBuyRequest request = new RecordBuyRequest("ID1000109507", new BigDecimal("100"), "SHARES",
                    new BigDecimal("2000"), null, LocalDate.of(2026, 10, 25));

            when(recordBuyUseCase.handle(any(RecordBuyCommand.class))).thenReturn(TransactionId.generate());

            // When
            mockMvc.perform(post(BUY_URI, PortfolioId.generate().value())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // Then
            ArgumentCaptor<RecordBuyCommand> captor = ArgumentCaptor.forClass(RecordBuyCommand.class);
            verify(recordBuyUseCase).handle(captor.capture());
            assertThat(captor.getValue().fee()).isNull();
        }
    }

    @Nested
    @DisplayName("Invalid Input Tests")
    class InvalidInputTest {

        @Test
        @DisplayName("Return 400 problem detail with field errors when required field is missing")
        void should_return400ProblemDetailWithFieldErrors_when_requiredFieldIsMissing() throws Exception {
            // Given
            String body = """
                    {"quantity": 100, "quantityKind": "SHARES", "price": 2000, "fee": 1500, "date": "2026-10-25"}
                    """;

            // When & Then
            mockMvc.perform(post(BUY_URI, PortfolioId.generate().value())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.errors", hasSize(1)))
                    .andExpect(jsonPath("$.errors[0].field").value("isin"));

            verify(recordBuyUseCase, never()).handle(any(RecordBuyCommand.class));
        }

        @Test
        @DisplayName("Return 400 problem detail when portfolio id is not UUID")
        void should_return400ProblemDetail_when_portfolioIdIsNotUuid() throws Exception{
            // Given
            RecordBuyRequest request = validRequest();

            // When & Then
            mockMvc.perform(post(BUY_URI, "Invalid UUID")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.title").value("Bad Request"));

            verify(recordBuyUseCase, never()).handle(any(RecordBuyCommand.class));
        }

        @Test
        @DisplayName("Return 400 problem detail when ISIN is invalid")
        void should_return400ProblemDetail_when_isinIsInvalid() throws Exception {
            // Given
            RecordBuyRequest request = new RecordBuyRequest("ID1000109509", new BigDecimal("100"), "SHARES",
                    new BigDecimal("2000"), new BigDecimal("1500"), LocalDate.of(2026, 10, 25));

            // When & Then
            mockMvc.perform(post(BUY_URI, PortfolioId.generate().value())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.title").value("Invalid Request"))
                    .andExpect(jsonPath("$.detail", containsStringIgnoringCase("ISIN")));

            verify(recordBuyUseCase, never()).handle(any(RecordBuyCommand.class));
        }

        @Test
        @DisplayName("Return 400 problem detail when quantity does not fit kind")
        void should_return400ProblemDetail_when_quantityDoesNotFitKind() throws Exception {
            // Given
            RecordBuyRequest request = new RecordBuyRequest("ID1000109507", new BigDecimal("100"), "UNKNOWN KIND",
                    new BigDecimal("2000"), new BigDecimal("1500"), LocalDate.of(2026, 10, 25));

            // When & Then
            mockMvc.perform(post(BUY_URI, PortfolioId.generate().value())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.title").value("Invalid Request"))
                    .andExpect(jsonPath("$.detail", containsString("Invalid quantity kind")));

            verify(recordBuyUseCase, never()).handle(any(RecordBuyCommand.class));
        }

        @Test
        @DisplayName("Return 400 problem detail when body is malformed")
        void should_return400ProblemDetail_when_bodyIsMalformed() throws Exception {
            // Given
            String body = """
                    {"isin": "ID1000109507", "quantity":}
                    """;

            // When & Then
            mockMvc.perform(post(BUY_URI, PortfolioId.generate().value())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));

            verify(recordBuyUseCase, never()).handle(any(RecordBuyCommand.class));
        }
    }

    @Nested
    @DisplayName("Use Case Fail Tests")
    class UseCaseFailTest {

        @Test
        @DisplayName("Return 404 problem detail when portfolio does not exist")
        void should_return404ProblemDetail_when_portfolioDoesNotExist() throws Exception {
            // Given
            RecordBuyRequest request = validRequest();
            PortfolioId portfolioId = PortfolioId.generate();
            PortfolioNotFoundException exception = new PortfolioNotFoundException(portfolioId);
            when(recordBuyUseCase.handle(any(RecordBuyCommand.class))).thenThrow(exception);

            // When & Then
            mockMvc.perform(post(BUY_URI, portfolioId.value())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.title").value("Portfolio Not Found"))
                    .andExpect(jsonPath("$.detail").value(exception.getMessage()));

            verify(recordBuyUseCase).handle(any(RecordBuyCommand.class));
        }

        @Test
        @DisplayName("Return 409 problem detail when concurrent update conflicts")
        void should_return409ProblemDetail_when_concurrentUpdateConflicts() throws Exception {
            // Given
            RecordBuyRequest request = validRequest();
            PortfolioId portfolioId = PortfolioId.generate();

            when(recordBuyUseCase.handle(any(RecordBuyCommand.class))).thenThrow(OptimisticLockingFailureException.class);

            // When & Then
            mockMvc.perform(post(BUY_URI, portfolioId.value())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.title").value("Concurrent Update"));

            verify(recordBuyUseCase).handle(any(RecordBuyCommand.class));
        }

        @Test
        @DisplayName("Return 422 problem detail when portfolio rule is violated")
        void should_return422ProblemDetail_when_portfolioRuleIsViolated() throws Exception {
            // Given
            RecordBuyRequest request = validRequest();
            InsufficientBalanceException exception = new InsufficientBalanceException(
                    Money.of(new BigDecimal("10000")), Money.of(new BigDecimal("20000")));

            when(recordBuyUseCase.handle(any(RecordBuyCommand.class))).thenThrow(exception);

            // When & Then
            mockMvc.perform(post(BUY_URI, PortfolioId.generate().value())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.title").value("Business Rule Violated"))
                    .andExpect(jsonPath("$.detail").value(exception.getMessage()));

            verify(recordBuyUseCase).handle(any(RecordBuyCommand.class));
        }

        @Test
        @DisplayName("Return 422 problem detail when brokerage rule is violated")
        void should_return422ProblemDetail_when_brokerageRuleIsViolated() throws Exception {
            // Given
            RecordBuyRequest request = validRequest();
            InsufficientRdnException exception = new InsufficientRdnException(
                    Money.of(new BigDecimal("10000")), Money.of(new BigDecimal("20000")));

            when(recordBuyUseCase.handle(any(RecordBuyCommand.class))).thenThrow(exception);

            // When & Then
            mockMvc.perform(post(BUY_URI, PortfolioId.generate().value())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.title").value("Business Rule Violated"))
                    .andExpect(jsonPath("$.detail").value(exception.getMessage()));

            verify(recordBuyUseCase).handle(any(RecordBuyCommand.class));
        }

        @Test
        @DisplayName("Propagate exception when use case throws illegal argument exception")
        void should_propagateException_when_useCaseThrowsIllegalArgumentException() throws Exception {
            // Given
            RecordBuyRequest request = validRequest();

            when(recordBuyUseCase.handle(any(RecordBuyCommand.class))).thenThrow(IllegalArgumentException.class);

            // When & Then
            assertThatThrownBy(() -> mockMvc.perform(post(BUY_URI, PortfolioId.generate().value())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))))
                    .hasCauseInstanceOf(IllegalArgumentException.class);

            verify(recordBuyUseCase).handle(any(RecordBuyCommand.class));
        }
    }

    private RecordBuyRequest validRequest() {
        return new RecordBuyRequest("ID1000109507", new BigDecimal("100"), "SHARES",
                new BigDecimal("2000"), new BigDecimal("1500"), LocalDate.of(2026, 10, 25));
    }
}
