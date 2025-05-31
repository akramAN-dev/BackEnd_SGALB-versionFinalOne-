package com.example.sgalb.Security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
@Slf4j
public class SecurityController {
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtEncoder jwtEncoder;
    @Autowired
    private CustomUserDetailsService customUserDetailsService;
    @Autowired
    private OAuth2AuthorizedClientService authorizedClientService;
    @GetMapping("/profile")
    public String getUserProfile(OAuth2AuthenticationToken authentication) {
        // Récupérer l'ID de la connexion OAuth2 (par exemple, "google")
        String registrationId = authentication.getAuthorizedClientRegistrationId();

        // Charger le client autorisé à partir du service OAuth2
        OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(
                registrationId, authentication.getName());

        // Récupérer le jeton d'accès de Google
        String accessToken = authorizedClient.getAccessToken().getTokenValue();

        // Retourner le token d'accès (ou l'utiliser comme bon te semble)
        return accessToken;
    }
    @PostMapping("/login")
    public Map<String,String> login(@RequestBody Map<String, String> credentials){
        String email = credentials.get("email");
        String password = credentials.get("motDePasse");

        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
        Instant instant=Instant.now();
        String scope=authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(" "));
        Long userId = customUserDetailsService.getUserId();
        String username=customUserDetailsService.getUsername();
        JwtClaimsSet jwtClaimsSet= JwtClaimsSet.builder()
                .issuedAt(instant)
                .expiresAt(instant.plus(10, ChronoUnit.DAYS))
                .subject(username)
                .claim("scope",scope)
                .claim("userId", userId)
                .build();
        JwtEncoderParameters jwtEncoderParameters=
                JwtEncoderParameters.from(
                        JwsHeader.with(MacAlgorithm.HS512).build(),
                        jwtClaimsSet
                );

        String jwt=jwtEncoder.encode(jwtEncoderParameters).getTokenValue();
        return Map.of("accessToken",jwt);
    }



}