package com.budiyanto.fintrackr.investmentservice.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "holding", uniqueConstraints = @UniqueConstraint(columnNames = {"portfolio_id", "instrument_id"}))
@Getter
@NoArgsConstructor(force = true)
public class Holding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false, updatable = false)
    private final Portfolio portfolio;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instrument_id", nullable = false, updatable = false)
    private final Instrument instrument;

    @Column(name = "quantity", nullable = false)
    private BigDecimal quantity; // This should be mutable via business methods, not a generic setter

    @Column(name = "average_price", nullable = false)
    private BigDecimal averagePrice; // This should be mutable via business methods, not a generic setter

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Holding(Portfolio portfolio, Instrument instrument, BigDecimal quantity, BigDecimal averagePrice) {
        this.portfolio = portfolio;
        this.instrument = instrument;
        this.quantity = quantity;
        this.averagePrice = averagePrice;
    }

    /**
     * Adds a buy transaction to the holding, updating the quantity and recalculating the average price.
     * @param quantity The quantity of the instrument bought.
     * @param price The price per unit of the instrument bought.
     */
    public void add(BigDecimal quantity, BigDecimal price) {
        BigDecimal currentTotalValue = this.quantity.multiply(this.averagePrice);
        BigDecimal tradeTotalValue = quantity.multiply(price);
        BigDecimal newTotalValue = currentTotalValue.add(tradeTotalValue);
        BigDecimal newTotalQuantity = this.quantity.add(quantity);

        this.quantity = newTotalQuantity;
        this.averagePrice = newTotalValue.divide(newTotalQuantity, 4, RoundingMode.HALF_UP);
    }

    /**
     * Subtracts a specified quantity from the holding.
     * 
     * @param quantity The quantity to be subtracted.
     */
    public void subtract(BigDecimal quantity) {
        this.quantity = this.quantity.subtract(quantity);
    }

}
