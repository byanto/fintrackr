package com.budiyanto.fintrackr.portfolio.domain.model;

import com.budiyanto.fintrackr.shared.AssetId;
import com.budiyanto.fintrackr.shared.Money;
import com.budiyanto.fintrackr.shared.Quantity;

import java.time.LocalDate;
import java.util.Objects;

public class Acquisition {

    private final AcquisitionId id;
    private final PortfolioId portfolioId;
    private final AssetId assetId;
    private final LocalDate openDate;
    private final Money openPrice;
    private final Money openFee;
    private final Quantity initialQuantity;

    private Acquisition(AcquisitionId id, PortfolioId portfolioId, AssetId assetId, LocalDate openDate, Money openPrice, Money openFee, Quantity initialQuantity) {
        this.id = id;
        this.portfolioId = portfolioId;
        this.assetId = assetId;
        this.openDate = openDate;
        this.openPrice = openPrice;
        this.openFee = openFee;
        this.initialQuantity = initialQuantity;
    }

    public static Acquisition create(PortfolioId portfolioId, AssetId assetId, LocalDate openDate, Money openPrice, Money openFee, Quantity initialQuantity) {
        return new Acquisition(AcquisitionId.generate(), portfolioId, assetId, openDate, openPrice, openFee, initialQuantity);
    }

    public AcquisitionId id() { return id; }

    public PortfolioId portfolioId() { return portfolioId; }

    public AssetId assetId() { return assetId; }

    public LocalDate openDate() { return openDate; }

    public Money openPrice() { return openPrice; }

    public Money openFee() { return openFee; }

    public Quantity initialQuantity() { return initialQuantity; }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Acquisition that = (Acquisition) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
