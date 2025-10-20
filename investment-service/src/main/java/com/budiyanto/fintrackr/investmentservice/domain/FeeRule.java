package com.budiyanto.fintrackr.investmentservice.domain;

import java.math.BigDecimal;
import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "fee_rule", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"broker_account_id", "instrument_type", "trade_type"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true) // Only for JPA
public class FeeRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "broker_account_id", nullable = false, updatable = false)
    private final BrokerAccount brokerAccount; // Should be final

    @Enumerated(EnumType.STRING)
    @Column(name = "instrument_type", nullable = false, updatable = false)
    private final InstrumentType instrumentType; // Should be final

    @Enumerated(EnumType.STRING)
    @Column(name = "trade_type", nullable = false, updatable = false)
    private final TradeType tradeType; // Should be final

    @Column(name = "fee_percentage", nullable = false, scale = 4, precision = 5) // e.g., 0.0018 for 0.18%
    private BigDecimal feePercentage;

    @Column(name = "min_fee", nullable = false, scale = 2, precision = 19) // e.g. 10000.00 IDR
    private BigDecimal minFee;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public FeeRule(BrokerAccount brokerAccount, InstrumentType instrumentType, TradeType tradeType, 
            BigDecimal feePercentage, BigDecimal minFee) {
        validateFees(feePercentage, minFee);
        this.brokerAccount = brokerAccount;
        this.instrumentType = instrumentType;
        this.tradeType = tradeType;
        this.feePercentage = feePercentage;
        this.minFee = minFee;
    }

    /**
     * Updates the mutable parts of the fee rule.
     * @param newFeePercentage The new percentage to apply.
     * @param newMinFee The new minimum fee to apply.
     */
    public void updateFees(BigDecimal newFeePercentage, BigDecimal newMinFee) {
        validateFees(newFeePercentage, newMinFee);
        this.feePercentage = newFeePercentage;
        this.minFee = newMinFee;
    }

    /**
     * Validates the provided fee percentage and minimum fee.
     * @param percentage the percentage to be validated
     * @param minFee the minFee to be validated
     */
    private void validateFees(BigDecimal percentage, BigDecimal minFee) {
        if (percentage == null || minFee == null) {
            throw new IllegalArgumentException("Fee percentage and minimum fee cannot be null.");
        }
        if (percentage.compareTo(BigDecimal.ZERO) < 0 || minFee.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Fee percentage and minimum fee cannot be negative.");
        }
    }
}