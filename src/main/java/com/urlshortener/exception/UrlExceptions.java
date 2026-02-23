package com.urlshortener.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class UrlExceptions {

    // if short code or custom alias is not found in the database.
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public static class UrlNotFoundException extends RuntimeException {

        public UrlNotFoundException(String message) {
            super(message);
        }
    }


    //will thrown when user tries to create a custom alias that already exists.
    @ResponseStatus(HttpStatus.CONFLICT)
    public static class DuplicateAliasException extends RuntimeException {
        public DuplicateAliasException(String message) {
            super(message);
        }
    }


    //it will be thrown when a short URL exists but has EXPIRED
    // or has been DEACTIVATED by the owner.
    @ResponseStatus(HttpStatus.GONE)
    public static class UrlExpiredException extends RuntimeException {
        public UrlExpiredException(String message) {
            super(message);
        }
    }

    //if user provides invalid input that passes @Valid checks but fails our business logic validation.
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class InvalidUrlException extends RuntimeException {
        public InvalidUrlException(String message) {
            super(message);
        }
    }
}
