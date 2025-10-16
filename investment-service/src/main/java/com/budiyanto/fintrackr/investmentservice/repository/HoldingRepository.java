package com.budiyanto.fintrackr.investmentservice.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.budiyanto.fintrackr.investmentservice.domain.Holding;

public interface HoldingRepository extends JpaRepository<Holding, Long> {
    Optional<Holding> findByPortfolioIdAndInstrumentId(Long portfolioId, Long instrumentId);
}
