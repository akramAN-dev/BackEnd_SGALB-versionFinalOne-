package com.example.sgalb.Controllers;


import com.example.sgalb.Entities.Utilisateur;
import com.example.sgalb.Services.Utilisateur.UtilisateurServiceInterfaceImpl;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/utilisateur")
@AllArgsConstructor
public class ClientController {
    UtilisateurServiceInterfaceImpl utilisateurServiceInterface;

    @PostMapping("/addUtilisateur")
    public ResponseEntity<String> addUtilisateur(@RequestBody Utilisateur utilisateur) {
        try {
            Utilisateur newUser = utilisateurServiceInterface.creationCompte(utilisateur);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("Utilisateur cree avec succee");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred");
        }
    }

    @GetMapping("/user-info")
    public Map<String, Object> userInfos(@RequestParam("userId") Long userId) {
        Utilisateur utilisateur = utilisateurServiceInterface.userInfo(userId);

        // Créer un Map pour renvoyer une réponse personnalisée
        Map<String, Object> response = new HashMap<>();
        response.put("idUtilisateur", utilisateur.getIdUtilisateur());
        response.put("name", utilisateur.getNom() +" "+utilisateur.getPrenom());  // Changer "nom" en "name"
        response.put("prenom", utilisateur.getPrenom());
        response.put("email", utilisateur.getEmail());
        response.put("motDePasse", utilisateur.getMotDePasse());
        response.put("role", utilisateur.getRole());
        response.put("dateInscription", utilisateur.getDateInscription());
        response.put("statut", utilisateur.getStatut());

        return response;
    }




}
