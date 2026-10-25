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

    private static final TransactionId TRANSACTION_ID = TransactionId.generate();
    private static final PortfolioId PORTFOLIO_ID = PortfolioId.generate();
    private static final LocalDate DEPOSIT_DATE = LocalDate.now();
    private static final Money AMOUNT = Money.of(new BigDecimal("10000"));

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
        return Stream.of(
                Arguments.of(null, PORTFOLIO_ID, DEPOSIT_DATE, AMOUNT, "id cannot be null"),
                Arguments.of(TRANSACTION_ID, null, DEPOSIT_DATE, AMOUNT, "portfolioId cannot be null"),
                Arguments.of(TRANSACTION_ID, PORTFOLIO_ID, null, AMOUNT, "date cannot be null"),
                Arguments.of(TRANSACTION_ID, PORTFOLIO_ID, DEPOSIT_DATE, null, "amount cannot be null")
        );
    }

    @Test
    @DisplayName("Keep the given id when reconstitute")
    void should_keepGivenId_when_reconstitute() {
        // When
        Deposit result = Deposit.reconstitute(TRANSACTION_ID, PORTFOLIO_ID, DEPOSIT_DATE, AMOUNT);

        // Then
        assertThat(result.id()).isEqualTo(TRANSACTION_ID);
    }

    @Test
    @DisplayName("Increase cash by amount when deposit")
    void should_increaseCashByAmount_when_deposit() {
        // Given
        Deposit deposit = Deposit.create(PORTFOLIO_ID, DEPOSIT_DATE, AMOUNT);

        // When
        Money result = deposit.cashFlow();

        // Then
        assertThat(result).isEqualTo(AMOUNT);
    }
}
