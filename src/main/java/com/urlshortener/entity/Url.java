package com.urlshortener.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "urls") //exact table name in PostgreSQL
public class Url {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    //original long URL that the user wants to shorten.
    // Ex: "https://www.youtube.com/xyDD=dQw4w9WgXcQ&list=..."
    // columnDefinition = "TEXT" → PostgreSQL TEXT type (no length limit)
    //                               Default VARCHAR(255) might not be enough for long URLs
    @Column(nullable =false, columnDefinition= "TEXT")
    private String originalUrl;

    // The unique short code we generate for this URL.
    // Ex : "abc123", "xK9pL2"
    // unique = true → PostgreSQL creates a UNIQUE constraint on this column.
    //                Prevents two URLs from having the same short code.
    // length = 10   → Maximum 10 characters (our codes are 6 chars by default)
    @Column(nullable= false, unique=true, length = 10)
    private String shortCode;

    /*
      Custom alias chosen by user (optional).
      Example: User wants "my-blog" instead of random "anM123"
      Result : http://localhost:8080/my-blog
    */
    @Column(unique =true, length = 50)
    private String customAlias;

    //how many times this short URL has been clicked,visited.
    // Starts at 0 when created, increments on every redirect.
    @Column(nullable = false)
    private Long clickCount = 0L;

    // Optional expiry date for the short URL.
    // If null, the URL never expires.
    //  Exam: Set to 30 days from now for a promotional campaign link
    @Column
    private LocalDateTime expiresAt;

    /*
    Whether this URL is active or has been deactivated.
     true= URL works normally
    false = URL returns "deactivated" error even if not expired
    Useful for manually disabling a URL without deleting it.
    */
    @Column(nullable = false)
    private Boolean isActive = true;

    /*
     Timestamp of when this URL was created.
     @Column(updatable = false)/
       This column is set ONCE when the row is inserted.
      JPA will never include it in UPDATE statements.
      Perfect for "created at" timestamps.
     @PrePersist automatically sets this value.
    */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Timestamp of the last time this URL record was updated.
    // Auto-updated by @PreUpdate method below.
    @Column
    private LocalDateTime updatedAt;

    /*
     @PrePersist
      JPA Lifecycle Callback — runs AUTOMATICALLY just BEFORE
     this entity is saved to the database for the FIRST TIME.
     we never call this method manually.
     JPA calls it automatically when we do: urlRepository.save(url)
     Use case: Setting timestamps, default values, etc.
    */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        //Ensures clickCount starts at 0 if not set
        if (this.clickCount == null) {
            this.clickCount = 0L;
        }
        if (this.isActive == null) {
            this.isActive = true;
        }
    }

    // @PreUpdate
    //  JPA Lifecycle Callback — runs AUTOMATICALLY just BEFORE
    //  this entity is UPDATED in the database.
    // Called when we: urlRepository.save(existingUrl) [where existingUrl has an id]
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
