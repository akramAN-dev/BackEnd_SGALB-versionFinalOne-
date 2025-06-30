package com.example.sgalb.Services.MicrosoftAuthentication;

import org.springframework.security.oauth2.core.user.OAuth2User;

public interface MicrosoftAuthenticationServiceInterface {
    public OAuth2User loadUser(String googleId, String name, String email, String avatar);
}
