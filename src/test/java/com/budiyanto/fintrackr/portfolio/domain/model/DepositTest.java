package com.budiyanto.fintrackr.portfolio.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.budiyanto.fintrackr.shared.Money;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class DepositTest {

    @ParameterizedTest(name = "\"{4}\"")
    @MethodSource("nullArgumentProvider")
    @DisplayName("Throw NPE with the field name when a field is null")
    void should_throwNPE_when_fieldIsNull(TransactionId id, PortfolioId portfolioId, LocalDate date, Money amount, String expectedMessage) {
        // When & Then
        assertThatThrownBy(() -> Deposit.reconstitute(id, portfolioId, date, amount))
                .isInstanceOf(NullPointerException.class)
                .hasMessage(expectedMessage);
    }

    static Stream<Arguments> nullArgumentProvider() {
        TransactionId id = TransactionId.generate();
        PortfolioId portfolioId = PortfolioId.generate();
        LocalDate date = LocalDate.now();
        Money amount = Money.of(new BigDecimal("10000"));

        return Stream.of(
                Arguments.of(null, portfolioId, date, amount, "id cannot be null"),
                Arguments.of(id, null, date, amount, "portfolioId cannot be null"),
                Arguments.of(id, portfolioId, null, amount, "date cannot be null"),
                Arguments.of(id, portfolioId, date, null, "amount cannot be null")
        );
    }

    @Test
    @DisplayName("Keep the given id when reconstitute")
    void should_keepGivenId_when_reconstitute() {
        // Given
        TransactionId id = TransactionId.generate();
        PortfolioId portfolioId = PortfolioId.generate();
        LocalDate date = LocalDate.now();
        Money amount = Money.of(new BigDecimal("10000"));

        // When
        Deposit result = Deposit.reconstitute(id, portfolioId, date, amount);

        // Then
        assertThat(result.id()).isEqualTo(id);
    }
}
