package com.budiyanto.fintrackr.brokerage.application.port.in;

import com.budiyanto.fintrackr.shared.BrokerAccountId;

public interface CreateBrokerAccountUseCase {

    BrokerAccountId handle(CreateBrokerAccountCommand command);

}
