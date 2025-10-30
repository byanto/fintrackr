package com.budiyanto.fintrackr.userservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.budiyanto.fintrackr.userservice.domain.RefreshToken;
import com.budiyanto.fintrackr.userservice.domain.User;
import com.budiyanto.fintrackr.userservice.exception.RefreshTokenExpiredException;
import com.budiyanto.fintrackr.userservice.exception.UserNotFoundException;
import com.budiyanto.fintrackr.userservice.repository.RefreshTokenRepository;
import com.budiyanto.fintrackr.userservice.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("RefreshTokenService Tests")
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private User user;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenDurationMs", 604800000L);
        this.user = new User("testuser", "password", "test@email.com");
    }

    @Nested
    @DisplayName("createRefreshToken method")
    class CreateRefreshToken {

        @Test
        @DisplayName("should create and save a new refresh token for an existing user")
        void should_createAndSaveToken_when_userExists() {
            // Arrange
            when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
            when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            RefreshToken result = refreshTokenService.createToken(user.getUsername());

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getUser()).isEqualTo(user);
            assertThat(result.getValue()).isNotNull();
            assertThat(result.getExpiryDate()).isAfter(Instant.now());

            ArgumentCaptor<RefreshToken> tokenCaptor = ArgumentCaptor.forClass(RefreshToken.class);
            verify(refreshTokenRepository).save(tokenCaptor.capture());
            RefreshToken capturedToken = tokenCaptor.getValue();

            assertThat(capturedToken.getUser()).isEqualTo(user);

            // Verify interactions
            verify(userRepository).findByUsername(user.getUsername());
            verify(refreshTokenRepository).save(any(RefreshToken.class));
        }

        @Test
        @DisplayName("should throw UserNotFoundException when user does not exist")
        void should_throwException_when_userDoesNotExist() {
            // Arrange
            String username = "nonexistent";
            when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> refreshTokenService.createToken(username))
                    .isInstanceOf(UserNotFoundException.class)
                    .asInstanceOf(InstanceOfAssertFactories.type(UserNotFoundException.class))
                    .satisfies(ex -> {
                        assertThat(ex.getUsername()).isEqualTo(username);
                    });

            // Verify no further interactions occured
            verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
        }        
    }

    @Nested
    @DisplayName("findByTokenValue method")
    class FindByTokenValue {
        @Test
        @DisplayName("should call repository method")
        void should_callRepository() {
            // Arrange
            String tokenValue = UUID.randomUUID().toString();
            when(refreshTokenRepository.findByToken(tokenValue)).thenReturn(Optional.empty());

            // Act
            refreshTokenService.findByTokenValue(tokenValue);

            // Assert
            verify(refreshTokenRepository).findByToken(tokenValue);
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
                    .isInstanceOf(RefreshTokenExpiredException.class)
                    .asInstanceOf(InstanceOfAssertFactories.type(RefreshTokenExpiredException.class))
                    .satisfies(ex -> {
                        assertThat(ex.getTokenValue()).isEqualTo(tokenValue);
                    });
                    
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
            
            String newTokenValue = UUID.randomUUID().toString();
            RefreshToken newToken = new RefreshToken(user, newTokenValue, Instant.now());
            when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
            when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(newToken);

            // Act
            RefreshToken result = refreshTokenService.rotateToken(oldToken);
            
            // Assert
            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(newToken);

            // Verify interactions
            verify(refreshTokenRepository).delete(oldToken);
            verify(userRepository).findByUsername(user.getUsername());
            verify(refreshTokenRepository).save(any(RefreshToken.class));
        }
    }

}
