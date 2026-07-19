package com.budiyanto.fintrackr.portfolio.domain.model;

import com.budiyanto.fintrackr.shared.AssetId;
import com.budiyanto.fintrackr.shared.Money;
import com.budiyanto.fintrackr.shared.Quantity;

import java.time.LocalDate;
import java.util.Objects;

public record Buy(TransactionId id, PortfolioId portfolioId, LocalDate date, AssetId assetId, Quantity quantity, Money price, Money fee, AcquisitionId acquisitionId) implements Transaction{

    public Buy {
        Objects.requireNonNull(id, "id cannot be null");
        Objects.requireNonNull(portfolioId, "portfolioId cannot be null");
        Objects.requireNonNull(assetId, "assetId cannot be null");
        Objects.requireNonNull(quantity, "quantity cannot be null");
        Objects.requireNonNull(price, "price cannot be null");
        Objects.requireNonNull(fee, "fee cannot be null");
        Objects.requireNonNull(date, "date cannot be null");
        Objects.requireNonNull(acquisitionId, "acquisitionId cannot be null");
    }

    public static Buy create(TransactionId id, PortfolioId portfolioId, LocalDate date, AssetId assetId, Quantity quantity, Money price, Money fee, AcquisitionId acquisitionId) {
        return new Buy(id, portfolioId, date, assetId, quantity, price, fee, acquisitionId);
    }

}
