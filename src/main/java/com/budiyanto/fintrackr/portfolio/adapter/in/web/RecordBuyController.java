package com.budiyanto.fintrackr.portfolio.adapter.in.web;

import com.budiyanto.fintrackr.InvalidRequestException;
import com.budiyanto.fintrackr.portfolio.application.port.in.RecordBuyCommand;
import com.budiyanto.fintrackr.portfolio.application.port.in.RecordBuyUseCase;
import com.budiyanto.fintrackr.portfolio.domain.model.PortfolioId;
import com.budiyanto.fintrackr.portfolio.domain.model.TransactionId;
import com.budiyanto.fintrackr.shared.AssetId;
import com.budiyanto.fintrackr.shared.Money;
import com.budiyanto.fintrackr.shared.Quantity;
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
class RecordBuyController {

    private final RecordBuyUseCase recordBuyUseCase;

    @PostMapping("/{pid}/buys")
    ResponseEntity<Void> recordBuy(@PathVariable("pid") UUID portfolioId, @Valid @RequestBody RecordBuyRequest request) {
        TransactionId transactionId = recordBuyUseCase.handle(toCommand(portfolioId, request));
        URI location = URI.create("/api/portfolios/" + portfolioId + "/transactions/" + transactionId.value());
        return ResponseEntity.created(location).build();
    }

    private RecordBuyCommand toCommand(UUID portfolioId, RecordBuyRequest request) {
        try {
            Quantity quantity = switch (request.quantityKind()) {
                case "SHARES" -> Quantity.ofShares(request.quantity());
                case "UNITS" -> Quantity.ofUnits(request.quantity());
                default -> throw new IllegalArgumentException("Invalid quantity kind: \"" +  request.quantityKind() + "\". Allowed kind: SHARES and UNITS");
            };

            Money fee = (request.fee() == null) ? null : Money.of(request.fee());

            return new RecordBuyCommand(
                    PortfolioId.of(portfolioId),
                    AssetId.of(request.isin()),
                    quantity,
                    Money.of(request.price()),
                    fee,
                    request.date()
            );
        } catch (IllegalArgumentException ex) {
            throw new InvalidRequestException(ex.getMessage(), ex);
        }
    }
}
