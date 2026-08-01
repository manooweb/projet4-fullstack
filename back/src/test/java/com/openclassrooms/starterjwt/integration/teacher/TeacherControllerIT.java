package com.openclassrooms.starterjwt.integration.teacher;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
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
import com.openclassrooms.starterjwt.integration.support.TeacherTestHelper;
import com.openclassrooms.starterjwt.integration.support.UserTestHelper;
import com.openclassrooms.starterjwt.models.Teacher;
import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.repository.TeacherRepository;
import com.openclassrooms.starterjwt.repository.UserRepository;

@AutoConfigureMockMvc
@Transactional
public class TeacherControllerIT extends IntegrationTestSupport {

    private static final String RAW_PASSWORD = "password";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private TeacherTestHelper teacherTestHelper;
    private String token;

    @BeforeEach
    void setUp() throws Exception {
        JwtTestHelper jwtTestHelper = new JwtTestHelper(mockMvc, objectMapper);
        UserTestHelper userTestHelper = new UserTestHelper(userRepository, passwordEncoder);
        teacherTestHelper = new TeacherTestHelper(teacherRepository);

        User authenticatedUser = userTestHelper.createTestUser(
                "authenticated@example.com",
                RAW_PASSWORD);
        token = jwtTestHelper.getBearerToken(authenticatedUser.getEmail(), RAW_PASSWORD);
    }

    @Nested
    @DisplayName("Given that an authenticated user wants to find a teacher by its ID")
    class FindByIdTests {

        @Test
        @DisplayName("When the requested teacher exists, then the teacher should be found")
        void shouldFindTeacherById() throws Exception {
            // Given an existing teacher
            String expectedFirstName = "John";
            String expectedLastName = "Doe";
            Teacher teacher = teacherTestHelper.createTestTeacher(expectedFirstName, expectedLastName);

            // When the authenticated user attempts to find the teacher by its ID
            mockMvc.perform(get("/api/teacher/{id}", teacher.getId())
                    .header(HttpHeaders.AUTHORIZATION, token))
                    // Then the teacher should be found
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(teacher.getId()))
                    .andExpect(jsonPath("$.firstName").value(expectedFirstName))
                    .andExpect(jsonPath("$.lastName").value(expectedLastName));
        }

        @Test
        @DisplayName("When the requested teacher does not exist, then a not found error should be returned")
        void shouldReturnNotFoundForUnknownTeacherId() throws Exception {
            // Given an unknown teacher ID
            long unknownTeacherId = Long.MAX_VALUE;

            // When the authenticated user attempts to find the unknown teacher
            mockMvc.perform(get("/api/teacher/{id}", unknownTeacherId)
                    .header(HttpHeaders.AUTHORIZATION, token))
                    // Then a not found error should be returned
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.error").value("Not Found"))
                    .andExpect(jsonPath("$.message")
                            .value("Teacher with id %d was not found.".formatted(unknownTeacherId)))
                    .andExpect(jsonPath("$.path").value("/api/teacher/" + unknownTeacherId));
        }
    }

    @Nested
    @DisplayName("Given that an authenticated user wants to retrieve all teachers")
    class FindAllTests {

        @Test
        @DisplayName("When teachers exist, then all teachers should be returned")
        void shouldFindAllTeachers() throws Exception {
            // Given existing teachers
            Teacher firstTeacher = teacherTestHelper.createTestTeacher("John", "Doe");
            Teacher secondTeacher = teacherTestHelper.createTestTeacher("Jane", "Smith");

            // When the authenticated user requests all teachers
            mockMvc.perform(get("/api/teacher")
                    .header(HttpHeaders.AUTHORIZATION, token))
                    // Then all teachers should be returned regardless of their order
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[*].id", containsInAnyOrder(
                            firstTeacher.getId().intValue(),
                            secondTeacher.getId().intValue())))
                    .andExpect(jsonPath("$[*].firstName", containsInAnyOrder("John", "Jane")))
                    .andExpect(jsonPath("$[*].lastName", containsInAnyOrder("Doe", "Smith")));
        }

        @Test
        @DisplayName("When no teacher exists, then an empty list should be returned")
        void shouldReturnEmptyListWhenNoTeacherExists() throws Exception {
            // When the authenticated user requests all teachers
            mockMvc.perform(get("/api/teacher")
                    .header(HttpHeaders.AUTHORIZATION, token))
                    // Then an empty list should be returned
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").isEmpty());
        }
    }
}
