package com.budiyanto.fintrackr.portfolio.application.port.in;

import com.budiyanto.fintrackr.portfolio.domain.model.PortfolioId;
import com.budiyanto.fintrackr.portfolio.domain.model.Transaction;
import java.util.List;

public interface ListTransactionsUseCase {

    List<Transaction> handle(PortfolioId portfolioId);

}
