package com.urlshortener.repository;

import com.urlshortener.entity.Url;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Repository
public interface UrlRepository extends JpaRepository<Url, Long> {

    //returning Optional to handle Null values
    Optional<Url> findByShortCode(String shortCode);

    //Finding URL by custom alias.
    Optional<Url> findByCustomAlias(String customAlias);

    // Checking if a short code already exists in the database.
    //Used to avoid duplicate short codes when generating random ones.
    boolean existsByShortCode(String shortCode);

    //Checking if a custom alias is already taken.
    //used to validate user's custom alias before saving.
    boolean existsByCustomAlias(String customAlias);

    //Get all active URLs ordered by creation date(newest ones first).
    List<Url> findByIsActiveOrderByCreatedAtDesc(Boolean isActive);

    // Get all URLs ordered by click count (most clicked first).
    List<Url> findAllByOrderByClickCountDesc();

    // CUSTOM JPQL QUERY —Increments click count by 1.
    // There's no derived query syntax for "increment a field"
    // So we'll write JPQL manually
    // @Modifying- Required for UPDATE and DELETE queries in JPA.
    // @Transactional -UPDATE queries must run inside a transaction.
    //   If the update fails, the transaction rolls back automatically.
    @Modifying
    @Transactional
    @Query("UPDATE Url u SET u.clickCount = u.clickCount + 1 WHERE u.id = :id")
    void incrementClickCount(@Param("id") Long id);

    // Using native queries for finding top 10 most clicked URLs.
    // nativeQuery = true → Write raw SQL (not JPQL)
    // Complex queries that are hard to write in JPQL
    @Query(value = "SELECT * FROM urls ORDER BY click_count DESC LIMIT 10",
            nativeQuery = true)
    List<Url> findTop10ByClickCount();
}
