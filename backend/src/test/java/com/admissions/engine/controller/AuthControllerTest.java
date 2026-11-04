package com.admissions.engine.controller;

import com.admissions.engine.model.AuthRequest;
import com.admissions.engine.model.UserDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpSession;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AuthControllerTest {

    private AuthController controller;
    private MockHttpSession session;

    @BeforeEach
    void setUp() {
        controller = new AuthController();
        session = new MockHttpSession();
    }

    @Test
    void testGuestUserDefault() {
        ResponseEntity<UserDto> response = controller.getCurrentUser(session);
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isAuthenticated());
        assertEquals("guest", response.getBody().getId());
    }

    @Test
    void testLoginWithGoogle() {
        AuthRequest req = new AuthRequest();
        req.setProvider("google");
        req.setName("Test Student");
        req.setEmail("test@gmail.com");

        ResponseEntity<UserDto> response = controller.login(req, session);
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isAuthenticated());
        assertEquals("google", response.getBody().getProvider());
        assertEquals("test@gmail.com", response.getBody().getEmail());
        assertEquals("Test Student", response.getBody().getName());

        // Now verify session persistence
        ResponseEntity<UserDto> persisted = controller.getCurrentUser(session);
        assertTrue(persisted.getBody().isAuthenticated());
        assertEquals("test@gmail.com", persisted.getBody().getEmail());
    }

    @Test
    void testLogout() {
        AuthRequest req = new AuthRequest();
        req.setProvider("demo");
        controller.login(req, session);

        ResponseEntity<Map<String, Object>> logoutResp = controller.logout(session);
        assertEquals("logged_out", logoutResp.getBody().get("status"));
        assertEquals(false, logoutResp.getBody().get("authenticated"));
    }

    @Test
    void testGetProviders() {
        ResponseEntity<Map<String, Object>> response = controller.getProviders();
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("google"));
        assertTrue(response.getBody().containsKey("github"));
        assertTrue(response.getBody().containsKey("demo"));
    }
}
