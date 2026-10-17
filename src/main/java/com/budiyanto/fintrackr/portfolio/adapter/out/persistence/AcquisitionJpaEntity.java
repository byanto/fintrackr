package com.budiyanto.fintrackr.portfolio.adapter.out.persistence;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "acquisitions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
class AcquisitionJpaEntity {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "asset_id")
    private String assetId;

    @Column(name = "open_date")
    private LocalDate openDate;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "open_price_amount")),
            @AttributeOverride(name = "currency", column = @Column(name = "open_price_currency"))
    })
    private MoneyEmbeddable openPrice;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "open_fee_amount")),
            @AttributeOverride(name = "currency", column = @Column(name = "open_fee_currency"))
    })
    private MoneyEmbeddable openFee;

    @Column(name = "initial_quantity")
    private BigDecimal initialQuantity;

    AcquisitionJpaEntity(UUID id, String assetId, LocalDate openDate, MoneyEmbeddable openPrice, MoneyEmbeddable openFee, BigDecimal initialQuantity) {
        this.id = id;
        this.assetId = assetId;
        this.openDate = openDate;
        this.openPrice = openPrice;
        this.openFee = openFee;
        this.initialQuantity = initialQuantity;
    }
}
