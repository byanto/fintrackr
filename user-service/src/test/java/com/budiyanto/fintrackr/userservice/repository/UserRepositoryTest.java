package com.budiyanto.fintrackr.userservice.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.budiyanto.fintrackr.userservice.TestcontainersConfiguration;
import com.budiyanto.fintrackr.userservice.domain.Role;
import com.budiyanto.fintrackr.userservice.domain.User;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
@DisplayName("UserRepository Tests")
class UserRepositoryTest {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    private static final String USERNAME = "testuser";
    private static final String PASSWORD = "testpassword";
    private static final String EMAIL = "test@email.com";

    @Autowired
    UserRepositoryTest(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("should save and retrieve user")
    void should_saveAndRetrieveUser() {
        // Arrange: Create a new User object
        User user = new User(USERNAME, PASSWORD, EMAIL);

        // Act: Save the user using the repository
        User savedUser = userRepository.save(user);

        // Assert: Verify that the user was saved correctly
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isGreaterThan(0);
        assertThat(savedUser.getUsername()).isEqualTo(USERNAME);
        assertThat(savedUser.getPassword()).isEqualTo(PASSWORD);
        assertThat(savedUser.getEmail()).isEqualTo(EMAIL);
        assertThat(savedUser.getCreatedAt()).isNotNull();
        assertThat(savedUser.getUpdatedAt()).isNotNull();

        // Act: Retrieve the saved user
        User retrievedUser = userRepository.findById(savedUser.getId()).orElse(null);

        // Assert: Verify that the user can be retrieved
        assertThat(retrievedUser).isNotNull();
        assertThat(retrievedUser.getId()).isEqualTo(savedUser.getId());
        assertThat(retrievedUser.getUsername()).isEqualTo(USERNAME);
        assertThat(retrievedUser.getPassword()).isEqualTo(PASSWORD);
        assertThat(retrievedUser.getEmail()).isEqualTo(EMAIL);
        assertThat(retrievedUser.getCreatedAt()).isNotNull();
        assertThat(retrievedUser.getUpdatedAt()).isNotNull();

    }

    @Test
    @DisplayName("should retrieve user when username exists")
    void should_retrieveUser_when_usernameExists() {
        // Arrange
        User user = new User(USERNAME, PASSWORD, EMAIL);
        userRepository.save(user);

        // Act
        Optional<User> foundUser = userRepository.findByUsername(USERNAME);

        // Assert
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo(USERNAME);
    }

    @Test
    @DisplayName("should return empty when retrieving non-existent username")
    void should_returnEmpty_when_retrievingNonExistentUsername() {
        // Act
        String nonExistentUsername = "nonExistentUsername";
        
        // Assert
        Optional<User> nonExistentUser = userRepository.findByUsername(nonExistentUsername);
        assertThat(nonExistentUser).isNotPresent();
    }

    @Test
    @DisplayName("should save user with roles")
    void should_saveUserWithRoles() {
        // Arrange: The 'ROLE_USER' role is pre-loaded by the V1 migration script.
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new IllegalStateException("Default role ROLE_USER not found"));

        User user = new User(USERNAME, PASSWORD, EMAIL);
        user.addRole(userRole);

        // Act
        User savedUser = userRepository.save(user);

        // Assert
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getRoles()).hasSize(1);
        assertThat(savedUser.getRoles()).extracting(Role::getName).containsExactly("ROLE_USER");
    }


}
