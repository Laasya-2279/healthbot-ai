package com.example.healthbot.controller;

import com.example.healthbot.service.GeminiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final GeminiService geminiService;

    @PostMapping("/{sessionId}")
    public ResponseEntity<String> chatWithBot(
            @PathVariable Long sessionId,
            @RequestBody String userInput) {
        try {
            System.out.println("User Input: " + userInput);  // 🔍 Debug
            String reply = geminiService.getGeminiResponse(userInput,sessionId);

            if (reply == null || reply.isEmpty()) {
                System.out.println("Gemini returned empty response ❗️");
                return ResponseEntity.status(500).body("No response from Gemini.");
            }

            return ResponseEntity.ok(reply);

        } catch (Exception e) {
            System.err.println("🔥 Error in chatWithBot: " + e.getMessage());
            e.printStackTrace(); // full trace
            return ResponseEntity.status(500).body("Something went wrong: " + e.getMessage());
        }
    }

}
