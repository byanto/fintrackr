package com.budiyanto.fintrackr.userservice.web;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.budiyanto.fintrackr.userservice.dto.AuthTokenRequest;
import com.budiyanto.fintrackr.userservice.dto.AuthTokenResponse;
import com.budiyanto.fintrackr.userservice.dto.LoginRequest;
import com.budiyanto.fintrackr.userservice.dto.LoginResponse;
import com.budiyanto.fintrackr.userservice.dto.RegisterRequest;
import com.budiyanto.fintrackr.userservice.dto.UserResponse;
import com.budiyanto.fintrackr.userservice.service.AuthenticationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        return authenticationService.registerUser(registerRequest);
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest loginRequest) {
        return authenticationService.authenticate(loginRequest);
    }

    @PostMapping("/renewtoken")
    @ResponseStatus(HttpStatus.OK)
    public AuthTokenResponse renewAuthToken(@Valid @RequestBody AuthTokenRequest request) {
        return authenticationService.renewAuthToken(request);        
    }

}
