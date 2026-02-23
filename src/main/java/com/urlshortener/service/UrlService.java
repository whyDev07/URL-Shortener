package com.urlshortener.service;

import com.urlshortener.dto.UrlDto.*;
import com.urlshortener.entity.Url;
import com.urlshortener.exception.UrlExceptions.*;
import com.urlshortener.repository.UrlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor// generates the constructor that Spring uses to inject this.
public class UrlService {

    //No @autowired required, Constructor injection by Spring itself
    private final UrlRepository urlRepository;

    // @Value("${app.base-url}")
    //   Reads the 'app.base-url' property from application.properties and injects its value into this field.
    @Value("${app.base-url}")
    private String baseUrl;

    //application.properties: app.short-code-length=6
    @Value("${app.short-code-length}")
    private int shortCodeLength;

    // Characters used to generate short codes
    //62 characters total: a-z (26) + A-Z (26) + 0-9 (10)
    private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    //date formatter for converting LocalDateTime to readable strings
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


    @Transactional
    public UrlResponse createShortUrl(CreateUrlRequest request) {

        //short code to use
        String shortCode;

        if(request.getCustomAlias() != null && !request.getCustomAlias().isBlank()) {
            // user provided a custom alias — validate it's not taken
            if(urlRepository.existsByCustomAlias(request.getCustomAlias())) {
                //Custom exception will get caught by GlobalExceptionHandler → 409 CONFLICT
                throw new DuplicateAliasException(
                        "Custom alias '" + request.getCustomAlias() + "' is already taken. Please choose another.");
            }
            shortCode = request.getCustomAlias();
        } else {
            // no custom alias so generating a random unique short code
            shortCode = generateUniqueShortCode();
        }

        //Building the Url entity from the request
        Url url = new Url();
        url.setOriginalUrl(request.getOriginalUrl());
        url.setShortCode(shortCode);
        url.setCustomAlias(request.getCustomAlias());
        url.setClickCount(0L);
        url.setIsActive(true);

        // setting the expiry date if provided
        if (request.getExpiryDays() != null && request.getExpiryDays() > 0) {
            //Adding expiryDays to current time
            url.setExpiresAt(LocalDateTime.now().plusDays(request.getExpiryDays()));
        }
        //if expiryDays is null, url.expiresAt remains null = never expires

        //now Saving to database
        Url savedUrl = urlRepository.save(url);

        //Converting the saved entity to response DTO to return
        return convertToResponse(savedUrl);
    }

    //It will fetch URL details by short code(for displaying info, not redirecting).
    public UrlResponse getUrlByShortCode(String shortCode) {

        //returns the Url or throws the exception
        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException(
                        "No URL found with short code : " + shortCode));

        return convertToResponse(url);
    }

    //This will resolve a short code to the original URL for redirection and Also validates
    // the URL is active and not expired.Also increments the click counter.
    // @Transactional ensures the click count increment is saved atomically.
    @Transactional
    public String resolveUrl(String shortCode) {
        //finding url
        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException(
                        "Short URL not found: " + shortCode));

        //Checking if URL is active
        if (!url.getIsActive()) {
            throw new UrlExpiredException(
                    "This short URL has been deactivated.");
        }

        //now checking if URL has expired or not
        if (url.getExpiresAt() != null && url.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new UrlExpiredException(
                    "This short URL expired on " + url.getExpiresAt().format(FORMATTER));
        }

        //Incrementing click count
        urlRepository.incrementClickCount(url.getId());

        // finally returning original URL for redirection
        return url.getOriginalUrl();
    }

    //Returns all URLs in the database, newest first.
    public List<UrlResponse> getAllUrls() {
        return urlRepository.findByIsActiveOrderByCreatedAtDesc(true)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    //for returning the top 10 most clicked URLs
    public List<UrlResponse> getTopUrls() {
        return urlRepository.findTop10ByClickCount()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    //This Updates an existing URL's originalUrl or active status.
    // @Transactional ensures the update is saved atomically.
    @Transactional
    public UrlResponse updateUrl(String shortCode, UpdateUrlRequest request) {
        //finding the existing URL or throw 404
        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException(
                        "No URL found with short code: " + shortCode));

        // Only updating fields that are provided in the request(partial update)
        if (request.getOriginalUrl() != null && !request.getOriginalUrl().isBlank()) {
            url.setOriginalUrl(request.getOriginalUrl());
        }
        if (request.getIsActive() != null) {
            url.setIsActive(request.getIsActive());
        }

        // saving updated entity
        Url updatedUrl = urlRepository.save(url);

        return convertToResponse(updatedUrl);
    }


    //for deleting
    @Transactional
    public void deleteUrl(String shortCode) {

        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException(
                        "No URL found with short code: " + shortCode));

        urlRepository.deleteById(url.getId());
    }


    //this returns analytics/statistics for a specific URL.
    public UrlStatsResponse getUrlStats(String shortCode) {

        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException(
                        "No URL found with short code: " + shortCode));

        // building stats response
        UrlStatsResponse stats = new UrlStatsResponse();
        stats.setShortCode(url.getShortCode());
        stats.setShortUrl(buildShortUrl(url.getShortCode()));
        stats.setOriginalUrl(url.getOriginalUrl());
        stats.setTotalClicks(url.getClickCount());
        stats.setIsActive(url.getIsActive());
        stats.setCreatedAt(url.getCreatedAt() != null
                ? url.getCreatedAt().format(FORMATTER) : null);
        stats.setExpiresAt(url.getExpiresAt() != null
                ? url.getExpiresAt().format(FORMATTER) : "Never");

        return stats;
    }

    //helper Methods
    //Generates a random, unique short code.
    // With 62 possible characters and length 6:possible combinations = 62^6 = 56,800,235,584 (56 billion)
    //so collision becomes extremely rare
    private String generateUniqueShortCode() {
        String shortCode;
        int maxAttempts = 10;  //safety limit to avoid infinite loop
        int attempt = 0;

        do {
            shortCode = generateRandomCode(shortCodeLength);
            attempt++;

            if (attempt >= maxAttempts) {
                //this would only happen if database is almost full
                throw new RuntimeException("Could not generate unique short code after " + maxAttempts + " attempts");
            }

        } while (urlRepository.existsByShortCode(shortCode));
        //condition,keep trying as long as the code already exists

        return shortCode;
    }

    //for generating a random string of specified length from CHARACTERS.
    private String generateRandomCode(int length) {
        StringBuilder sb = new StringBuilder(length);
        Random random = new Random();

        for (int i = 0; i < length; i++) {
            //for random index between 0 and 61
            int randomIndex = random.nextInt(CHARACTERS.length());
            //character at that position
            sb.append(CHARACTERS.charAt(randomIndex));
        }

        return sb.toString();
    }

    //now building the complete short URL string.
    // the "/shorty/" prefix distinguishes redirect URLs from API URLs.
    // API URLs look like: /api/urls/abc123
    // Redirect URLs look like: /shorty/abc123
    private String buildShortUrl(String shortCode) {
        return baseUrl + "/shorty/" + shortCode;
    }

    //for converting a Url entity to a UrlResponse DTO.
    private UrlResponse convertToResponse(Url url) {
        UrlResponse response = new UrlResponse();

        response.setId(url.getId());
        response.setOriginalUrl(url.getOriginalUrl());
        response.setShortCode(url.getShortCode());
        response.setShortUrl(buildShortUrl(url.getShortCode()));  //constructed,not stored
        response.setCustomAlias(url.getCustomAlias());
        response.setClickCount(url.getClickCount());
        response.setIsActive(url.getIsActive());

        //formatting LocalDateTime to readable string (or null if not set)
        response.setCreatedAt(url.getCreatedAt() != null
                ? url.getCreatedAt().format(FORMATTER) : null);
        response.setUpdatedAt(url.getUpdatedAt() != null
                ? url.getUpdatedAt().format(FORMATTER) : null);
        response.setExpiresAt(url.getExpiresAt() != null
                ? url.getExpiresAt().format(FORMATTER) : "Never");

        return response;
    }
}
