package com.budiyanto.fintrackr;

import com.budiyanto.fintrackr.shared.DomainException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Nullable
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ProblemDetail body = ex.getBody();
        List<InvalidField> invalidFields = new ArrayList<>();
        for(FieldError error: ex.getBindingResult().getFieldErrors()) {
            invalidFields.add(new InvalidField(error.getField(), error.getDefaultMessage()));
        }
        body.setProperty("errors", invalidFields);

        return handleExceptionInternal(ex, body, headers, status, request);
    }

    @ExceptionHandler(DomainException.class)
    protected ProblemDetail handleDomainException(DomainException ex) {
        ProblemDetail body = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNPROCESSABLE_ENTITY,
                ex.getMessage()
        );
        body.setTitle("Business Rule Violated");
        return body;
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    protected ProblemDetail handleOptimisticLockingFailureException(OptimisticLockingFailureException ex) {
        ProblemDetail body = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                "The data was changed by another request in the meantime. Reload it and try again."
        );
        body.setTitle("Concurrent Update");
        return body;
    }

    private record InvalidField(
            String field,
            String message
    ) {}

}
