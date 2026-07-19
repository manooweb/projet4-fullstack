package com.openclassrooms.starterjwt.security.handler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openclassrooms.starterjwt.configuration.properties.YogaProperties;
import com.openclassrooms.starterjwt.exception.dto.ApiErrorResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class ApiAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper objectMapper;
    private final YogaProperties yogaProperties;

    public ApiAuthenticationEntryPoint(ObjectMapper objectMapper, YogaProperties yogaProperties) {
        this.objectMapper = objectMapper;
        this.yogaProperties = yogaProperties;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authenticationException) throws IOException {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        ApiErrorResponse errorResponse = new ApiErrorResponse(
                status.value(),
                yogaProperties.getMessages().getErrors().getUnauthorized(),
                yogaProperties.getMessages().getErrors().getNotAuthorized(),
                request.getRequestURI());

        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }
}
