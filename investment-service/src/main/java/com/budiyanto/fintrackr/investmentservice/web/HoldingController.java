package com.budiyanto.fintrackr.investmentservice.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.budiyanto.fintrackr.investmentservice.dto.HoldingResponse;
import com.budiyanto.fintrackr.investmentservice.service.HoldingService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class HoldingController {

    private final HoldingService holdingService;

    @GetMapping("/holdings/{id}")
    public HoldingResponse retrieveHoldingById(@PathVariable Long id) {
        return holdingService.retrieveHoldingById(id);
    }

    @GetMapping("/portfolios/{portfolioId}/holdings")
    public List<HoldingResponse> retrieveHoldingsByPortfolioId(@PathVariable Long portfolioId) {
        return holdingService.retrieveHoldingsByPortfolioId(portfolioId);
    }


}
