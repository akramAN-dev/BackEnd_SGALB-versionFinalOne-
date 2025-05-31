package com.example.sgalb.Controllers;

import com.example.sgalb.Entities.Utilisateur;
import com.example.sgalb.Repositories.UtilisateurRepository;
import com.example.sgalb.Services.GoogleAuthentication.GoogleAuthenticationServiceInterface;
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
@RequestMapping("/googleStuff")
public class GoogleController {
    GoogleAuthenticationServiceInterface googleAuthenticationRepository;
    UtilisateurRepository utilisateurRepository;
    @GetMapping("/user-info")
    public Map<String, Object> userInfo(@AuthenticationPrincipal OAuth2User principal) {
        if (principal != null) {
            String googleId = principal.getAttribute("sub");
            String name = principal.getAttribute("name");
            String email = principal.getAttribute("email");
            String avatar = principal.getAttribute("picture");

            // Charger ou mettre à jour l'utilisateur si nécessaire
            googleAuthenticationRepository.loadUser(googleId, name, email, avatar);

            // Récupérer l'utilisateur depuis la BDD
            Utilisateur utilisateur = utilisateurRepository.findByEmail(email);
            // Obtenir les attributs de l'utilisateur Google
            Map<String, Object> attributes = new HashMap<>(principal.getAttributes());

            // Ajouter l'idUtilisateur si trouvé
            if (utilisateur != null) {
                attributes.put("idUtilisateur", utilisateur.getIdUtilisateur());
            }

            return attributes;
        }

        return Map.of("error", "Utilisateur non authentifié");
    }




    @GetMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        if (authentication != null) {
            new SecurityContextLogoutHandler().logout(request, response, authentication);
        }

        // Retourne l'URL de déconnexion Google
        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("logoutUrl", "https://accounts.google.com/logout");
        return ResponseEntity.ok(responseMap);
    }

}
