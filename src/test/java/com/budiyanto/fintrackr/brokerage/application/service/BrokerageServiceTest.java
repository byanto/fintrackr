package com.budiyanto.fintrackr.brokerage.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.budiyanto.fintrackr.brokerage.BrokerAccountNotFoundException;
import com.budiyanto.fintrackr.brokerage.application.port.out.BrokerAccountRepository;
import com.budiyanto.fintrackr.brokerage.domain.exception.InsufficientRdnException;
import com.budiyanto.fintrackr.brokerage.domain.model.BrokerAccount;
import com.budiyanto.fintrackr.brokerage.domain.model.FeeStructure;
import com.budiyanto.fintrackr.brokerage.domain.model.Percentage;
import com.budiyanto.fintrackr.shared.BrokerAccountId;
import com.budiyanto.fintrackr.shared.Money;
import com.budiyanto.fintrackr.shared.Quantity;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("BrokerageService Tests")
class BrokerageServiceTest {

    private final BrokerAccountRepository brokerAccountRepository = new InMemoryBrokerAccountRepository();
    private final BrokerageService brokerageService = new BrokerageService(brokerAccountRepository);

    private final FeeStructure feeStructure = FeeStructure.of(Percentage.of(new BigDecimal("0.0015")), Percentage.of(new BigDecimal("0.0025")));
    private final BrokerAccount brokerAccount = BrokerAccount.create("Test BrokerAccount", feeStructure);
    private final Quantity quantity = Quantity.ofShares(new BigDecimal("1000"));
    private final Money price = Money.of(new BigDecimal("3500"));

    @BeforeEach
    void setup() {
        brokerAccountRepository.save(brokerAccount);
    }

    @Nested
    @DisplayName("ComputeBuyFee Tests")
    class ComputeBuyFeeTest {
        @Test
        @DisplayName("Return the fee computed by the broker account when computeBuyFee")
        void should_returnFeeComputedByBrokerAccount_when_computeBuyFee() {
            // When
            Money buyFee = brokerageService.computeBuyFee(brokerAccount.id(), quantity, price);

            // Then
            assertThat(buyFee.amount()).isEqualByComparingTo(new BigDecimal("5250")); // 3500 * 1000 * 0.0015 = 5250
        }

        @Test
        @DisplayName("Reject computeBuyFee when the broker account is not found")
        void should_throwException_when_computeBuyFeeOnUnknownAccount() {
            // Given
            BrokerAccountId unknownId = BrokerAccountId.generate();

            // When & Then
            assertThatThrownBy(() -> brokerageService.computeBuyFee(unknownId, quantity, price))
                    .isInstanceOf(BrokerAccountNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("ApplyCashFlow Tests")
    class ApplyCashFlowTest {
        @Test
        @DisplayName("Persist the changed RDN when applyCashFlow")
        void should_persistChangedRdn_when_applyCashFlow() {
            // When
            brokerageService.applyCashFlow(brokerAccount.id(), Money.of(new BigDecimal("150000")));

            BrokerAccount updatedBrokerAccount = brokerAccountRepository.findById(brokerAccount.id()).orElseThrow();

            // Then
            assertThat(updatedBrokerAccount.rdn()).isEqualTo(Money.of(new BigDecimal("150000")));
        }

        @Test
        @DisplayName("Reject applyCashFlow when the broker account is not found")
        void should_throwException_when_applyCashFlowOnUnknownAccount() {
            // Given
            BrokerAccountId unknownId = BrokerAccountId.generate();

            // When & Then
            assertThatThrownBy(
                    () -> brokerageService.applyCashFlow(unknownId, Money.of(new BigDecimal("150000"))))
                    .isInstanceOf(BrokerAccountNotFoundException.class);

        }

        @Test
        @DisplayName("Leave the stored RDN unchanged when the delta exceeds it")
        void should_leaveStoredRdnUnchanged_when_deltaExceedsRdn() {
            // Given
            BrokerAccount account = BrokerAccount.reconstitute(
                    BrokerAccountId.generate(), "Test Account", Money.of(new BigDecimal("100000")), feeStructure);
            brokerAccountRepository.save(account);

            // When & Then
            assertThatThrownBy(
                    () -> brokerageService.applyCashFlow(account.id(), Money.of(new BigDecimal("-300000"))))
                    .isInstanceOf(InsufficientRdnException.class);

            BrokerAccount updatedBrokerAccount = brokerAccountRepository.findById(account.id()).orElseThrow();
            assertThat(updatedBrokerAccount.rdn()).isEqualTo(Money.of(new BigDecimal("100000")));
        }
    }
}
