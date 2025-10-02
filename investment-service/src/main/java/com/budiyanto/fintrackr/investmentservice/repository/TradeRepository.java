package com.budiyanto.fintrackr.investmentservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.budiyanto.fintrackr.investmentservice.domain.Trade;

public interface TradeRepository extends JpaRepository<Trade, Long>{

}
