package com.openclassrooms.starterjwt.integration.session;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openclassrooms.starterjwt.dto.SessionDto;
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
class SessionManagementIT extends IntegrationTestSupport {

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

        private TeacherTestHelper teacherTestHelper;
        private SessionTestHelper sessionTestHelper;
        private String token;

        @BeforeEach
        void setUp() throws Exception {
                JwtTestHelper jwtTestHelper = new JwtTestHelper(mockMvc, objectMapper);
                UserTestHelper userTestHelper = new UserTestHelper(userRepository, passwordEncoder);
                teacherTestHelper = new TeacherTestHelper(teacherRepository);
                sessionTestHelper = new SessionTestHelper(sessionRepository);

                User authenticatedUser = userTestHelper.createTestUser(
                                "authenticated@example.com",
                                RAW_PASSWORD);
                token = jwtTestHelper.getBearerToken(authenticatedUser.getEmail(), RAW_PASSWORD);
        }

        @Nested
        @DisplayName("Given that an authenticated user wants to create a session")
        class CreateTests {

                @Test
                @DisplayName("When the session payload is valid, then the session should be created")
                void shouldCreateSession() throws Exception {
                        // Given an existing teacher and a valid session payload
                        Teacher teacher = teacherTestHelper.createTestTeacher("John", "Doe");
                        SessionDto sessionDto = sessionDto(
                                        "Morning yoga",
                                        SESSION_DATE,
                                        "A morning yoga session",
                                        teacher.getId());

                        // When the authenticated user creates the session
                        mockMvc.perform(post("/api/session")
                                        .header(HttpHeaders.AUTHORIZATION, token)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(sessionDto)))
                                        // Then the session should be created
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.id").isNumber())
                                        .andExpect(jsonPath("$.name").value("Morning yoga"))
                                        .andExpect(jsonPath("$.description").value("A morning yoga session"))
                                        .andExpect(jsonPath("$.teacher_id").value(teacher.getId()))
                                        .andExpect(jsonPath("$.users").isArray())
                                        .andExpect(jsonPath("$.users").isEmpty());

                        assertTrue(sessionRepository.findAll().stream()
                                        .anyMatch(session -> session.getName().equals("Morning yoga")));
                }

                @Test
                @DisplayName("When the teacher does not exist, then a not found error should be returned")
                void shouldReturnNotFoundForUnknownTeacher() throws Exception {
                        // Given a session payload with an unknown teacher
                        long unknownTeacherId = Long.MAX_VALUE;
                        SessionDto sessionDto = sessionDto(
                                        "Morning yoga",
                                        SESSION_DATE,
                                        "A morning yoga session",
                                        unknownTeacherId);

                        // When the authenticated user creates the session
                        mockMvc.perform(post("/api/session")
                                        .header(HttpHeaders.AUTHORIZATION, token)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(sessionDto)))
                                        // Then a not found error should be returned
                                        .andExpect(status().isNotFound())
                                        .andExpect(jsonPath("$.status").value(404))
                                        .andExpect(jsonPath("$.error").value("Not Found"))
                                        .andExpect(jsonPath("$.message")
                                                        .value("Teacher with id %d was not found."
                                                                        .formatted(unknownTeacherId)))
                                        .andExpect(jsonPath("$.path").value("/api/session"));
                }

                @Test
                @DisplayName("When the teacher already belongs to a session, then a conflict error should be returned")
                void shouldReturnConflictWhenTeacherAlreadyAssignedToSession() throws Exception {
                        // Given a teacher who already belongs to a session
                        Teacher teacher = teacherTestHelper.createTestTeacher("John", "Doe");
                        Session existingSession = sessionTestHelper.createTestSession(
                                        "Existing yoga session",
                                        SESSION_DATE,
                                        "An existing yoga session",
                                        teacher);
                        SessionDto sessionDto = sessionDto(
                                        "Another yoga session",
                                        Date.from(Instant.parse("2026-08-11T09:00:00Z")),
                                        "Another yoga session",
                                        teacher.getId());

                        // When the authenticated user creates another session with the same teacher
                        mockMvc.perform(post("/api/session")
                                        .header(HttpHeaders.AUTHORIZATION, token)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(sessionDto)))
                                        // Then a conflict error should be returned
                                        .andExpect(status().isConflict())
                                        .andExpect(jsonPath("$.status").value(409))
                                        .andExpect(jsonPath("$.error").value("Conflict"))
                                        .andExpect(jsonPath("$.message")
                                                        .value("Teacher with id %d is already assigned to the session with id %d."
                                                                        .formatted(teacher.getId(), existingSession.getId())))
                                        .andExpect(jsonPath("$.path").value("/api/session"));
                }

                @ParameterizedTest(name = "{0}")
                @MethodSource("com.openclassrooms.starterjwt.integration.session.SessionManagementIT#invalidSessionPayloadMutators")
                @DisplayName("When the session payload is invalid, then a bad request error should be returned")
                void shouldReturnBadRequestForInvalidSessionPayload(
                                String scenario,
                                Consumer<SessionDto> invalidSessionPayloadMutator) throws Exception {
                        // Given a valid session payload made invalid by one validation rule
                        Teacher teacher = teacherTestHelper.createTestTeacher("John", "Doe");
                        SessionDto sessionDto = sessionDto(
                                        "Morning yoga",
                                        SESSION_DATE,
                                        "A morning yoga session",
                                        teacher.getId());
                        invalidSessionPayloadMutator.accept(sessionDto);

                        // When the authenticated user creates the session
                        mockMvc.perform(post("/api/session")
                                        .header(HttpHeaders.AUTHORIZATION, token)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(sessionDto)))
                                        // Then a bad request error should be returned
                                        .andExpect(status().isBadRequest())
                                        .andExpect(jsonPath("$.status").value(400))
                                        .andExpect(jsonPath("$.error").value("Bad Request"))
                                        .andExpect(jsonPath("$.message")
                                                        .value("The request contains an invalid value."))
                                        .andExpect(jsonPath("$.path").value("/api/session"));
                }
        }

        @Nested
        @DisplayName("Given that an authenticated user wants to update a session")
        class UpdateTests {

                @AfterEach
                void cleanUpCommittedData() {
                        sessionRepository.deleteAll();
                        teacherRepository.deleteAll();
                        userRepository.deleteAll();
                }

                @Test
                @DisplayName("When the session and payload are valid, then the session should be updated")
                void shouldUpdateSession() throws Exception {
                        // Given an existing session and another existing teacher
                        Teacher initialTeacher = teacherTestHelper.createTestTeacher("John", "Doe");
                        Teacher updatedTeacher = teacherTestHelper.createTestTeacher("Jane", "Smith");
                        Session session = sessionTestHelper.createTestSession(
                                        "Morning yoga",
                                        SESSION_DATE,
                                        "A morning yoga session",
                                        initialTeacher);
                        SessionDto sessionDto = sessionDto(
                                        "Evening yoga",
                                        Date.from(Instant.parse("2026-08-11T18:00:00Z")),
                                        "An evening yoga session",
                                        updatedTeacher.getId());

                        // When the authenticated user updates the session
                        mockMvc.perform(put("/api/session/{id}", session.getId())
                                        .header(HttpHeaders.AUTHORIZATION, token)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(sessionDto)))
                                        // Then the session should be updated
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.id").value(session.getId()))
                                        .andExpect(jsonPath("$.name").value("Evening yoga"))
                                        .andExpect(jsonPath("$.description").value("An evening yoga session"))
                                        .andExpect(jsonPath("$.teacher_id").value(updatedTeacher.getId()));

                        Session updatedSession = sessionRepository.findById(session.getId()).orElseThrow();
                        assertEquals("Evening yoga", updatedSession.getName());
                        assertEquals(updatedTeacher.getId(), updatedSession.getTeacher().getId());
                }

                @Test
                @DisplayName("When the session does not exist, then a not found error should be returned")
                void shouldReturnNotFoundForUnknownSession() throws Exception {
                        // Given a valid payload and an unknown session ID
                        Teacher teacher = teacherTestHelper.createTestTeacher("John", "Doe");
                        long unknownSessionId = Long.MAX_VALUE;
                        SessionDto sessionDto = sessionDto(
                                        "Morning yoga",
                                        SESSION_DATE,
                                        "A morning yoga session",
                                        teacher.getId());

                        // When the authenticated user updates the unknown session
                        mockMvc.perform(put("/api/session/{id}", unknownSessionId)
                                        .header(HttpHeaders.AUTHORIZATION, token)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(sessionDto)))
                                        // Then a not found error should be returned
                                        .andExpect(status().isNotFound())
                                        .andExpect(jsonPath("$.status").value(404))
                                        .andExpect(jsonPath("$.error").value("Not Found"))
                                        .andExpect(jsonPath("$.message")
                                                        .value("Session with id %d was not found."
                                                                        .formatted(unknownSessionId)))
                                        .andExpect(jsonPath("$.path").value("/api/session/" + unknownSessionId));
                }

                @ParameterizedTest(name = "{0}")
                @MethodSource("com.openclassrooms.starterjwt.integration.session.SessionManagementIT#invalidSessionPayloadMutators")
                @DisplayName("When the session payload is invalid, then a bad request error should be returned")
                void shouldReturnBadRequestForInvalidSessionPayload(
                                String scenario,
                                Consumer<SessionDto> invalidSessionPayloadMutator) throws Exception {
                        // Given an existing session and a valid payload made invalid by one validation
                        // rule
                        Teacher teacher = teacherTestHelper.createTestTeacher("John", "Doe");
                        Session session = sessionTestHelper.createTestSession(
                                        "Morning yoga",
                                        SESSION_DATE,
                                        "A morning yoga session",
                                        teacher);
                        SessionDto sessionDto = sessionDto(
                                        "Updated morning yoga",
                                        Date.from(Instant.parse("2026-08-11T18:00:00Z")),
                                        "An updated morning yoga session",
                                        teacher.getId());
                        invalidSessionPayloadMutator.accept(sessionDto);

                        // When the authenticated user updates the session
                        mockMvc.perform(put("/api/session/{id}", session.getId())
                                        .header(HttpHeaders.AUTHORIZATION, token)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(sessionDto)))
                                        // Then a bad request error should be returned
                                        .andExpect(status().isBadRequest())
                                        .andExpect(jsonPath("$.status").value(400))
                                        .andExpect(jsonPath("$.error").value("Bad Request"))
                                        .andExpect(jsonPath("$.message")
                                                        .value("The request contains an invalid value."))
                                        .andExpect(jsonPath("$.path").value("/api/session/" + session.getId()));
                }

                @Test
                @Transactional(propagation = Propagation.NOT_SUPPORTED)
                @DisplayName("When the teacher already belongs to another session, then a conflict error should be returned")
                void shouldReturnConflictErrorWhenUpdatingWithTeacherAlreadyAssignedToAnotherSession()
                                throws Exception {
                        // Given two sessions with different teachers
                        Teacher firstTeacher = teacherTestHelper.createTestTeacher("John", "Doe");
                        Teacher secondTeacher = teacherTestHelper.createTestTeacher("Jane", "Smith");
                        Session sessionToUpdate = sessionTestHelper.createTestSession(
                                        "Morning yoga",
                                        SESSION_DATE,
                                        "A morning yoga session",
                                        firstTeacher);
                        Session secondSession = sessionTestHelper.createTestSession(
                                        "Evening yoga",
                                        Date.from(Instant.parse("2026-08-11T18:00:00Z")),
                                        "An evening yoga session",
                                        secondTeacher);
                        SessionDto sessionDto = sessionDto(
                                        "Updated morning yoga",
                                        Date.from(Instant.parse("2026-08-12T09:00:00Z")),
                                        "An updated morning yoga session",
                                        secondTeacher.getId());

                        // When the authenticated user assigns the second teacher to the first session
                        mockMvc.perform(put("/api/session/{id}", sessionToUpdate.getId())
                                        .header(HttpHeaders.AUTHORIZATION, token)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(sessionDto)))
                                        // Then the current technical error should be characterized
                                        .andExpect(status().isConflict())
                                        .andExpect(jsonPath("$.status").value(409))
                                        .andExpect(jsonPath("$.error").value("Conflict"))
                                        .andExpect(jsonPath("$.message")
                                                        .value("Teacher with id %d is already assigned to the session with id %d."
                                                                        .formatted(secondTeacher.getId(), secondSession.getId())))

                                        .andExpect(jsonPath("$.path").value("/api/session/" + sessionToUpdate.getId()));
                }
        }

        @Nested
        @DisplayName("Given that an authenticated user wants to delete a session")
        class DeleteTests {

                @Test
                @DisplayName("When the session exists, then the session should be deleted")
                void shouldDeleteSession() throws Exception {
                        // Given an existing session
                        Teacher teacher = teacherTestHelper.createTestTeacher("John", "Doe");
                        Session session = sessionTestHelper.createTestSession(
                                        "Morning yoga",
                                        SESSION_DATE,
                                        "A morning yoga session",
                                        teacher);

                        // When the authenticated user deletes the session
                        mockMvc.perform(delete("/api/session/{id}", session.getId())
                                        .header(HttpHeaders.AUTHORIZATION, token))
                                        // Then the session should be deleted
                                        .andExpect(status().isOk());

                        assertFalse(sessionRepository.existsById(session.getId()));
                }

                @Test
                @DisplayName("When the session does not exist, then a not found error should be returned")
                void shouldReturnNotFoundForUnknownSession() throws Exception {
                        // Given an unknown session ID
                        long unknownSessionId = Long.MAX_VALUE;

                        // When the authenticated user deletes the unknown session
                        mockMvc.perform(delete("/api/session/{id}", unknownSessionId)
                                        .header(HttpHeaders.AUTHORIZATION, token))
                                        // Then a not found error should be returned
                                        .andExpect(status().isNotFound())
                                        .andExpect(jsonPath("$.status").value(404))
                                        .andExpect(jsonPath("$.error").value("Not Found"))
                                        .andExpect(jsonPath("$.message")
                                                        .value("Session with id %d was not found."
                                                                        .formatted(unknownSessionId)))
                                        .andExpect(jsonPath("$.path").value("/api/session/" + unknownSessionId));
                }
        }

        public static Stream<Arguments> invalidSessionPayloadMutators() {
                return Stream.of(
                                arguments("name is null",
                                                (Consumer<SessionDto>) sessionDto -> sessionDto.setName(null)),
                                arguments("name is blank", (Consumer<SessionDto>) sessionDto -> sessionDto.setName("")),
                                arguments("name is too long",
                                                (Consumer<SessionDto>) sessionDto -> sessionDto
                                                                .setName("a".repeat(51))),
                                arguments("date is null",
                                                (Consumer<SessionDto>) sessionDto -> sessionDto.setDate(null)),
                                arguments("teacher ID is null",
                                                (Consumer<SessionDto>) sessionDto -> sessionDto.setTeacher_id(null)),
                                arguments("description is null",
                                                (Consumer<SessionDto>) sessionDto -> sessionDto.setDescription(null)),
                                arguments("description is too long",
                                                (Consumer<SessionDto>) sessionDto -> sessionDto
                                                                .setDescription("a".repeat(2501))));
        }

        private SessionDto sessionDto(String name, Date date, String description, Long teacherId) {
                SessionDto sessionDto = new SessionDto();
                sessionDto.setName(name);
                sessionDto.setDate(date);
                sessionDto.setDescription(description);
                sessionDto.setTeacher_id(teacherId);
                sessionDto.setUsers(List.of());
                return sessionDto;
        }
}
