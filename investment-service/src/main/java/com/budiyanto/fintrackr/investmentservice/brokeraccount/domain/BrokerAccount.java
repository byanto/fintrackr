package com.budiyanto.fintrackr.investmentservice.brokeraccount.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.budiyanto.fintrackr.investmentservice.brokeraccount.domain.entity.Broker; // Corrected import
import com.budiyanto.fintrackr.investmentservice.brokeraccount.domain.entity.Rdn;
import com.budiyanto.fintrackr.investmentservice.brokeraccount.exception.InvalidBrokerAccountNameException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
public class BrokerAccount {

    private final UUID id;
    private String name;
    private final Broker broker;
    private final Rdn rdn;
    private final List<UUID> portfolioIds;

    private BrokerAccount(UUID id, String name, Broker broker, Rdn rdn) {
        this.id = id;
        this.name = name;
        this.broker = broker;
        this.rdn = rdn;
        this.portfolioIds = new ArrayList<>();
        this.portfolioIds.add(UUID.randomUUID()); // TODO: Use UUID7, Send Domain Event
    }

    public static BrokerAccount create(UUID id, String name, Broker broker, Rdn rdn) {
        validateName(name);
        return new BrokerAccount(id, name, broker, rdn);
    }

    public void updateName(String name) {
        validateName(name);
        this.name = name;
    }

    private static void validateName(String name) {
        if (name.isBlank() || name.length() < 3 || name.length() > 255) {
            throw new InvalidBrokerAccountNameException(name);
        }
    }
}
