package com.budiyanto.fintrackr.userservice.exception;

import lombok.Getter;

@Getter
public class UserAlreadyExistsException extends RuntimeException {
    private String username;

    public UserAlreadyExistsException(String username) {
        super("Username '" + username + "' is already taken.");
        this.username = username;
    }
}
