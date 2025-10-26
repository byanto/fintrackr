package com.budiyanto.fintrackr.investmentservice.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.budiyanto.fintrackr.investmentservice.domain.Instrument;
import com.budiyanto.fintrackr.investmentservice.dto.CreateInstrumentRequest;
import com.budiyanto.fintrackr.investmentservice.dto.InstrumentResponse;
import com.budiyanto.fintrackr.investmentservice.dto.UpdateInstrumentRequest;
import com.budiyanto.fintrackr.investmentservice.exception.InstrumentNotFoundException;
import com.budiyanto.fintrackr.investmentservice.mapper.InstrumentMapper;
import com.budiyanto.fintrackr.investmentservice.repository.InstrumentRepository;

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

    @Transactional(readOnly = true)
    public InstrumentResponse retrieveInstrumentById(Long id) {
        Instrument retrievedInstrument = instrumentRepository.findById(id)
            .orElseThrow(() -> new InstrumentNotFoundException(id));
        return instrumentMapper.toResponseDto(retrievedInstrument);
    }

    @Transactional(readOnly = true)
    public List<InstrumentResponse> retrieveAllInstruments() {
        List<Instrument> allInstruments = instrumentRepository.findAll();
        return instrumentMapper.toResponseDtoList(allInstruments);
    }

    @Transactional  
    public InstrumentResponse updateInstrument(Long id, UpdateInstrumentRequest request) {
        Instrument retrievedInstrument = instrumentRepository.findById(id)
            .orElseThrow(() -> new InstrumentNotFoundException(id));
        retrievedInstrument.setCode(request.code());
        retrievedInstrument.setName(request.name());
        Instrument updatedInstrument = instrumentRepository.save(retrievedInstrument);
        return instrumentMapper.toResponseDto(updatedInstrument);
    }

    @Transactional
    public void deleteInstrumentById(Long id) {
        instrumentRepository.deleteById(id);
    }

}
