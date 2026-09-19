package com.budiyanto.fintrackr.brokerage.adapter.out.persistence;

import com.budiyanto.fintrackr.brokerage.domain.model.BrokerAccount;
import com.budiyanto.fintrackr.brokerage.domain.model.FeeStructure;
import com.budiyanto.fintrackr.brokerage.domain.model.Percentage;
import com.budiyanto.fintrackr.shared.BrokerAccountId;
import com.budiyanto.fintrackr.shared.Money;
import java.util.Currency;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class BrokerAccountPersistenceMapper {

    BrokerAccount toDomain(BrokerAccountJpaEntity entity) {
        BrokerAccountId id = new BrokerAccountId(entity.getId());
        String name = entity.getName();
        Money rdn = Money.of(entity.getRdn().getAmount(), Currency.getInstance(entity.getRdn().getCurrency()));
        FeeStructure feeStructure = FeeStructure.of(Percentage.of(entity.getFeeStructure().getBuyRate()), Percentage.of(entity.getFeeStructure().getSellRate()));
        return BrokerAccount.reconstitute(id, name, rdn, feeStructure);
    }

    BrokerAccountJpaEntity toEntity(BrokerAccount domain) {
        UUID id = domain.id().value();
        String name = domain.name();
        MoneyEmbeddable rdn = new MoneyEmbeddable(domain.rdn().amount(), domain.rdn().currency().getCurrencyCode());
        FeeStructureEmbeddable feeStructure = new FeeStructureEmbeddable(domain.feeStructure().buyRate().rate(), domain.feeStructure().sellRate().rate());
        return new BrokerAccountJpaEntity(id, name, rdn, feeStructure);
    }

}
