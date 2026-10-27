package com.budiyanto.fintrackr.portfolio.adapter.in.web;

import com.budiyanto.fintrackr.portfolio.application.exception.PortfolioNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class PortfolioExceptionHandler {

    @ExceptionHandler(InvalidRequestException.class)
    ProblemDetail handleInvalidRequestException(InvalidRequestException ex) {
        ProblemDetail body = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );
        body.setTitle("Invalid Request");
        return body;
    }

    @ExceptionHandler(PortfolioNotFoundException.class)
    ProblemDetail handlePortfolioNotFoundException(PortfolioNotFoundException ex) {
        ProblemDetail body = ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND,
            ex.getMessage()
        );
        body.setTitle("Portfolio Not Found");
        return body;
    }
}
