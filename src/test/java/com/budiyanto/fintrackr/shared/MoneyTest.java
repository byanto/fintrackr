package com.budiyanto.fintrackr.shared;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.util.Currency;

@DisplayName("Money Tests")
class MoneyTest {

    @Test
    @DisplayName("Given valid amount, when constructed via canonical constructor, then return Money with scale 0")
    void should_returnMoneyWithScaleZero_when_constructedViaCanonicalConstructor() {
        // Given
        BigDecimal amount = new BigDecimal("1500.00");

        // When
        Money result = new Money(amount, Currency.getInstance("IDR"));

        // Then
        assertThat(result.amount()).isEqualByComparingTo(amount);
        assertThat(result.amount().scale()).isZero();
        assertThat(result.currency().getCurrencyCode()).isEqualTo("IDR");
    }

    @Test
    @DisplayName("Given valid amount, when constructed via factory method, then return Money with scale 0")
    void should_returnMoneyWithScaleZero_when_constructedViaFactory() {
        // Given
        BigDecimal amount = new BigDecimal("1500.00");

        // When
        Money result = Money.of(amount);

        // Then
        assertThat(result.amount()).isEqualByComparingTo(amount);
        assertThat(result.amount().scale()).isZero();
        assertThat(result.currency().getCurrencyCode()).isEqualTo("IDR");
    }

    @ParameterizedTest(name = "Input {0} should round to {1} using HALF_EVEN")
    @CsvSource({
            "1498.4,  1498", // Down (< .5)
            "1498.6,  1499", // Up (> .5)
            "1498.5,  1498", // Rounds to nearest even number (8 is even)
            "1499.5,  1500", // Rounds to nearest even number (0 is even)
            "1500.0,  1500"  // Stays same
    })
    @DisplayName("Given valid amount, when constructed, then return Money with scale 0 and correct rounding")
    void should_roundAmountCorrectly_when_constructed(String input, String expected) {
        // Given
        BigDecimal inputAmount = new BigDecimal(input);
        BigDecimal expectedAmount = new BigDecimal(expected);

        // When
        Money result = Money.of(inputAmount);

        // Then
        assertThat(result.amount()).isEqualByComparingTo(expectedAmount);
        assertThat(result.amount().scale()).isZero();
        assertThat(result.currency().getCurrencyCode()).isEqualTo("IDR");
    }

    @Test
    @DisplayName("Given two amounts differing only in scale, when constructed, then the Moneys are equal")
    void should_beEqual_when_amountsDifferOnlyInScale() {
        // Given
        BigDecimal amount = new BigDecimal("1500.00");

        // When
        Money result = new Money(amount, Currency.getInstance("IDR"));

        // Then
        assertThat(result).isEqualTo(new Money(new BigDecimal("1500"), Currency.getInstance("IDR")));
    }

    @Test
    @DisplayName("Given valid amount, when constructed via factory method, then return Money with default currency IDR")
    void should_returnMoneyWithIDR_when_createMoneyWithFactory() {
        // Given
        BigDecimal amount = new BigDecimal("1500.00");

        // When
        Money result = Money.of(amount);

        // Then
        assertThat(result.currency().getCurrencyCode()).isEqualTo("IDR");
    }

    @Test
    @DisplayName("Given null amount when constructed, then throws exception")
    void should_throwException_when_amountIsNull() {
        // Then
        assertThatThrownBy(() -> Money.of(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("amount");
    }

    @Test
    @DisplayName("Given null currency when constructed, then throws exception")
    void should_throwException_when_currencyIsNull() {
        // Then
        assertThatThrownBy(() -> new Money(new BigDecimal("1500"), null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("currency");
    }
}
