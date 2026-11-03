package com.budiyanto.fintrackr.portfolio.application.port.in;

import com.budiyanto.fintrackr.portfolio.domain.model.PortfolioId;

public interface GetPortfolioUseCase {

    PortfolioDetails handle(PortfolioId portfolioId);

}
