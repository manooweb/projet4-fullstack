package com.openclassrooms.starterjwt.integration.session;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.Date;

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
class SessionReadIT extends IntegrationTestSupport {

    private static final String RAW_PASSWORD = "password";

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
    @DisplayName("Given that an authenticated user wants to find a session by its ID")
    class FindByIdTests {

        @Test
        @DisplayName("When the requested session exists, then the session should be found")
        void shouldFindSessionById() throws Exception {
            // Given an existing session
            String expectedName = "Morning yoga";
            String expectedDescription = "A morning yoga session";
            Date sessionDate = Date.from(Instant.parse("2026-08-10T09:00:00Z"));
            Teacher teacher = teacherTestHelper.createTestTeacher("John", "Doe");
            Session session = sessionTestHelper.createTestSession(
                    expectedName,
                    sessionDate,
                    expectedDescription,
                    teacher);

            // When the authenticated user attempts to find the session by its ID
            mockMvc.perform(get("/api/session/{id}", session.getId())
                    .header(HttpHeaders.AUTHORIZATION, token))
                    // Then the session should be found
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(session.getId()))
                    .andExpect(jsonPath("$.name").value(expectedName))
                    .andExpect(jsonPath("$.date").isNotEmpty())
                    .andExpect(jsonPath("$.description").value(expectedDescription))
                    .andExpect(jsonPath("$.teacher_id").value(teacher.getId()))
                    .andExpect(jsonPath("$.users").isArray())
                    .andExpect(jsonPath("$.users").isEmpty());
        }

        @Test
        @DisplayName("When the requested session does not exist, then a not found error should be returned")
        void shouldReturnNotFoundForUnknownSessionId() throws Exception {
            // Given an unknown session ID
            long unknownSessionId = Long.MAX_VALUE;

            // When the authenticated user attempts to find the unknown session
            mockMvc.perform(get("/api/session/{id}", unknownSessionId)
                    .header(HttpHeaders.AUTHORIZATION, token))
                    // Then a not found error should be returned
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.error").value("Not Found"))
                    .andExpect(jsonPath("$.message")
                            .value("Session with id %d was not found.".formatted(unknownSessionId)))
                    .andExpect(jsonPath("$.path").value("/api/session/" + unknownSessionId));
        }
    }

    @Nested
    @DisplayName("Given that an authenticated user wants to retrieve all sessions")
    class FindAllTests {

        @Test
        @DisplayName("When sessions exist, then all sessions should be returned")
        void shouldFindAllSessions() throws Exception {
            // Given existing sessions
            // A teacher can belong to only one session because Session.teacher is a @OneToOne relation.
            Teacher firstTeacher = teacherTestHelper.createTestTeacher("John", "Doe");
            Teacher secondTeacher = teacherTestHelper.createTestTeacher("Jane", "Smith");
            Date firstDate = Date.from(Instant.parse("2026-08-10T09:00:00Z"));
            Date secondDate = Date.from(Instant.parse("2026-08-11T18:00:00Z"));

            Session firstSession = sessionTestHelper.createTestSession(
                    "Morning yoga",
                    firstDate,
                    "A morning yoga session",
                    firstTeacher);
            Session secondSession = sessionTestHelper.createTestSession(
                    "Evening yoga",
                    secondDate,
                    "An evening yoga session",
                    secondTeacher);

            // When the authenticated user requests all sessions
            mockMvc.perform(get("/api/session")
                    .header(HttpHeaders.AUTHORIZATION, token))
                    // Then all sessions should be returned regardless of their order
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[*].id", containsInAnyOrder(
                            firstSession.getId().intValue(),
                            secondSession.getId().intValue())))
                    .andExpect(jsonPath("$[*].name", containsInAnyOrder(
                            "Morning yoga",
                            "Evening yoga")))
                    .andExpect(jsonPath("$[*].description", containsInAnyOrder(
                            "A morning yoga session",
                            "An evening yoga session")));
        }

        @Test
        @DisplayName("When no session exists, then an empty list should be returned")
        void shouldReturnEmptyListWhenNoSessionExists() throws Exception {
            // When the authenticated user requests all sessions
            mockMvc.perform(get("/api/session")
                    .header(HttpHeaders.AUTHORIZATION, token))
                    // Then an empty list should be returned
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").isEmpty());
        }
    }
}
