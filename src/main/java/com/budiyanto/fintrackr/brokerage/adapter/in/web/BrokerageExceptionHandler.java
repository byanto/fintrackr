package com.budiyanto.fintrackr.brokerage.adapter.in.web;

import com.budiyanto.fintrackr.brokerage.application.exception.BrokerAccountNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class BrokerageExceptionHandler {

    @ExceptionHandler(BrokerAccountNotFoundException.class)
    ProblemDetail handleBrokerAccountNotFoundException(BrokerAccountNotFoundException ex) {
        ProblemDetail body = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                ex.getMessage()
        );
        body.setTitle("Broker Account Not Found");
        return body;
    }

}
