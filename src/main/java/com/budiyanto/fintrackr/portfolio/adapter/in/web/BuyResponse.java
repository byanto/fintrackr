package com.budiyanto.fintrackr.portfolio.adapter.in.web;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

record BuyResponse(UUID id, UUID portfolioId, LocalDate date, String isin, BigDecimal quantity, BigDecimal price, BigDecimal fee) implements TransactionResponse { }
