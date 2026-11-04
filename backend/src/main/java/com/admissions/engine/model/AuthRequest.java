package com.admissions.engine.model;

/**
 * Payload for initiating or completing OAuth2 / session login.
 */
public class AuthRequest {

    private String provider;
    private String token;
    private String code;
    private String email;
    private String name;
    private String avatarUrl;

    public AuthRequest() {}

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
}
