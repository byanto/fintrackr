package com.budiyanto.fintrackr.portfolio.adapter.out.persistence;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("DEPOSIT")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
class DepositJpaEntity extends TransactionJpaEntity {

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "cash_amount")),
            @AttributeOverride(name = "currency", column = @Column(name = "cash_currency"))
    })
    private MoneyEmbeddable amount;

    DepositJpaEntity(UUID id, LocalDate date, MoneyEmbeddable amount) {
        super(id, date);
        this.amount = amount;
    }

}
