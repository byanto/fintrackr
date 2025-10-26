package com.budiyanto.fintrackr.userservice.service;

import com.budiyanto.fintrackr.userservice.domain.Role;
import com.budiyanto.fintrackr.userservice.domain.User;
import com.budiyanto.fintrackr.userservice.dto.LoginRequest;
import com.budiyanto.fintrackr.userservice.dto.LoginResponse;
import com.budiyanto.fintrackr.userservice.dto.RegisterRequest;
import com.budiyanto.fintrackr.userservice.dto.UserResponse;
import com.budiyanto.fintrackr.userservice.exception.RoleNotFoundException;
import com.budiyanto.fintrackr.userservice.exception.UserAlreadyExistsException;
import com.budiyanto.fintrackr.userservice.mapper.UserMapper;
import com.budiyanto.fintrackr.userservice.repository.RoleRepository;
import com.budiyanto.fintrackr.userservice.repository.UserRepository;

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

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private RegisterRequest registerRequest;
    private Role userRole;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest("testuser", "test@email.com", "password123");
        userRole = new Role("ROLE_USER");
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
            when(passwordEncoder.encode(registerRequest.password())).thenReturn(encodedPassword);

            User savedUser = new User(registerRequest.username(), encodedPassword, registerRequest.email());
            ReflectionTestUtils.setField(savedUser, "id", id);
            ReflectionTestUtils.setField(savedUser, "createdAt", createdAt);
            ReflectionTestUtils.setField(savedUser, "updatedAt", createdAt);
            when(userRepository.save(any(User.class))).thenReturn(savedUser);

            UserResponse response = new UserResponse(savedUser.getId(), registerRequest.username(), registerRequest.email(), createdAt);
            when(userMapper.toUserResponse(savedUser)).thenReturn(response);

            // Act
            UserResponse result = userService.registerUser(registerRequest);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(response);

            // Capture the User object passed to save() and assert its properties
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            User capturedUser = userCaptor.getValue();
            assertThat(capturedUser.getId()).isNull();
            assertThat(capturedUser.getUsername()).isEqualTo(registerRequest.username());
            assertThat(capturedUser.getPassword()).isEqualTo(encodedPassword);
            assertThat(capturedUser.getCreatedAt()).isNull();
            assertThat(capturedUser.getUpdatedAt()).isNull();
            assertThat(capturedUser.getRoles()).contains(userRole);

            // Verify interactions
            verify(userRepository).findByUsername(registerRequest.username());
            verify(roleRepository).findByName("ROLE_USER");
            verify(passwordEncoder).encode(registerRequest.password());
            verify(userMapper).toUserResponse(savedUser);
        }

        @Test
        @DisplayName("should throw UserAlreadyExistsException when username is taken")
        void should_throwException_when_usernameIsTaken() {
            // Arrange
            User user = new User(registerRequest.username(), "encodedPassword", registerRequest.email());
            when(userRepository.findByUsername(registerRequest.username())).thenReturn(Optional.of(user));

            // Act & Assert
            assertThatThrownBy(() -> userService.registerUser(registerRequest))
                    .isInstanceOf(UserAlreadyExistsException.class)
                    .asInstanceOf(InstanceOfAssertFactories.type(UserAlreadyExistsException.class))
                    .satisfies(ex -> {
                        assertThat(ex.getUsername()).isEqualTo(registerRequest.username());
                    });

            // Verify further interactions never occured
            verify(roleRepository, never()).findByName("ROLE_USER");
            verify(passwordEncoder, never()).encode(registerRequest.password());
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
            assertThatThrownBy(() -> userService.registerUser(registerRequest))
                    .isInstanceOf(RoleNotFoundException.class)
                    .hasMessageContaining("Default role ROLE_USER not found.");

            // Verify further interactions never occured
            verify(passwordEncoder, never()).encode(registerRequest.password());
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
            when(userMapper.toLoginResponse(existingUser)).thenReturn(expectedResponse);

            // Act
            LoginResponse result = userService.authenticate(loginRequest);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(expectedResponse);

            // Verify interactions
            verify(userRepository).findByUsername(loginRequest.username());
            verify(passwordEncoder).matches(rawPassword, encodedPassword);
            verify(userMapper).toLoginResponse(existingUser);
        }

        @Test
        @DisplayName("should throw BadCredentialsException for non-existent username")
        void should_throwException_when_usernameDoesNotExist() {
            // Arrange
            when(userRepository.findByUsername(loginRequest.username())).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> userService.authenticate(loginRequest))
                    .isInstanceOf(BadCredentialsException.class)
                    .hasMessage("Invalid username or password");

            // Verify further interactions never occured
            verify(passwordEncoder, never()).matches(anyString(), anyString());
            verify(userMapper, never()).toLoginResponse(any(User.class));
        }

        @Test
        @DisplayName("should throw BadCredentialsException for incorrect password")
        void should_throwException_when_passwordIsIncorrect() {
            // Arrange
            when(userRepository.findByUsername(loginRequest.username())).thenReturn(Optional.of(existingUser));
            when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> userService.authenticate(loginRequest))
                    .isInstanceOf(BadCredentialsException.class)
                    .hasMessage("Invalid username or password");

            // Verify further interactions never occured
            verify(userMapper, never()).toLoginResponse(any(User.class));
        }
    }
}
