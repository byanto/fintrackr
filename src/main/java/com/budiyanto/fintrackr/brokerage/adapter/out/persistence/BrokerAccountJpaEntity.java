package com.budiyanto.fintrackr.brokerage.adapter.out.persistence;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "broker_accounts")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
class BrokerAccountJpaEntity {
    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "name")
    private String name;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "rdn_amount")),
            @AttributeOverride(name = "currency", column = @Column(name = "rdn_currency"))
    })
    private MoneyEmbeddable rdn;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "buyRate", column = @Column(name = "fee_structure_buy_rate")),
            @AttributeOverride(name = "sellRate", column = @Column(name = "fee_structure_sell_rate"))
    })
    private FeeStructureEmbeddable feeStructure;

}
