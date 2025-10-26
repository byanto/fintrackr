package com.budiyanto.fintrackr.investmentservice.service;

import com.budiyanto.fintrackr.investmentservice.domain.BrokerAccount;
import com.budiyanto.fintrackr.investmentservice.domain.FeeRule;
import com.budiyanto.fintrackr.investmentservice.dto.CreateFeeRuleRequest;
import com.budiyanto.fintrackr.investmentservice.dto.FeeRuleResponse;
import com.budiyanto.fintrackr.investmentservice.dto.UpdateFeeRuleRequest;
import com.budiyanto.fintrackr.investmentservice.exception.BrokerAccountNotFoundException;
import com.budiyanto.fintrackr.investmentservice.exception.FeeRuleNotFoundException;
import com.budiyanto.fintrackr.investmentservice.mapper.FeeRuleMapper;
import com.budiyanto.fintrackr.investmentservice.repository.BrokerAccountRepository;
import com.budiyanto.fintrackr.investmentservice.repository.FeeRuleRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FeeRuleService {

    private final FeeRuleRepository feeRuleRepository;
    private final BrokerAccountRepository brokerAccountRepository;
    private final FeeRuleMapper feeRuleMapper;

    @Transactional
    public FeeRuleResponse createFeeRule(CreateFeeRuleRequest request) {
        BrokerAccount brokerAccount = brokerAccountRepository.findById(request.brokerAccountId())
                .orElseThrow(() -> new BrokerAccountNotFoundException(request.brokerAccountId()));

        FeeRule feeRule = new FeeRule(
                brokerAccount,
                request.instrumentType(),
                request.tradeType(),
                request.feePercentage(),
                request.minFee()
        );

        FeeRule savedFeeRule = feeRuleRepository.save(feeRule);
        return feeRuleMapper.toResponseDto(savedFeeRule);
    }

    @Transactional(readOnly = true)
    public FeeRuleResponse retrieveFeeRuleById(Long id) {
        return feeRuleRepository.findById(id)
                .map(feeRuleMapper::toResponseDto)
                .orElseThrow(() -> new FeeRuleNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<FeeRuleResponse> retrieveAllFeeRules() {
        List<FeeRule> feeRules = feeRuleRepository.findAll();
        return feeRuleMapper.toResponseDtoList(feeRules);
    }

    @Transactional
    public FeeRuleResponse updateFeeRule(Long id, UpdateFeeRuleRequest request) {
        FeeRule feeRule = feeRuleRepository.findById(id)
                .orElseThrow(() -> new FeeRuleNotFoundException(id));

        feeRule.updateFees(request.feePercentage(), request.minFee());

        FeeRule updatedFeeRule = feeRuleRepository.save(feeRule);
        return feeRuleMapper.toResponseDto(updatedFeeRule);
    }

    @Transactional
    public void deleteFeeRule(Long id) {
        feeRuleRepository.deleteById(id);
    }
}
