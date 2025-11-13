package com.example.urlshortener.web;

import com.example.urlshortener.domain.ShortUrl;
import com.example.urlshortener.domain.User;
import com.example.urlshortener.dto.ShortUrlResponse;
import com.example.urlshortener.dto.ShortenUrlRequest;
import com.example.urlshortener.service.UrlService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class UrlController {

    private final UrlService urlService;

    public UrlController(UrlService urlService) {
        this.urlService = urlService;
    }

    @PostMapping("/shorten")
    public ResponseEntity<ShortUrlResponse> shorten(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ShortenUrlRequest request) {
        ShortUrl shortUrl = urlService.createShortUrl(user, request.getOriginalUrl());
        return ResponseEntity.ok(toResponse(shortUrl));
    }

    @GetMapping("/urls")
    public ResponseEntity<List<ShortUrlResponse>> list(@AuthenticationPrincipal User user) {
        List<ShortUrl> urls = urlService.getUrlsForUser(user);
        return ResponseEntity.ok(urls.stream().map(this::toResponse).toList());
    }

    @DeleteMapping("/urls/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal User user, @PathVariable UUID id) {
        urlService.deactivateShortUrl(id, user);
        return ResponseEntity.noContent().build();
    }

    private ShortUrlResponse toResponse(ShortUrl shortUrl) {
        return new ShortUrlResponse(
                shortUrl.getId(),
                shortUrl.getOriginalUrl(),
                shortUrl.getShortUrl(),
                shortUrl.isActive(),
                shortUrl.getCreatedAt()
        );
    }
}

