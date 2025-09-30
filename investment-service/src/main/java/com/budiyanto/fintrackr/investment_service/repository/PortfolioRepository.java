package com.budiyanto.fintrackr.investment_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.budiyanto.fintrackr.investment_service.domain.Portfolio;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

}
