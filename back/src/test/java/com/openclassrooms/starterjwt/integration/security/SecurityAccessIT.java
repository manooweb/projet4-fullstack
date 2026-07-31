package com.openclassrooms.starterjwt.integration.security;

import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.servlet.MockMvc;

import com.openclassrooms.starterjwt.integration.IntegrationTestSupport;

@AutoConfigureMockMvc
class SecurityAccessIT extends IntegrationTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @ParameterizedTest(name = "{0} {1} should return 401")
    @MethodSource("protectedEndpoints")
    void shouldRejectUnauthenticatedRequests(
            HttpMethod method,
            String endpoint
    ) throws Exception {
        mockMvc.perform(request(method, endpoint))
                .andExpect(status().isUnauthorized());
    }

    static Stream<Arguments> protectedEndpoints() {
        return Stream.of(
                arguments(HttpMethod.GET, "/api/user/1"),
                arguments(HttpMethod.GET, "/api/teacher"),
                arguments(HttpMethod.GET, "/api/session")
        );
    }
}