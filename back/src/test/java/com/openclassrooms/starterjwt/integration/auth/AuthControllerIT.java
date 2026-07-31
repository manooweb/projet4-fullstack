package com.openclassrooms.starterjwt.integration.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openclassrooms.starterjwt.integration.IntegrationTestSupport;
import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.payload.request.LoginRequest;
import com.openclassrooms.starterjwt.repository.UserRepository;

@AutoConfigureMockMvc
@Transactional
public class AuthControllerIT extends IntegrationTestSupport {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    @DisplayName("Given that a registered user wants to log in")
    class LoginTests {

        @Test
        @DisplayName("When the user provides valid credentials, then the user should be logged in")
        void shouldLoginUserWithValidCredentials() throws Exception {
            // Given a registered user
            String rawPassword = "password";

            createTestUser("test@example.com", rawPassword);

            // When the user attempts to log in with valid credentials
            LoginRequest loginRequest = new LoginRequest();
            loginRequest.setEmail("test@example.com");
            loginRequest.setPassword(rawPassword);

            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest)))
            // Then the user should be logged in successfully
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").isNotEmpty())
                    .andExpect(jsonPath("$.type").value("Bearer"))
                    .andExpect(jsonPath("$.username").value("test@example.com"))
                    .andExpect(jsonPath("$.firstName").value("Test"))
                    .andExpect(jsonPath("$.lastName").value("Demo"));
        }

        @Test
        @DisplayName("When the user provides unknown email, then the user should not be logged in")
        void shouldNotLoginUserWithUnknownEmail() throws Exception {
            // Given a registered user
            createTestUser("test@example.com", "password");

            // When the user attempts to log in with unknown email
            LoginRequest loginRequest = new LoginRequest();
            loginRequest.setEmail("unknown.email@example.com");
            loginRequest.setPassword("password");

            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest)))
            // Then the user should not be logged in (unauthorized)
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("When the user provides invalid credentials, then the user should not be logged in")
        void shouldNotLoginUserWithInvalidCredentials() throws Exception {
            // Given a registered user
            createTestUser("test@example.com", "password");

            // When the user attempts to log in with invalid credentials
            LoginRequest loginRequest = new LoginRequest();
            loginRequest.setEmail("test@example.com");
            loginRequest.setPassword("wrongpassword");

            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest)))
            // Then the user should not be logged in (unauthorized)
                    .andExpect(status().isUnauthorized());
        }
    }

    private void createTestUser(String email, String rawPassword) {
        User user = new User(
            email, 
            "Demo", 
            "Test", 
            passwordEncoder.encode(rawPassword), 
            false);
        userRepository.save(user);
    }
}
