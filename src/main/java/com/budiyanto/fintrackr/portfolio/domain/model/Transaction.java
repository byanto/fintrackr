package com.budiyanto.fintrackr.portfolio.domain.model;

import com.budiyanto.fintrackr.shared.Money;
import java.time.LocalDate;

public sealed interface Transaction permits Deposit, Buy {
    TransactionId id();
    PortfolioId portfolioId();
    LocalDate date();

    /**
     * How this transaction changes the portfolio's trading balance.
     *
     * <p>Positive means money comes into the portfolio, for example a deposit. Negative means money
     * goes out, for example a buy (quantity times price plus fee). Brokerage applies the same value
     * to the RDN balance, so every transaction type must follow this sign convention.
     *
     * <p>The sum of the cash flows of all transactions in a portfolio's ledger equals its trading
     * balance (ADR-0004).
     *
     * @return the signed amount, never null
     */
    Money cashFlow();
}
