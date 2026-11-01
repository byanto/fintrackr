package com.budiyanto.fintrackr.brokerage.adapter.in.web;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.fromMethodCall;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import com.budiyanto.fintrackr.InvalidRequestException;
import com.budiyanto.fintrackr.brokerage.application.port.in.CreateBrokerAccountCommand;
import com.budiyanto.fintrackr.brokerage.application.port.in.CreateBrokerAccountUseCase;
import com.budiyanto.fintrackr.brokerage.domain.model.FeeStructure;
import com.budiyanto.fintrackr.brokerage.domain.model.Percentage;
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
@RequestMapping("/api/broker-accounts")
@RequiredArgsConstructor
class CreateBrokerAccountController {

    private final CreateBrokerAccountUseCase createBrokerAccountUseCase;

    @PostMapping
    ResponseEntity<Void> createBrokerAccount(@Valid @RequestBody CreateBrokerAccountRequest request) {
        BrokerAccountId brokerAccountId = createBrokerAccountUseCase.handle(toCommand(request));
        URI location = fromMethodCall(on(GetBrokerAccountController.class).getBrokerAccount(brokerAccountId.value()))
                .build().toUri();
        return ResponseEntity.created(location).build();
    }

    private CreateBrokerAccountCommand toCommand(CreateBrokerAccountRequest request) {
        try {
            FeeStructure feeStructure = FeeStructure.of(Percentage.of(request.buyRate()), Percentage.of(request.sellRate()));
            return new CreateBrokerAccountCommand(request.name(), feeStructure);
        } catch (IllegalArgumentException ex) {
            throw new InvalidRequestException(ex.getMessage(), ex);
        }
    }
}
