package com.urlshortener.controller;

import com.urlshortener.service.UrlService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

//This controller will handle the ACTUAL URL redirection.
// used 302(temporary) so every visit is counted and URLs can be changed/expired rather than using 301 which is
// a permanent redirect
@RestController
@RequiredArgsConstructor
public class RedirectController {

    private final UrlService urlService;

    @GetMapping("/shorty/{shortCode}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode) {

        //for resolving the short code → original URL and also validating the URL and incrementing click count
        String originalUrl = urlService.resolveUrl(shortCode);

        // now setting the Location header to the original URL
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(java.net.URI.create(originalUrl));//converting the string to URI object to pass in setLocation

        //returning HTTP 302 with Location header and nobody
        return new ResponseEntity<>(null, headers, HttpStatus.FOUND);
    }
}
