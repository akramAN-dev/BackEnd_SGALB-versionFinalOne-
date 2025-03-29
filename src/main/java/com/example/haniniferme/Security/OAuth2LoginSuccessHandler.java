package com.example.haniniferme.Security;

import com.example.haniniferme.Security.CustomUserDetailsService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {
    private final JwtEncoder jwtEncoder;
    private final CustomUserDetailsService customUserDetailsService;

    public OAuth2LoginSuccessHandler(JwtEncoder jwtEncoder, CustomUserDetailsService customUserDetailsService) {
        this.jwtEncoder = jwtEncoder;
        this.customUserDetailsService = customUserDetailsService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        DefaultOAuth2User oAuth2User = (DefaultOAuth2User) authentication.getPrincipal();

        // Récupération des informations Google
        String email = oAuth2User.getAttribute("email");
        Long userId = customUserDetailsService.getUserId();
        String username = customUserDetailsService.getUsername();

        // Génération du token JWT
        Instant instant = Instant.now();
        JwtClaimsSet jwtClaimsSet = JwtClaimsSet.builder()
                .issuedAt(instant)
                .expiresAt(instant.plus(10, ChronoUnit.DAYS))
                .subject(username)
                .claim("userId", userId)
                .claim("scope", "USER")  // Ajoute le rôle USER pour l'instant
                .build();

        JwtEncoderParameters jwtEncoderParameters =
                JwtEncoderParameters.from(
                        JwsHeader.with(MacAlgorithm.HS512).build(),
                        jwtClaimsSet
                );

        String jwt = jwtEncoder.encode(jwtEncoderParameters).getTokenValue();

        // Retourner le token en JSON
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{ \"accessToken\": \"" + jwt + "\" }");
    }
}
