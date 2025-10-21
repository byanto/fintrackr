package com.budiyanto.fintrackr.investmentservice.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.budiyanto.fintrackr.investmentservice.TestcontainersConfiguration;
import com.budiyanto.fintrackr.investmentservice.domain.BrokerAccount;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
@DisplayName("BrokerAccountRepository Tests")
class BrokerAccountRepositoryTest {

    private final BrokerAccountRepository brokerAccountRepository;

    @Autowired
    BrokerAccountRepositoryTest(BrokerAccountRepository brokerAccountRepository) {
        this.brokerAccountRepository = brokerAccountRepository;
    }

    @Test
    @DisplayName("should save and retrieve broker account")
    void should_saveAndRetrieveBrokerAccount() {
        // Arrange: Create a new BrokerAccount object
        String name = "My Broker Account";
        String brokerName = "Broker A";
        BrokerAccount brokerAccount = new BrokerAccount(name, brokerName);
        
        // Act: Save the broker account using the repository
        BrokerAccount savedBrokerAccount = brokerAccountRepository.save(brokerAccount);

        // Assert: Verify that the broker account was saved correctly
        assertThat(savedBrokerAccount).isNotNull();
        assertThat(savedBrokerAccount.getId()).isGreaterThan(0);
        assertThat(savedBrokerAccount.getName()).isEqualTo(name);
        assertThat(savedBrokerAccount.getBrokerName()).isEqualTo(brokerName);
        assertThat(savedBrokerAccount.getCreatedAt()).isNotNull();

        // Act: Retrieve the saved broker account
        BrokerAccount retrievedBrokerAccount = brokerAccountRepository.findById(savedBrokerAccount.getId()).get();

        // Assert: Verify that the broker account can be retrieved
        assertThat(retrievedBrokerAccount).isNotNull();
        assertThat(retrievedBrokerAccount.getId()).isEqualTo(savedBrokerAccount.getId());
        assertThat(retrievedBrokerAccount.getName()).isEqualTo(name);
        assertThat(retrievedBrokerAccount.getBrokerName()).isEqualTo(brokerName);
        assertThat(retrievedBrokerAccount.getCreatedAt()).isNotNull();
    }


}
