package com.example.urlshortener.repository;

import com.example.urlshortener.domain.ShortUrl;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ShortUrlRepository extends JpaRepository<ShortUrl, UUID> {

    Optional<ShortUrl> findByCodeAndActiveTrue(String code);

    Optional<ShortUrl> findByIdAndOwnerId(UUID id, UUID ownerId);

    List<ShortUrl> findAllByOwnerIdOrderByCreatedAtDesc(UUID ownerId);

    boolean existsByCode(String code);
}

