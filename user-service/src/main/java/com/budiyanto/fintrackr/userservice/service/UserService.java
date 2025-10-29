package com.budiyanto.fintrackr.userservice.service;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.budiyanto.fintrackr.userservice.domain.Role;
import com.budiyanto.fintrackr.userservice.domain.User;
import com.budiyanto.fintrackr.userservice.dto.LoginRequest;
import com.budiyanto.fintrackr.userservice.dto.LoginResponse;
import com.budiyanto.fintrackr.userservice.dto.RegisterRequest;
import com.budiyanto.fintrackr.userservice.dto.UserResponse;
import com.budiyanto.fintrackr.userservice.exception.RoleNotFoundException;
import com.budiyanto.fintrackr.userservice.exception.UserAlreadyExistsException;
import com.budiyanto.fintrackr.userservice.mapper.AuthMapper;
import com.budiyanto.fintrackr.userservice.mapper.UserMapper;
import com.budiyanto.fintrackr.userservice.repository.RoleRepository;
import com.budiyanto.fintrackr.userservice.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final AuthMapper authMapper;

    @Transactional
    public UserResponse registerUser(RegisterRequest request) {
        if (userRepository.findByUsername(request.username()).isPresent()) {
            throw new UserAlreadyExistsException(request.username());
        }

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RoleNotFoundException("Default role ROLE_USER not found."));

        User user = userMapper.toUser(request);
        user.addRole(userRole);

        User savedUser = userRepository.save(user);

        return userMapper.toUserResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public LoginResponse authenticate(LoginRequest request) {
    
        User authenticatedUser = userRepository.findByUsername(request.username())
                .filter(user -> passwordEncoder.matches(request.password(), user.getPassword()))
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

        return authMapper.toLoginResponse(authenticatedUser);
    }
}
