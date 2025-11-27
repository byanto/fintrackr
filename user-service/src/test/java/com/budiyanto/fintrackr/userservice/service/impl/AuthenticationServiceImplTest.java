package com.budiyanto.fintrackr.userservice.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

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

import com.budiyanto.fintrackr.userservice.dto.AuthenticationTokenRequest;
import com.budiyanto.fintrackr.userservice.dto.AuthenticationTokenResponse;
import com.budiyanto.fintrackr.userservice.dto.UserLoginRequest;
import com.budiyanto.fintrackr.userservice.dto.UserLoginResponse;
import com.budiyanto.fintrackr.userservice.dto.UserRegistrationRequest;
import com.budiyanto.fintrackr.userservice.dto.UserResponse;
import com.budiyanto.fintrackr.userservice.entity.RefreshToken;
import com.budiyanto.fintrackr.userservice.entity.Role;
import com.budiyanto.fintrackr.userservice.entity.User;
import com.budiyanto.fintrackr.userservice.exception.InvalidRefreshTokenException;
import com.budiyanto.fintrackr.userservice.exception.RoleNotFoundException;
import com.budiyanto.fintrackr.userservice.exception.UserAlreadyExistsException;
import com.budiyanto.fintrackr.userservice.mapper.UserMapper;
import com.budiyanto.fintrackr.userservice.repository.RoleRepository;
import com.budiyanto.fintrackr.userservice.repository.UserRepository;
import com.budiyanto.fintrackr.userservice.security.JwtService;
import com.budiyanto.fintrackr.userservice.service.RefreshTokenService;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthenticationService Tests")
class AuthenticationServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @Mock
    private RefreshTokenService refreshTokenService;
    
    @Mock
    private JwtService jwtService;
    
    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    private static final Long ID = 1L;
    private static final String USERNAME = "testuser";
    private static final String EMAIL = "test@email.com";
    private static final String RAW_PASSWORD = "password123";
    private static final String ENCODED_PASSWORD = "encodedPassword123";
    private static final String ROLE_USER = "ROLE_USER";
    
    @Nested
    @DisplayName("registerUser method")
    class RegisterUser {

        private UserRegistrationRequest registerRequest;

        @BeforeEach
        void setUp() {
            registerRequest = new UserRegistrationRequest(USERNAME, RAW_PASSWORD, EMAIL);
        }

        @Test
        @DisplayName("should register user successfully when username is unique")
        void should_registerUser_when_usernameIsUnique() {
            // Arrange
            Long id = 1L;
            Instant createdAt = Instant.now();
            Role userRole = new Role(ROLE_USER);

            // Mock
            when(userRepository.findByUsername(registerRequest.username())).thenReturn(Optional.empty());
            when(roleRepository.findByName(ROLE_USER)).thenReturn(Optional.of(userRole));
            
            User user = new User(registerRequest.username(), ENCODED_PASSWORD, registerRequest.email());
            when(userMapper.toUser(registerRequest)).thenReturn(user);
            
            User savedUser = new User(registerRequest.username(), ENCODED_PASSWORD, registerRequest.email());
            ReflectionTestUtils.setField(savedUser, "id", id);
            ReflectionTestUtils.setField(savedUser, "createdAt", createdAt);
            ReflectionTestUtils.setField(savedUser, "updatedAt", createdAt);
            savedUser.addRole(userRole);
            when(userRepository.save(any(User.class))).thenReturn(savedUser);

            UserResponse response = new UserResponse(savedUser.getId(), savedUser.getUsername(), savedUser.getEmail(), savedUser.getCreatedAt());
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
            verify(roleRepository).findByName(ROLE_USER);
            verify(userMapper).toUser(registerRequest);
            verify(userMapper).toUserResponse(savedUser);
        }

        @Test
        @DisplayName("should throw UserAlreadyExistsException when username is taken")
        void should_throwException_when_usernameIsTaken() {
            // Arrange
            User user = new User(registerRequest.username(), ENCODED_PASSWORD, registerRequest.email());
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
            verify(userMapper, never()).toUser(any(UserRegistrationRequest.class));
            verify(userRepository, never()).save(any(User.class));
            verify(userMapper, never()).toUserResponse(any(User.class));
        }

        @Test
        @DisplayName("should throw RoleNotFoundException when default role is missing")
        void should_throwException_when_defaultRoleIsMissing() {
            // Arrange
            when(userRepository.findByUsername(registerRequest.username())).thenReturn(Optional.empty());
            when(roleRepository.findByName(ROLE_USER)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> authenticationService.registerUser(registerRequest))
                    .isInstanceOf(RoleNotFoundException.class)
                    .hasMessageContaining("Default role ROLE_USER not found.");

            // Verify further interactions never occured
            verify(userMapper, never()).toUser(any(UserRegistrationRequest.class));
            verify(userRepository, never()).save(any(User.class));
            verify(userMapper, never()).toUserResponse(any(User.class));
        }
    }

    @Nested
    @DisplayName("authenticate method")
    class Authenticate {

        private User existingUser;
        private UserLoginRequest loginRequest;
        
        @BeforeEach
        void setUp() {            
            existingUser = new User(USERNAME, ENCODED_PASSWORD, EMAIL);
            ReflectionTestUtils.setField(existingUser, "id", ID);
            existingUser.addRole(new Role(ROLE_USER));

            loginRequest = new UserLoginRequest(USERNAME, RAW_PASSWORD);
        }

        @Test
        @DisplayName("should return LoginResponse on successful authentication")
        void should_returnLoginResponse_when_authenticationSuccess() {
            // Arrange
            when(userRepository.findByUsername(loginRequest.username())).thenReturn(Optional.of(existingUser));
            when(passwordEncoder.matches(RAW_PASSWORD, ENCODED_PASSWORD)).thenReturn(true);

            String accessToken = "access.token";
            String refreshToken = "refresh.token";
            when(jwtService.generateAccessToken(existingUser.getUsername(), List.of(ROLE_USER))).thenReturn(accessToken);
            when(refreshTokenService.createToken(existingUser)).thenReturn(refreshToken);

            UserLoginResponse expectedResponse = new UserLoginResponse(existingUser.getId(), existingUser.getUsername(), EMAIL, List.of(ROLE_USER), accessToken, refreshToken);
            when(userMapper.toLoginResponse(existingUser, accessToken, refreshToken)).thenReturn(expectedResponse);

            // Act
            UserLoginResponse result = authenticationService.authenticate(loginRequest);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(expectedResponse);

            // Verify interactions
            verify(userRepository).findByUsername(loginRequest.username());
            verify(passwordEncoder).matches(RAW_PASSWORD, ENCODED_PASSWORD);
            verify(jwtService).generateAccessToken(existingUser.getUsername(), List.of(ROLE_USER));
            verify(refreshTokenService).createToken(existingUser);
            verify(userMapper).toLoginResponse(existingUser, accessToken, refreshToken);
        }

        @Test
        @DisplayName("should throw BadCredentialsException for non-existent username")
        void should_throwException_when_usernameDoesNotExist() {
            // Arrange
            when(userRepository.findByUsername(loginRequest.username())).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> authenticationService.authenticate(loginRequest))
                    .isInstanceOf(BadCredentialsException.class)
                    .hasMessageContaining("Invalid username or password");

            // Verify further interactions never occured
            verify(passwordEncoder, never()).matches(anyString(), anyString());
            verify(jwtService, never()).generateAccessToken(anyString(), anyList());
            verify(refreshTokenService, never()).createToken(any(User.class));
            verify(userMapper, never()).toLoginResponse(any(User.class), anyString(), anyString());
        }

        @Test
        @DisplayName("should throw BadCredentialsException for incorrect password")
        void should_throwException_when_passwordIsIncorrect() {
            // Arrange
            when(userRepository.findByUsername(loginRequest.username())).thenReturn(Optional.of(existingUser));
            when(passwordEncoder.matches(RAW_PASSWORD, ENCODED_PASSWORD)).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> authenticationService.authenticate(loginRequest))
                    .isInstanceOf(BadCredentialsException.class)
                    .hasMessageContaining("Invalid username or password");

            // Verify further interactions never occured
            verify(jwtService, never()).generateAccessToken(anyString(), anyList());
            verify(refreshTokenService, never()).createToken(any(User.class));
            verify(userMapper, never()).toLoginResponse(any(User.class), anyString(), anyString());
        }
    }

    @Nested
    @DisplayName("renewAuthToken method")
    class renewAuthToken {

        private User existingUser;
        private AuthenticationTokenRequest authRequest;
        private RefreshToken oldRefreshToken;
        private static final String OLD_REFRESH_TOKEN_VALUE = "old.refresh.token";
        private static final String ENCODED_OLD_REFRESH_TOKEN_VALUE = "encoded.old.refresh.token";
        private static final Duration ACCESS_TOKEN_EXPIRATION = Duration.ofHours(1);

        @BeforeEach
        void setup() {
            existingUser = new User(USERNAME, ENCODED_PASSWORD, EMAIL);
            ReflectionTestUtils.setField(existingUser, "id", ID);
            existingUser.addRole(new Role(ROLE_USER));

            authRequest = new AuthenticationTokenRequest(USERNAME, OLD_REFRESH_TOKEN_VALUE);
            oldRefreshToken = new RefreshToken(existingUser, ENCODED_OLD_REFRESH_TOKEN_VALUE, Instant.now());
        }

        @Test
        @DisplayName("should return new auth token")
        void should_returnNewToken() {
            // Arrange
            when(userRepository.findByUsername(authRequest.username())).thenReturn(Optional.of(existingUser));
            when(refreshTokenService.findByUser(existingUser)).thenReturn(List.of(oldRefreshToken));
            when(passwordEncoder.matches(OLD_REFRESH_TOKEN_VALUE, ENCODED_OLD_REFRESH_TOKEN_VALUE)).thenReturn(true);
            when(refreshTokenService.verifyExpiration(oldRefreshToken)).thenReturn(oldRefreshToken);
            
            String newRefreshTokenValue = "new.refresh.token";
            when(refreshTokenService.rotateToken(oldRefreshToken)).thenReturn(newRefreshTokenValue);

            String newAccessTokenValue = "new.access.token";
            when(jwtService.generateAccessToken(eq(existingUser.getUsername()), anyList())).thenReturn(newAccessTokenValue);
            when(jwtService.getAccessTokenExpiration()).thenReturn(ACCESS_TOKEN_EXPIRATION);

            // Act
            AuthenticationTokenResponse result = authenticationService.renewAuthToken(authRequest);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.accessToken()).isEqualTo(newAccessTokenValue);
            assertThat(result.refreshToken()).isEqualTo(newRefreshTokenValue);
            assertThat(result.tokenType()).isEqualTo("Bearer");
            assertThat(result.expiresIn()).isEqualTo(ACCESS_TOKEN_EXPIRATION.toSeconds());

            // Verify interactions
            verify(userRepository).findByUsername(existingUser.getUsername());
            verify(refreshTokenService).findByUser(existingUser);
            verify(passwordEncoder).matches(OLD_REFRESH_TOKEN_VALUE, ENCODED_OLD_REFRESH_TOKEN_VALUE);
            verify(refreshTokenService).verifyExpiration(oldRefreshToken);
            verify(refreshTokenService).rotateToken(oldRefreshToken);
            verify(jwtService).generateAccessToken(eq(existingUser.getUsername()), anyList());
            verify(jwtService).getAccessTokenExpiration();

        }

        @Test
        @DisplayName("should throw RefreshTokenNotFoundException when user is not found")
        void should_throwException_when_userIsNotFound() {
            // Arrange
            when(userRepository.findByUsername(authRequest.username())).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> authenticationService.renewAuthToken(authRequest))
                    .isInstanceOf(InvalidRefreshTokenException.class)
                    .hasMessageContaining("Invalid refresh token.");

            // Verify no further interactions occured
            verify(refreshTokenService, never()).findByUser(any(User.class));
            verify(passwordEncoder, never()).matches(anyString(), anyString());
            verify(refreshTokenService, never()).verifyExpiration(any(RefreshToken.class));
            verify(refreshTokenService, never()).rotateToken(any(RefreshToken.class));
            verify(jwtService, never()).generateAccessToken(anyString(), anyList());
            verify(jwtService, never()).getAccessTokenExpiration();
        }

        @Test
        @DisplayName("should throw RefreshTokenNotFoundException when refresh token is not found")
        void should_throwException_when_refreshTokenNotFound() {
            // Arrange
            when(userRepository.findByUsername(authRequest.username())).thenReturn(Optional.of(existingUser));
            when(refreshTokenService.findByUser(existingUser)).thenReturn(List.of());

            // Act & Assert
            assertThatThrownBy(() -> authenticationService.renewAuthToken(authRequest))
                    .isInstanceOf(InvalidRefreshTokenException.class)
                    .hasMessageContaining("Invalid refresh token.");

            // Verify no further interactions occured
            verify(passwordEncoder, never()).matches(anyString(), anyString());
            verify(refreshTokenService, never()).verifyExpiration(any(RefreshToken.class));
            verify(refreshTokenService, never()).rotateToken(any(RefreshToken.class));
            verify(jwtService, never()).generateAccessToken(anyString(), anyList());
            verify(jwtService, never()).getAccessTokenExpiration();
        }

        @Test
        @DisplayName("should throw RefreshTokenNotFoundException when the given refresh token is not matched with the one in DB")
        void should_throwException_when_refreshTokenIsNotMatched() {
            // Arrange
            when(userRepository.findByUsername(authRequest.username())).thenReturn(Optional.of(existingUser));
            when(refreshTokenService.findByUser(existingUser)).thenReturn(List.of(oldRefreshToken));
            when(passwordEncoder.matches(OLD_REFRESH_TOKEN_VALUE, ENCODED_OLD_REFRESH_TOKEN_VALUE)).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> authenticationService.renewAuthToken(authRequest))
                    .isInstanceOf(InvalidRefreshTokenException.class)
                    .hasMessageContaining("Invalid refresh token.");

            // Verify no further interactions occured
            verify(refreshTokenService, never()).verifyExpiration(any(RefreshToken.class));
            verify(refreshTokenService, never()).rotateToken(any(RefreshToken.class));
            verify(jwtService, never()).generateAccessToken(anyString(), anyList());
            verify(jwtService, never()).getAccessTokenExpiration();
        }

        @Test
        @DisplayName("should throw RefreshTokenExpiredException when refresh token is expired")
        void should_throwException_when_refreshTokenIsExpired() {
            // Arrange
            when(userRepository.findByUsername(authRequest.username())).thenReturn(Optional.of(existingUser));
            when(refreshTokenService.findByUser(existingUser)).thenReturn(List.of(oldRefreshToken));
            when(passwordEncoder.matches(OLD_REFRESH_TOKEN_VALUE, ENCODED_OLD_REFRESH_TOKEN_VALUE)).thenReturn(true);
            when(refreshTokenService.verifyExpiration(oldRefreshToken)).thenThrow(new InvalidRefreshTokenException("Invalid refresh token. Refresh token has expired."));

            // Act & Assert
            assertThatThrownBy(() -> authenticationService.renewAuthToken(authRequest))
                    .isInstanceOf(InvalidRefreshTokenException.class)
                    .hasMessageContaining("Invalid refresh token.");

            // Verify no further interactions occured
            verify(refreshTokenService, never()).rotateToken(any(RefreshToken.class));
            verify(jwtService, never()).generateAccessToken(anyString(), anyList());
            verify(jwtService, never()).getAccessTokenExpiration();
        }   
    }   
}
