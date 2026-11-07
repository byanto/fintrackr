package com.budiyanto.fintrackr.portfolio.application.port.in;

import com.budiyanto.fintrackr.portfolio.domain.model.PortfolioId;
import com.budiyanto.fintrackr.portfolio.domain.model.Transaction;
import com.budiyanto.fintrackr.portfolio.domain.model.TransactionId;

public interface GetTransactionUseCase {

    Transaction handle(PortfolioId portfolioId, TransactionId transactionId);

}
