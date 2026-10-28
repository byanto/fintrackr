package com.budiyanto.fintrackr.portfolio.application.port.in;

import com.budiyanto.fintrackr.shared.BrokerAccountId;

public record CreatePortfolioCommand(
    BrokerAccountId brokerAccountId,
    String name
) { }
