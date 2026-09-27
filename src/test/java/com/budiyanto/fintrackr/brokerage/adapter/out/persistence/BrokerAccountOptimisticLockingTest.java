package com.budiyanto.fintrackr.brokerage.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.budiyanto.fintrackr.TestcontainersConfiguration;
import com.budiyanto.fintrackr.brokerage.domain.model.BrokerAccount;
import com.budiyanto.fintrackr.brokerage.domain.model.FeeStructure;
import com.budiyanto.fintrackr.brokerage.domain.model.Percentage;
import com.budiyanto.fintrackr.shared.Money;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@DataJpaTest
@Import({TestcontainersConfiguration.class, BrokerAccountPersistenceAdapter.class, BrokerAccountPersistenceMapper.class})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class BrokerAccountOptimisticLockingTest {

    private final BrokerAccountPersistenceAdapter adapter;
    private final PlatformTransactionManager transactionManager;

    @Autowired
    BrokerAccountOptimisticLockingTest(BrokerAccountPersistenceAdapter adapter, PlatformTransactionManager transactionManager) {
        this.adapter = adapter;
        this.transactionManager = transactionManager;
    }

    @Test
    @DisplayName("Reject a stale save when a concurrent transaction updated the account first")
    void should_rejectStaleSave_when_concurrentTransactionUpdatedAccountFirst() {
        // Given
        BrokerAccount brokerAccount = savedAccount();

        // When
        TransactionTemplate templateA = new TransactionTemplate(transactionManager);
        TransactionTemplate templateB = new TransactionTemplate(transactionManager);
        templateB.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        assertThatThrownBy(() -> templateA.execute(statusA -> {
            BrokerAccount accountA = adapter.findById(brokerAccount.id()).orElseThrow();
            templateB.execute(statusB -> {
                BrokerAccount accountB = adapter.findById(brokerAccount.id()).orElseThrow();
                accountB.applyCashFlow(Money.of(new BigDecimal("-200000")));
                adapter.save(accountB);
                return null;
            });
            accountA.applyCashFlow(Money.of(new BigDecimal("-300000")));
            adapter.save(accountA);
            return null;
        }))
                .isInstanceOf(OptimisticLockingFailureException.class);

        // Then
        BrokerAccount result = adapter.findById(brokerAccount.id()).orElseThrow();
        assertThat(result.rdn()).isEqualTo(Money.of(new BigDecimal("800000")));

    }

    private BrokerAccount savedAccount() {
        TransactionTemplate setupTemplate = new TransactionTemplate(transactionManager);

        return setupTemplate.execute(status -> {
            FeeStructure feeStructure = FeeStructure.of(Percentage.of(new BigDecimal("0.0015")), Percentage.of(new BigDecimal("0.0025")));
            BrokerAccount brokerAccount = BrokerAccount.create("Test BrokerAccount", feeStructure);
            brokerAccount.applyCashFlow(Money.of(new BigDecimal("1000000")));
            return adapter.save(brokerAccount);
        });
    }

}
