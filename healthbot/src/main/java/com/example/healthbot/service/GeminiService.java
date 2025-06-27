package com.example.healthbot.service;

import com.example.healthbot.model.ChatMessage;
import com.example.healthbot.model.ChatSession;
import com.example.healthbot.repository.ChatMessageRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import com.example.healthbot.model.User;
import com.example.healthbot.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import com.example.healthbot.repository.ChatSessionRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;



import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GeminiService {

    private final UserRepository userRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatSessionRepository chatSessionRepository;

    @Value("${gemini.api.key}")
    private String apiKey;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String getGeminiResponse(String userInput,Long sessionId) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-pro-latest:generateContent?key=" + apiKey;

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", userInput)
                        ))
                )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());

            if (!root.has("candidates")) {
                return "No response from Gemini.";
            }

            String replyText = root.path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
            String auth0Id = oAuth2User.getAttribute("sub");

            User user = userRepository.findByAuth0Id(auth0Id)
                    .orElseThrow(() -> new RuntimeException("User not found with Auth0 ID: " + auth0Id));
            ChatSession session = chatSessionRepository.findById(sessionId)
                    .orElseThrow(() -> new RuntimeException("Chat session not found"));
            if (!session.getUser().getId().equals(user.getId())) {
                throw new RuntimeException("Unauthorized access to this session");
            }
            ChatMessage message = ChatMessage.builder()
                    .userMessage(userInput)
                    .botResponse(replyText)
                    .timestamp(LocalDateTime.now())
                    .user(user)
                    .chatSession(session)
                    .build();
            chatMessageRepository.save(message);

            return replyText;

        } catch (Exception e) {
            e.printStackTrace();

            return "Something went wrong while talking to Gemini: " + e.getMessage();
        }

    }
}
