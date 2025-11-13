package com.example.urlshortener.web;

import com.example.urlshortener.domain.ShortUrl;
import com.example.urlshortener.service.UrlService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
public class RedirectController {

    private final UrlService urlService;

    public RedirectController(UrlService urlService) {
        this.urlService = urlService;
    }

    @GetMapping("/r/{code}")
    public ResponseEntity<Void> redirect(@PathVariable String code) {
        ShortUrl shortUrl = urlService.getActiveShortUrlByCode(code);
        return ResponseEntity.status(302)
                .header(HttpHeaders.LOCATION, shortUrl.getOriginalUrl())
                .build();
    }
}

