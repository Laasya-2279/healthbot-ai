package com.example.healthbot.controller;

import com.example.healthbot.model.ChatMessage;
import com.example.healthbot.model.ChatSession;
import com.example.healthbot.model.User;
import com.example.healthbot.repository.ChatMessageRepository;
import com.example.healthbot.repository.ChatSessionRepository;
import com.example.healthbot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class ChatSessionController {

    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String auth0Id = oAuth2User.getAttribute("sub");

        return userRepository.findByAuth0Id(auth0Id)
                .orElseThrow(() -> new RuntimeException("User not found with Auth0 ID: " + auth0Id));
    }

    @PostMapping
    public ResponseEntity<ChatSession> createSession() {
        User user = getCurrentUser();
        ChatSession session = ChatSession.builder()
                .title("Chat started at " + LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .user(user)
                .build();
        ChatSession saved = chatSessionRepository.save(session);
        return ResponseEntity.ok(saved);
    }

    @GetMapping
    public ResponseEntity<List<ChatSession>> getSessions() {
        User user = getCurrentUser();
        List<ChatSession> sessions = chatSessionRepository.findByUserOrderByCreatedAtDesc(user);
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/{sessionId}/messages")
    public ResponseEntity<List<Map<String, String>>> getMessagesForSession(@PathVariable Long sessionId) {
        Optional<ChatSession> optionalSession = chatSessionRepository.findById(sessionId);
        if (optionalSession.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        ChatSession session = optionalSession.get();

        if (!session.getUser().getId().equals(getCurrentUser().getId())) {
            return ResponseEntity.status(403).build();
        }

        List<ChatMessage> messages = chatMessageRepository.findByChatSession(session);
        List<Map<String, String>> simplifiedMessages = messages.stream()
                .map(msg -> {
                    Map<String, String> map = new HashMap<>();
                    map.put("userMessage", msg.getUserMessage());
                    map.put("botResponse", msg.getBotResponse());
                    return map;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(simplifiedMessages);

    }

    @DeleteMapping("/{sessionId}")
    public ResponseEntity<Void> deleteSession(@PathVariable Long sessionId) {
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));
        if (!session.getUser().getId().equals(getCurrentUser().getId())) {
            return ResponseEntity.status(403).build();
        }

        chatSessionRepository.delete(session);
        return ResponseEntity.noContent().build();
    }

}
