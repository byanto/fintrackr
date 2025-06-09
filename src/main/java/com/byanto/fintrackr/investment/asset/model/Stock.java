package com.byanto.fintrackr.investment.asset.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "stock")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // For JPA use only
public class Stock extends Asset {
	
	@Column(name = "ticker", nullable = false, unique = true) // Each stock ticker should be unique
	private String ticker;
	
	@Column(name = "sector", nullable = false)
	private String sector;
	
	@Column(name = "exchange", nullable = false)
	private String exchange;
	
	@Column(name = "current_price", nullable = false)
	private Double currentPrice;
	
	@Column(name = "last_price_updated_at", nullable = false)
	private LocalDateTime lastPriceUpdatedAt;
	
}
