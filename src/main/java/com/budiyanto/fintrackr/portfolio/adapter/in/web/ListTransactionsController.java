package com.budiyanto.fintrackr.portfolio.adapter.in.web;

import com.budiyanto.fintrackr.portfolio.application.port.in.ListTransactionsUseCase;
import com.budiyanto.fintrackr.portfolio.domain.model.PortfolioId;
import com.budiyanto.fintrackr.portfolio.domain.model.Transaction;
import java.util.List;
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
class ListTransactionsController {

    private final ListTransactionsUseCase listTransactionsUseCase;

    @GetMapping("/{pid}/transactions")
    ResponseEntity<TransactionListResponse> listTransactions(@PathVariable("pid") UUID portfolioId) {
        TransactionListResponse transactionListResponse = toTransactionListResponse(
                listTransactionsUseCase.handle(PortfolioId.of(portfolioId)));
        return ResponseEntity.ok(transactionListResponse);
    }

    private TransactionListResponse toTransactionListResponse(List<Transaction> items) {
        List<TransactionResponse> transactionResponseList = items.stream()
                .map(TransactionResponse::from).toList();
        return new TransactionListResponse(transactionResponseList);
    }

}
