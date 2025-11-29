package com.budiyanto.fintrackr.userservice.exception;

import java.net.URI;
import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ProblemDetail handleUserAlreadyExistsException(UserAlreadyExistsException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problemDetail.setTitle("User Already Exists");
        problemDetail.setType(URI.create("/errors/user-already-exists"));
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;    
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ProblemDetail handleUserNotFoundException(UserNotFoundException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problemDetail.setTitle("User Not Found");
        problemDetail.setType(URI.create("/errors/user-not-found"));
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    @ExceptionHandler(RoleNotFoundException.class)
    public ProblemDetail handleRoleNotFoundException(RoleNotFoundException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problemDetail.setTitle("Role Not Found");
        problemDetail.setType(URI.create("/errors/role-not-found"));
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ProblemDetail handleBadCredentialsException(BadCredentialsException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage());
        problemDetail.setTitle("Invalid Credentials");
        problemDetail.setType(URI.create("/errors/invalid-credentials"));
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ProblemDetail handleInvalidRefreshTokenException(InvalidRefreshTokenException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, "The provided refresh token is invalid, not found, or expired.");
        problemDetail.setTitle("Invalid Refresh Token");
        problemDetail.setType(URI.create("/errors/invalid-refresh-token"));
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolationException(ConstraintViolationException ex) {
        Map<String, String> errors = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        violation -> violation.getPropertyPath().toString(),
                        ConstraintViolation::getMessage
                ));

        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setTitle("Validation Failed");
        problemDetail.setDetail("One or more parameters are invalid.");
        problemDetail.setProperty("errors", errors);
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        Map<String, String> errors = ex.getBindingResult().getFieldErrors()
                .stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));

        ProblemDetail problemDetail = ex.getBody();
        problemDetail.setTitle("Validation Failed");
        problemDetail.setDetail("One or more fields have an error.");
        problemDetail.setProperty("errors", errors);
        problemDetail.setProperty("timestamp", Instant.now());

        return handleExceptionInternal(ex, problemDetail, headers, status, request);
    }

}
