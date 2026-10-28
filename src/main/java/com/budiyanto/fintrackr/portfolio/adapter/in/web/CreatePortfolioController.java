package com.budiyanto.fintrackr.portfolio.adapter.in.web;

import com.budiyanto.fintrackr.portfolio.application.port.in.CreatePortfolioCommand;
import com.budiyanto.fintrackr.portfolio.application.port.in.CreatePortfolioUseCase;
import com.budiyanto.fintrackr.portfolio.domain.model.PortfolioId;
import com.budiyanto.fintrackr.shared.BrokerAccountId;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/portfolios")
@RequiredArgsConstructor
class CreatePortfolioController {

    private final CreatePortfolioUseCase createPortfolioUseCase;

    @PostMapping
    ResponseEntity<Void> createPortfolio(@Valid @RequestBody CreatePortfolioRequest request) {
        PortfolioId portfolioId = createPortfolioUseCase.handle(toCommand(request));
        URI location = URI.create("/api/portfolios/" + portfolioId.value());
        return ResponseEntity.created(location).build();
    }

    private CreatePortfolioCommand toCommand(CreatePortfolioRequest request) {
        return new CreatePortfolioCommand(
                BrokerAccountId.of(request.brokerAccountId()),
                request.name()
        );
    }
}
