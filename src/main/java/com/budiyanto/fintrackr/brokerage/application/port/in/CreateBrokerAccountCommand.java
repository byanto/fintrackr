package com.budiyanto.fintrackr.brokerage.application.port.in;

import com.budiyanto.fintrackr.brokerage.domain.model.FeeStructure;

public record CreateBrokerAccountCommand(
        String name,
        FeeStructure feeStructure
) { }
