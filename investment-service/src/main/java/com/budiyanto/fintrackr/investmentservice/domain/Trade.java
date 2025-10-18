package com.budiyanto.fintrackr.investmentservice.domain;

import java.math.BigDecimal;
import java.time.Instant;

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
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "trade")
@Getter
@NoArgsConstructor(force = true)
public class Trade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id; // ID cannot be final as it's generated in DB

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false, updatable = false)
    private final Portfolio portfolio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instrument_id", nullable = false, updatable = false)
    private final Instrument instrument;

    @Enumerated(EnumType.STRING)
    @Column(name = "trade_type", nullable = false, updatable = false)
    private final TradeType tradeType;

    @Column(name = "quantity", nullable = false, updatable = false)
    private final BigDecimal quantity;

    @Column(name = "price", nullable = false, updatable = false)
    private final BigDecimal price;

    /*
    @Column(name = "fee", nullable = false, updatable = false)
    private final BigDecimal fee;
     */
    
    @Column(name = "traded_at", nullable = false, updatable = false)
    private final Instant tradedAt;

    public Trade(Portfolio portfolio, Instrument instrument, TradeType tradeType, BigDecimal quantity, BigDecimal price,
            Instant tradedAt) {
        this.portfolio = portfolio;
        this.instrument = instrument;
        this.tradeType = tradeType;
        this.quantity = quantity;
        this.price = price;
        // this.fee = fee;
        this.tradedAt = tradedAt;
    }
    
}
