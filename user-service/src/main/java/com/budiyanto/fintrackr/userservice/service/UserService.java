package com.budiyanto.fintrackr.userservice.service;

import com.budiyanto.fintrackr.userservice.dto.UserResponse;

public interface UserService {
    UserResponse getUserByUsername(String username);
}
