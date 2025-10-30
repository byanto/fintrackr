package com.budiyanto.fintrackr.userservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import com.budiyanto.fintrackr.userservice.domain.RefreshToken;
import com.budiyanto.fintrackr.userservice.domain.Role;
import com.budiyanto.fintrackr.userservice.domain.User;
import com.budiyanto.fintrackr.userservice.dto.AuthTokenRequest;
import com.budiyanto.fintrackr.userservice.dto.AuthTokenResponse;
import com.budiyanto.fintrackr.userservice.dto.LoginRequest;
import com.budiyanto.fintrackr.userservice.dto.LoginResponse;
import com.budiyanto.fintrackr.userservice.dto.RegisterRequest;
import com.budiyanto.fintrackr.userservice.dto.UserResponse;
import com.budiyanto.fintrackr.userservice.exception.RefreshTokenExpiredException;
import com.budiyanto.fintrackr.userservice.exception.RefreshTokenNotFoundException;
import com.budiyanto.fintrackr.userservice.exception.RoleNotFoundException;
import com.budiyanto.fintrackr.userservice.exception.UserAlreadyExistsException;
import com.budiyanto.fintrackr.userservice.mapper.AuthMapper;
import com.budiyanto.fintrackr.userservice.mapper.UserMapper;
import com.budiyanto.fintrackr.userservice.repository.RoleRepository;
import com.budiyanto.fintrackr.userservice.repository.UserRepository;
import com.budiyanto.fintrackr.userservice.security.JwtUtil;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthenticationService Tests")
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @Mock
    private AuthMapper authMapper;

    @Mock
    private RefreshTokenService refreshTokenService;
    
    @Mock
    private JwtUtil jwtUtil;
    
    @InjectMocks
    private AuthenticationService authenticationService;

    private User user;
    private RegisterRequest registerRequest;
    private Role userRole;
    private static final Long ACCESS_TOKEN_EXPIRATION_MS = 900000L;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest("testuser", "password123", "test@email.com");
        userRole = new Role("ROLE_USER");
        this.user = new User("testuser", "password", "test@email.com");
    }

    @Nested
    @DisplayName("registerUser method")
    class RegisterUser {

        @Test
        @DisplayName("should register user successfully when username is unique")
        void should_registerUser_when_usernameIsUnique() {
            // Arrange
            Long id = 1L;
            Instant createdAt = Instant.now();
            String encodedPassword = "encodedPassword";
            
            // Mock
            when(userRepository.findByUsername(registerRequest.username())).thenReturn(Optional.empty());
            when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(userRole));
            
            User user = new User(registerRequest.username(), encodedPassword, registerRequest.email());
            when(userMapper.toUser(registerRequest)).thenReturn(user);
            
            User savedUser = new User(registerRequest.username(), "encodedPassword", registerRequest.email());
            ReflectionTestUtils.setField(savedUser, "id", id);
            ReflectionTestUtils.setField(savedUser, "createdAt", createdAt);
            ReflectionTestUtils.setField(savedUser, "updatedAt", createdAt);
            savedUser.addRole(userRole);
            when(userRepository.save(any(User.class))).thenReturn(savedUser);

            UserResponse response = new UserResponse(savedUser.getId(), registerRequest.username(), registerRequest.email(), createdAt);
            when(userMapper.toUserResponse(savedUser)).thenReturn(response);

            // Act
            UserResponse result = authenticationService.registerUser(registerRequest);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(response);

            // Capture the User object passed to save() and assert its properties
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            User capturedUser = userCaptor.getValue();
            assertThat(capturedUser.getId()).isNull();
            assertThat(capturedUser).isEqualTo(user);
            assertThat(capturedUser.getRoles()).contains(userRole);

            // Verify interactions
            verify(userRepository).findByUsername(registerRequest.username());
            verify(roleRepository).findByName("ROLE_USER");
            verify(userMapper).toUser(registerRequest);
            verify(userMapper).toUserResponse(savedUser);
        }

        @Test
        @DisplayName("should throw UserAlreadyExistsException when username is taken")
        void should_throwException_when_usernameIsTaken() {
            // Arrange
            User user = new User(registerRequest.username(), "encodedPassword", registerRequest.email());
            when(userRepository.findByUsername(registerRequest.username())).thenReturn(Optional.of(user));

            // Act & Assert
            assertThatThrownBy(() -> authenticationService.registerUser(registerRequest))
                    .isInstanceOf(UserAlreadyExistsException.class)
                    .asInstanceOf(InstanceOfAssertFactories.type(UserAlreadyExistsException.class))
                    .satisfies(ex -> {
                        assertThat(ex.getUsername()).isEqualTo(registerRequest.username());
                    });

            // Verify further interactions never occured
            verify(roleRepository, never()).findByName("ROLE_USER");
            verify(userMapper, never()).toUser(any(RegisterRequest.class));
            verify(userRepository, never()).save(any(User.class));
            verify(userMapper, never()).toUserResponse(any(User.class));
        }

        @Test
        @DisplayName("should throw RoleNotFoundException when default role is missing")
        void should_throwException_when_defaultRoleIsMissing() {
            // Arrange
            when(userRepository.findByUsername(registerRequest.username())).thenReturn(Optional.empty());
            when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> authenticationService.registerUser(registerRequest))
                    .isInstanceOf(RoleNotFoundException.class)
                    .hasMessageContaining("Default role ROLE_USER not found.");

            // Verify further interactions never occured
            verify(userMapper, never()).toUser(any(RegisterRequest.class));
            verify(userRepository, never()).save(any(User.class));
            verify(userMapper, never()).toUserResponse(any(User.class));
        }
    }

    @Nested
    @DisplayName("authenticate method")
    class Authenticate {

        private LoginRequest loginRequest;
        private User existingUser;
        private final String rawPassword = "password123";
        private final String encodedPassword = "encodedPassword";

        @BeforeEach
        void setUp() {
            loginRequest = new LoginRequest("testuser", rawPassword);
            existingUser = new User("testuser", encodedPassword, "test@email.com");
            ReflectionTestUtils.setField(existingUser, "id", 1L);
            existingUser.addRole(new Role("ROLE_USER"));
        }

        @Test
        @DisplayName("should return LoginResponse on successful authentication")
        void should_returnLoginResponse_when_authenticationSuccess() {
            // Arrange
            when(userRepository.findByUsername(loginRequest.username())).thenReturn(Optional.of(existingUser));
            when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);

            LoginResponse expectedResponse = new LoginResponse(existingUser.getUsername(), List.of("ROLE_USER"));
            when(authMapper.toLoginResponse(existingUser)).thenReturn(expectedResponse);

            // Act
            LoginResponse result = authenticationService.authenticate(loginRequest);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(expectedResponse);

            // Verify interactions
            verify(userRepository).findByUsername(loginRequest.username());
            verify(passwordEncoder).matches(rawPassword, encodedPassword);
            verify(authMapper).toLoginResponse(existingUser);
        }

        @Test
        @DisplayName("should throw BadCredentialsException for non-existent username")
        void should_throwException_when_usernameDoesNotExist() {
            // Arrange
            when(userRepository.findByUsername(loginRequest.username())).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> authenticationService.authenticate(loginRequest))
                    .isInstanceOf(BadCredentialsException.class)
                    .hasMessage("Invalid username or password");

            // Verify further interactions never occured
            verify(passwordEncoder, never()).matches(anyString(), anyString());
            verify(authMapper, never()).toLoginResponse(any(User.class));
        }

        @Test
        @DisplayName("should throw BadCredentialsException for incorrect password")
        void should_throwException_when_passwordIsIncorrect() {
            // Arrange
            when(userRepository.findByUsername(loginRequest.username())).thenReturn(Optional.of(existingUser));
            when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> authenticationService.authenticate(loginRequest))
                    .isInstanceOf(BadCredentialsException.class)
                    .hasMessage("Invalid username or password");

            // Verify further interactions never occured
            verify(authMapper, never()).toLoginResponse(any(User.class));
        }
    }

    @Nested
    @DisplayName("renewAuthToken method")
    class renewAuthToken {

        @Test
        @DisplayName("should return new auth token")
        void should_returnNewToken() {
            // Arrange
            String oldRefreshTokenValue = UUID.randomUUID().toString();
            AuthTokenRequest request = new AuthTokenRequest(oldRefreshTokenValue);
            RefreshToken oldRefreshToken = new RefreshToken(user, oldRefreshTokenValue, Instant.now());
            when(refreshTokenService.findByTokenValue(oldRefreshTokenValue)).thenReturn(Optional.of(oldRefreshToken));
            when(refreshTokenService.verifyExpiration(oldRefreshToken)).thenReturn(oldRefreshToken);

            String newRefreshTokenValue = UUID.randomUUID().toString();
            RefreshToken newRefreshToken = new RefreshToken(user, newRefreshTokenValue, Instant.now());
            when(refreshTokenService.rotateToken(oldRefreshToken)).thenReturn(newRefreshToken);

            String newAccessTokenValue = "new.access.token";
            when(jwtUtil.generateAccessToken(eq(user.getUsername()), anyList())).thenReturn(newAccessTokenValue);
            when(jwtUtil.getAccessTokenExpirationMs()).thenReturn(ACCESS_TOKEN_EXPIRATION_MS);

            // Act
            AuthTokenResponse result = authenticationService.renewAuthToken(request);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.accessToken()).isEqualTo(newAccessTokenValue);
            assertThat(result.refreshToken()).isEqualTo(newRefreshTokenValue);
            assertThat(result.tokenType()).isEqualTo("Bearer");
            assertThat(result.expiresIn()).isEqualTo(ACCESS_TOKEN_EXPIRATION_MS);

            // Verify interactions
            verify(refreshTokenService).findByTokenValue(oldRefreshTokenValue);
            verify(refreshTokenService).verifyExpiration(oldRefreshToken);
            verify(refreshTokenService).rotateToken(oldRefreshToken);
            verify(jwtUtil).generateAccessToken(eq(user.getUsername()), anyList());
            verify(jwtUtil).getAccessTokenExpirationMs();

        }

        @Test
        @DisplayName("should throw RefreshTokenNotFoundException when refresh token is not found")
        void should_throwException_when_refreshTokenNotFound() {
            // Arrange
            String oldRefreshTokenValue = UUID.randomUUID().toString();
            AuthTokenRequest request = new AuthTokenRequest(oldRefreshTokenValue);
            when(refreshTokenService.findByTokenValue(oldRefreshTokenValue)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> authenticationService.renewAuthToken(request))
                    .isInstanceOf(RefreshTokenNotFoundException.class)
                    .asInstanceOf(InstanceOfAssertFactories.type(RefreshTokenNotFoundException.class))
                    .satisfies(ex -> {
                        assertThat(ex.getTokenValue()).isEqualTo(oldRefreshTokenValue);
                    });

            // Verify no further interactions occured
            verify(refreshTokenService, never()).verifyExpiration(any(RefreshToken.class));
            verify(refreshTokenService, never()).rotateToken(any(RefreshToken.class));
            verify(jwtUtil, never()).generateAccessToken(anyString(), anyList());
            verify(jwtUtil, never()).getAccessTokenExpirationMs();
        }

        @Test
        @DisplayName("should throw RefreshTokenExpiredException when refresh token is expired")
        void should_throwException_when_refreshTokenIsExpired() {
            // Arrange
            String oldRefreshTokenValue = UUID.randomUUID().toString();
            AuthTokenRequest request = new AuthTokenRequest(oldRefreshTokenValue);          
            RefreshToken oldRefreshToken = new RefreshToken(user, oldRefreshTokenValue, Instant.now());
            when(refreshTokenService.findByTokenValue(oldRefreshTokenValue)).thenReturn(Optional.of(oldRefreshToken));
            when(refreshTokenService.verifyExpiration(oldRefreshToken)).thenThrow(new RefreshTokenExpiredException(oldRefreshTokenValue));

            // Act & Assert
            assertThatThrownBy(() -> authenticationService.renewAuthToken(request))
                    .isInstanceOf(RefreshTokenExpiredException.class)
                    .asInstanceOf(InstanceOfAssertFactories.type(RefreshTokenExpiredException.class))
                    .satisfies(ex -> {
                        assertThat(ex.getTokenValue()).isEqualTo(oldRefreshTokenValue);
                    });

            // Verify no further interactions occured
            verify(refreshTokenService, never()).rotateToken(any(RefreshToken.class));
            verify(jwtUtil, never()).generateAccessToken(anyString(), anyList());
            verify(jwtUtil, never()).getAccessTokenExpirationMs();
        }   
    }   
}
