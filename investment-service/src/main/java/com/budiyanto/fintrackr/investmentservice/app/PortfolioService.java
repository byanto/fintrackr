package com.budiyanto.fintrackr.investmentservice.app;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.budiyanto.fintrackr.investmentservice.api.dto.CreatePortfolioRequest;
import com.budiyanto.fintrackr.investmentservice.api.dto.PortfolioResponse;
import com.budiyanto.fintrackr.investmentservice.api.dto.UpdatePortfolioRequest;
import com.budiyanto.fintrackr.investmentservice.app.exception.PortfolioNotFoundException;
import com.budiyanto.fintrackr.investmentservice.app.mapper.PortfolioMapper;
import com.budiyanto.fintrackr.investmentservice.domain.Portfolio;
import com.budiyanto.fintrackr.investmentservice.repository.PortfolioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final PortfolioMapper portfolioMapper;

    @Transactional
    public PortfolioResponse createPortfolio(CreatePortfolioRequest request) {
        Portfolio portfolio = portfolioMapper.toPortfolio(request);
        Portfolio savedPortfolio = portfolioRepository.save(portfolio);
        return portfolioMapper.toDto(savedPortfolio);
    }

    @Transactional(readOnly = true)
    public PortfolioResponse retrievePortfolioById(Long id) {
        Portfolio retrievedPortfolio = portfolioRepository.findById(id)
            .orElseThrow(() -> new PortfolioNotFoundException(id));
        return portfolioMapper.toDto(retrievedPortfolio); 
    }

    @Transactional(readOnly = true)
    public List<PortfolioResponse> retrieveAllPortfolios() {
        List<Portfolio> allPortfolios = portfolioRepository.findAll();
        return portfolioMapper.toDtoList(allPortfolios);
    }

    @Transactional
    public PortfolioResponse updatePortfolio(Long id, UpdatePortfolioRequest request) {
        Portfolio existingPortfolio = portfolioRepository.findById(id)
            .orElseThrow(() -> new PortfolioNotFoundException(id));
        existingPortfolio.setName(request.name());
        Portfolio updatedPortfolio = portfolioRepository.save(existingPortfolio);
        return portfolioMapper.toDto(updatedPortfolio);
    }

    @Transactional
    public void deletePortfolioById(Long id) {
        portfolioRepository.deleteById(id);
    }

}
