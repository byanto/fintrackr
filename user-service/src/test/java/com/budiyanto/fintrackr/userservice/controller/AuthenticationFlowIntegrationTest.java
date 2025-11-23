package com.budiyanto.fintrackr.userservice.controller;

import com.budiyanto.fintrackr.userservice.TestcontainersConfiguration;
import com.budiyanto.fintrackr.userservice.dto.UserLoginRequest;
import com.budiyanto.fintrackr.userservice.dto.UserLoginResponse;
import com.budiyanto.fintrackr.userservice.dto.UserRegistrationRequest;
import com.budiyanto.fintrackr.userservice.dto.UserResponse;
import com.budiyanto.fintrackr.userservice.entity.Role;
import com.budiyanto.fintrackr.userservice.repository.RoleRepository;
import com.budiyanto.fintrackr.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
@Transactional
@DisplayName("Full Authentication Flow Integration Test")
public class AuthenticationFlowIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String ROLE_USER = "ROLE_USER";

    @BeforeEach
    void setUp() {
        // Ensure the default role exists in the database for the registration to work
        if (roleRepository.findByName(ROLE_USER).isEmpty()) {
            roleRepository.save(new Role(ROLE_USER));
        }
    }

    @Test
    @DisplayName("should register a new user and then log in successfully")
    void shouldRegisterAndLoginSuccessfully() {
        // === 1. REGISTRATION PHASE ===

        // Arrange
        String username = "integration.user";
        String password = "Password123!";
        String email = "integration.user@email.com";
        UserRegistrationRequest registrationRequest = new UserRegistrationRequest(username, password, email);

        // Act: Call the registration endpoint
        ResponseEntity<UserResponse> registrationResponse = restTemplate.postForEntity(
                "/api/auth/register",
                registrationRequest,
                UserResponse.class
        );

        // Assert: Registration was successful
        assertThat(registrationResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(registrationResponse.getBody()).isNotNull();
        assertThat(registrationResponse.getBody().username()).isEqualTo(username);
        assertThat(registrationResponse.getBody().email()).isEqualTo(email);

        // Assert: Verify user exists in the database with the correct role and encoded password
        var savedUser = userRepository.findByUsername(username).orElseThrow();
        assertThat(savedUser.getEmail()).isEqualTo(email);
        assertThat(passwordEncoder.matches(password, savedUser.getPassword())).isTrue();
        assertThat(savedUser.getRoles()).anyMatch(role -> role.getName().equals(ROLE_USER));


        // === 2. LOGIN PHASE ===

        // Arrange
        UserLoginRequest loginRequest = new UserLoginRequest(username, password);

        // Act: Call the login endpoint
        ResponseEntity<UserLoginResponse> loginResponse = restTemplate.postForEntity(
                "/api/auth/login",
                loginRequest,
                UserLoginResponse.class
        );

        // Assert: Login was successful
        assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(loginResponse.getBody()).isNotNull();
        assertThat(loginResponse.getBody().username()).isEqualTo(username);
        assertThat(loginResponse.getBody().accessToken()).isNotBlank();
        assertThat(loginResponse.getBody().refreshToken()).isNotBlank();
        assertThat(loginResponse.getBody().roles()).contains(ROLE_USER);
    }
}
