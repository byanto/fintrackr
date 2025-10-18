package com.budiyanto.fintrackr.investmentservice.api.advice;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.budiyanto.fintrackr.investmentservice.app.exception.InsufficientHoldingsException;
import com.budiyanto.fintrackr.investmentservice.app.exception.HoldingNotFoundException;
import com.budiyanto.fintrackr.investmentservice.app.exception.InstrumentNotFoundException;
import com.budiyanto.fintrackr.investmentservice.app.exception.PortfolioNotFoundException;
import com.budiyanto.fintrackr.investmentservice.app.exception.TradeNotFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PortfolioNotFoundException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public Map<String, String> handlePortfolioNotFound(PortfolioNotFoundException ex) {
        return Map.of("error", ex.getMessage());
    }

    @ExceptionHandler(InstrumentNotFoundException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public Map<String, String> handleInstrumentNotFound(InstrumentNotFoundException ex) {
        return Map.of("error", ex.getMessage());
    }

    @ExceptionHandler(TradeNotFoundException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public Map<String, String> handleTradeNotFound(TradeNotFoundException ex) {
        return Map.of("error", ex.getMessage());
    }

    @ExceptionHandler(InsufficientHoldingsException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public Map<String, String> handleInsufficientHoldings(InsufficientHoldingsException ex) {
        return Map.of("error", ex.getMessage());
    }

    @ExceptionHandler(HoldingNotFoundException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public Map<String, String> handleHoldingNotFound(HoldingNotFoundException ex) {
        return Map.of("error", ex.getMessage());
    }
}
