package com.example.urlshortener.dto;

import java.time.Instant;
import java.util.UUID;

public record ShortUrlResponse(
        UUID id,
        String originalUrl,
        String shortUrl,
        boolean active,
        Instant createdAt) {
}

