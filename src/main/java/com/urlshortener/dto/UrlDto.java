package com.urlshortener.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

public class UrlDto {
    //Creating Url Request
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateUrlRequest {

        // @URL → Validates that the string is a valid URL format, will reject: "not-a-url", "http://broken", etc.
        @NotBlank(message = "Original URL is required")
        @URL(message = "Please provide a valid URL (must start with http:// or https://)")
        private String originalUrl;

        // Optional custom short code chosen by user.If not provided, we generate a random one.
        // @Pattern → regex validates only letters, numbers, and hyphens allowed
        //            ^[a-zA-Z0-9-]+$ means: start(^) to end($), only alphanumeric or hyphen
        @Size(min = 3, max = 20, message = "Custom alias must be between 3 and 20 characters")
        @Pattern(regexp = "^[a-zA-Z0-9-]*$",
                message = "Custom alias can only contain letters, numbers, and hyphens")
        private String customAlias;   //nullable - optional field

        //days until this URL expires.
        private Integer expiryDays;   // nullable/optional
    }

    //updating UrlRequest
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateUrlRequest {

        @URL(message = "Please provide a valid URL")
        private String originalUrl;   // nullable -optional in updates

        // Enable or disable this short URL without deleting it.
        // true = URL works | false = URL returns error when visited
        private Boolean isActive;     // nullable - optional in updates
    }


    /*
     * UrlResponse — The JSON we return after creating, fetching, or updating a URL.
     * Example JSON returned to client:
     * {
     *   "id": 1,
     *   "originalUrl": "https://www.youtube.com/wMOw4w9WgXcQ",
     *   "shortCode": "abc123",
     *   "shortUrl": "http://localhost:8080/s/abc123",
     *   "customAlias": "rick-roll",
     *   "clickCount": 42,
     *   "isActive": true,
     *   "expiresAt": "2024-12-31T23:59:59",
     *   "createdAt": "2024-06-01T10:30:00"
     * }
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UrlResponse {

        private Long id;
        private String originalUrl;
        private String shortCode;

        //This is CONSTRUCTED in the service layer,not stored in DB.
        private String shortUrl;

        private String customAlias;
        private Long clickCount;
        private Boolean isActive;
        private String expiresAt;
        private String createdAt;
        private String updatedAt;
    }

    // urlStatsResponse — Returned when user asks for stats of a URL.
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UrlStatsResponse {

        private String shortCode;
        private String shortUrl;
        private String originalUrl;
        private Long totalClicks;
        private Boolean isActive;
        private String createdAt;
        private String expiresAt;
    }

    // ApiResponse<T> — generic wrapper for ALL API responses.
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApiResponse<T> {

        private boolean success;
        private String message;
        private T data;   // T =any type, determined when used

        // Static factory method for success responses (convenience method)
        // Usage: ApiResponse.success("Created!", urlResponse)
        public static <T> ApiResponse<T> success(String message, T data) {
            return new ApiResponse<>(true, message, data);
        }

        // Static factory method for error responses
        // Usage: ApiResponse.error("Not found")
        public static <T> ApiResponse<T> error(String message) {
            return new ApiResponse<>(false, message, null);
        }
    }
}
