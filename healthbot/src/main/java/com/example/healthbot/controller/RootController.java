package com.example.healthbot.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RootController {

    @GetMapping("/")
    public String rootWelcome() {
        return "✨ Welcome to HealthBot! You're at the root endpoint. ✨";
    }
}
