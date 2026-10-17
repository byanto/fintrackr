package com.budiyanto.fintrackr.portfolio.adapter.out.persistence;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("BUY")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
class BuyJpaEntity extends TransactionJpaEntity {

    @Column(name = "asset_id")
    private String assetId;

    @Column(name = "quantity")
    private BigDecimal quantity;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "price_amount")),
            @AttributeOverride(name = "currency", column = @Column(name = "price_currency"))
    })
    private MoneyEmbeddable price;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "fee_amount")),
            @AttributeOverride(name = "currency", column = @Column(name = "fee_currency"))
    })
    private MoneyEmbeddable fee;

    @Column(name = "acquisition_id")
    private UUID acquisitionId;

    BuyJpaEntity(UUID id, LocalDate date, String assetId, BigDecimal quantity, MoneyEmbeddable price, MoneyEmbeddable fee, UUID acquisitionId) {
        super(id, date);
        this.assetId = assetId;
        this.quantity = quantity;
        this.price = price;
        this.fee = fee;
        this.acquisitionId = acquisitionId;
    }

}
