package com.budiyanto.fintrackr.investmentservice.app.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.budiyanto.fintrackr.investmentservice.api.dto.CreateInstrumentRequest;
import com.budiyanto.fintrackr.investmentservice.api.dto.InstrumentResponse;
import com.budiyanto.fintrackr.investmentservice.domain.Instrument;

@Mapper(componentModel = "spring")
public interface InstrumentMapper {

    Instrument toInstrument(CreateInstrumentRequest request);
    Instrument toInstrument(InstrumentResponse response);
    InstrumentResponse toResponseDto(Instrument instrument);
    List<InstrumentResponse> toResponseDtoList(List<Instrument> instruments);

}
