package com.fintrackr.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    private Stock stock;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private StockTransactionType type;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "price_per_share", nullable = false)
    private double pricePerShare;

    @Column(name = "fee", nullable = false)
    private double fee;

    @Column(name = "created_at", nullable = false)
    @Setter(AccessLevel.NONE)
    private LocalDateTime createdAt;

    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
