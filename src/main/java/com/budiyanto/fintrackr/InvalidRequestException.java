package com.budiyanto.fintrackr;

public class InvalidRequestException extends RuntimeException {
    public InvalidRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
