package com.budiyanto.fintrackr.investmentservice.app.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.budiyanto.fintrackr.investmentservice.api.dto.HoldingResponse;
import com.budiyanto.fintrackr.investmentservice.domain.Holding;

@Mapper(componentModel = "spring")
public interface HoldingMapper {

    @Mapping(target = "portfolioId", source = "portfolio.id")
    // No mapping needed for `instrument` field, MapStruct handles it automatically
    HoldingResponse toResponseDto(Holding holding);

    List<HoldingResponse> toResponseDtoList(List<Holding> holdings);
}