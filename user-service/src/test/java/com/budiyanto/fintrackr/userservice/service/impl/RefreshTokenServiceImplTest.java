package com.budiyanto.fintrackr.userservice.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import com.budiyanto.fintrackr.userservice.entity.RefreshToken;
import com.budiyanto.fintrackr.userservice.entity.User;
import com.budiyanto.fintrackr.userservice.exception.InvalidRefreshTokenException;
import com.budiyanto.fintrackr.userservice.exception.UserNotFoundException;
import com.budiyanto.fintrackr.userservice.repository.RefreshTokenRepository;
import com.budiyanto.fintrackr.userservice.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("RefreshTokenService Tests")
class RefreshTokenServiceImplTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private Clock clock;
    
    @InjectMocks
    private RefreshTokenServiceImpl refreshTokenService;

    private User user;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenDuration", Duration.ofDays(7));
        user = new User("testuser", "password", "test@email.com");

        // Set a default behavior for the mocked clock to avoid NullPointerExceptions.
        // Tests that need specific times can override this with their own `when()` calls.
        Mockito.lenient().when(clock.instant()).thenReturn(Instant.now());
    }

    @Nested
    @DisplayName("createToken method")
    class CreateToken {

        @Test
        @DisplayName("should create and save a new refresh token for an existing user")
        void should_createAndSaveToken_when_userExists() {
            // Arrange
            when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
            when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));            

            String encodedTokenValue = "encodedTokenValue";
            when(passwordEncoder.encode(anyString())).thenReturn(encodedTokenValue);

            // Act
            String result = refreshTokenService.createToken(user);

            // Assert
            assertThat(result).isNotEmpty();

            ArgumentCaptor<RefreshToken> tokenCaptor = ArgumentCaptor.forClass(RefreshToken.class);
            verify(refreshTokenRepository).save(tokenCaptor.capture());
            RefreshToken capturedToken = tokenCaptor.getValue();

            assertThat(capturedToken.getUser()).isEqualTo(user);
            assertThat(capturedToken.getValue()).isEqualTo(encodedTokenValue);

            // Verify interactions
            verify(userRepository).findByUsername(user.getUsername());
            verify(passwordEncoder).encode(anyString());
            verify(refreshTokenRepository).save(any(RefreshToken.class));
        }

        @Test
        @DisplayName("should throw UserNotFoundException when user does not exist")
        void should_throwException_when_userDoesNotExist() {
            // Arrange
            String nonExistentUsername = "nonexistent";
            User nonExistentUser = new User(nonExistentUsername, "password", "no@email.com");
            when(userRepository.findByUsername(nonExistentUsername)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> refreshTokenService.createToken(nonExistentUser))
                    .isInstanceOf(UserNotFoundException.class)
                    .asInstanceOf(InstanceOfAssertFactories.type(UserNotFoundException.class))
                    .satisfies(ex -> {
                        assertThat(ex.getUsername()).isEqualTo(nonExistentUser.getUsername());
                    });

            // Verify no further interactions occured
            verify(passwordEncoder, never()).encode(anyString());
            verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
        }        
    }

    @Nested
    @DisplayName("findByUser method")
    class FindByUser {
        @Test
        @DisplayName("should call repository method")
        void should_callRepository() {
            // Arrange
            when(refreshTokenRepository.findByUser(user)).thenReturn(anyList());

            // Act
            refreshTokenService.findByUser(user);

            // Assert
            verify(refreshTokenRepository).findByUser(user);
        }
    }

    @Nested
    @DisplayName("verifyExpiration method")
    class VerifyExpiration {

        @Test
        @DisplayName("should return the token if it is not expired")
        void should_returnToken_when_notExpired() {
            // Arrange
            String tokenValue = UUID.randomUUID().toString();
            Instant expiryDate = Instant.now().plus(1, ChronoUnit.DAYS);
            RefreshToken token = new RefreshToken(user, tokenValue, expiryDate);

            // Act
            RefreshToken result = refreshTokenService.verifyExpiration(token);

            // Assert
            assertThat(result).isEqualTo(token);

            // Verify no further interactions occured
            verify(refreshTokenRepository, never()).delete(any(RefreshToken.class));
        }
         
        @Test
        @DisplayName("should throw RefreshTokenExpiredException and delete the token if it is expired")
        void should_throwException_when_expired() {
            // Arrange
            String tokenValue = UUID.randomUUID().toString();
            Instant expiryDate = Instant.now().minus(1, ChronoUnit.DAYS);
            RefreshToken token = new RefreshToken(user, tokenValue, expiryDate);

            // Act & Assert
            assertThatThrownBy(() -> refreshTokenService.verifyExpiration(token))
                    .isInstanceOf(InvalidRefreshTokenException.class)
                    .hasMessageContaining("Invalid refresh token.");
                    
            // Verify that the expired Token is deleted
            verify(refreshTokenRepository).delete(token);
        }
    } 

    @Nested
    @DisplayName("rotateToken method")
    class RotateToken {

        @Test
        @DisplayName("should delete the old token and create a new one for the same user")
        void should_returnNewToken_when_rotationSuccess() {
            // Arrange
            String oldTokenValue = UUID.randomUUID().toString();
            RefreshToken oldToken = new RefreshToken(user, oldTokenValue, Instant.now());
            
            // Let the real createToken method do its job. We only mock the dependencies.
            when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
            when(passwordEncoder.encode(anyString())).thenReturn("some-encoded-value");
            when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            String result = refreshTokenService.rotateToken(oldToken);
            
            // Assert
            // We don't know the exact UUID, but we can assert that a valid, non-empty string was returned.
            assertThat(result).isNotEmpty();

            // Verify interactions
            verify(refreshTokenRepository).delete(oldToken);
            verify(userRepository).findByUsername(user.getUsername());
            verify(refreshTokenRepository).save(any(RefreshToken.class));
        }
    }

    @Nested
    @DisplayName("invalidateAllTokensByUser method")    
    class InvalidateAllTokensByUser {

        @Test
        @DisplayName("should delete all refresh tokens associated with a user")
        void should_deleteAllTokens_when_userExists() {
            // Arrange
            // No specific arrangement needed for this test, as it primarily verifies method invocation

            // Act
            refreshTokenService.invalidateAllTokensForUser(user);

            // Assert
            verify(refreshTokenRepository).deleteByUser(user);
        }

    }

}
