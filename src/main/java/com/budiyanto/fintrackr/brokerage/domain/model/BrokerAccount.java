package com.budiyanto.fintrackr.brokerage.domain.model;

import com.budiyanto.fintrackr.brokerage.domain.exception.InsufficientRdnException;
import com.budiyanto.fintrackr.shared.BrokerAccountId;
import com.budiyanto.fintrackr.shared.Money;
import com.budiyanto.fintrackr.shared.Quantity;

import java.util.Objects;

public class BrokerAccount {

    private final BrokerAccountId id;
    private String name;
    private Money rdn;
    private FeeStructure feeStructure;

    private BrokerAccount(String name, FeeStructure feeStructure) {
        validateName(name);
        Objects.requireNonNull(feeStructure, "feeStructure cannot be null");

        this.id = BrokerAccountId.generate();
        this.name = name;
        this.rdn = Money.zero();
        this.feeStructure = feeStructure;
    }

    public static BrokerAccount create(String name, FeeStructure feeStructure) {
        return new BrokerAccount(name, feeStructure);
    }

    public void rename(String name) {
        validateName(name);
        this.name = name;
    }

    public void applyCashFlow(Money delta) {
        Objects.requireNonNull(delta, "delta cannot be null");
        Money result = rdn.add(delta);
        if (result.isNegative()) {
            throw new InsufficientRdnException(rdn, delta);
        }
        rdn = result;
    }

    public Money computeBuyFee(Quantity quantity, Money price) {
        return feeStructure.computeBuyFee(quantity, price);
    }

    public Money computeSellFee(Quantity quantity, Money price) {
        return feeStructure.computeSellFee(quantity, price);
    }

    private void validateName(String name) {
        Objects.requireNonNull(name, "name cannot be null");
        if (name.isBlank()) {
            throw new IllegalArgumentException("name cannot be blank");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        BrokerAccount that = (BrokerAccount) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    public BrokerAccountId id() { return id; }

    public String name() { return name; }

    public Money rdn() { return rdn; }

    public FeeStructure feeStructure() { return feeStructure; }

}
