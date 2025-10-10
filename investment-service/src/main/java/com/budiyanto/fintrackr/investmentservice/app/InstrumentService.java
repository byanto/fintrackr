package com.budiyanto.fintrackr.investmentservice.app;

import org.springframework.stereotype.Service;

import com.budiyanto.fintrackr.investmentservice.api.dto.CreateInstrumentRequest;
import com.budiyanto.fintrackr.investmentservice.api.dto.InstrumentResponse;
import com.budiyanto.fintrackr.investmentservice.app.mapper.InstrumentMapper;
import com.budiyanto.fintrackr.investmentservice.domain.Instrument;
import com.budiyanto.fintrackr.investmentservice.repository.InstrumentRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InstrumentService {

    private final InstrumentRepository instrumentRepository;
    private final InstrumentMapper instrumentMapper;

    @Transactional
    public InstrumentResponse createInstrument(CreateInstrumentRequest request) {
        Instrument instrument = instrumentMapper.toInstrument(request);
        Instrument savedInstrument = instrumentRepository.save(instrument);
        return instrumentMapper.toResponseDto(savedInstrument);
    }

}
