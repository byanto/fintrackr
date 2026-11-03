package com.budiyanto.fintrackr.portfolio.adapter.in.web;

import com.budiyanto.fintrackr.portfolio.application.port.in.GetPortfolioUseCase;
import com.budiyanto.fintrackr.portfolio.application.port.in.PortfolioDetails;
import com.budiyanto.fintrackr.portfolio.domain.model.PortfolioId;
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
class GetPortfolioController {

    private final GetPortfolioUseCase getPortfolioUseCase;

    @GetMapping("/{id}")
    ResponseEntity<PortfolioResponse> getPortfolio(@PathVariable UUID id) {
        PortfolioResponse response = toResponse(getPortfolioUseCase.handle(PortfolioId.of(id)));
        return ResponseEntity.ok(response);
    }

    private PortfolioResponse toResponse(PortfolioDetails details) {
        return new PortfolioResponse(
                details.id(),
                details.brokerAccountId(),
                details.name(),
                details.tradingBalance()
        );
    }
}
