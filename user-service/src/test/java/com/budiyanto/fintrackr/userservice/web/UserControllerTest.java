package com.budiyanto.fintrackr.userservice.web;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.budiyanto.fintrackr.userservice.dto.UserResponse;
import com.budiyanto.fintrackr.userservice.security.SecurityConfig;
import com.budiyanto.fintrackr.userservice.service.UserService;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
@DisplayName("UserController Tests")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Nested
    @DisplayName("GET /api/users/me")
    class GetMeEndpoint {

        @Test
        @WithMockUser(username = "testuser", roles = "USER")
        @DisplayName("should return 200 OK with user details for authenticated user")
        void should_return200_when_userIsAuthenticated() throws Exception {
            // Arrange
            String username = "testuser";
            UserResponse userResponse = new UserResponse(1L, username, "test@email.com", Instant.now());
            when(userService.getUserByUsername(username)).thenReturn(userResponse);

            // Act & Assert
            mockMvc.perform(get("/api/users/me"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value(username))
                    .andExpect(jsonPath("$.email").value("test@email.com"));
        }

        @Test
        @DisplayName("should return 401 Unauthorized when user is not authenticated")
        void should_return401_when_userIsNotAuthenticated() throws Exception {
            // Act & Assert
            mockMvc.perform(get("/api/users/me"))
                    .andExpect(status().isUnauthorized());
        }
    }
}
