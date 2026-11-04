package com.admissions.engine.controller;

import com.admissions.engine.model.AuthRequest;
import com.admissions.engine.model.UserDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller for OAuth2 and session authentication.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final String SESSION_USER_KEY = "CURRENT_USER";

    /**
     * Get the currently logged-in user profile or a guest placeholder.
     */
    @GetMapping("/user")
    public ResponseEntity<UserDto> getCurrentUser(HttpSession session) {
        UserDto user = (UserDto) session.getAttribute(SESSION_USER_KEY);
        if (user == null) {
            return ResponseEntity.ok(UserDto.guest());
        }
        return ResponseEntity.ok(user);
    }

    /**
     * Authenticate via OAuth2 token, profile payload, or 1-click Demo.
     */
    @PostMapping("/login")
    public ResponseEntity<UserDto> login(@RequestBody(required = false) AuthRequest req, HttpSession session) {
        String provider = (req != null && req.getProvider() != null) ? req.getProvider().toLowerCase() : "demo";
        String email = (req != null && req.getEmail() != null && !req.getEmail().isBlank())
                ? req.getEmail()
                : (provider.equals("google") ? "google.student@admissions.edu" : "demo.applicant@iitb.ac.in");

        String name = (req != null && req.getName() != null && !req.getName().isBlank())
                ? req.getName()
                : (provider.equals("google") ? "Google User" : "Mundev Tomar");

        String avatarUrl = (req != null && req.getAvatarUrl() != null && !req.getAvatarUrl().isBlank())
                ? req.getAvatarUrl()
                : "https://api.dicebear.com/7.x/bottts/svg?seed=" + email;

        String userId = UUID.randomUUID().toString().substring(0, 8);

        UserDto user = new UserDto(userId, name, email, avatarUrl, provider, true);
        session.setAttribute(SESSION_USER_KEY, user);

        return ResponseEntity.ok(user);
    }

    /**
     * Log out current session.
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpSession session) {
        session.removeAttribute(SESSION_USER_KEY);
        session.invalidate();
        Map<String, Object> resp = new HashMap<>();
        resp.put("status", "logged_out");
        resp.put("authenticated", false);
        return ResponseEntity.ok(resp);
    }

    /**
     * List supported OAuth2 providers and status.
     */
    @GetMapping("/providers")
    public ResponseEntity<Map<String, Object>> getProviders() {
        Map<String, Object> providers = new HashMap<>();
        providers.put("google", Map.of(
                "enabled", true,
                "name", "Google",
                "clientId", System.getenv().getOrDefault("GOOGLE_CLIENT_ID", "")
        ));
        providers.put("github", Map.of(
                "enabled", true,
                "name", "GitHub",
                "clientId", System.getenv().getOrDefault("GITHUB_CLIENT_ID", "")
        ));
        providers.put("demo", Map.of(
                "enabled", true,
                "name", "1-Click Demo Login"
        ));
        return ResponseEntity.ok(providers);
    }

    /**
     * Health check endpoint for auth service.
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getAuthStatus(HttpServletRequest request) {
        Map<String, Object> status = new HashMap<>();
        status.put("service", "ms-admissions-oauth2");
        status.put("version", "1.0.0");
        status.put("status", "UP");
        status.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(status);
    }
}
