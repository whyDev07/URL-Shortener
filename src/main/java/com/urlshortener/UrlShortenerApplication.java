package com.urlshortener;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class UrlShortenerApplication {

    public static void main(String[] args) {
        SpringApplication.run(UrlShortenerApplication.class, args);

        System.out.println("\n=========================================================");
        System.out.println("  ----->>>>> URL Shortener is running!!!!!");
        System.out.println("  ----->>>>> API Base :   http://localhost:8080/api");
        System.out.println("=========================================================\n");
    }
}
