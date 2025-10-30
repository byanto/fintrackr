package com.budiyanto.fintrackr.userservice.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.budiyanto.fintrackr.userservice.TestcontainersConfiguration;
import com.budiyanto.fintrackr.userservice.domain.RefreshToken;
import com.budiyanto.fintrackr.userservice.domain.User;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
@DisplayName("RefreshTokenRepository Tests")
class RefreshTokenRepositoryTest {

    private RefreshTokenRepository refreshTokenRepository;
    private UserRepository userRepository;

    private static final String username1 = "user1";
    private static final String username2 = "user2";

    @Autowired
    RefreshTokenRepositoryTest(RefreshTokenRepository refreshTokenRepository, UserRepository userRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
    }

    @BeforeEach
    void setUp() {
        User user1 = new User(username1, "pass1", "user1@mail.com");
        User user2 = new User(username2, "pass2", "user2@mail.com");
        userRepository.saveAll(List.of(user1, user2));
    }

    @Nested
    @DisplayName("findByToken method")
    class FindByToken {
        
        @Test
        @DisplayName("should return token when it exists")
        void should_returnToken_when_tokenExists() {
            // Arrange
            User user = userRepository.findByUsername(username1).orElseThrow();

            String tokenValue = UUID.randomUUID().toString();
            RefreshToken refreshToken = new RefreshToken(user, tokenValue, Instant.now().plus(1, ChronoUnit.DAYS));
            refreshTokenRepository.save(refreshToken);

            // Act
            Optional<RefreshToken> foundToken = refreshTokenRepository.findByToken(tokenValue);

            // Assert
            assertThat(foundToken).isPresent();
            assertThat(foundToken.get().getValue()).isEqualTo(tokenValue);
            assertThat(foundToken.get().getUser().getUsername()).isEqualTo(username1);
        }

        @Test
        @DisplayName("should return empty optional when token does not exist")
        void should_returnEmpty_whenTokenDoesNotExist() {
            // Act
            Optional<RefreshToken> foundToken = refreshTokenRepository.findByToken(UUID.randomUUID().toString());

            // Assert
            assertThat(foundToken).isNotPresent();
        }
    }

    @Nested
    @DisplayName("deleteByUser method")
    class DeleteByUser {

        @Test
        @DisplayName("should delete only the specified user's token")
        void should_deleteToken_when_userExists() {
            // Arrange
            User user1 = userRepository.findByUsername(username1).orElseThrow();
            User user2 = userRepository.findByUsername(username2).orElseThrow();

            RefreshToken token1 = new RefreshToken(user1, UUID.randomUUID().toString(), Instant.now().plus(1, ChronoUnit.DAYS));
            RefreshToken token2 = new RefreshToken(user2, UUID.randomUUID().toString(), Instant.now().plus(1, ChronoUnit.DAYS));
            refreshTokenRepository.saveAll(List.of(token1, token2));

            // Act
            int deletedCount = refreshTokenRepository.deleteByUser(user1);

            // Assert
            assertThat(deletedCount).isEqualTo(1);
            assertThat(refreshTokenRepository.findByToken(token1.getValue())).isNotPresent();
            assertThat(refreshTokenRepository.findByToken(token2.getValue())).isPresent();
        }

    }

}
