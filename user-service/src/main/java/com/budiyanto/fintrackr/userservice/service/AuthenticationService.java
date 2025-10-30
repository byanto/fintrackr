package com.budiyanto.fintrackr.userservice.service;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.budiyanto.fintrackr.userservice.domain.Role;
import com.budiyanto.fintrackr.userservice.domain.User;
import com.budiyanto.fintrackr.userservice.dto.AuthTokenRequest;
import com.budiyanto.fintrackr.userservice.dto.AuthTokenResponse;
import com.budiyanto.fintrackr.userservice.dto.LoginRequest;
import com.budiyanto.fintrackr.userservice.dto.LoginResponse;
import com.budiyanto.fintrackr.userservice.dto.RegisterRequest;
import com.budiyanto.fintrackr.userservice.dto.UserResponse;
import com.budiyanto.fintrackr.userservice.exception.RefreshTokenNotFoundException;
import com.budiyanto.fintrackr.userservice.exception.RoleNotFoundException;
import com.budiyanto.fintrackr.userservice.exception.UserAlreadyExistsException;
import com.budiyanto.fintrackr.userservice.mapper.AuthMapper;
import com.budiyanto.fintrackr.userservice.mapper.UserMapper;
import com.budiyanto.fintrackr.userservice.repository.RoleRepository;
import com.budiyanto.fintrackr.userservice.repository.UserRepository;
import com.budiyanto.fintrackr.userservice.security.JwtUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final AuthMapper authMapper;
    private final RefreshTokenService refreshTokenService;
    private final JwtUtil jwtUtil;

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

    @Transactional
    public AuthTokenResponse renewAuthToken(AuthTokenRequest oldTokenRequest) {
        return refreshTokenService.findByTokenValue(oldTokenRequest.refreshToken())
                .map(refreshTokenService::verifyExpiration) // Throws RefreshTokenExpiredException if expired
                .map(oldToken -> {
                    User user = oldToken.getUser();
                    var newRefreshToken = refreshTokenService.rotateToken(oldToken);
                    var roles = user.getRoles().stream().map(Role::getName).toList();
                    String newAccessToken = jwtUtil.generateAccessToken(user.getUsername(), roles);
                    return new AuthTokenResponse(newAccessToken, newRefreshToken.getValue(), jwtUtil.getAccessTokenExpirationMs());
                })
                .orElseThrow(() -> new RefreshTokenNotFoundException(oldTokenRequest.refreshToken())); // Throws if token was not found
    }
}
