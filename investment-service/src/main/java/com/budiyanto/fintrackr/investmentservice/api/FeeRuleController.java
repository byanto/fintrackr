package com.budiyanto.fintrackr.investmentservice.api;

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

import com.budiyanto.fintrackr.investmentservice.api.dto.CreateFeeRuleRequest;
import com.budiyanto.fintrackr.investmentservice.api.dto.FeeRuleResponse;
import com.budiyanto.fintrackr.investmentservice.api.dto.UpdateFeeRuleRequest;
import com.budiyanto.fintrackr.investmentservice.app.FeeRuleService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/fee-rules")
@RequiredArgsConstructor
public class FeeRuleController {

    private final FeeRuleService feeRuleService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FeeRuleResponse createFeeRule(@Valid @RequestBody CreateFeeRuleRequest request) {
        return feeRuleService.createFeeRule(request); 
    }

    @GetMapping("/{id}")
    public FeeRuleResponse retrieveFeeRuleById(@PathVariable Long id) {
        return feeRuleService.retrieveFeeRuleById(id);
    }

    @GetMapping
    public List<FeeRuleResponse> retrieveAllFeeRules() {
        return feeRuleService.retrieveAllFeeRules();
    }

    @PutMapping("/{id}")
    public FeeRuleResponse updateFeeRule(@PathVariable Long id, @Valid @RequestBody UpdateFeeRuleRequest request) {
        return feeRuleService.updateFeeRule(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFeeRule(@PathVariable Long id) {
        feeRuleService.deleteFeeRule(id);
    }
}
