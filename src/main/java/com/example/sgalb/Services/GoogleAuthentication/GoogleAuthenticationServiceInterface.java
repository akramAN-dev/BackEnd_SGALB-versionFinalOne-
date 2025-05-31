package com.example.sgalb.Services.GoogleAuthentication;

import org.springframework.security.oauth2.core.user.OAuth2User;

public interface GoogleAuthenticationServiceInterface {
    public OAuth2User loadUser(String googleId, String name, String email, String avatar);
}
