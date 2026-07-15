package com.budiyanto.fintrackr.brokerage.domain.model;

import com.budiyanto.fintrackr.brokerage.domain.exception.InsufficientRdnException;
import com.budiyanto.fintrackr.shared.DomainException;
import com.budiyanto.fintrackr.shared.Money;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("BrokerAccount Tests")
class BrokerAccountTest {

    // Given
    private BrokerAccount brokerAccount;
    private final String name = "Stockbit";
    private final FeeStructure feeStructure = FeeStructure.of(
            Percentage.of(new BigDecimal("0.0015")),
            Percentage.of(new BigDecimal("0.0025")));

    @BeforeEach
    void setUp() {
        brokerAccount = BrokerAccount.create(name, feeStructure);
    }

    @Nested
    @DisplayName("Create BrokerAccount Tests")
    class CreateTest {

        @Test
        @DisplayName("Create a BrokerAccount when inputs are valid")
        void should_createBrokerAccount_when_inputsAreValid() {
            // When
            var result = BrokerAccount.create(name, feeStructure);

            // Then
            assertThat(result.id()).isNotNull();
            assertThat(result.name()).isEqualTo(name);
            assertThat(result.feeStructure()).isEqualTo(feeStructure);
            assertThat(result.rdn()).isEqualTo(Money.zero());
        }

        @ParameterizedTest
        @MethodSource("provideNullArguments")
        @DisplayName("Reject BrokerAccount creation when input arguments are null")
        void should_throwNPE_when_inputsAreNull(String name, FeeStructure feeStructure) {
            // When & Then
            assertThatThrownBy(() -> BrokerAccount.create(name, feeStructure))
                    .isInstanceOf(NullPointerException.class);
        }

        private static Stream<Arguments> provideNullArguments() {
            var feeStructure = FeeStructure.of(
                    Percentage.of(new BigDecimal("0.0015")),
                    Percentage.of(new BigDecimal("0.0025")));

            return Stream.of(
                    Arguments.of(null, feeStructure),
                    Arguments.of("Stockbit", null));
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   ", "\t", "\n"})
        @DisplayName("Reject BrokerAccount creation when input name is empty or blank")
        void should_throwIAE_when_nameIsBlankOrEmpty(String name) {
            // When & Then
            assertThatThrownBy(() -> BrokerAccount.create(name, feeStructure))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Rename Tests")
    class RenameTest {

        @Test
        @DisplayName("Rename BrokerAccount when input name is valid")
        void should_rename_when_inputNameIsValid() {
            // Given
            String newName = "New Name";

            // When
            brokerAccount.rename(newName);

            // Then
            assertThat(brokerAccount.name()).isEqualTo(newName);
        }

        @Test
        @DisplayName("Reject rename when input name is null")
        void should_throwNPE_when_inputNameIsNull() {
            // When & Then
            assertThatThrownBy(() -> brokerAccount.rename(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   ", "\t", "\n"})
        @DisplayName("Reject rename when input name is empty or blank")
        void should_throwIAE_when_inputNameIsEmptyOrBlank(String name) {
            // When & Then
            assertThatThrownBy(() -> brokerAccount.rename(name))
                    .isInstanceOf(IllegalArgumentException.class);
        }

    }

    @Nested
    @DisplayName("Apply Cash Flow Tests")
    class ApplyCashFlowTest {

        @Test
        @DisplayName("Increase RDN when a positive delta is successfully applied")
        void should_increaseRdn_when_deltaIsPositive() {
            // Given
            var currentRdn = brokerAccount.rdn();
            var delta = Money.of(new BigDecimal("15000"));

            // When
            brokerAccount.applyCashFlow(delta);

            // Then
            assertThat(brokerAccount.rdn()).isEqualTo(currentRdn.add(delta));
        }

        @Test
        @DisplayName("Decrease RDN when a negative delta is successfully applied")
        void should_decreaseRdn_when_deltaIsNegative() {
            // Given
            var initialDeposit = Money.of(new BigDecimal("60000"));
            brokerAccount.applyCashFlow(initialDeposit);
            var delta = Money.of(new BigDecimal("-25000"));

            // When
            brokerAccount.applyCashFlow(delta);

            // Then
            assertThat(brokerAccount.rdn()).isEqualTo(Money.of(new BigDecimal("35000")));
        }

        @Test
        @DisplayName("Accumulate the rdn when multiple cash flows are correctly applied")
        void should_accumulateRdn_when_applyMultipleCashFlows() {
            // Given
            Money amount1 = Money.of(new BigDecimal("15000"));
            Money amount2 = Money.of(new BigDecimal("-8000"));
            Money amount3 = Money.of(new BigDecimal("25000"));

            // When
            brokerAccount.applyCashFlow(amount1);
            brokerAccount.applyCashFlow(amount2);
            brokerAccount.applyCashFlow(amount3);

            // Then
            assertThat(brokerAccount.rdn()).isEqualTo(Money.of(new BigDecimal("32000")));
        }

        @Test
        @DisplayName("Decrease RDN to zero when delta equals negative RDN")
        void should_allowRdnToReachZero_when_deltaEqualsNegativeRdn() {
            // Given
            var currentRdn = Money.of(new BigDecimal("25000"));
            brokerAccount.applyCashFlow(currentRdn);
            var delta = Money.of(new BigDecimal("-25000"));

            // When
            brokerAccount.applyCashFlow(delta);

            // Then
            assertThat(brokerAccount.rdn()).isEqualTo(Money.zero());
        }

        @Test
        @DisplayName("Do nothing when delta is zero")
        void should_doNothing_when_deltaIsZero() {
            // Given
            var currentRdn = brokerAccount.rdn();

            // When
            brokerAccount.applyCashFlow(Money.zero());

            // Then
            assertThat(brokerAccount.rdn()).isEqualTo(currentRdn);
        }

        @Test
        @DisplayName("Reject applying cash flow when delta is null")
        void should_throwNPE_when_deltaIsNull() {
            // When & Then
            assertThatThrownBy(() -> brokerAccount.applyCashFlow(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Reject applying cash flow when RDN is insufficient")
        void should_throwInsufficientRdnException_when_rdnIsInsufficient() {
            // Given
            brokerAccount.applyCashFlow(Money.of(new BigDecimal("10000")));

            // When & Then
            assertThatThrownBy(() -> brokerAccount.applyCashFlow(Money.of(new BigDecimal("-25000"))))
                    .isInstanceOf(InsufficientRdnException.class);
        }

        @Test
        @DisplayName("Keep RDN unchanged when applying cash flow is rejected")
        void should_leaveRdnUnchanged_when_applyCashFlowRejected() {
            // Given
            brokerAccount.applyCashFlow(Money.of(new BigDecimal("10000")));
            var currentRdn = brokerAccount.rdn();

            // When
            assertThatThrownBy(() -> brokerAccount.applyCashFlow(Money.of(new BigDecimal("-20000"))))
                    .isInstanceOf(InsufficientRdnException.class);

            // Then
            assertThat(brokerAccount.rdn()).isEqualTo(currentRdn);
        }
    }

}
