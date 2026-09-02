package com.budiyanto.fintrackr.portfolio.domain.model;

import com.budiyanto.fintrackr.portfolio.domain.exception.FutureDatedTransactionException;
import com.budiyanto.fintrackr.portfolio.domain.exception.InsufficientBalanceException;
import com.budiyanto.fintrackr.portfolio.domain.exception.NegativeFeeException;
import com.budiyanto.fintrackr.portfolio.domain.exception.NonPositiveAmountException;
import com.budiyanto.fintrackr.portfolio.domain.exception.NonPositivePriceException;
import com.budiyanto.fintrackr.portfolio.domain.exception.ZeroQuantityException;
import com.budiyanto.fintrackr.shared.AssetId;
import com.budiyanto.fintrackr.shared.BrokerAccountId;
import com.budiyanto.fintrackr.shared.Money;
import com.budiyanto.fintrackr.shared.Quantity;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Portfolio {

    private final PortfolioId id;
    private final BrokerAccountId brokerAccountId;
    private String name;
    private Money tradingBalance;
    private final List<Transaction> transactions;
    private final List<Acquisition> acquisitions;

    private Portfolio(PortfolioId id, BrokerAccountId brokerAccountId, String name, Money tradingBalance,
                      List<Transaction> transactions, List<Acquisition> acquisitions) {
        Objects.requireNonNull(id, "id cannot be null");
        Objects.requireNonNull(brokerAccountId, "brokerAccountId cannot be null");
        Objects.requireNonNull(name, "name cannot be null");
        Objects.requireNonNull(tradingBalance, "tradingBalance cannot be null");
        Objects.requireNonNull(transactions, "transactions cannot be null");
        Objects.requireNonNull(acquisitions, "acquisitions cannot be null");

        this.id = id;
        this.brokerAccountId = brokerAccountId;
        this.name = name;
        this.tradingBalance = tradingBalance;
        this.transactions = new ArrayList<>(transactions);
        this.acquisitions = new ArrayList<>(acquisitions);
    }

    public static Portfolio create(BrokerAccountId brokerAccountId, String name) {
        validateName(name);
        return new Portfolio(PortfolioId.generate(), brokerAccountId, name, Money.zero(), new ArrayList<>(), new ArrayList<>());
    }

    public static Portfolio reconstitute(PortfolioId id, BrokerAccountId brokerAccountId, String name, Money tradingBalance, List<Transaction> transactions, List<Acquisition> acquisitions) {
        return new Portfolio(id, brokerAccountId, name, tradingBalance, transactions, acquisitions);
    }

    public void recordDeposit(Money amount, LocalDate date, LocalDate today) {
        Objects.requireNonNull(amount, "amount cannot be null");
        Objects.requireNonNull(date, "date cannot be null");
        Objects.requireNonNull(today, "today cannot be null");

        if (amount.isZeroOrNegative()) {
            throw new NonPositiveAmountException(amount);
        }

        if (date.isAfter(today)) {
            throw new FutureDatedTransactionException(date, today);
        }

        Transaction deposit = Deposit.create(TransactionId.generate(), id, date, amount);
        transactions.add(deposit);

        tradingBalance = tradingBalance.add(amount);
    }

    public Money recordBuy(AssetId assetId, Quantity quantity, Money price, Money fee, LocalDate date, LocalDate today) {
        Objects.requireNonNull(assetId, "assetId cannot be null");
        Objects.requireNonNull(quantity, "quantity cannot be null");
        Objects.requireNonNull(price, "price cannot be null");
        Objects.requireNonNull(fee, "fee cannot be null");
        Objects.requireNonNull(date, "date cannot be null");
        Objects.requireNonNull(today, "today cannot be null");

        if (quantity.isZero()) {
            throw new ZeroQuantityException(quantity);
        }

        if (price.isZeroOrNegative()) {
            throw new NonPositivePriceException(price);
        }

        if (fee.isNegative()) {
            throw new NegativeFeeException(fee);
        }

        if (date.isAfter(today)) {
            throw new FutureDatedTransactionException(date, today);
        }

        Money costDelta = Money.of(quantity.value().multiply(price.amount()).add(fee.amount()), price.currency()).negate();

        Money endBalance = tradingBalance.add(costDelta);
        if (endBalance.isNegative()) {
            throw new InsufficientBalanceException(tradingBalance, costDelta.negate());
        }

        Acquisition acquisition = Acquisition.create(id, assetId, date, price, fee, quantity);
        acquisitions.add(acquisition);

        Transaction buy = Buy.create(TransactionId.generate(), id, date, assetId, quantity, price, fee, acquisition.id());
        transactions.add(buy);

        tradingBalance = endBalance;
        return costDelta;
    }

    public PortfolioId id() { return id; }

    public String name() { return name; }

    public BrokerAccountId brokerAccountId() { return brokerAccountId; }

    public Money tradingBalance() { return tradingBalance; }

    public List<Transaction> transactions() { return List.copyOf(transactions); }

    public List<Acquisition> acquisitions() { return List.copyOf(acquisitions); }

    private static void validateName(String name) {
        Objects.requireNonNull(name, "name cannot be null");
        if (name.isBlank()) {
            throw new IllegalArgumentException("name cannot be blank");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Portfolio portfolio = (Portfolio) o;
        return Objects.equals(id, portfolio.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
