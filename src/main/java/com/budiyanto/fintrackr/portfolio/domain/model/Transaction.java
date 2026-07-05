package com.budiyanto.fintrackr.portfolio.domain.model;

import java.time.LocalDate;

public sealed interface Transaction permits Deposit {
    TransactionId id();
    PortfolioId portfolioId();
    LocalDate date();
}
