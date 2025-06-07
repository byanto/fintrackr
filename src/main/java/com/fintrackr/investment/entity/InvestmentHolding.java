package com.fintrackr.investment.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

@Entity
@Data
public abstract class InvestmentHolding {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false, unique = true)
	private Long id;
	
	@Column(name = "name", nullable = false, unique = true)
	private String name; // e.g., "BBCA Stock Holding", "Bank Mandiri Deposito Account"
	
	@Column(name = "totalInvestedAmount", nullable = false)
	private Double totalInvestedAmount; // Aggregated from transactions
	
	@Column(name = "currentValue")
	private Double currentValue;
	
	@Column(name = "createdDate", nullable = false)
	@Setter(AccessLevel.NONE)
	private LocalDateTime createdAt;
	
	@Column(name = "lastUpdatedDate", nullable = false)
	private LocalDateTime lastUpdatedAt; // When the holding was last updated or affected by a transaction
	
	@PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
