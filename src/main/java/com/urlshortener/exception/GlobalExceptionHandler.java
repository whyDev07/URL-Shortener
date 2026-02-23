package com.urlshortener.exception;

import com.urlshortener.dto.UrlDto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.HashMap;
import java.util.Map;

//Instead of writing try-catch in EVERY controller method, we'll handle ALL exceptions in one place this class.
@RestControllerAdvice
public class GlobalExceptionHandler {
    //if URL not found
    @ExceptionHandler(UrlExceptions.UrlNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleUrlNotFound(
            UrlExceptions.UrlNotFoundException ex) {

        //ApiResponse.error() creates: {success: false, message: "...", data: null}
        ApiResponse<Object> response = ApiResponse.error(ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);
    }

    //for handling duplicate aliases
    @ExceptionHandler(UrlExceptions.DuplicateAliasException.class)
    public ResponseEntity<ApiResponse<Object>> handleDuplicateAlias(
            UrlExceptions.DuplicateAliasException ex) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ex.getMessage()));
    }

    // handling expiration of URL
    @ExceptionHandler(UrlExceptions.UrlExpiredException.class)
    public ResponseEntity<ApiResponse<Object>> handleUrlExpired(
            UrlExceptions.UrlExpiredException ex) {

        return ResponseEntity
                .status(HttpStatus.GONE)
                .body(ApiResponse.error(ex.getMessage()));
    }

   // to handle invalid URL's
    @ExceptionHandler(UrlExceptions.InvalidUrlException.class)
    public ResponseEntity<ApiResponse<Object>> handleInvalidUrl(
            UrlExceptions.InvalidUrlException ex) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage()));
    }


     //Handles MethodArgumentNotValidException
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationErrors(
            MethodArgumentNotValidException ex) {

        //collects all field-level validation errors into a Map
        Map<String, String> errors = new HashMap<>();

        // ex.getBindingResult() contains all the validation failures
        // getFieldErrors() returns a list of FieldError objects
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            // fieldError.getField()   → "originalUrl"(which field failed)
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        ApiResponse<Map<String, String>> response =
                new ApiResponse<>(false, "Validation failed!!", errors);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    //for handling ALL OTHER unhandled exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(Exception ex) {

        //logging the actual error
        System.err.println("Unexpected error: " + ex.getMessage());
        ex.printStackTrace();

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("An unexpected error occurred. Please try again later :("));
    }
}
