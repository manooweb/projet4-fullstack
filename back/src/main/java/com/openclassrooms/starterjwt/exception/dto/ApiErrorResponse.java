package com.openclassrooms.starterjwt.exception.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ApiErrorResponse(
    int status,
    String error,
    String message,
    String path
){}
    