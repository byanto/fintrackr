package com.fintrackr.investment.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Data;

@Entity
@Data
@Builder
public class Stock {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false, unique = true)
	private Long id;
	
	@Column(name = "ticker", nullable = false, unique = true)
	private String ticker;
	
	@Column(name = "name")
	private String companyName;
	
	@Column(name = "sector")
	private String sector;
	
	@Column(name = "exchange")
	private String exchange; // e.g., "IDX", "NYSE"
	
	@Column(name = "currentPrice")
	private Double currentPrice;
	
	@Column(name = "lastPriceUpdatedAt")
	private LocalDateTime lastPriceUpdatedAt;
	
}