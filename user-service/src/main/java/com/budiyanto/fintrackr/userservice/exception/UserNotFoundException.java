package com.budiyanto.fintrackr.userservice.exception;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String message) {
        super(message);        
    }
}
