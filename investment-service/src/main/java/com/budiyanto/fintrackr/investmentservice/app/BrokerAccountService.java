package com.budiyanto.fintrackr.investmentservice.app;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.budiyanto.fintrackr.investmentservice.api.dto.BrokerAccountResponse;
import com.budiyanto.fintrackr.investmentservice.api.dto.CreateBrokerAccountRequest;
import com.budiyanto.fintrackr.investmentservice.api.dto.UpdateBrokerAccountRequest;
import com.budiyanto.fintrackr.investmentservice.app.exception.BrokerAccountNotFoundException;
import com.budiyanto.fintrackr.investmentservice.app.mapper.BrokerAccountMapper;
import com.budiyanto.fintrackr.investmentservice.domain.BrokerAccount;
import com.budiyanto.fintrackr.investmentservice.repository.BrokerAccountRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BrokerAccountService {

    private final BrokerAccountRepository brokerAccountRepository;
    private final BrokerAccountMapper brokerAccountMapper;

    @Transactional
    public BrokerAccountResponse createBrokerAccount(CreateBrokerAccountRequest request) {
        BrokerAccount brokerAccount = brokerAccountMapper.toBrokerAccount(request);
        BrokerAccount savedBrokerAccount = brokerAccountRepository.save(brokerAccount);
        return brokerAccountMapper.toResponseDto(savedBrokerAccount);
    }

    @Transactional(readOnly = true)
    public BrokerAccountResponse retrieveBrokerAccountById(Long id) {
        BrokerAccount brokerAccount = brokerAccountRepository.findById(id)
            .orElseThrow(() -> new BrokerAccountNotFoundException(id));
        return brokerAccountMapper.toResponseDto(brokerAccount);
    }

    @Transactional(readOnly = true)
    public List<BrokerAccountResponse> retrieveAllBrokerAccounts() {
        List<BrokerAccount> brokerAccounts = brokerAccountRepository.findAll();
        return brokerAccountMapper.toResponseDtoList(brokerAccounts);
    }

    @Transactional
    public BrokerAccountResponse updateBrokerAccount(Long id, UpdateBrokerAccountRequest request) {
        BrokerAccount brokerAccount = brokerAccountRepository.findById(id)
            .orElseThrow(() -> new BrokerAccountNotFoundException(id));
        brokerAccount.setName(request.name());
        brokerAccount.setBrokerName(request.brokerName());
        BrokerAccount savedBrokerAccount = brokerAccountRepository.save(brokerAccount);
        return brokerAccountMapper.toResponseDto(savedBrokerAccount);
    }

    @Transactional
    public void deleteBrokerAccountById(Long id) {
        brokerAccountRepository.deleteById(id);
    }    

}
