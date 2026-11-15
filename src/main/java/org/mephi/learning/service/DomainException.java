package org.mephi.learning.service;

import org.springframework.http.HttpStatus;

public class DomainException extends RuntimeException {
    private final HttpStatus status;

    public DomainException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public static DomainException missing(String type, Long id) {
        return new DomainException(HttpStatus.NOT_FOUND, type + " " + id + " was not found");
    }

    public static DomainException missing(String type, String identifier) {
        return new DomainException(
                HttpStatus.NOT_FOUND, type + " " + identifier + " was not found");
    }

    public static DomainException conflict(String message) {
        return new DomainException(HttpStatus.CONFLICT, message);
    }

    public static DomainException invalid(String message) {
        return new DomainException(HttpStatus.BAD_REQUEST, message);
    }
}
