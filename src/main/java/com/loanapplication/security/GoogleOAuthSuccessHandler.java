
package com.loanapplication.security;

import com.loanapplication.entities.User;
import com.loanapplication.repo.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class GoogleOAuthSuccessHandler
        implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    public GoogleOAuthSuccessHandler(
            UserRepository userRepository,
            JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication)
            throws IOException, ServletException {

        OAuth2User googleUser = (OAuth2User) authentication.getPrincipal();

        String email = googleUser.getAttribute("email");
        Boolean emailVerified = googleUser.getAttribute("email_verified");

        if (email == null || !Boolean.TRUE.equals(emailVerified)) {
            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Google email is not verified");
            return;
        }

        User user = userRepository.findByEmail(email)
                .orElse(null);

        if (user == null
                || !Boolean.TRUE.equals(user.getEnabled())) {
            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "No eligible account found for this Google email");
            return;
        }

        String authToken = jwtService.generateAuthToken(email);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(
                "{\"token\":\"" + authToken + "\",\"stage\":\"AUTH\"}");
    }
}
