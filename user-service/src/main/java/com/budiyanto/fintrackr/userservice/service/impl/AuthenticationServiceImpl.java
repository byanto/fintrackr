package com.budiyanto.fintrackr.userservice.service.impl;

import java.util.List;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.budiyanto.fintrackr.userservice.dto.AuthenticationTokenRequest;
import com.budiyanto.fintrackr.userservice.dto.AuthenticationTokenResponse;
import com.budiyanto.fintrackr.userservice.dto.UserLoginRequest;
import com.budiyanto.fintrackr.userservice.dto.UserLoginResponse;
import com.budiyanto.fintrackr.userservice.dto.UserRegistrationRequest;
import com.budiyanto.fintrackr.userservice.dto.UserResponse;
import com.budiyanto.fintrackr.userservice.entity.RefreshToken;
import com.budiyanto.fintrackr.userservice.entity.Role;
import com.budiyanto.fintrackr.userservice.entity.User;
import com.budiyanto.fintrackr.userservice.exception.RefreshTokenNotFoundException;
import com.budiyanto.fintrackr.userservice.exception.RoleNotFoundException;
import com.budiyanto.fintrackr.userservice.exception.UserAlreadyExistsException;
import com.budiyanto.fintrackr.userservice.mapper.UserMapper;
import com.budiyanto.fintrackr.userservice.repository.RoleRepository;
import com.budiyanto.fintrackr.userservice.repository.UserRepository;
import com.budiyanto.fintrackr.userservice.security.JwtService;
import com.budiyanto.fintrackr.userservice.service.AuthenticationService;
import com.budiyanto.fintrackr.userservice.service.RefreshTokenService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;

    @Override
    @Transactional
    public UserResponse registerUser(UserRegistrationRequest request) {
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

    @Override
    @Transactional
    public UserLoginResponse authenticate(UserLoginRequest request) {
    
        User authenticatedUser = userRepository.findByUsername(request.username())
                .filter(user -> passwordEncoder.matches(request.password(), user.getPassword()))
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

        List<String> roles = authenticatedUser.getRoles().stream().map(Role::getName).toList();
        String accessToken = jwtService.generateAccessToken(authenticatedUser.getUsername(), roles);

        // refreshTokenService.invalidateAllTokensForUser(authenticatedUser);
        String refreshToken = refreshTokenService.createToken(authenticatedUser);

        return userMapper.toLoginResponse(authenticatedUser, accessToken, refreshToken);
    }

    @Override
    @Transactional
    public AuthenticationTokenResponse renewAuthToken(AuthenticationTokenRequest oldTokenRequest) {
        String username = oldTokenRequest.username();
        String rawOldToken = oldTokenRequest.refreshToken();

        User user = userRepository.findByUsername(username)
                        .orElseThrow(() -> new RefreshTokenNotFoundException(rawOldToken));
        
        // Find the specific token entity that matches the raw token sent by the client
        RefreshToken oldToken = refreshTokenService.findByUser(user)
                .stream()
                .filter(token -> passwordEncoder.matches(rawOldToken, token.getValue()))
                .findFirst()
                .orElseThrow(() -> new RefreshTokenNotFoundException(rawOldToken));
        
        // Validate the token is not expired
        refreshTokenService.verifyExpiration(oldToken);

        // Rotate the token: Delete the old one and create a new one
        String newRefreshToken = refreshTokenService.rotateToken(oldToken);

        var roles = user.getRoles().stream().map(Role::getName).toList();
        String newAccessToken = jwtService.generateAccessToken(user.getUsername(), roles);

        return new AuthenticationTokenResponse(newAccessToken, newRefreshToken, jwtService.getAccessTokenExpirationMs());       
    }
}
