package com.budiyanto.fintrackr.userservice.exception;

import lombok.Getter;

@Getter
public class RefreshTokenExpiredException extends RuntimeException {
    
    private String tokenValue;
    
    public RefreshTokenExpiredException(String tokenValue) {
        super(String.format("Failed for [%s]: %s", 
            tokenValue, "Refresh token was expired. Please make a new sign-in request"));
        this.tokenValue = tokenValue;
    }

}