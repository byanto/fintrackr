package com.budiyanto.fintrackr.brokerage.adapter.in.web;

import java.math.BigDecimal;
import java.util.UUID;

record BrokerAccountResponse (
        UUID id,
        String name,
        BigDecimal rdn,
        BigDecimal buyRate,
        BigDecimal sellRate
) { }
