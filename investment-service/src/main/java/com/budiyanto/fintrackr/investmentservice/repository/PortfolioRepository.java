package com.budiyanto.fintrackr.investmentservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.budiyanto.fintrackr.investmentservice.domain.Portfolio;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

}
