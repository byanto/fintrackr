package com.byanto.fintrackr.investment.portfolio.dto;

import com.byanto.fintrackr.investment.asset.model.Asset;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Holding {

	private Asset asset;
	private Double totalShares;
	private Double averagePurchasePrice;
}
