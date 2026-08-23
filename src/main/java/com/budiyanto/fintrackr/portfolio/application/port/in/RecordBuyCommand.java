package com.budiyanto.fintrackr.portfolio.application.port.in;

import com.budiyanto.fintrackr.portfolio.domain.model.PortfolioId;
import com.budiyanto.fintrackr.shared.AssetId;
import com.budiyanto.fintrackr.shared.Money;
import com.budiyanto.fintrackr.shared.Quantity;

import java.time.LocalDate;

public record RecordBuyCommand(
        PortfolioId portfolioId,
        AssetId assetId,
        Quantity quantity,
        Money price,
        Money fee, // null → fee is computed from the broker's FeeStructure
        LocalDate date
) { }
