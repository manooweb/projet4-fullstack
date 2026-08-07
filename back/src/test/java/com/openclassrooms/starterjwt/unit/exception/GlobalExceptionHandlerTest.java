package com.openclassrooms.starterjwt.unit.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.openclassrooms.starterjwt.configuration.properties.YogaMessagesProperties;
import com.openclassrooms.starterjwt.configuration.properties.YogaProperties;
import com.openclassrooms.starterjwt.exception.BadRequestException;
import com.openclassrooms.starterjwt.exception.GlobalExceptionHandler;
import com.openclassrooms.starterjwt.exception.dto.ApiErrorResponse;

@DisplayName("Given that an API exception is handled")
class GlobalExceptionHandlerTest {

    private static final String BAD_REQUEST = "Bad request";
    private static final String INVALID_REQUEST = "The request is invalid.";
    private static final String INTERNAL_SERVER_ERROR = "Internal Server Error";
    private static final String UNEXPECTED_ERROR = "An unexpected error occurred.";

    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    void setUp() {
        YogaMessagesProperties messages = new YogaMessagesProperties();
        messages.getErrors().setBadRequest(BAD_REQUEST);
        messages.getErrors().setInvalidParameter("Invalid parameter: %s.");
        messages.getErrors().setInvalidRequest(INVALID_REQUEST);
        messages.getErrors().setInternalServerError(INTERNAL_SERVER_ERROR);
        messages.getErrors().setUnexpected(UNEXPECTED_ERROR);

        globalExceptionHandler = new GlobalExceptionHandler(new YogaProperties(messages));
    }

    @Test
    @DisplayName("When a path parameter has an invalid type, then a detailed bad request is returned")
    void shouldHandleMethodArgumentTypeMismatchException() {
        MethodArgumentTypeMismatchException exception = mock(MethodArgumentTypeMismatchException.class);
        when(exception.getName()).thenReturn("id");
        MockHttpServletRequest request = requestFor("/api/session/not-a-number");

        ApiErrorResponse response = globalExceptionHandler
                .handleMethodArgumentTypeMismatchException(exception, request);

        assertEquals(
                new ApiErrorResponse(400, BAD_REQUEST, "Invalid parameter: id.", "/api/session/not-a-number"),
                response);
    }

    @Test
    @DisplayName("When a request body cannot be read, then a generic bad request is returned")
    void shouldHandleHttpMessageNotReadableException() {
        MockHttpServletRequest request = requestFor("/api/session");

        ApiErrorResponse response = globalExceptionHandler.handleHttpMessageNotReadableException(request);

        assertEquals(new ApiErrorResponse(400, BAD_REQUEST, INVALID_REQUEST, "/api/session"), response);
    }

    @Test
    @DisplayName("When a bad request has no message, then the generic invalid request message is returned")
    void shouldHandleBadRequestExceptionWithoutMessage() {
        MockHttpServletRequest request = requestFor("/api/session/1/participate/2");

        ApiErrorResponse response = globalExceptionHandler
                .handleBadRequestException(new BadRequestException((String) null), request);

        assertEquals(
                new ApiErrorResponse(400, BAD_REQUEST, INVALID_REQUEST, "/api/session/1/participate/2"),
                response);
    }

    @Test
    @DisplayName("When an unexpected exception occurs, then a generic internal server error is returned")
    void shouldHandleUnexpectedException() {
        MockHttpServletRequest request = requestFor("/api/session");

        ApiErrorResponse response = globalExceptionHandler.handleException(
                new RuntimeException("Unexpected test exception"),
                request);

        assertEquals(
                new ApiErrorResponse(500, INTERNAL_SERVER_ERROR, UNEXPECTED_ERROR, "/api/session"),
                response);
    }

    private MockHttpServletRequest requestFor(String requestUri) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI(requestUri);
        return request;
    }
}
