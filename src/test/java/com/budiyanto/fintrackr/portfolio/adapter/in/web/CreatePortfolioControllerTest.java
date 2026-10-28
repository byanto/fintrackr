package com.budiyanto.fintrackr.portfolio.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.budiyanto.fintrackr.portfolio.application.exception.UnknownBrokerAccountException;
import com.budiyanto.fintrackr.portfolio.application.port.in.CreatePortfolioCommand;
import com.budiyanto.fintrackr.portfolio.application.port.in.CreatePortfolioUseCase;
import com.budiyanto.fintrackr.portfolio.domain.model.PortfolioId;
import com.budiyanto.fintrackr.shared.BrokerAccountId;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CreatePortfolioController.class)
class CreatePortfolioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreatePortfolioUseCase createPortfolioUseCase;

    private static final String URI = "/api/portfolios";

    @Nested
    @DisplayName("Valid Request Tests")
    class ValidRequestTest {
        @Test
        @DisplayName("Return 201 with location when request is valid")
        void should_return201WithLocation_when_requestIsValid() throws Exception {
            // Given
            PortfolioId portfolioId = PortfolioId.generate();
            CreatePortfolioRequest request = validRequest();
            when(createPortfolioUseCase.handle(any(CreatePortfolioCommand.class))).thenReturn(portfolioId);

            // When & Then
            mockMvc.perform(post(URI)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", "/api/portfolios/" + portfolioId.value()))
                    .andExpect(content().string(""));

            verify(createPortfolioUseCase).handle(any(CreatePortfolioCommand.class));
        }

        @Test
        @DisplayName("Pass broker account id and name to use case when request is valid")
        void should_passBrokerAccountIdAndNameToUseCase_when_requestIsValid() throws Exception {
            // Given
            CreatePortfolioRequest request = validRequest();
            when(createPortfolioUseCase.handle(any(CreatePortfolioCommand.class))).thenReturn(PortfolioId.generate());

            // When
            mockMvc.perform(post(URI)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // Then
            ArgumentCaptor<CreatePortfolioCommand> captor = ArgumentCaptor.forClass(CreatePortfolioCommand.class);
            verify(createPortfolioUseCase).handle(captor.capture());
            assertThat(captor.getValue().brokerAccountId().value()).isEqualTo(request.brokerAccountId());
            assertThat(captor.getValue().name()).isEqualTo(request.name());
        }
    }

    @Nested
    @DisplayName("Invalid Input Tests")
    class InvalidInputTest {
        @Test
        @DisplayName("Return 422 problem detail when broker account is unknown")
        void should_return422ProblemDetail_when_brokerAccountIsUnknown() throws Exception {
            // Given
            BrokerAccountId unknownBrokerAccountId = BrokerAccountId.generate();
            CreatePortfolioRequest request = new CreatePortfolioRequest(unknownBrokerAccountId.value(), "Test Portfolio");

            UnknownBrokerAccountException exception = new UnknownBrokerAccountException(unknownBrokerAccountId);
            when(createPortfolioUseCase.handle(any(CreatePortfolioCommand.class))).thenThrow(exception);

            // When & Then
            mockMvc.perform(post(URI)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.title").value("Unknown Broker Account"))
                    .andExpect(jsonPath("$.detail", containsStringIgnoringCase(unknownBrokerAccountId.value().toString())));

            verify(createPortfolioUseCase).handle(any(CreatePortfolioCommand.class));
        }

        @Test
        @DisplayName("Return 400 problem detail with field errors when name is blank")
        void should_return400ProblemDetailWithFieldErrors_when_nameIsBlank() throws Exception {
            // Given
            CreatePortfolioRequest invalidRequest = new CreatePortfolioRequest(BrokerAccountId.generate().value(), "");

            // When & Then
            mockMvc.perform(post(URI)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.title").value("Bad Request"))
                    .andExpect(jsonPath("$.errors[0].field").value("name"));

            verify(createPortfolioUseCase, never()).handle(any(CreatePortfolioCommand.class));
        }

        @Test
        @DisplayName("Return 400 problem detail with field errors when broker account id is missing")
        void should_return400ProblemDetailWithFieldErrors_when_brokerAccountIdIsMissing() throws Exception {
            // Given
            String invalidRequest = """
                    {"name": "Test Portfolio"}
                    """;

            // When & Then
            mockMvc.perform(post(URI)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.errors[0].field").value("brokerAccountId"));

            verify(createPortfolioUseCase, never()).handle(any(CreatePortfolioCommand.class));
        }
    }

    private CreatePortfolioRequest validRequest() {
        return new CreatePortfolioRequest(
                UUID.randomUUID(),
                "Test Portfolio"
        );
    }
}
