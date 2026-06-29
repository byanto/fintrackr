package com.budiyanto.fintrackr.brokerage.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Percentage Tests")
class PercentageTest {

    @ParameterizedTest
    @ValueSource(strings = {"0", "0.0015", "0.00123", "0.001403", "1"})
    @DisplayName("Given valid rate, when constructed via canonical constructor, then return Percentage with scale 6")
    void should_returnPercentageWithScale6_when_constructedViaCanonicalConstructor(String input) {
        // Given
        BigDecimal rate = new BigDecimal(input);

        // When
        Percentage result = new Percentage(rate);

        // Then
        assertThat(result.rate()).isEqualByComparingTo(rate);
        assertThat(result.rate().scale()).isEqualTo(6);
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "0.0015", "0.00123", "0.001403", "1"})
    @DisplayName("Given valid rate, when constructed via factory method, then return Percentage with scale 6")
    void should_returnPercentageWithScale6_when_constructedViaFactory(String input) {
        // Given
        BigDecimal rate = new BigDecimal(input);

        // When
        Percentage result = Percentage.of(rate);

        // Then
        assertThat(result.rate()).isEqualByComparingTo(rate);
        assertThat(result.rate().scale()).isEqualTo(6);
    }

    @ParameterizedTest
    @NullSource
    @DisplayName("Given a null input rate, when constructed, then throws a NullPointerException")
    void should_throwException_when_rateIsNull(BigDecimal input) {
        // When & Then
        assertThatThrownBy(() -> Percentage.of(input))
                .isInstanceOf(NullPointerException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"-10", "-5.8", "1.25", "20"})
    @DisplayName("Given a rate outside the valid range [0, 1], when constructed, then throws an IllegalArgumentException")
    void should_throwException_when_rateIsOutOfRange(String input) {
        // Given
        BigDecimal rate = new BigDecimal(input);

        // When & Then
        assertThatThrownBy(() -> Percentage.of(rate))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @CsvSource({
            "0.1234567, 0.123457",
            "0.4792031423, 0.479203",
            "0.3243135, 0.324314",
            "0.3243135000, 0.324314"
    })
    @DisplayName("Given a rate with a scale greater than 6, when constructed, then it is rounded to 6 decimal places")
    void should_scaleTo6_when_rateHasScaleLargerThan6(String input1, String input2) {
        // Given
        BigDecimal rate1 = new BigDecimal(input1);
        BigDecimal rate2 = new BigDecimal(input2);

        // When
        Percentage percentage = Percentage.of(rate1);

        // Then
        assertThat(percentage.rate()).isEqualTo(rate2);
    }
}
