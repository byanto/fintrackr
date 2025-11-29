package com.budiyanto.fintrackr.userservice.service;

import com.budiyanto.fintrackr.userservice.dto.UserResponse;
import com.budiyanto.fintrackr.userservice.dto.UserUpdateRequest;

public interface UserService {

    UserResponse getUserByUsername(String username);

    UserResponse updateUser(String username, UserUpdateRequest request);
    
}
