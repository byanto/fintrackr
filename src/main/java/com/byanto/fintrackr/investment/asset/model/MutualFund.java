package com.byanto.fintrackr.investment.asset.model;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "mutual_fund")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // For JPA use only
public class MutualFund extends Asset{
	
	private String fundManager;
	private Double currentNAV;
	private LocalDate lunchDate;
	private Double AUM;
	
	
}
