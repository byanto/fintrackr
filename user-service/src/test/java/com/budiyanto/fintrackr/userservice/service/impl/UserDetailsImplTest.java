package com.budiyanto.fintrackr.userservice.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import com.budiyanto.fintrackr.userservice.entity.Role;
import com.budiyanto.fintrackr.userservice.entity.User;

@DisplayName("UserDetailsImpl Tests")
class UserDetailsImplTest {

    @Test
    @DisplayName("should correctly build UserDetails from a User object")
    void should_buildUserDetailsImplFromUser() {
        // Arrange
        Role userRole = new Role("ROLE_USER");
        Role adminRole = new Role("ROLE_ADMIN");

        User user = new User("testuser", "password123", "test@example.com");
        user.addRole(userRole);
        user.addRole(adminRole);

        // Set the ID using reflection since it's normally database-generated
        ReflectionTestUtils.setField(user, "id", 1L);

        // Act
        UserDetailsImpl userDetails = UserDetailsImpl.build(user);

        // Assert
        assertThat(userDetails.getId()).isEqualTo(1L);
        assertThat(userDetails.getUsername()).isEqualTo("testuser");
        assertThat(userDetails.getEmail()).isEqualTo("test@example.com");
        assertThat(userDetails.getPassword()).isEqualTo("password123");

        assertThat(userDetails.getAuthorities())
                .isNotNull()
                .hasSize(2)
                .extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
    }
}
