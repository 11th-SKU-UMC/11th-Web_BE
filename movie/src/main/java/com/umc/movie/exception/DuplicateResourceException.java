package com.umc.movie.exception;

public class DuplicateResourceException extends RuntimeException {
    private final DuplicateError error;

    public DuplicateResourceException(DuplicateError error) {
        super(error.message());
        this.error = error;
    }

    public DuplicateError error() {
        return error;
    }
}
