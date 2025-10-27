package com.budiyanto.fintrackr.userservice.exception;

import lombok.Getter;

@Getter
public class RefreshTokenExpiredException extends RuntimeException {
    
    private String token;
    
    public RefreshTokenExpiredException(String token) {
        super(String.format("Failed for [%s]: %s", 
            token, "Refresh token was expired. Please make a new sign-in request"));
        this.token = token;
    }

}