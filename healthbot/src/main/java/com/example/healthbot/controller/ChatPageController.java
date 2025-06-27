package com.example.healthbot.controller;

import com.example.healthbot.service.GeminiService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class ChatPageController {
    private final GeminiService geminiService;
    @GetMapping("/chat")
    public String showChatPage() {
        return "chat";
    }
    @PostMapping("/chat")
    public String handleChat(@RequestParam("userMessage") String userMessage,
                             @RequestParam("sessionId") Long sessionId,
                             Model model) {
        String response = geminiService.getGeminiResponse(userMessage, sessionId);
        model.addAttribute("response", response);
        return "chat";
    }
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }
    @GetMapping("/logout-page")
    public String logoutSuccessPage() {
        return "logout";
    }
}
