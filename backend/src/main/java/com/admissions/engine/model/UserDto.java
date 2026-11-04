package com.admissions.engine.model;

/**
 * Data Transfer Object representing an authenticated user session.
 */
public class UserDto {

    private String id;
    private String name;
    private String email;
    private String avatarUrl;
    private String provider;
    private boolean authenticated;
    private long loginTime;

    public UserDto() {}

    public UserDto(String id, String name, String email, String avatarUrl, String provider, boolean authenticated) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.avatarUrl = avatarUrl;
        this.provider = provider;
        this.authenticated = authenticated;
        this.loginTime = System.currentTimeMillis();
    }

    public static UserDto guest() {
        UserDto dto = new UserDto();
        dto.setId("guest");
        dto.setName("Guest User");
        dto.setEmail(null);
        dto.setAvatarUrl(null);
        dto.setProvider("none");
        dto.setAuthenticated(false);
        dto.setLoginTime(System.currentTimeMillis());
        return dto;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public boolean isAuthenticated() { return authenticated; }
    public void setAuthenticated(boolean authenticated) { this.authenticated = authenticated; }

    public long getLoginTime() { return loginTime; }
    public void setLoginTime(long loginTime) { this.loginTime = loginTime; }
}
