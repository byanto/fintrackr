package com.budiyanto.fintrackr.brokerage.adapter.out.persistence;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "broker_accounts")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
class BrokerAccountJpaEntity {
    @Version
    @Column(name = "version")
    private Long version;

    @Id
    @Column(name = "id")
    private UUID id;

    @Setter(AccessLevel.PACKAGE)
    @Column(name = "name")
    private String name;

    @Setter(AccessLevel.PACKAGE)
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "rdn_amount")),
            @AttributeOverride(name = "currency", column = @Column(name = "rdn_currency"))
    })
    private MoneyEmbeddable rdn;

    @Setter(AccessLevel.PACKAGE)
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "buyRate", column = @Column(name = "fee_structure_buy_rate")),
            @AttributeOverride(name = "sellRate", column = @Column(name = "fee_structure_sell_rate"))
    })
    private FeeStructureEmbeddable feeStructure;

    BrokerAccountJpaEntity(UUID id) {
        this.id = id;
    }

}
