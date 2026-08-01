package com.openclassrooms.starterjwt.integration.user;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openclassrooms.starterjwt.integration.IntegrationTestSupport;
import com.openclassrooms.starterjwt.integration.support.JwtTestHelper;
import com.openclassrooms.starterjwt.integration.support.UserTestHelper;
import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.repository.UserRepository;

@AutoConfigureMockMvc
@Transactional
public class UserControllerIT extends IntegrationTestSupport {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private JwtTestHelper jwtTestHelper;
    private UserTestHelper userTestHelper;

    @BeforeEach
    void setUp() {
        jwtTestHelper = new JwtTestHelper(mockMvc, objectMapper);
        userTestHelper = new UserTestHelper(userRepository, passwordEncoder);
    }

    @Nested
    @DisplayName("Given that an authenticated user wants to find a user by its ID")
    class FindByIdTests {

        private static final String RAW_PASSWORD = "password";

        private String token;

        @BeforeEach
        void authenticateUser() throws Exception {
            User authenticatedUser = userTestHelper.createTestUser(
                    "authenticated@example.com",
                    RAW_PASSWORD);
            token = jwtTestHelper.getBearerToken(authenticatedUser.getEmail(), RAW_PASSWORD);
        }

        @Test
        @DisplayName("When the requested user exists, then the user should be found")
        void shouldFindUserById() throws Exception {
            // Given an existing requested user
            String requestedEmail = "requested@example.com";
            String expectedFirstName = "Test";
            String expectedLastName = "Demo";

            User requestedUser = userTestHelper.createTestUser(requestedEmail, RAW_PASSWORD);

            // When the authenticated user attempts to find the user by its ID
            mockMvc.perform(get("/api/user/{id}", requestedUser.getId())
                    .header(HttpHeaders.AUTHORIZATION, token))
                    // Then the user should be found
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(requestedUser.getId()))
                    .andExpect(jsonPath("$.email").value(requestedEmail))
                    .andExpect(jsonPath("$.firstName").value(expectedFirstName))
                    .andExpect(jsonPath("$.lastName").value(expectedLastName))
                    .andExpect(jsonPath("$.admin").value(false))
                    .andExpect(jsonPath("$.password").doesNotExist());
        }

        @Test
        @DisplayName("When the requested user does not exist, then a not found error should be returned")
        void shouldReturnNotFoundForUnknownUserId() throws Exception {
            // Given an unknown user ID
            long unknownUserId = Long.MAX_VALUE;

            // When the authenticated user attempts to find the unknown user
            mockMvc.perform(get("/api/user/{id}", unknownUserId)
                    .header(HttpHeaders.AUTHORIZATION, token))
                    // Then a not found error should be returned
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.error").value("Not Found"))
                    .andExpect(jsonPath("$.message")
                            .value("User with id %d was not found.".formatted(unknownUserId)))
                    .andExpect(jsonPath("$.path").value("/api/user/" + unknownUserId));
        }
    }

    @Nested
    @DisplayName("Given that an authenticated user wants to delete a user")
    class DeleteTests {

        private static final String RAW_PASSWORD = "password";

        private User authenticatedUser;
        private String token;

        @BeforeEach
        void authenticateUser() throws Exception {
            authenticatedUser = userTestHelper.createTestUser(
                    "authenticated@example.com",
                    RAW_PASSWORD);
            token = jwtTestHelper.getBearerToken(authenticatedUser.getEmail(), RAW_PASSWORD);
        }

        @Test
        @DisplayName("When the user requests their own deletion, then the user should be deleted")
        void shouldDeleteAuthenticatedUser() throws Exception {
            // When the authenticated user requests their own deletion
            mockMvc.perform(delete("/api/user/{id}", authenticatedUser.getId())
                    .header(HttpHeaders.AUTHORIZATION, token))
                    // Then the user should be deleted
                    .andExpect(status().isOk());

            assertFalse(userRepository.existsById(authenticatedUser.getId()));
        }

        @Test
        @DisplayName("When the user requests another user's deletion, then a forbidden error should be returned")
        void shouldReturnForbiddenWhenDeletingAnotherUser() throws Exception {
            // Given another existing user
            User otherUser = userTestHelper.createTestUser(
                    "other@example.com",
                    RAW_PASSWORD);

            // When the authenticated user requests the other user's deletion
            mockMvc.perform(delete("/api/user/{id}", otherUser.getId())
                    .header(HttpHeaders.AUTHORIZATION, token))
                    // Then a forbidden error should be returned
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status").value(403))
                    .andExpect(jsonPath("$.error").value("Forbidden"))
                    .andExpect(jsonPath("$.message")
                            .value("You are not allowed to delete this user."))
                    .andExpect(jsonPath("$.path").value("/api/user/" + otherUser.getId()));

            assertTrue(userRepository.existsById(otherUser.getId()));
        }

        @Test
        @DisplayName("When the requested user does not exist, then a not found error should be returned")
        void shouldReturnNotFoundWhenDeletingUnknownUser() throws Exception {
            // Given an unknown user ID
            long unknownUserId = Long.MAX_VALUE;

            // When the authenticated user requests the unknown user's deletion
            mockMvc.perform(delete("/api/user/{id}", unknownUserId)
                    .header(HttpHeaders.AUTHORIZATION, token))
                    // Then a not found error should be returned
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.error").value("Not Found"))
                    .andExpect(jsonPath("$.message")
                            .value("User with id %d was not found.".formatted(unknownUserId)))
                    .andExpect(jsonPath("$.path").value("/api/user/" + unknownUserId));
        }
    }
}
