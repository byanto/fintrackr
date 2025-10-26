package com.budiyanto.fintrackr.investmentservice.web;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.budiyanto.fintrackr.investmentservice.dto.CreateTradeRequest;
import com.budiyanto.fintrackr.investmentservice.dto.TradeResponse;
import com.budiyanto.fintrackr.investmentservice.service.TradeService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/trades")
@RequiredArgsConstructor
public class TradeController {

    private final TradeService tradeService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TradeResponse createTrade(@Valid @RequestBody CreateTradeRequest request) {
        return tradeService.createTrade(request);
    }

    @GetMapping("/{id}")
    public TradeResponse retrieveTradeById(@PathVariable Long id) {
        return tradeService.retrieveTradeById(id);
    }

    @GetMapping
    public List<TradeResponse> retrieveAllTrades() {
        return tradeService.retrieveAllTrades();
    }

}
