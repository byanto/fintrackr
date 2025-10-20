package com.budiyanto.fintrackr.investmentservice.app.mapper;

import com.budiyanto.fintrackr.investmentservice.api.dto.FeeRuleResponse;
import com.budiyanto.fintrackr.investmentservice.domain.FeeRule;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FeeRuleMapper {
    FeeRuleResponse toResponseDto(FeeRule feeRule);
    List<FeeRuleResponse> toResponseDtoList(List<FeeRule> feeRules);
}
