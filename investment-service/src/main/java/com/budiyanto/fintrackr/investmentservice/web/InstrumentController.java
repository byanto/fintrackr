package com.budiyanto.fintrackr.investmentservice.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.budiyanto.fintrackr.investmentservice.dto.CreateInstrumentRequest;
import com.budiyanto.fintrackr.investmentservice.dto.InstrumentResponse;
import com.budiyanto.fintrackr.investmentservice.dto.UpdateInstrumentRequest;
import com.budiyanto.fintrackr.investmentservice.service.InstrumentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/instruments")
@RequiredArgsConstructor
public class InstrumentController {

    private final InstrumentService instrumentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InstrumentResponse createInstrument(@Valid @RequestBody CreateInstrumentRequest request) {
        return instrumentService.createInstrument(request);
    }

    @GetMapping("/{id}")
    public InstrumentResponse retrieveInstrumentById(@PathVariable Long id) {
        return instrumentService.retrieveInstrumentById(id);
    }

    @GetMapping
    public List<InstrumentResponse> retrieveAllInstruments() {
        return instrumentService.retrieveAllInstruments();
    }

    @PutMapping("/{id}")
    public InstrumentResponse updateInstrument(@PathVariable Long id, @Valid @RequestBody UpdateInstrumentRequest request) {
        return instrumentService.updateInstrument(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteInstrumentById(@PathVariable Long id) {
        instrumentService.deleteInstrumentById(id);
    }


}
