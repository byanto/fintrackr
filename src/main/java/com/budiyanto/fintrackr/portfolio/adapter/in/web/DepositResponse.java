package com.budiyanto.fintrackr.portfolio.adapter.in.web;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

record DepositResponse(UUID id, UUID portfolioId, LocalDate date, BigDecimal amount) implements TransactionResponse { }
