package com.budiyanto.fintrackr.investmentservice.app.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.budiyanto.fintrackr.investmentservice.api.dto.BrokerAccountResponse;
import com.budiyanto.fintrackr.investmentservice.api.dto.CreateBrokerAccountRequest;
import com.budiyanto.fintrackr.investmentservice.domain.BrokerAccount;

@Mapper(componentModel = "spring")
public interface BrokerAccountMapper {

    BrokerAccount toBrokerAccount(CreateBrokerAccountRequest request);
    BrokerAccountResponse toResponseDto(BrokerAccount brokerAccount);
    List<BrokerAccountResponse> toResponseDtoList(List<BrokerAccount> brokerAccounts);

}
