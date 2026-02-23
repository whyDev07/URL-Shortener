# URL Shortener

A  backend REST API built with Spring Boot that converts long URLs into short, 
shareable links with click tracking and analytics.

---

## Overview

This project shortens long URLs into compact links that are easy to share. When someone clicks the short link, they are automatically redirected to the original URL while the system tracks analytics like click count and timestamps.

Example:
- Long URL: https://www.amazon.in/One94Store-Reading-Students-Rechargeable-Eye-Caring/dp/B0DPKNT3KP
- Short URL: http://localhost:8080/shorty/abc123

---
## Why I Built This

I wanted to learn Spring Boot properly and build something more interesting than the usual student management systems. URL shorteners actually solve a real problem - sharing long links on social media, tracking click analytics, and making URLs look cleaner.

## Tech Stack

- Java 21
- Spring Boot 4.0.2
- Spring Data JPA (Hibernate)
- PostgreSQL
- Lombok
- Maven

## Features

- Random short code generation (6 characters)
- Custom aliases if you want a specific short link
- Click counter for each URL
- Optional expiry dates
- Activate/deactivate URLs without deleting them
- Get stats on any short URL

---
## Project Structure

```
src/main/java/com/urlshortener/
├── UrlShortenerApplication.java       Main application entry point
├── controller/
│   ├── UrlController.java             REST API endpoints for URL management
│   └── RedirectController.java        Handles short URL redirection
├── service/
│   └── UrlService.java                Business logic and URL processing
├── repository/
│   └── UrlRepository.java             Database queries using JPA
├── entity/
│   └── Url.java                       Database table mapping
├── dto/
│   └── UrlDto.java                    Request and response data structures
├── exception/
│   ├── UrlExceptions.java             Custom exception classes
│   └── GlobalExceptionHandler.java    Centralized error handling
└── config/
    └── WebConfig.java                 Application configuration
```

---
## Quick Start

1. Make sure PostgreSQL is running

2. Create the database:
```sql
CREATE DATABASE url_shortener_db;
```

3. Update `application.properties` with your database password

4. Run it:
```bash
mvn spring-boot:run
```

App starts on `http://localhost:8080`

## How to Use

### Create a short URL

```bash
POST http://localhost:8080/api/urls
Content-Type: application/json

{
  "originalUrl": "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
  "customAlias": "my-link",
  "expiryDays": 30
}
```

You'll get back something like:
```json
{
  "success": true,
  "message": "Short URL created successfully!",
  "data": {
    "shortCode": "my-link",
    "shortUrl": "http://localhost:8080/shorty/my-link",
    "clickCount": 0,
    ...
  }
}
```

### Use the short URL

Just paste `http://localhost:8080/shorty/my-link` in your browser and you'll get redirected.

### See the stats

```bash
GET http://localhost:8080/api/urls/my-link/stats
```

Shows you total clicks, when it was created, if it's active, etc.

## All Endpoints

```
POST   /api/urls                      Create short URL
GET    /api/urls                      List all URLs
GET    /api/urls/{shortCode}          Get URL details
GET    /api/urls/{shortCode}/stats    Get analytics
GET    /api/urls/top/popular          Top 10 most clicked
PUT    /api/urls/{shortCode}          Update URL
DELETE /api/urls/{shortCode}          Delete URL

GET    /shorty/{shortCode}            Redirect to original URL
```

## Project Structure

Pretty standard Spring Boot setup:
- `controller` - handles HTTP requests
- `service` - business logic (generating codes, validating stuff)
- `repository` - talks to the database
- `entity` - the Url table structure
- `dto` - request/response objects
- `exception` - error handling

## Some Design Choices

**Why 6 characters for the short code?**
Using 62 characters (a-z, A-Z, 0-9) with length 6 gives me 56 billion possible combinations. More than enough and still short.

**Why HTTP 302 redirect instead of 301?**
Because 301 is permanent and browsers cache it. Then I can't count clicks or change where the link goes. 302 is temporary so every click hits my server.

**Why separate DTOs from entities?**
Don't want users sending `"clickCount": 9999` and faking their stats. DTOs let me control exactly what data comes in and goes out.

## Things I Learned

- How Spring Data JPA works (derived query methods are pretty cool)
- Exception handling with `@ControllerAdvice`
- HTTP status codes matter (404 vs 410 vs 409)
- Validation annotations save a lot of boilerplate
- Transaction management with `@Transactional`

## What I'd Add Next

- User accounts so people can manage their own URLs
- QR code generation
- Better analytics (geography, devices, browsers)
- Rate limiting
- Bulk upload via CSV

## Notes

This was my first proper Spring Boot project. There's probably stuff I could do better, but it works and I learned a ton building it. Feel free to use it or suggest improvements.

---

Built by Dev Adhikari | [GitHub](https://github.com/whyDev07)