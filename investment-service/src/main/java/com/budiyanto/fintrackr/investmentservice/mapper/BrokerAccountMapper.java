package com.budiyanto.fintrackr.investmentservice.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.budiyanto.fintrackr.investmentservice.domain.BrokerAccount;
import com.budiyanto.fintrackr.investmentservice.dto.BrokerAccountResponse;
import com.budiyanto.fintrackr.investmentservice.dto.CreateBrokerAccountRequest;

@Mapper(componentModel = "spring")
public interface BrokerAccountMapper {

    BrokerAccount toBrokerAccount(CreateBrokerAccountRequest request);
    BrokerAccountResponse toResponseDto(BrokerAccount brokerAccount);
    List<BrokerAccountResponse> toResponseDtoList(List<BrokerAccount> brokerAccounts);

}
