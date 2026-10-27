package com.budiyanto.fintrackr.brokerage.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.budiyanto.fintrackr.brokerage.application.port.in.CreateBrokerAccountCommand;
import com.budiyanto.fintrackr.brokerage.domain.model.BrokerAccount;
import com.budiyanto.fintrackr.brokerage.domain.model.FeeStructure;
import com.budiyanto.fintrackr.brokerage.domain.model.Percentage;
import com.budiyanto.fintrackr.shared.BrokerAccountId;
import com.budiyanto.fintrackr.shared.Money;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("CreateBrokerAccountService Tests")
class CreateBrokerAccountServiceTest {

    private final InMemoryBrokerAccountRepository repository = new InMemoryBrokerAccountRepository();
    private final CreateBrokerAccountService service = new CreateBrokerAccountService(repository);

    @Test
    @DisplayName("Store account with zero RDN when account is created")
    void should_storeAccountWithZeroRdn_when_accountIsCreated() {
        // Given
        CreateBrokerAccountCommand command = new CreateBrokerAccountCommand(
                "Test Broker Account",
                new FeeStructure(Percentage.of(new BigDecimal("0.0015")), Percentage.of(new BigDecimal("0.0025"))));

        // When
        BrokerAccountId result = service.handle(command);

        // Then
        BrokerAccount loadedBrokerAccount = repository.findById(result).orElseThrow();
        assertThat(loadedBrokerAccount.name()).isEqualTo("Test Broker Account");
        assertThat(loadedBrokerAccount.rdn()).isEqualTo(Money.zero());
    }

    @Test
    @DisplayName("Return id of stored account when account is created")
    void should_returnIdOfStoredAccount_when_accountIsCreated() {
        // Given
        CreateBrokerAccountCommand command = new CreateBrokerAccountCommand(
                "Test Broker Account",
                new FeeStructure(Percentage.of(new BigDecimal("0.0015")), Percentage.of(new BigDecimal("0.0025"))));

        // When
        BrokerAccountId result = service.handle(command);

        // Then
        BrokerAccount loadedBrokerAccount = repository.findById(result).orElseThrow();
        assertThat(result).isEqualTo(loadedBrokerAccount.id());
    }
}
