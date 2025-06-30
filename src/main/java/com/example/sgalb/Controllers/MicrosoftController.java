package com.example.sgalb.Controllers;

import com.example.sgalb.Entities.Utilisateur;
import com.example.sgalb.Repositories.UtilisateurRepository;
import com.example.sgalb.Services.MicrosoftAuthentication.MicrosoftAuthenticationServiceInterface;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping("/microsoftStuff")
public class MicrosoftController {

    private final MicrosoftAuthenticationServiceInterface microsoftAuthenticationService;
    private final UtilisateurRepository utilisateurRepository;

    @GetMapping("/user-info")
    public Map<String, Object> userInfo(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) {
            return Map.of("error", "Utilisateur non authentifié");
        }

        Map<String, Object> principalAttributes = principal.getAttributes();
        if (principalAttributes == null || principalAttributes.isEmpty()) {
            return Map.of("error", "Aucun attribut utilisateur reçu");
        }

        // Debug pour voir ce qui est reçu
        System.out.println("Attributs Microsoft reçus : " + principalAttributes);

        // Récupération sécurisée des attributs clés
        String microsoftId = safeGetAttribute(principal, "sub");
        if (microsoftId == null) {
            microsoftId = safeGetAttribute(principal, "oid");
        }
        String name = safeGetAttribute(principal, "name");
        String email = safeGetAttribute(principal, "preferred_username");
        if (email == null) {
            email = safeGetAttribute(principal, "email");
        }

        if (microsoftId == null || email == null) {
            return Map.of("error", "Identifiants Microsoft incomplets");
        }

        microsoftAuthenticationService.loadUser(microsoftId, name, email, null);

        Utilisateur utilisateur = utilisateurRepository.findByEmail(email);

        // Construire un Map modifiable avec seulement les attributs non-null
        Map<String, Object> filteredAttributes = new HashMap<>();
        for (Map.Entry<String, Object> entry : principalAttributes.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null) {
                filteredAttributes.put(entry.getKey(), entry.getValue());
            }
        }

        if (utilisateur != null) {
            filteredAttributes.put("idUtilisateur", utilisateur.getIdUtilisateur());
        }

        return filteredAttributes;
    }

    // Méthode helper pour récupérer un attribut en String sans lancer d’exception
    private String safeGetAttribute(OAuth2User principal, String key) {
        Object val = principal.getAttribute(key);
        if (val == null) return null;
        return val.toString();
    }



    @GetMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        if (authentication != null) {
            new SecurityContextLogoutHandler().logout(request, response, authentication);
        }

        // URL de déconnexion Microsoft (à adapter si besoin)
        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("logoutUrl", "https://login.microsoftonline.com/common/oauth2/v2.0/logout");
        return ResponseEntity.ok(responseMap);
    }
}
