package com.example.healthbot.repository;

import com.example.healthbot.model.ChatSession;
import com.example.healthbot.model.ChatMessage;
import com.example.healthbot.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {
    List<ChatSession> findByUser(User user);
    List<ChatSession> findByUserOrderByCreatedAtDesc(User user);


}
