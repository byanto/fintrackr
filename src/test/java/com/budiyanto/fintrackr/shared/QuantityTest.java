package com.budiyanto.fintrackr.shared;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Quantity Tests")
class QuantityTest {

    @Nested
    @DisplayName("ofShares Tests")
    class OfSharesConstructor {

        @ParameterizedTest
        @ValueSource(strings = {"10", "25", "1000", "100.00", "28.00"})
        @DisplayName("Given a whole number, when ofShares is called, then a Quantity is created successfully")
        void should_returnQuantity_when_valueIsWholeNumber(String input) {
            // Given
            BigDecimal value = new BigDecimal(input);

            // When
            Quantity result = Quantity.ofShares(value);

            // Then
            assertThat(result.value()).isEqualByComparingTo(value);
            assertThat(result.value().scale()).isEqualTo(0);

        }

        @ParameterizedTest
        @NullSource
        @DisplayName("Given a null value, when constructed, then throws a NullPointerException")
        void should_throwException_when_valueIsNull(BigDecimal nullValue) {
            // When & Then
            assertThatThrownBy(() -> Quantity.ofShares(nullValue))
                    .isInstanceOf(NullPointerException.class);
        }

        @ParameterizedTest
        @ValueSource(strings = {"-1000", "-1245.00", "-1234.8789"})
        @DisplayName("Given a negative value, when constructed, then throws an IllegalArgumentException")
        void should_throwException_when_valueIsNegative(String input) {
            // Given
            BigDecimal negativeValue = new BigDecimal(input);

            // When & Then
            assertThatThrownBy(() -> Quantity.ofShares(negativeValue))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @ParameterizedTest
        @ValueSource(strings = {"10.5", "10.678", "15.120"})
        @DisplayName("Given a non-whole number, when ofShares is called, then throws an IllegalArgumentException")
        void should_throwException_when_valueIsNotWholeNumber(String input) {
            // Given
            BigDecimal value = new BigDecimal(input);

            // When & Then
            assertThatThrownBy(() -> Quantity.ofShares(value))
                    .isInstanceOf(IllegalArgumentException.class);

        }

        @ParameterizedTest
        @CsvSource({
                "10, 10",
                "15, 15.00",
        })
        @DisplayName("Given 2 same values, when comparing, then return true")
        void should_returnTrue_when_comparingTwoEqualQuantities(String input1, String input2) {
            // Given
            BigDecimal value1 = new BigDecimal(input1);
            BigDecimal value2 = new BigDecimal(input2);

            // When
            Quantity quantity1 = Quantity.ofShares(value1);
            Quantity quantity2 = Quantity.ofShares(value2);

            // Then
            assertThat(quantity1).isEqualTo(quantity2);
        }

    }

    @Nested
    @DisplayName("ofUnits Tests")
    class OfUnitsConstructor {

        @ParameterizedTest
        @ValueSource(strings = {"10", "10.5", "10.38", "10.258", "10.3894", "15.3230", "18.129300"})
        @DisplayName("Given a value with a scale of 4 or less, when ofUnits is called, then a Quantity is created successfully")
        void should_returnQuantity_when_valueHasScaleLessEqual4(String input) {
            // Given
            BigDecimal value = new BigDecimal(input);

            // When
            Quantity result = Quantity.ofUnits(value);

            // Then
            assertThat(result.value()).isEqualByComparingTo(value);
            assertThat(result.value().scale()).isEqualTo(4);
        }

        @ParameterizedTest
        @NullSource
        @DisplayName("Given a null value, when constructed, then throws a NullPointerException")
        void should_throwException_when_valueIsNull(BigDecimal nullValue) {
            // When & Then
            assertThatThrownBy(() -> Quantity.ofUnits(nullValue))
                    .isInstanceOf(NullPointerException.class);
        }

        @ParameterizedTest
        @ValueSource(strings = {"-1000", "-1245.00", "-1234.8789"})
        @DisplayName("Given a negative value, when constructed, then throws an IllegalArgumentException")
        void should_throwException_when_valueIsNegative(String input) {
            // Given
            BigDecimal negativeValue = new BigDecimal(input);

            // When & Then
            assertThatThrownBy(() -> Quantity.ofUnits(negativeValue))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @ParameterizedTest
        @ValueSource(strings = {"10.12345", "10.232023"})
        @DisplayName("Given a value with a scale greater than 4, when ofUnits is called, then throws an IllegalArgumentException")
        void should_throwException_when_valueHasScaleGreaterThan4(String input) {
            // Given
            BigDecimal value = new BigDecimal(input);

            // When & Then
            assertThatThrownBy(() -> Quantity.ofUnits(value))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @ParameterizedTest
        @CsvSource({
                "10, 10",
                "15, 15.00",
                "15.12, 15.1200",
                "15.345, 15.345",
                "13.2122, 13.2122"
        })
        @DisplayName("Given 2 same values, when comparing, then return true")
        void should_returnTrue_when_comparingTwoEqualQuantities(String input1, String input2) {
            // Given
            BigDecimal value1 = new BigDecimal(input1);
            BigDecimal value2 = new BigDecimal(input2);

            // When
            Quantity quantity1 = Quantity.ofUnits(value1);
            Quantity quantity2 = Quantity.ofUnits(value2);

            // Then
            assertThat(quantity1).isEqualTo(quantity2);
        }

    }

}
