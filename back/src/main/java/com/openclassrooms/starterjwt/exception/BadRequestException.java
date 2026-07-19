package com.openclassrooms.starterjwt.exception;

public class BadRequestException extends RuntimeException {
    private static final String DEFAULT_MESSAGE =
            "The request contains an invalid value.";

    public BadRequestException() {
        super(DEFAULT_MESSAGE);
    }

    public BadRequestException(String message) {
        super(message);
    }
}
