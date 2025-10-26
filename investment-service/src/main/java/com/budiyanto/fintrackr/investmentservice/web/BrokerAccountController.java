package com.budiyanto.fintrackr.investmentservice.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.budiyanto.fintrackr.investmentservice.dto.BrokerAccountResponse;
import com.budiyanto.fintrackr.investmentservice.dto.CreateBrokerAccountRequest;
import com.budiyanto.fintrackr.investmentservice.dto.UpdateBrokerAccountRequest;
import com.budiyanto.fintrackr.investmentservice.service.BrokerAccountService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/broker-accounts")
@RequiredArgsConstructor
public class BrokerAccountController {

    private final BrokerAccountService brokerAccountService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BrokerAccountResponse createBrokerAccount(@Valid @RequestBody CreateBrokerAccountRequest request){
        return brokerAccountService.createBrokerAccount(request);
    }

    @GetMapping("/{id}")
    public BrokerAccountResponse retrieveBrokerAccountById(@PathVariable Long id) {
        return brokerAccountService.retrieveBrokerAccountById(id);
    }

    @GetMapping
    public List<BrokerAccountResponse> retrieveAllBrokerAccounts() {
        return brokerAccountService.retrieveAllBrokerAccounts();
    }

    @PutMapping("/{id}")
    public BrokerAccountResponse updateBrokerAccount(@PathVariable Long id, @Valid @RequestBody UpdateBrokerAccountRequest request) {
        return brokerAccountService.updateBrokerAccount(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBrokerAccountById(@PathVariable Long id) {
        brokerAccountService.deleteBrokerAccountById(id);
    }

}
