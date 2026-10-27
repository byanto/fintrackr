package com.budiyanto.fintrackr.portfolio.adapter.in.web;

class InvalidRequestException extends RuntimeException {
    InvalidRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
