package com.budiyanto.fintrackr.brokerage.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.budiyanto.fintrackr.brokerage.application.exception.BrokerAccountNotFoundException;
import com.budiyanto.fintrackr.brokerage.application.port.in.BrokerAccountDetails;
import com.budiyanto.fintrackr.brokerage.domain.model.BrokerAccount;
import com.budiyanto.fintrackr.brokerage.domain.model.FeeStructure;
import com.budiyanto.fintrackr.brokerage.domain.model.Percentage;
import com.budiyanto.fintrackr.shared.BrokerAccountId;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("GetBrokerAccountService Tests")
class GetBrokerAccountServiceTest {

    private final InMemoryBrokerAccountRepository repository = new InMemoryBrokerAccountRepository();
    private final GetBrokerAccountService getBrokerAccountService = new GetBrokerAccountService(repository);

    @Test
    @DisplayName("Return account when account exists")
    void should_returnAccount_when_accountExists() {
        // Given
        BrokerAccount brokerAccount = BrokerAccount.create(
                "Test Broker Account",
                new FeeStructure(Percentage.of(new BigDecimal("0.0015")), Percentage.of(new BigDecimal("0.0025"))));

        repository.save(brokerAccount);

        // When
        BrokerAccountDetails result = getBrokerAccountService.handle(brokerAccount.id());

        // Then
        assertThat(result.id()).isEqualTo(brokerAccount.id().value());
        assertThat(result.name()).isEqualTo(brokerAccount.name());
        assertThat(result.rdn()).isEqualTo(brokerAccount.rdn().amount());
        assertThat(result.buyRate()).isEqualTo(brokerAccount.feeStructure().buyRate().rate());
        assertThat(result.sellRate()).isEqualTo(brokerAccount.feeStructure().sellRate().rate());
    }

    @Test
    @DisplayName("Throw broker account not found when account does not exist")
    void should_throwBrokerAccountNotFound_when_accountDoesNotExist() {
        // When & Then
        assertThatThrownBy(() -> getBrokerAccountService.handle(BrokerAccountId.generate()))
                .isInstanceOf(BrokerAccountNotFoundException.class)
                .hasMessageContaining("Broker account not found");
    }
}
