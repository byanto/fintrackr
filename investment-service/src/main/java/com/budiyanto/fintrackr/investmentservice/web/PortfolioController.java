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

import com.budiyanto.fintrackr.investmentservice.dto.CreatePortfolioRequest;
import com.budiyanto.fintrackr.investmentservice.dto.PortfolioResponse;
import com.budiyanto.fintrackr.investmentservice.dto.UpdatePortfolioRequest;
import com.budiyanto.fintrackr.investmentservice.service.PortfolioService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/portfolios")
@RequiredArgsConstructor
public class PortfolioController {

    private final PortfolioService portfolioService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PortfolioResponse createPortfolio(@Valid @RequestBody CreatePortfolioRequest request) {
        return portfolioService.createPortfolio(request);
    }

    @GetMapping("/{id}")
    public PortfolioResponse retrievePortfolioById(@PathVariable Long id) {
        return portfolioService.retrievePortfolioById(id);
    }
    
    @GetMapping
    public List<PortfolioResponse> retrieveAllPortfolios() {
        return portfolioService.retrieveAllPortfolios();
    }

    @PutMapping("/{id}")
    public PortfolioResponse updatePortfolio(@PathVariable Long id, @Valid @RequestBody UpdatePortfolioRequest request) {
        return portfolioService.updatePortfolio(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePortfolioById(@PathVariable Long id) {
        portfolioService.deletePortfolioById(id);
    }

}
