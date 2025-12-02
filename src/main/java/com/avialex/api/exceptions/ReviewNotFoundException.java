package com.avialex.api.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ReviewNotFoundException extends RuntimeException {

    public ReviewNotFoundException() {
        super("Review não encontrada");
    }

    public ReviewNotFoundException(Long id) {
        super("Review não encontrada. id=" + id);
    }

    public ReviewNotFoundException(String message) {
        super(message);
    }

    public static ReviewNotFoundException byId(Long id) {
        return new ReviewNotFoundException(id);
    }
}