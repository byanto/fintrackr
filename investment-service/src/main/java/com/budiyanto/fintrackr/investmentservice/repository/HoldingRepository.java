package com.budiyanto.fintrackr.investmentservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.budiyanto.fintrackr.investmentservice.domain.Holding;

public interface HoldingRepository extends JpaRepository<Holding, Long> {

}
