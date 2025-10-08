package com.budiyanto.fintrackr.investmentservice.api.advice;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.budiyanto.fintrackr.investmentservice.app.exception.PortfolioNotFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PortfolioNotFoundException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public Map<String, String> handlePortfolioNotFound(PortfolioNotFoundException ex) {
        return Map.of("error", ex.getMessage());
    }
}
