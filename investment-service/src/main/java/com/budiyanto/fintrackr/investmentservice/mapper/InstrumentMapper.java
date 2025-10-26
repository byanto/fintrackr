package com.budiyanto.fintrackr.investmentservice.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.budiyanto.fintrackr.investmentservice.domain.Instrument;
import com.budiyanto.fintrackr.investmentservice.dto.CreateInstrumentRequest;
import com.budiyanto.fintrackr.investmentservice.dto.InstrumentResponse;

@Mapper(componentModel = "spring")
public interface InstrumentMapper {

    Instrument toInstrument(CreateInstrumentRequest request);
    Instrument toInstrument(InstrumentResponse response);
    InstrumentResponse toResponseDto(Instrument instrument);
    List<InstrumentResponse> toResponseDtoList(List<Instrument> instruments);

}
