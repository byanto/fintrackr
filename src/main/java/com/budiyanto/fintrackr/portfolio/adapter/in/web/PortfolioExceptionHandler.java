package com.budiyanto.fintrackr.portfolio.adapter.in.web;

import com.budiyanto.fintrackr.portfolio.application.exception.PortfolioNotFoundException;
import com.budiyanto.fintrackr.portfolio.application.exception.UnknownBrokerAccountException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class PortfolioExceptionHandler {

    @ExceptionHandler(PortfolioNotFoundException.class)
    ProblemDetail handlePortfolioNotFoundException(PortfolioNotFoundException ex) {
        ProblemDetail body = ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND,
            ex.getMessage()
        );
        body.setTitle("Portfolio Not Found");
        return body;
    }

    @ExceptionHandler(UnknownBrokerAccountException.class)
    ProblemDetail handleUnknownBrokerAccountException(UnknownBrokerAccountException ex) {
        ProblemDetail body = ProblemDetail.forStatusAndDetail(
            HttpStatus.UNPROCESSABLE_ENTITY,
            ex.getMessage()
        );
        body.setTitle("Unknown Broker Account");
        return body;
    }
}
