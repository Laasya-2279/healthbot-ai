package com.example.healthbot.repository;

import com.example.healthbot.model.ChatMessage;
import com.example.healthbot.model.ChatSession;
import com.example.healthbot.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByUser(User user);
    List<ChatMessage> findByUserOrderByTimestampDesc(User user);
    List<ChatMessage> findTop5ByUserOrderByTimestampDesc(User user);
    List<ChatMessage> findByUserAndTimestampBetween(User user, LocalDateTime start, LocalDateTime end);
    List<ChatMessage> findByChatSession(ChatSession chatSession);
}
