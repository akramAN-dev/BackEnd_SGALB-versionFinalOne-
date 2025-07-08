package com.example.sgalb.Security;

import com.example.sgalb.Entities.Utilisateur;
import com.example.sgalb.Repositories.UtilisateurRepository;
import com.example.sgalb.Services.GoogleAuthentication.GoogleAuthenticationServiceInterface;
import com.example.sgalb.Services.MicrosoftAuthentication.MicrosoftAuthenticationServiceInterface;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtEncoder jwtEncoder;
    private final UtilisateurRepository utilisateurRepository;
    private final GoogleAuthenticationServiceInterface googleService;
    private final MicrosoftAuthenticationServiceInterface microsoftService;

    private static final String FRONTEND_URL = "http://localhost:5173";// this elements is to change too

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {

        OAuth2AuthenticationToken authToken = (OAuth2AuthenticationToken) authentication;
        String registrationId = authToken.getAuthorizedClientRegistrationId(); // "google" ou "microsoft"

        DefaultOAuth2User oAuth2User = (DefaultOAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        if (email == null) email = oAuth2User.getAttribute("preferred_username");

        String name = oAuth2User.getAttribute("name");
        String avatar = oAuth2User.getAttribute("picture");

        if ("google".equals(registrationId)) {
            googleService.loadUser(oAuth2User.getAttribute("sub"), name, email, avatar);
        } else if ("microsoft".equals(registrationId)) {
            microsoftService.loadUser(oAuth2User.getAttribute("sub"), name, email, avatar);
        }

        Utilisateur utilisateur = utilisateurRepository.findByEmail(email);
        Long userId = utilisateur != null ? utilisateur.getIdUtilisateur() : null;

        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(name)
                .issuedAt(now)
                .expiresAt(now.plus(10, ChronoUnit.DAYS))
                .claim("userId", userId)
                .claim("scope", "USER")
                .build();

        JwtEncoderParameters params = JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS512).build(),
                claims
        );

        String jwt = jwtEncoder.encode(params).getTokenValue();

        // Stocker le token dans la session temporairement
        request.getSession().setAttribute("jwt", jwt);

        // Redirection vers la route frontend appropriée
        if ("google".equals(registrationId)) {
            response.sendRedirect(FRONTEND_URL + "/auth/google/callback");
        } else if ("microsoft".equals(registrationId)) {
            response.sendRedirect(FRONTEND_URL + "/auth/microsoft/callback");
        } else {
            response.sendRedirect(FRONTEND_URL + "/login");
        }
    }
}
