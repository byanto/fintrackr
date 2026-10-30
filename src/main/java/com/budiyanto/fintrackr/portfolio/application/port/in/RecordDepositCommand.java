package com.budiyanto.fintrackr.portfolio.application.port.in;

import com.budiyanto.fintrackr.portfolio.domain.model.PortfolioId;
import com.budiyanto.fintrackr.shared.Money;
import java.time.LocalDate;

public record RecordDepositCommand(
    PortfolioId portfolioId,
    LocalDate date,
    Money amount
) { }
