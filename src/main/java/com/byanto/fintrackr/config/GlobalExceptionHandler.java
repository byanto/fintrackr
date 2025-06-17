package com.byanto.fintrackr.config;

import java.time.Instant;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.byanto.fintrackr.shared.exception.ResourceNotFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<Map<String, String>> handleResourceNotFoundException(ResourceNotFoundException ex) {
		Map<String, String> errorResponse = Map.of(
				"error", ex.getMessage(),
				"timestamp", Instant.now().toString());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
	}
}
