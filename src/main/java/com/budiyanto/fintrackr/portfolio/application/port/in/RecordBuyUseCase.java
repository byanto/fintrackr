package com.budiyanto.fintrackr.portfolio.application.port.in;

import com.budiyanto.fintrackr.portfolio.domain.model.TransactionId;

public interface RecordBuyUseCase {

    TransactionId handle(RecordBuyCommand command);

}
