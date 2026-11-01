package com.budiyanto.fintrackr.brokerage.adapter.in.web;

import com.budiyanto.fintrackr.brokerage.application.port.in.BrokerAccountDetails;
import com.budiyanto.fintrackr.brokerage.application.port.in.GetBrokerAccountUseCase;
import com.budiyanto.fintrackr.shared.BrokerAccountId;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/broker-accounts")
@RequiredArgsConstructor
class GetBrokerAccountController {

    private final GetBrokerAccountUseCase getBrokerAccountUseCase;

    @GetMapping("/{id}")
    ResponseEntity<BrokerAccountResponse> getBrokerAccount(@PathVariable UUID id) {
        BrokerAccountResponse response = toResponse(getBrokerAccountUseCase.handle(BrokerAccountId.of(id)));
        return ResponseEntity.ok(response);
    }

    private BrokerAccountResponse toResponse(BrokerAccountDetails details) {
        return new BrokerAccountResponse(
                details.id(),
                details.name(),
                details.rdn(),
                details.buyRate(),
                details.sellRate()
        );
    }

}
