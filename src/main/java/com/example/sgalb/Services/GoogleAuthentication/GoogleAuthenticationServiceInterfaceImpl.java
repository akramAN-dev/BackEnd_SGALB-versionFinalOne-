package com.example.sgalb.Services.GoogleAuthentication;

import com.example.sgalb.Entities.GoogleAuthentication;
import com.example.sgalb.Entities.Utilisateur;
import com.example.sgalb.Repositories.GoogleAuthenticationRepository;
import com.example.sgalb.Repositories.UtilisateurRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

@Service
@AllArgsConstructor
public class GoogleAuthenticationServiceInterfaceImpl extends DefaultOAuth2UserService implements GoogleAuthenticationServiceInterface {

     UtilisateurRepository utilisateurRepository;
    GoogleAuthenticationRepository googleAuthRepository;

    @Override
    public OAuth2User loadUser(String googleId, String name, String email, String avatar) throws OAuth2AuthenticationException {
        // 1. Recherche dans GoogleAuthentication pour voir si l'utilisateur existe déjà
        Optional<GoogleAuthentication> existingGoogleAuth = googleAuthRepository.findByIdGoogle(googleId);

        if (!existingGoogleAuth.isPresent()) {
            // 2. Créer ou récupérer l'utilisateur dans 'Utilisateur'
            Utilisateur utilisateur = utilisateurRepository.findByEmail(email);
            if (utilisateur == null) {
                utilisateur = new Utilisateur();
                utilisateur.setNom(name);
                utilisateur.setPrenom(""); // À compléter si nécessaire
                utilisateur.setEmail(email);
                utilisateur.setMotDePasse(null); // Pas de mot de passe pour Google OAuth
                utilisateur.setRole("CLIENT");
                utilisateur.setDateInscription(new Date());
                utilisateur.setStatut("ACTIF");
                utilisateurRepository.save(utilisateur);
            }

            // 3. Créer une nouvelle entrée de GoogleAuthentication
            GoogleAuthentication googleAuth = new GoogleAuthentication();
            googleAuth.setIdGoogle(googleId);
            googleAuth.setUtilisateur(utilisateur); // Association avec Utilisateur
            googleAuth.setAvatar(avatar);
            googleAuth.setRole("CLIENT"); // Rôle par défaut ou selon la logique métier
            googleAuth.setDateConnexion(new Date());
            googleAuthRepository.save(googleAuth);
        }

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                Map.of("name", name, "email", email, "avatar", avatar),
                "name"
        );
    }
}
