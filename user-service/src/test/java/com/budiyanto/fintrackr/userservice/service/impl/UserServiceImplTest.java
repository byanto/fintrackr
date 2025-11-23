package com.budiyanto.fintrackr.userservice.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.budiyanto.fintrackr.userservice.dto.UserResponse;
import com.budiyanto.fintrackr.userservice.entity.User;
import com.budiyanto.fintrackr.userservice.mapper.UserMapper;
import com.budiyanto.fintrackr.userservice.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests")
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @Nested
    @DisplayName("getUserByUsername method")
    class GetUserByUsername {

        @Test
        @DisplayName("should return UserResponse when user exists")
        void should_returnUserResponse_when_userExists() {
            // Arrange
            String username = "testuser";
            User user = new User();
            UserResponse userResponse = new UserResponse(1L, username, "test@email.com", Instant.now());

            when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
            when(userMapper.toUserResponse(user)).thenReturn(userResponse);

            // Act
            UserResponse result = userService.getUserByUsername(username);

            // Assert
            assertThat(result).isEqualTo(userResponse);
            verify(userRepository).findByUsername(username);
            verify(userMapper).toUserResponse(user);
        }

        @Test
        @DisplayName("should throw UsernameNotFoundException when user does not exist")
        void should_throwException_when_userDoesNotExist() {
            // Arrange
            String username = "nonexistent";
            when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> userService.getUserByUsername(username))
                    .isInstanceOf(UsernameNotFoundException.class)
                    .hasMessage("User not found with username: " + username);
        }
    }
}
