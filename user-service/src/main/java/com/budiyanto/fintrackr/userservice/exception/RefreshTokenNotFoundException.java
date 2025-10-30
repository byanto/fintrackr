package com.budiyanto.fintrackr.userservice.exception;

import lombok.Getter;

@Getter
public class RefreshTokenNotFoundException extends RuntimeException {

    private String tokenValue;

    public RefreshTokenNotFoundException(String tokenValue) {
        super(String.format("Failed for [%s]: %s", tokenValue, 
            "Refresh token is not in database!"));
        this.tokenValue = tokenValue;
    }
}
