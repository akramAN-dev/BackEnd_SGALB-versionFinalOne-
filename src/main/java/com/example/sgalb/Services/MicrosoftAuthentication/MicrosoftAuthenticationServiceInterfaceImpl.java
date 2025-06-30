package com.example.sgalb.Services.MicrosoftAuthentication;

import com.example.sgalb.Entities.MicrosoftAuthentication;
import com.example.sgalb.Entities.Utilisateur;
import com.example.sgalb.Repositories.MicrosoftAuthenticationRepository;
import com.example.sgalb.Repositories.UtilisateurRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@AllArgsConstructor
public class MicrosoftAuthenticationServiceInterfaceImpl extends DefaultOAuth2UserService implements MicrosoftAuthenticationServiceInterface {

    private final UtilisateurRepository utilisateurRepository;
    private final MicrosoftAuthenticationRepository microsoftAuthRepository;

    @Override
    public OAuth2User loadUser(String microsoftId, String name, String email, String avatar) throws OAuth2AuthenticationException {
        // 1. Vérifier si l'utilisateur existe déjà dans MicrosoftAuthentication
        Optional<MicrosoftAuthentication> existingAuth = microsoftAuthRepository.findByIdMicrosoft(microsoftId);

        if (!existingAuth.isPresent()) {
            // 2. Créer ou récupérer l'utilisateur dans 'Utilisateur'
            Utilisateur utilisateur = utilisateurRepository.findByEmail(email);
            if (utilisateur == null) {
                utilisateur = new Utilisateur();
                utilisateur.setNom(name);
                utilisateur.setPrenom(""); // À adapter si besoin
                utilisateur.setEmail(email);
                utilisateur.setMotDePasse(null); // Pas de mot de passe pour OAuth Microsoft
                utilisateur.setRole("CLIENT");
                utilisateur.setDateInscription(new Date());
                utilisateur.setStatut("ACTIF");
                utilisateurRepository.save(utilisateur);
            }

            // 3. Créer une nouvelle entrée dans MicrosoftAuthentication
            MicrosoftAuthentication auth = new MicrosoftAuthentication();
            auth.setIdMicrosoft(microsoftId);
            auth.setUtilisateur(utilisateur);
            auth.setAvatar(avatar);
            auth.setRole("CLIENT");
            auth.setDateConnexion(new Date());

            microsoftAuthRepository.save(auth);
        }

        // 4. Construire une map d'attributs en filtrant les nulls
        Map<String, Object> attributes = new HashMap<>();
        if (name != null) attributes.put("name", name);
        if (email != null) attributes.put("email", email);
        if (avatar != null) attributes.put("avatar", avatar);

        // 5. Retourner l'utilisateur OAuth2 avec les attributs
        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                attributes,
                "name"
        );
    }

}
