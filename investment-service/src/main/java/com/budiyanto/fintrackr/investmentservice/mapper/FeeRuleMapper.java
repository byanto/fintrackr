package com.budiyanto.fintrackr.investmentservice.mapper;

import com.budiyanto.fintrackr.investmentservice.domain.FeeRule;
import com.budiyanto.fintrackr.investmentservice.dto.FeeRuleResponse;

import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FeeRuleMapper {
    FeeRuleResponse toResponseDto(FeeRule feeRule);
    List<FeeRuleResponse> toResponseDtoList(List<FeeRule> feeRules);
}
