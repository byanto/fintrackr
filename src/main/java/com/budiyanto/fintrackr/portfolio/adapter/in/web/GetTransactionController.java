package com.budiyanto.fintrackr.portfolio.adapter.in.web;

import com.budiyanto.fintrackr.portfolio.application.port.in.GetTransactionUseCase;
import com.budiyanto.fintrackr.portfolio.domain.model.PortfolioId;
import com.budiyanto.fintrackr.portfolio.domain.model.TransactionId;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/portfolios")
@RequiredArgsConstructor
class GetTransactionController {

    private final GetTransactionUseCase getTransactionUseCase;

    @GetMapping("/{pid}/transactions/{tid}")
    ResponseEntity<TransactionResponse> getTransaction(@PathVariable("pid") UUID portfolioId, @PathVariable("tid") UUID transactionId) {
        TransactionResponse response = TransactionResponse.from(
                getTransactionUseCase.handle(PortfolioId.of(portfolioId), TransactionId.of(transactionId)));
        return ResponseEntity.ok(response);
    }

}
