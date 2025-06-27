package com.example.healthbot.controller;
import java.security.Principal;
import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;


@RestController
@RequestMapping("/api")
public class TestController {

    @GetMapping("/test")
    public String test() {
        return "Auth is 🔒 secured and working!";
    }

    @GetMapping("/user")
    public String getUser(Authentication authentication) {
        return "Hello, " + authentication.getName() + " 👋";
    }

    @GetMapping("/")
    public String home(Principal principal) {
        return "Hey " + principal.getName() + ", you're logged in ";
    }

    @GetMapping("/whoami")
    public String whoami(Authentication authentication) {
        return "Logged in as: " + authentication.getName();
    }

    @GetMapping("/profile")
    public Map<String, Object> profile(@AuthenticationPrincipal OAuth2User principal) {
        return principal.getAttributes();
    }



}
