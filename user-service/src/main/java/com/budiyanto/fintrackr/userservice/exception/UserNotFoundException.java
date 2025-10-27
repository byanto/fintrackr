package com.budiyanto.fintrackr.userservice.exception;

import lombok.Getter;

@Getter
public class UserNotFoundException extends RuntimeException {
    private String username;

    public UserNotFoundException(String username) {
        super("Username '" + username + "' is not found.");
        this.username = username;
    }
}
