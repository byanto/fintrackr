package com.budiyanto.fintrackr.portfolio.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.budiyanto.fintrackr.portfolio.application.exception.PortfolioNotFoundException;
import com.budiyanto.fintrackr.portfolio.application.port.in.RecordDepositCommand;
import com.budiyanto.fintrackr.portfolio.application.port.in.RecordDepositUseCase;
import com.budiyanto.fintrackr.portfolio.domain.exception.FutureDatedTransactionException;
import com.budiyanto.fintrackr.portfolio.domain.model.PortfolioId;
import com.budiyanto.fintrackr.portfolio.domain.model.TransactionId;
import com.budiyanto.fintrackr.shared.Money;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(RecordDepositController.class)
class RecordDepositControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String URI = "/api/portfolios/{pid}/deposits";

    @MockitoBean
    private RecordDepositUseCase recordDepositUseCase;

    @Nested
    @DisplayName("Valid Request Tests")
    class ValidRequestTest {
        @Test
        @DisplayName("Return 201 with location of the new transaction when deposit is valid")
        void should_return201WithLocationOfTheNewTransaction_when_depositIsValid() throws Exception{
            // Given
            PortfolioId portfolioId = PortfolioId.generate();
            RecordDepositRequest request = validRequest();
            TransactionId transactionId = TransactionId.generate();

            when(recordDepositUseCase.handle(any(RecordDepositCommand.class))).thenReturn(transactionId);

            // When & Then
            mockMvc.perform(post(URI, portfolioId.value())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", "/api/portfolios/" + portfolioId.value() + "/transactions/" + transactionId.value()))
                    .andExpect(content().string(""));

            verify(recordDepositUseCase).handle(any(RecordDepositCommand.class));
        }

        @Test
        @DisplayName("Pass command with domain types when request is valid")
        void should_passCommandWithDomainTypes_when_requestIsValid() throws Exception {
            // Given
            PortfolioId portfolioId = PortfolioId.generate();
            RecordDepositRequest request = validRequest();

            when(recordDepositUseCase.handle(any(RecordDepositCommand.class))).thenReturn(TransactionId.generate());

            // When
            mockMvc.perform(post(URI, portfolioId.value())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // Then
            verify(recordDepositUseCase).handle(new RecordDepositCommand(
                    portfolioId,
                    request.date(),
                    Money.of(request.amount())
            ));
        }
    }

    @Nested
    @DisplayName("Invalid Input Tests")
    class InvalidInputTest {

        @Test
        @DisplayName("Return 400 problem detail with field errors when required field is missing")
        void should_return400ProblemDetailWithFieldErrors_when_requiredFieldIsMissing() throws Exception {
            // Given
            PortfolioId portfolioId = PortfolioId.generate();
            String request = """
                    {"amount": 100000}
                    """;

            // When & Then
            mockMvc.perform(post(URI, portfolioId.value())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.errors[0].field").value("date"));

            verify(recordDepositUseCase, never()).handle(any(RecordDepositCommand.class));
        }

        @Test
        @DisplayName("Return 400 problem detail when body is malformed")
        void should_return400ProblemDetail_when_bodyIsMalformed() throws Exception {
            // Given
            PortfolioId portfolioId = PortfolioId.generate();
            String request = """
                    {"amount": 100000, "date":}
                    """;

            // When & Then
            mockMvc.perform(post(URI, portfolioId.value())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(request))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.title").value("Bad Request"));

            verify(recordDepositUseCase, never()).handle(any(RecordDepositCommand.class));
        }

        @Test
        @DisplayName("Return 400 problem detail with field errors when amount has decimal places")
        void should_return400ProblemDetailWithFieldErrors_when_amountHasDecimalPlaces() throws Exception {
            // Given
            PortfolioId portfolioId = PortfolioId.generate();
            RecordDepositRequest request = new RecordDepositRequest(
                    new BigDecimal("100.56"),
                    LocalDate.of(2026, 10, 15)
            );

            // When & Then
            mockMvc.perform(post(URI, portfolioId.value())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.errors[0].field").value("amount"));

            verify(recordDepositUseCase, never()).handle(any(RecordDepositCommand.class));
        }
    }

    @Nested
    @DisplayName("Use Case Fail Tests")
    class UseCaseFailTest {

        @Test
        @DisplayName("Return 404 problem detail when portfolio does not exist")
        void should_return404ProblemDetail_when_portfolioDoesNotExist() throws Exception {
            // Given
            PortfolioId portfolioId = PortfolioId.generate();
            RecordDepositRequest request = validRequest();

            PortfolioNotFoundException exception = new PortfolioNotFoundException(portfolioId);
            when(recordDepositUseCase.handle(any(RecordDepositCommand.class))).thenThrow(exception);

            // When & Then
            mockMvc.perform(post(URI, portfolioId.value())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Portfolio Not Found"))
                .andExpect(jsonPath("$.detail").value(exception.getMessage()));

            verify(recordDepositUseCase).handle(any(RecordDepositCommand.class));
        }

        @Test
        @DisplayName("Return 409 problem detail when concurrent update conflicts")
        void should_return409ProblemDetail_when_concurrentUpdateConflicts() throws Exception {
            // Given
            PortfolioId portfolioId = PortfolioId.generate();
            RecordDepositRequest request = validRequest();

            when(recordDepositUseCase.handle(any(RecordDepositCommand.class))).thenThrow(OptimisticLockingFailureException.class);

            // When & Then
            mockMvc.perform(post(URI, portfolioId.value())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Concurrent Update"));

            verify(recordDepositUseCase).handle(any(RecordDepositCommand.class));
        }

        @Test
        @DisplayName("Return 422 problem detail when portfolio rule is violated")
        void should_return422ProblemDetail_when_portfolioRuleIsViolated() throws Exception {
            // Given
            PortfolioId portfolioId = PortfolioId.generate();
            RecordDepositRequest request = validRequest();

            FutureDatedTransactionException exception = new FutureDatedTransactionException(request.date(), request.date().minusDays(10));
            when(recordDepositUseCase.handle(any(RecordDepositCommand.class))).thenThrow(exception);

            // When & Then
            mockMvc.perform(post(URI, portfolioId.value())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Business Rule Violated"))
                .andExpect(jsonPath("$.detail").value(exception.getMessage()));

            verify(recordDepositUseCase).handle(any(RecordDepositCommand.class));
        }
    }

    private RecordDepositRequest validRequest() {
        return new RecordDepositRequest(
                new BigDecimal("10000"),
                LocalDate.of(2026, 10, 25));
    }
}
