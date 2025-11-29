package com.budiyanto.fintrackr.userservice.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.util.ReflectionTestUtils;

import com.budiyanto.fintrackr.userservice.TestcontainersConfiguration;
import com.budiyanto.fintrackr.userservice.entity.RefreshToken;
import com.budiyanto.fintrackr.userservice.entity.User;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
@DisplayName("RefreshTokenRepository Tests")
class RefreshTokenRepositoryTest {

    private RefreshTokenRepository refreshTokenRepository;
    private UserRepository userRepository;

    private static final String USERNAME = "testuser";
    private static final String TOKEN_VALUE_1 = "token.value.1";
    private static final String TOKEN_VALUE_2 = "token.value.2";

    private User savedUser;

    @Autowired
    RefreshTokenRepositoryTest(RefreshTokenRepository refreshTokenRepository, UserRepository userRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
    }

    @BeforeEach
    void setUp() {
        // Arrange
        User user = new User(USERNAME, "pass1", "John", "Doe", "user1@mail.com");
        savedUser = userRepository.save(user);

        RefreshToken token1 = new RefreshToken(savedUser, TOKEN_VALUE_1, Instant.now().plus(1, ChronoUnit.DAYS));
        RefreshToken token2 = new RefreshToken(savedUser, TOKEN_VALUE_2, Instant.now().plus(1, ChronoUnit.DAYS));
        refreshTokenRepository.saveAll(List.of(token1, token2));
    }

    @Nested
    @DisplayName("findByUser method")
    class FindByUser {
        
        @Test
        @DisplayName("should return a list of tokens when user exists")
        void should_returnTokens_when_userExists() {            
            // Act
            List<RefreshToken> foundTokens = refreshTokenRepository.findByUser(savedUser);

            // Assert
            assertThat(foundTokens).size().isEqualTo(2);
            assertThat(foundTokens.get(0).getUser().getUsername()).isEqualTo(USERNAME);
            assertThat(foundTokens.get(0).getValue()).isEqualTo(TOKEN_VALUE_1);
            assertThat(foundTokens.get(1).getUser().getUsername()).isEqualTo(USERNAME);
            assertThat(foundTokens.get(1).getValue()).isEqualTo(TOKEN_VALUE_2);
        }

        @Test
        @DisplayName("should return empty list when user does not exist")
        void should_returnEmptyList_when_userDoesNotExist() {
            // Arrange
            // Create a User object with an ID that we know does not exist in the test DB.
            User nonExistentUser = new User("nonexistent", "password", "John", "Doe", "no@email.com");
            ReflectionTestUtils.setField(nonExistentUser, "id", 999L);

            // Act
            List<RefreshToken> foundTokens = refreshTokenRepository.findByUser(nonExistentUser);

            // Assert
            assertThat(foundTokens).isEmpty();
        }
    }

    @Nested
    @DisplayName("deleteByUser method")
    class DeleteByUser {

        @Test
        @DisplayName("should delete only the specified user's token")
        void should_deleteToken_when_userExists() {            
            // Act
            int deletedCount = refreshTokenRepository.deleteByUser(savedUser);

            // Assert
            assertThat(deletedCount).isEqualTo(2);
            assertThat(refreshTokenRepository.findByUser(savedUser)).isEmpty();            
        }

    }

}
