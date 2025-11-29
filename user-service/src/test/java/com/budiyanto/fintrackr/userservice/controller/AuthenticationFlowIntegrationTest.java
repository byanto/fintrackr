package com.budiyanto.fintrackr.userservice.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.budiyanto.fintrackr.userservice.TestcontainersConfiguration;
import com.budiyanto.fintrackr.userservice.dto.AuthenticationTokenRequest;
import com.budiyanto.fintrackr.userservice.dto.AuthenticationTokenResponse;
import com.budiyanto.fintrackr.userservice.dto.UserLoginRequest;
import com.budiyanto.fintrackr.userservice.dto.UserLoginResponse;
import com.budiyanto.fintrackr.userservice.dto.UserRegistrationRequest;
import com.budiyanto.fintrackr.userservice.dto.UserResponse;
import com.budiyanto.fintrackr.userservice.entity.Role;
import com.budiyanto.fintrackr.userservice.repository.RefreshTokenRepository;
import com.budiyanto.fintrackr.userservice.repository.RoleRepository;
import com.budiyanto.fintrackr.userservice.repository.UserRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
@DisplayName("Full Authentication Flow Integration Test")
public class AuthenticationFlowIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private Clock clock;

    private static final String ROLE_USER = "ROLE_USER";

    @BeforeEach
    void setUp() {
        // Ensure the default role exists in the database for the registration to work
        if (roleRepository.findByName(ROLE_USER).isEmpty()) {
            roleRepository.save(new Role(ROLE_USER));
        }
        
        // Set a default behavior for the mocked clock to avoid NullPointerExceptions.
        // Tests that need specific times can override this with their own `when()` calls.
        when(clock.instant()).thenReturn(Instant.now());
    }

    @AfterEach
    void tearDown() {
        // Clean up the database after each test since we are not using @Transactional
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("should register a new user and then log in successfully")
    void shouldRegisterAndLoginSuccessfully() {
        // === 1. REGISTRATION PHASE ===

        // Arrange
        String username = "integration.user";
        String password = "Password123!";
        String firstName = "John";
        String lastName = "Doe";
        String email = "integration.user@email.com";
        
        UserRegistrationRequest registrationRequest = new UserRegistrationRequest(username, password, firstName, lastName, email);

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

    @Test
    @DisplayName("should renew tokens successfully with a valid refresh token")
    void should_renewTokensSuccessfully() {
        // === 1. SETUP: Register and Login to get a valid refresh token ===
        String username = "renew.user";
        String password = "Password123!";
        String firstName = "John";
        String lastName = "Doe";
        UserRegistrationRequest registrationRequest = new UserRegistrationRequest(username, password, firstName, lastName, "renew.user@email.com");
        restTemplate.postForEntity("/api/auth/register", registrationRequest, UserResponse.class);

        // Arrange: Control the clock for the first call
        Instant loginTime = Instant.parse("2025-01-01T10:00:00Z");
        when(clock.instant()).thenReturn(loginTime);

        UserLoginRequest loginRequest = new UserLoginRequest(username, password);
        ResponseEntity<UserLoginResponse> loginResponse = restTemplate.postForEntity(
                "/api/auth/login",
                loginRequest,
                UserLoginResponse.class
        );

        UserLoginResponse loginResponseBody = loginResponse.getBody();
        assertThat(loginResponseBody).isNotNull();
        String initialAccessToken = loginResponseBody.accessToken();
        String initialRefreshToken = loginResponseBody.refreshToken();

        // === 2. RENEW TOKEN PHASE ===

        // Arrange: Advance the clock for the second call
        Instant renewalTime = loginTime.plusSeconds(3600); // plus 1 hour
        when(clock.instant()).thenReturn(renewalTime);

        // Arrange
        AuthenticationTokenRequest renewRequest = new AuthenticationTokenRequest(username, initialRefreshToken);

        // Act: Call the renew token endpoint
        ResponseEntity<AuthenticationTokenResponse> renewResponse = restTemplate.postForEntity(
                "/api/auth/renewtoken",
                renewRequest,
                AuthenticationTokenResponse.class
        );

        // Assert: Token renewal was successful
        assertThat(renewResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        AuthenticationTokenResponse renewResponseBody = renewResponse.getBody();
        assertThat(renewResponseBody).isNotNull();
        assertThat(renewResponseBody.accessToken()).isNotBlank();
        assertThat(renewResponseBody.refreshToken()).isNotBlank();
        assertThat(renewResponseBody.tokenType()).isEqualTo("Bearer");

        // Assert: Ensure the new tokens are different from the initial ones
        assertThat(renewResponseBody.accessToken()).isNotEqualTo(initialAccessToken);
        assertThat(renewResponseBody.refreshToken()).isNotEqualTo(initialRefreshToken);
    }

    @Test
    @DisplayName("should return 403 Forbidden when using a non-existent refresh token for token renewal")
    void should_return403Forbidden_when_refreshTokenNotExistsForTokenRenewal() {
        // Arrange: Register a user so the username is valid, but we won't use their actual token.
        String username = "invalid.token.user";
        String firstName = "John";
        String lastName = "Doe";
        UserRegistrationRequest registrationRequest = new UserRegistrationRequest(username, "Password123!", firstName, lastName, "invalid.token.user@email.com");
        restTemplate.postForEntity("/api/auth/register", registrationRequest, UserResponse.class);

        // Arrange: Create a request with a completely fake refresh token
        String fakeRefreshToken = UUID.randomUUID().toString();
        AuthenticationTokenRequest renewRequest = new AuthenticationTokenRequest(username, fakeRefreshToken);

        // Act: Call the renew token endpoint
        ResponseEntity<Object> renewResponse = restTemplate.postForEntity(
                "/api/auth/renewtoken",
                renewRequest,
                Object.class // We expect a generic error object, not a success DTO
        );

        // Assert: The request is forbidden
        assertThat(renewResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("should return 403 Forbidden when using an expired refresh token for token renewal")
    void should_return403Forbidden_when_refreshTokenExpiredForTokenRenewal() {
        // === 1. SETUP: Register and Login to get a valid refresh token ===       

        String username = "expired.token.user";
        String firstName = "John";
        String lastName = "Doe";
        UserRegistrationRequest registrationRequest = new UserRegistrationRequest(username, "Password123!", firstName, lastName, "expired.token.user@email.com");
        restTemplate.postForEntity("/api/auth/register", registrationRequest, UserResponse.class);

        // Arrange: Set the clock for the login call
        Instant now = Instant.parse("2025-02-01T10:00:00Z");
        when(clock.instant()).thenReturn(now);

        ResponseEntity<UserLoginResponse> loginResponse = restTemplate.postForEntity(
                "/api/auth/login", new UserLoginRequest(username, "Password123!"), UserLoginResponse.class);
        String rawRefreshTokenValue = loginResponse.getBody().refreshToken();

        // === 3. ACT & ASSERT: Use the original (but now expired) token ===
        // We set the renewal time in future (+ 30 days) so that the original refresh token has expired
        when(clock.instant()).thenReturn(now.plusSeconds(30*(24*60*60))); // plus 30 days

        AuthenticationTokenRequest renewRequest = new AuthenticationTokenRequest(username, rawRefreshTokenValue);
        ResponseEntity<Object> renewResponse = restTemplate.postForEntity(
                "/api/auth/renewtoken",
                renewRequest,
                Object.class
        );

        // Assert: The request is forbidden
        assertThat(renewResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}
