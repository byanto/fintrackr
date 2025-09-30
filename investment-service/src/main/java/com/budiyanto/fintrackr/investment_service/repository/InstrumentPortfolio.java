package com.budiyanto.fintrackr.investment_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.budiyanto.fintrackr.investment_service.domain.Instrument;

public interface InstrumentPortfolio extends JpaRepository<Instrument, Long> {

}
