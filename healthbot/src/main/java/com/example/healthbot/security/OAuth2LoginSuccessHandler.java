package com.example.healthbot.security;
import java.util.ArrayList;

import com.example.healthbot.model.User;
import com.example.healthbot.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
@Transactional
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private static final Logger log = LoggerFactory.getLogger(OAuth2LoginSuccessHandler.class);


    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String auth0Id = oAuth2User.getAttribute("sub");
        String name = oAuth2User.getAttribute("name");
        String email = oAuth2User.getAttribute("email");

        log.info("OAuth2 login: auth0Id = {}", auth0Id);
        log.info("OAuth2 Attributes: {}", oAuth2User.getAttributes());

        userRepository.findByAuth0Id(auth0Id).ifPresentOrElse(
                existingUser -> {

                },
                () -> {
                    User newUser = User.builder()
                            .auth0Id(auth0Id)
                            .email(email)
                            .name(name)
                            .build();
                    User savedUser = userRepository.save(newUser);
                    List<User> allUsers = userRepository.findAll();
                    userRepository.flush();
                }
        );
        getRedirectStrategy().sendRedirect(request, response, "/chat");

    }

}
