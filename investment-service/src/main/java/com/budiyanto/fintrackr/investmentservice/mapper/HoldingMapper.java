package com.budiyanto.fintrackr.investmentservice.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.budiyanto.fintrackr.investmentservice.domain.Holding;
import com.budiyanto.fintrackr.investmentservice.dto.HoldingResponse;

@Mapper(componentModel = "spring")
public interface HoldingMapper {

    @Mapping(target = "portfolioId", source = "portfolio.id")
    // No mapping needed for `instrument` field, MapStruct handles it automatically
    HoldingResponse toResponseDto(Holding holding);

    List<HoldingResponse> toResponseDtoList(List<Holding> holdings);
}