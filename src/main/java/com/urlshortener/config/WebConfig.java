package com.urlshortener.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


//This config class now handles CORS (Cross-Origin Resource Sharing).
//we'll override addCorsMappings() to configure CORS rules.
@Configuration
public class WebConfig implements WebMvcConfigurer {

    //Configures CORS to allow requests from any origin.
    //allowedOrigins("*") allows requests from anywhere.
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")  //applied to all endpoints
                .allowedOrigins("*")      //allowed requests from any origin
                .allowedMethods(          //Allowed these HTTP methods
                        "GET", "POST", "PUT", "DELETE", "OPTIONS"
                )
                .allowedHeaders("*");     // allowed any headers
    }
}
