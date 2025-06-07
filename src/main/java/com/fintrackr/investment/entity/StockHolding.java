package com.fintrackr.investment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

public class StockHolding extends InvestmentHolding {

	@ManyToOne // Many StockHoldings can refer to one Stock
	@JoinColumn(name = "stockId", nullable = false) // Foreign key column in stock_holdings table
	@Column(name = "stock", nullable = false)
	private Stock stock; // Reference to the Stock entity

	@Column(name = "totalShares", nullable = false)
	private Long totalShares; // Current total shares held
	
	@Column(name = "averageCostPerShare", nullable = false)
	private Double averageCostPerShare; // Calculated average cost
	
	@Enumerated(EnumType.STRING)
    @Column(name = "securitiesCompany", nullable = false)
	private SecuritiesCompany securitiesCompany;
	
}
