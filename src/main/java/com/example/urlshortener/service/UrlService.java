package com.example.urlshortener.service;

import com.example.urlshortener.domain.ShortUrl;
import com.example.urlshortener.domain.User;
import com.example.urlshortener.exception.ShortUrlNotFoundException;
import com.example.urlshortener.repository.ShortUrlRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.UUID;

@Service
public class UrlService {

    private static final int MAX_GENERATION_ATTEMPTS = 10;

    private final ShortUrlRepository shortUrlRepository;
    private final ShortCodeGenerator shortCodeGenerator;
    private final String baseUrl;

    public UrlService(
            ShortUrlRepository shortUrlRepository,
            ShortCodeGenerator shortCodeGenerator,
            @Value("${app.base-url}") String baseUrl) {
        this.shortUrlRepository = shortUrlRepository;
        this.shortCodeGenerator = shortCodeGenerator;
        this.baseUrl = baseUrl;
    }

    @Transactional
    public ShortUrl createShortUrl(User owner, String originalUrl) {
        validateOriginalUrl(originalUrl);
        String code = generateUniqueCode();
        String shortUrlValue = buildShortUrl(code);
        ShortUrl shortUrl = new ShortUrl(code, originalUrl, shortUrlValue, owner);
        return shortUrlRepository.save(shortUrl);
    }

    @Transactional(readOnly = true)
    public List<ShortUrl> getUrlsForUser(User owner) {
        return shortUrlRepository.findAllByOwnerIdOrderByCreatedAtDesc(owner.getId());
    }

    @Transactional
    public void deactivateShortUrl(UUID id, User owner) {
        ShortUrl shortUrl = shortUrlRepository.findByIdAndOwnerId(id, owner.getId())
                .orElseThrow(() -> new ShortUrlNotFoundException("Short URL not found"));
        shortUrl.setActive(false);
    }

    @Transactional(readOnly = true)
    public ShortUrl getActiveShortUrlByCode(String code) {
        return shortUrlRepository.findByCodeAndActiveTrue(code)
                .orElseThrow(() -> new ShortUrlNotFoundException("Short URL not found"));
    }

    private String buildShortUrl(String code) {
        return baseUrl + "/r/" + code;
    }

    private String generateUniqueCode() {
        for (int attempt = 0; attempt < MAX_GENERATION_ATTEMPTS; attempt++) {
            String candidate = shortCodeGenerator.generate();
            if (!shortUrlRepository.existsByCode(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException("Unable to generate unique short code");
    }

    private void validateOriginalUrl(String originalUrl) {
        try {
            URI uri = new URI(originalUrl);
            String scheme = uri.getScheme();
            if (scheme == null || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
                throw new IllegalArgumentException("URL must use http or https scheme");
            }
            if (uri.getHost() == null) {
                throw new IllegalArgumentException("URL must include a valid host");
            }
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid URL format");
        }
    }
}

