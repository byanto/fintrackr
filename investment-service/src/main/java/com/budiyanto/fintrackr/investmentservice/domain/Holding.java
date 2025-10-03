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
     * Updates the holding based on a new trade, recalculating quantity and average price.
     * @param trade The trade to apply.
     */
    public void applyTrade(Trade trade) {
        if (trade.getTradeType() == TradeType.BUY) {
            BigDecimal newTotalValue = (this.averagePrice.multiply(this.quantity))
                    .add(trade.getPrice().multiply(trade.getQuantity()));
            BigDecimal newQuantity = this.quantity.add(trade.getQuantity());
            
            this.quantity = newQuantity;
            // Avoid division by zero if quantity becomes zero after a sell.
            if (newQuantity.compareTo(BigDecimal.ZERO) == 0) {
                this.averagePrice = BigDecimal.ZERO;
            } else {
                this.averagePrice = newTotalValue.divide(newQuantity, 4, RoundingMode.HALF_UP);
            }
        } else if (trade.getTradeType() == TradeType.SELL) {
            // Average price does not change on a sell, only quantity is reduced.
            BigDecimal newQuantity = this.quantity.subtract(trade.getQuantity());
            if (newQuantity.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Cannot sell more than the current holding quantity.");
            }
            this.quantity = newQuantity;
        }
    }

}
