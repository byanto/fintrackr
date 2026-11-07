package com.budiyanto.fintrackr.portfolio.adapter.in.web;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.fromMethodCall;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import com.budiyanto.fintrackr.portfolio.application.port.in.RecordDepositCommand;
import com.budiyanto.fintrackr.portfolio.application.port.in.RecordDepositUseCase;
import com.budiyanto.fintrackr.portfolio.domain.model.PortfolioId;
import com.budiyanto.fintrackr.portfolio.domain.model.TransactionId;
import com.budiyanto.fintrackr.shared.Money;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/portfolios")
@RequiredArgsConstructor
class RecordDepositController {

    private final RecordDepositUseCase recordDepositUseCase;

    @PostMapping("/{pid}/deposits")
    ResponseEntity<Void> recordDeposit(@PathVariable("pid") UUID portfolioId, @Valid @RequestBody RecordDepositRequest request) {
        TransactionId transactionId = recordDepositUseCase.handle(toCommand(portfolioId, request));
        URI location = fromMethodCall(on(GetTransactionController.class).getTransaction(portfolioId, transactionId.value()))
                .build().toUri();
        return ResponseEntity.created(location).build();
    }

    private RecordDepositCommand toCommand(UUID portfolioId, RecordDepositRequest request) {
        return new RecordDepositCommand(
                PortfolioId.of(portfolioId),
                request.date(),
                Money.of(request.amount())
        );
    }
}
