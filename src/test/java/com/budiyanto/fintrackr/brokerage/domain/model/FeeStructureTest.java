package com.budiyanto.fintrackr.brokerage.domain.model;

import com.budiyanto.fintrackr.shared.Quantity;
import com.budiyanto.fintrackr.shared.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("FeeStructure Tests")
class FeeStructureTest {

    // Given
    private final Percentage buyRate = Percentage.of(new BigDecimal("0.0015"));
    private final Percentage sellRate = Percentage.of(new BigDecimal("0.0025"));
    private final FeeStructure feeStructure = FeeStructure.of(buyRate, sellRate);

    private final Quantity quantity = Quantity.ofShares(new BigDecimal("1000"));
    private final Money price = Money.of(new BigDecimal("150"));

    @Nested
    @DisplayName("FeeStructure ComputeBuyFee Tests")
    class ComputeBuyFee {

        @Test
        @DisplayName("Compute the correct buy fee for valid inputs")
        void should_computeCorrectBuyFee_when_inputsAreValid() {
            // When
            var buyFee = feeStructure.computeBuyFee(quantity, price);

            // Then
            assertThat(buyFee).isEqualTo(Money.of(new BigDecimal("225"))); // 1000 * 150 * 0.0015 = 225
        }

        @ParameterizedTest
        @CsvSource({
                "3, 1250, 6",
                "5, 1625, 12",
                "7, 1000, 10",
                "2, 500, 2",
        })
        @DisplayName("Compute the correct buy fee with rounding for fractional fee")
        void should_computeCorrectBuyFeeWithRounding_when_rawFeeIsFractional(String quantityInput, String priceInput, String expected) {
            // Given
            var quantity = Quantity.ofShares(new BigDecimal(quantityInput));
            var price = Money.of(new BigDecimal(priceInput));

            // When
            var buyFee = feeStructure.computeBuyFee(quantity, price);

            // Then
            assertThat(buyFee).isEqualTo(Money.of(new BigDecimal(expected)));
        }

        @Test
        @DisplayName("Returns zero buy fee when quantity is zero")
        void should_returnZeroFee_when_quantityIsZero() {
            // Given
            var quantity = Quantity.ofUnits(BigDecimal.ZERO);

            // When
            var buyFee = feeStructure.computeBuyFee(quantity, price);

            // Then
            assertThat(buyFee).isEqualTo(Money.zero());
        }

        @Test
        @DisplayName("Returns zero buy fee when price is zero")
        void should_returnZeroFee_when_priceIsZero() {
            // Given
            var price = Money.zero();

            // When
            var buyFee = feeStructure.computeBuyFee(quantity, price);

            // Then
            assertThat(buyFee).isEqualTo(Money.zero());
        }

        @Test
        @DisplayName("Reject calculating buy fee when quantity is null")
        void should_throwNPE_when_quantityIsNull() {
            // When & Then
            assertThatThrownBy(() -> feeStructure.computeBuyFee(null, price))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Reject calculating buy fee when price is null")
        void should_throwNPE_when_priceIsNull() {
            // When & Then
            assertThatThrownBy(() -> feeStructure.computeBuyFee(quantity, null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Reject calculating buy fee when price is negative")
        void should_throwIAE_when_priceIsNegative() {
            // When & Then
            assertThatThrownBy(() -> feeStructure.computeBuyFee(quantity, Money.of(new BigDecimal("-1500"))))
                    .isInstanceOf(IllegalArgumentException.class);
        }

    }

    @Nested
    @DisplayName("FeeStructure ComputeSellFee Tests")
    class ComputeSellFee {

        @Test
        @DisplayName("Compute the correct sell fee for valid inputs")
        void should_computeCorrectSellFee_when_inputsAreValid() {
            // When
            var sellFee = feeStructure.computeSellFee(quantity, price);

            // Then
            assertThat(sellFee).isEqualTo(Money.of(new BigDecimal("375"))); // 1000 * 150 * 0.0025 = 375
        }

        @ParameterizedTest
        @CsvSource({
                "5, 3750, 47",
                "3, 1250, 9",
                "6, 700, 10",
                "2, 300, 2",
        })
        @DisplayName("Compute the correct sell fee with rounding for fractional fee")
        void should_computeCorrectSellFeeWithRounding_when_rawFeeIsFractional(String quantityInput, String priceInput, String expected) {
            // Given
            var quantity = Quantity.ofShares(new BigDecimal(quantityInput));
            var price = Money.of(new BigDecimal(priceInput));

            // When
            var sellFee = feeStructure.computeSellFee(quantity, price);

            // Then
            assertThat(sellFee).isEqualTo(Money.of(new BigDecimal(expected)));
        }

        @Test
        @DisplayName("Returns zero sell fee when quantity is zero")
        void should_returnZeroFee_when_quantityIsZero() {
            // Given
            var quantity = Quantity.ofUnits(BigDecimal.ZERO);

            // When
            var sellFee = feeStructure.computeSellFee(quantity, price);

            // Then
            assertThat(sellFee).isEqualTo(Money.zero());
        }

        @Test
        @DisplayName("Returns zero sell fee when price is zero")
        void should_returnZeroFee_when_priceIsZero() {
            // Given
            var price = Money.zero();

            // When
            var sellFee = feeStructure.computeSellFee(quantity, price);

            // Then
            assertThat(sellFee).isEqualTo(Money.zero());
        }

        @Test
        @DisplayName("Reject calculating sell fee when quantity is null")
        void should_throwNPE_when_quantityIsNull() {
            // When & Then
            assertThatThrownBy(() -> feeStructure.computeSellFee(null, price))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Reject calculating sell fee when price is null")
        void should_throwNPE_when_priceIsNull() {
            // When & Then
            assertThatThrownBy(() -> feeStructure.computeSellFee(quantity, null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Reject calculating sell fee when price is negative")
        void should_throwIAE_when_priceIsNegative() {
            // When & Then
            assertThatThrownBy(() -> feeStructure.computeSellFee(quantity, Money.of(new BigDecimal("-1500"))))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

}
