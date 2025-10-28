package com.budiyanto.fintrackr.userservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.budiyanto.fintrackr.userservice.domain.Role;
import com.budiyanto.fintrackr.userservice.domain.User;
import com.budiyanto.fintrackr.userservice.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserDetailsServiceImpl Tests")
class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Nested
    @DisplayName("loadUserByUsername method")
    class LoadUserByUsername {

        @Test
        @DisplayName("should load user by username when user exists")
        void should_loadUserByUsername_when_userExists() {
            // Arrange
            String userRole = "ROLE_USER";
            Role role = new Role(userRole);

            String username = "testuser";
            String password = "password";
            String email = "test@example.com";
            User user = new User(username, password, email);
            user.addRole(role);

            when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

            // Act
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // Assert
            assertThat(userDetails).isNotNull();
            assertThat(userDetails.getUsername()).isEqualTo(username);
            assertThat(userDetails.getPassword()).isEqualTo(password);
            assertThat(userDetails.getAuthorities())
                .isNotNull()
                .hasSize(1)
                .extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder(userRole);
    
            // Verify interactions
            verify(userRepository).findByUsername(username);
        }

        @Test
        @DisplayName("should throw UsernameNotFoundException when user does not exist")
        void should_throwUsernameNotFoundException_when_userDoesNotExist() {
            // Arrange
            String nonExistentUsername = "nonexistent";
            when(userRepository.findByUsername(nonExistentUsername)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> userDetailsService.loadUserByUsername(nonExistentUsername))
                    .isInstanceOf(UsernameNotFoundException.class)
                    .hasMessageContaining("User Not Found with username: " + nonExistentUsername);
        }
    }
}
