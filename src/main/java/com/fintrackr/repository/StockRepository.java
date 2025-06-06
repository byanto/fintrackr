package com.fintrackr.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fintrackr.model.Stock;

public interface StockRepository extends JpaRepository<Stock, Long> {

}
