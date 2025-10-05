package com.budiyanto.fintrackr.investmentservice.app;

import org.springframework.stereotype.Service;

import com.budiyanto.fintrackr.investmentservice.api.dto.CreatePortfolioRequest;
import com.budiyanto.fintrackr.investmentservice.app.exception.PortfolioNotFoundException;
import com.budiyanto.fintrackr.investmentservice.domain.Portfolio;
import com.budiyanto.fintrackr.investmentservice.repository.PortfolioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;

    public Portfolio createPortfolio(CreatePortfolioRequest request) {
        return portfolioRepository.save(new Portfolio(request.name()));
    }

    public Portfolio retrievePortfolioById(Long portfolioId) {
        return portfolioRepository.findById(portfolioId)
            .orElseThrow(() -> new PortfolioNotFoundException(portfolioId));        
    }

}
