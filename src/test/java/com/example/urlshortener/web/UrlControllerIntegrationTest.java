package com.example.urlshortener.web;

import com.example.urlshortener.dto.ShortenUrlRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UrlControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private com.example.urlshortener.repository.UserRepository userRepository;

    @Autowired
    private com.example.urlshortener.repository.ShortUrlRepository shortUrlRepository;

    @AfterEach
    void tearDown() {
        shortUrlRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void shouldShortenListRedirectAndDeactivateUrl() throws Exception {
        String token = registerAndFetchToken("owner@example.com", "Secret123!");

        ShortenUrlRequest shortenRequest = new ShortenUrlRequest("https://example.com/resource");
        MvcResult shortenResult = mockMvc.perform(post("/api/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(shortenRequest)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode shortenJson = objectMapper.readTree(shortenResult.getResponse().getContentAsString());
        UUID shortUrlId = UUID.fromString(shortenJson.get("id").asText());
        String shortUrl = shortenJson.get("shortUrl").asText();
        String code = shortUrl.substring(shortUrl.lastIndexOf('/') + 1);

        MvcResult listResult = mockMvc.perform(get("/api/urls")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode list = objectMapper.readTree(listResult.getResponse().getContentAsString());
        assertThat(list.isArray()).isTrue();
        assertThat(list.get(0).get("shortUrl").asText()).isEqualTo(shortUrl);
        assertThat(list.get(0).get("active").asBoolean()).isTrue();

        mockMvc.perform(get("/r/{code}", code))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://example.com/resource"));

        mockMvc.perform(delete("/api/urls/{id}", shortUrlId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        MvcResult afterDeleteResult = mockMvc.perform(get("/api/urls")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode afterDelete = objectMapper.readTree(afterDeleteResult.getResponse().getContentAsString());
        assertThat(afterDelete.get(0).get("active").asBoolean()).isFalse();
    }

    @Test
    void unauthorizedRequestsAreRejected() throws Exception {
        mockMvc.perform(post("/api/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ShortenUrlRequest("https://example.com"))))
                .andExpect(status().isUnauthorized());
    }

    private String registerAndFetchToken(String email, String password) throws Exception {
        String requestBody = """
                {"email":"%s","password":"%s"}
                """.formatted(email, password);

        MvcResult result = mockMvc.perform(post("/api/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("token").asText();
    }
}

