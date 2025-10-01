package com.budiyanto.fintrackr.investmentservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.budiyanto.fintrackr.investmentservice.domain.Instrument;

public interface InstrumentPortfolio extends JpaRepository<Instrument, Long> {

}
