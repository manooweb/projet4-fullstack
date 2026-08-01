package com.openclassrooms.starterjwt.integration.session;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.Date;
import java.util.List;

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
import com.openclassrooms.starterjwt.integration.support.SessionTestHelper;
import com.openclassrooms.starterjwt.integration.support.TeacherTestHelper;
import com.openclassrooms.starterjwt.integration.support.UserTestHelper;
import com.openclassrooms.starterjwt.models.Session;
import com.openclassrooms.starterjwt.models.Teacher;
import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.repository.SessionRepository;
import com.openclassrooms.starterjwt.repository.TeacherRepository;
import com.openclassrooms.starterjwt.repository.UserRepository;

@AutoConfigureMockMvc
@Transactional
class SessionParticipationIT extends IntegrationTestSupport {

    private static final String RAW_PASSWORD = "password";
    private static final Date SESSION_DATE = Date.from(Instant.parse("2026-08-10T09:00:00Z"));

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private UserTestHelper userTestHelper;
    private TeacherTestHelper teacherTestHelper;
    private SessionTestHelper sessionTestHelper;
    private String token;

    @BeforeEach
    void setUp() throws Exception {
        JwtTestHelper jwtTestHelper = new JwtTestHelper(mockMvc, objectMapper);
        userTestHelper = new UserTestHelper(userRepository, passwordEncoder);
        teacherTestHelper = new TeacherTestHelper(teacherRepository);
        sessionTestHelper = new SessionTestHelper(sessionRepository);

        User authenticatedUser = userTestHelper.createTestUser(
                "authenticated@example.com",
                RAW_PASSWORD);
        token = jwtTestHelper.getBearerToken(authenticatedUser.getEmail(), RAW_PASSWORD);
    }

    @Nested
    @DisplayName("Given that an authenticated user wants to participate in a session")
    class ParticipateTests {

        @Test
        @DisplayName("When the session and user exist and the user is not participating, then the user should participate")
        void shouldAddUserToSessionParticipants() throws Exception {
            // Given an existing session and a user who does not participate yet
            Session session = createTestSession();
            User participant = userTestHelper.createTestUser("participant@example.com", RAW_PASSWORD);

            // When the authenticated user requests the participation
            mockMvc.perform(post("/api/session/{id}/participate/{userId}", session.getId(), participant.getId())
                    .header(HttpHeaders.AUTHORIZATION, token))
                    // Then the user should participate in the session
                    .andExpect(status().isOk());

            Session updatedSession = sessionRepository.findById(session.getId()).orElseThrow();
            assertTrue(updatedSession.getUsers().stream()
                    .anyMatch(user -> user.getId().equals(participant.getId())));
        }

        @Test
        @DisplayName("When the session does not exist, then a not found error should be returned")
        void shouldReturnNotFoundForUnknownSession() throws Exception {
            // Given an existing user and an unknown session ID
            User participant = userTestHelper.createTestUser("participant@example.com", RAW_PASSWORD);
            long unknownSessionId = Long.MAX_VALUE;

            // When the authenticated user requests the participation
            mockMvc.perform(post("/api/session/{id}/participate/{userId}", unknownSessionId, participant.getId())
                    .header(HttpHeaders.AUTHORIZATION, token))
                    // Then a not found error should be returned
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.error").value("Not Found"))
                    .andExpect(jsonPath("$.message")
                            .value("Session with id %d was not found.".formatted(unknownSessionId)))
                    .andExpect(jsonPath("$.path")
                            .value("/api/session/%d/participate/%d".formatted(unknownSessionId, participant.getId())));
        }

        @Test
        @DisplayName("When the user does not exist, then a not found error should be returned")
        void shouldReturnNotFoundForUnknownUser() throws Exception {
            // Given an existing session and an unknown user ID
            Session session = createTestSession();
            long unknownUserId = Long.MAX_VALUE;

            // When the authenticated user requests the participation
            mockMvc.perform(post("/api/session/{id}/participate/{userId}", session.getId(), unknownUserId)
                    .header(HttpHeaders.AUTHORIZATION, token))
                    // Then a not found error should be returned
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.error").value("Not Found"))
                    .andExpect(jsonPath("$.message")
                            .value("User with id %d was not found.".formatted(unknownUserId)))
                    .andExpect(jsonPath("$.path")
                            .value("/api/session/%d/participate/%d".formatted(session.getId(), unknownUserId)));
        }

        @Test
        @DisplayName("When the user already participates, then a bad request error should be returned")
        void shouldReturnBadRequestForExistingParticipation() throws Exception {
            // Given a session with an existing participant
            User participant = userTestHelper.createTestUser("participant@example.com", RAW_PASSWORD);
            Session session = createTestSession(participant);

            // When the authenticated user requests the participation again
            mockMvc.perform(post("/api/session/{id}/participate/{userId}", session.getId(), participant.getId())
                    .header(HttpHeaders.AUTHORIZATION, token))
                    // Then a bad request error should be returned
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.message").value(
                            "User with id %d already participate to the session with id %d."
                                    .formatted(participant.getId(), session.getId())))
                    .andExpect(jsonPath("$.path")
                            .value("/api/session/%d/participate/%d".formatted(session.getId(), participant.getId())));
        }
    }

    @Nested
    @DisplayName("Given that an authenticated user wants to leave a session")
    class NoLongerParticipateTests {

        @Test
        @DisplayName("When the user participates in the session, then the user should no longer participate")
        void shouldRemoveUserFromSessionParticipants() throws Exception {
            // Given a session with an existing participant
            User participant = userTestHelper.createTestUser("participant@example.com", RAW_PASSWORD);
            Session session = createTestSession(participant);

            // When the authenticated user requests the user's withdrawal
            mockMvc.perform(delete("/api/session/{id}/participate/{userId}", session.getId(), participant.getId())
                    .header(HttpHeaders.AUTHORIZATION, token))
                    // Then the user should no longer participate in the session
                    .andExpect(status().isOk());

            Session updatedSession = sessionRepository.findById(session.getId()).orElseThrow();
            assertFalse(updatedSession.getUsers().stream()
                    .anyMatch(user -> user.getId().equals(participant.getId())));
        }

        @Test
        @DisplayName("When the session does not exist, then a not found error should be returned")
        void shouldReturnNotFoundForUnknownSession() throws Exception {
            // Given an existing user and an unknown session ID
            User participant = userTestHelper.createTestUser("participant@example.com", RAW_PASSWORD);
            long unknownSessionId = Long.MAX_VALUE;

            // When the authenticated user requests the user's withdrawal
            mockMvc.perform(delete("/api/session/{id}/participate/{userId}", unknownSessionId, participant.getId())
                    .header(HttpHeaders.AUTHORIZATION, token))
                    // Then a not found error should be returned
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.error").value("Not Found"))
                    .andExpect(jsonPath("$.message")
                            .value("Session with id %d was not found.".formatted(unknownSessionId)))
                    .andExpect(jsonPath("$.path")
                            .value("/api/session/%d/participate/%d".formatted(unknownSessionId, participant.getId())));
        }

        @Test
        @DisplayName("When the user does not participate, then a bad request error should be returned")
        void shouldReturnBadRequestForUserWhoDoesNotParticipate() throws Exception {
            // Given an existing session and a user who does not participate
            Session session = createTestSession();
            User user = userTestHelper.createTestUser("user@example.com", RAW_PASSWORD);

            // When the authenticated user requests the user's withdrawal
            mockMvc.perform(delete("/api/session/{id}/participate/{userId}", session.getId(), user.getId())
                    .header(HttpHeaders.AUTHORIZATION, token))
                    // Then a bad request error should be returned
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.message").value(
                            "User with id %d is not participating in the session with id %d."
                                    .formatted(user.getId(), session.getId())))
                    .andExpect(jsonPath("$.path")
                            .value("/api/session/%d/participate/%d".formatted(session.getId(), user.getId())));
        }

        @Test
        @DisplayName("When the user does not exist, then a bad request error should be returned")
        void shouldReturnBadRequestForUnknownUser() throws Exception {
            // Given an existing session and an unknown user ID
            Session session = createTestSession();
            long unknownUserId = Long.MAX_VALUE;

            // When the authenticated user requests the unknown user's withdrawal
            mockMvc.perform(delete("/api/session/{id}/participate/{userId}", session.getId(), unknownUserId)
                    .header(HttpHeaders.AUTHORIZATION, token))
                    // Then the current service behavior should be characterized
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.message").value(
                            "User with id %d is not participating in the session with id %d."
                                    .formatted(unknownUserId, session.getId())))
                    .andExpect(jsonPath("$.path")
                            .value("/api/session/%d/participate/%d".formatted(session.getId(), unknownUserId)));
        }
    }

    private Session createTestSession(User... users) {
        Teacher teacher = teacherTestHelper.createTestTeacher("John", "Doe");
        return sessionTestHelper.createTestSession(
                "Morning yoga",
                SESSION_DATE,
                "A morning yoga session",
                teacher,
                List.of(users));
    }
}
