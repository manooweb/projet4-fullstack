package com.openclassrooms.starterjwt.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.openclassrooms.starterjwt.configuration.properties.YogaProperties;
import com.openclassrooms.starterjwt.exception.dto.ApiErrorResponse;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    private final YogaProperties yogaProperties;

    public GlobalExceptionHandler(YogaProperties yogaProperties) {
        this.yogaProperties = yogaProperties;
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleBadRequestException(BadRequestException ex, HttpServletRequest request) {
        return errorResponse(
                HttpStatus.BAD_REQUEST,
                yogaProperties.getMessages().getErrors().getBadRequest(),
                ex.getMessage() != null
                        ? ex.getMessage()
                        : yogaProperties.getMessages().getErrors().getInvalidRequest(),
                request.getRequestURI());
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiErrorResponse handleNotFoundException(NotFoundException ex, HttpServletRequest request) {
        return errorResponse(
                HttpStatus.NOT_FOUND,
                yogaProperties.getMessages().getErrors().getNotFound(),
                ex.getMessage(),
                request.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {
        return errorResponse(
                HttpStatus.BAD_REQUEST,
                yogaProperties.getMessages().getErrors().getBadRequest(),
                yogaProperties.getMessages().getErrors().getInvalidParameter().formatted(ex.getName()),
                request.getRequestURI());
    }

    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiErrorResponse handleForbiddenException(ForbiddenException ex, HttpServletRequest request) {
        return errorResponse(
                HttpStatus.FORBIDDEN,
                yogaProperties.getMessages().getErrors().getForbidden(),
                ex.getMessage(),
                request.getRequestURI());
    }

    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiErrorResponse handleBadCredentialsException(BadCredentialsException ex, HttpServletRequest request) {
        return errorResponse(
                HttpStatus.UNAUTHORIZED,
                yogaProperties.getMessages().getErrors().getUnauthorized(),
                yogaProperties.getMessages().getErrors().getInvalidCredentials(),
                request.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleMethodArgumentNotValidException(HttpServletRequest request) {
        return errorResponse(
                HttpStatus.BAD_REQUEST,
                yogaProperties.getMessages().getErrors().getBadRequest(),
                yogaProperties.getMessages().getErrors().getInvalidRequest(),
                request.getRequestURI());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleHttpMessageNotReadableException(HttpServletRequest request) {
        return errorResponse(
                HttpStatus.BAD_REQUEST,
                yogaProperties.getMessages().getErrors().getBadRequest(),
                yogaProperties.getMessages().getErrors().getInvalidRequest(),
                request.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiErrorResponse handleException(Exception ex, HttpServletRequest request) {
        log.error(
                "Unexpected error while processing request {}",
                request.getRequestURI(),
                ex);
        return errorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                yogaProperties.getMessages().getErrors().getInternalServerError(),
                yogaProperties.getMessages().getErrors().getUnexpected(),
                request.getRequestURI());
    }

    private ApiErrorResponse errorResponse(HttpStatus status, String error, String message, String path) {
        return new ApiErrorResponse(status.value(), error, message, path);
    }
}
