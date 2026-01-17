package com.budiyanto.fintrackr.investmentservice.brokeraccount.domain;

import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenThrownBy;

import com.budiyanto.fintrackr.investmentservice.brokeraccount.domain.entity.Broker;
import com.budiyanto.fintrackr.investmentservice.brokeraccount.domain.entity.Rdn;
import com.budiyanto.fintrackr.investmentservice.brokeraccount.exception.InvalidBrokerAccountNameException;
import com.github.f4b6a3.uuid.UuidCreator;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

@DisplayName("BrokerAccount Tests")
class BrokerAccountTest {

    private static final UUID ID = UuidCreator.getTimeOrderedEpoch();
    private static final String BROKER_ACCOUNT_NAME = "Stockbit Account";
    private static final Broker BROKER = Broker.create("Stockbit", "https://stockbit.com");
    private static final Rdn RDN = Rdn.create("Bank Mandiri", "1234567890", Money.of(1000000, "IDR"));

    @Nested
    @DisplayName("Create method")
    class Create {

        @Test
        @DisplayName("Should create broker account with valid details")
        void given_validDetails_when_creatingBrokerAccount_then_succeed() {
            // When
            var brokerAccount = BrokerAccount.create(ID, BROKER_ACCOUNT_NAME, BROKER, RDN);

            // Then
            // It should create a new BrokerAccount
            then(brokerAccount).isNotNull();
            then(brokerAccount.getId()).isEqualTo(ID);
            then(brokerAccount.getName()).isEqualTo("Stockbit Account");

            // It should associate with a broker
            then(brokerAccount.getBroker().getName()).isEqualTo("Stockbit");
            then(brokerAccount.getBroker().getUrl()).isEqualTo("https://stockbit.com");

            // It should associate with a RDN account
            then(brokerAccount.getRdn().getBankName()).isEqualTo("Bank Mandiri");
            then(brokerAccount.getRdn().getAccountNumber()).isEqualTo("1234567890");

            // It should create a new default portfolio
            then(brokerAccount.getPortfolioIds()).hasSize(1);
        }

        @ParameterizedTest
        @ArgumentsSource(InvalidNameProvider.class)
        @DisplayName("Should fail to create broker account with invalid details")
        void given_invalidName_when_creatingBrokerAccount_then_fail(String name) {
            // Then
            // It should fail to create a new Broker Account
            thenThrownBy(() -> {
                BrokerAccount.create(ID, name, BROKER, RDN);
            }).isInstanceOf(InvalidBrokerAccountNameException.class)
                    .hasMessageContaining("Broker account name is invalid");
        }

    }

    @Nested
    @DisplayName("UpdateName method")
    class UpdateName {

        // Given
        private BrokerAccount brokerAccount = BrokerAccount.create(ID, BROKER_ACCOUNT_NAME, BROKER, RDN);

        @Test
        @DisplayName("Should update broker account name")
        void given_newValidName_when_updateName_then_nameIsUpdated() {

            // When
            var newName = "Stockbit Account Name Updated";
            brokerAccount.updateName(newName);

            // Then
            then(brokerAccount.getName()).isEqualTo(newName);
        }

        @ParameterizedTest
        @ArgumentsSource(InvalidNameProvider.class)
        @DisplayName("Should fail to update broker account name with invalid details")
        void given_invalidNewName_when_updateName_then_fail(String invalidName) {
            thenThrownBy(() -> {
                brokerAccount.updateName(invalidName);
            }).isInstanceOf(InvalidBrokerAccountNameException.class)
                    .hasMessageContaining("Broker account name is invalid");
        }
    }

    private static class InvalidNameProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                    "",
                    "ab",
                    "a".repeat(256)
            ).map(Arguments::of);
        }
    }
}