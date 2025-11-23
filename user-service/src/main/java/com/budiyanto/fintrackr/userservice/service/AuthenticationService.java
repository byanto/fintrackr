package com.budiyanto.fintrackr.userservice.service;

import com.budiyanto.fintrackr.userservice.dto.AuthenticationTokenRequest;
import com.budiyanto.fintrackr.userservice.dto.AuthenticationTokenResponse;
import com.budiyanto.fintrackr.userservice.dto.UserLoginRequest;
import com.budiyanto.fintrackr.userservice.dto.UserLoginResponse;
import com.budiyanto.fintrackr.userservice.dto.UserRegistrationRequest;
import com.budiyanto.fintrackr.userservice.dto.UserResponse;

public interface AuthenticationService {

    UserResponse registerUser(UserRegistrationRequest request);

    UserLoginResponse authenticate(UserLoginRequest request);

    AuthenticationTokenResponse renewAuthToken(AuthenticationTokenRequest oldTokenRequest);
    
}
