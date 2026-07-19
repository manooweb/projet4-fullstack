package com.openclassrooms.starterjwt.exception;

public class BadRequestException extends RuntimeException {
    public BadRequestException() {
        super("Bad Request");
    }

    public BadRequestException(String message) {
        super(message);
    }
}
