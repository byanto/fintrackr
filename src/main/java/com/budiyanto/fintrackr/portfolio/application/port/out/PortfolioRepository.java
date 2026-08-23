package com.budiyanto.fintrackr.portfolio.application.port.out;

import com.budiyanto.fintrackr.portfolio.domain.model.Portfolio;
import com.budiyanto.fintrackr.portfolio.domain.model.PortfolioId;

import java.util.Optional;

public interface PortfolioRepository {

    Optional<Portfolio> findById(PortfolioId id);

    Portfolio save(Portfolio portfolio);

}
