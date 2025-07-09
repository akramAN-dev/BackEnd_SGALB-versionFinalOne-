package com.example.sgalb.Controllers;

import com.azure.core.annotation.Get;
import com.example.sgalb.Entities.Serveur;
import com.example.sgalb.Repositories.ServeurRepository;
import com.example.sgalb.Services.Serveur.ServeurServiceInterface;
import com.example.sgalb.Services.SshService.SshServiceInterface;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/serveurs")
@AllArgsConstructor
public class ServeurController {
    ServeurRepository serveurRepository;
    ServeurServiceInterface serveurServiceInterface;
    SshServiceInterface sshServiceInterface;

    @PostMapping("/addServeur")
    public ResponseEntity<Serveur> ajouterServeur(@RequestParam Long idUtilisateur,@RequestBody Serveur serveur) {
        Serveur nouveauServeur = serveurServiceInterface.ajouterServeur(serveur,idUtilisateur);
        return new ResponseEntity<>(nouveauServeur, HttpStatus.CREATED);
    }

    // 🔄 Modifier un serveur existant
//    @PostMapping("/updateServeur")
//    public ResponseEntity<Serveur> modifierServeur(@RequestParam Long idUtilisateur, @RequestBody Serveur serveur) {
//        Serveur serveurMisAJour = serveurServiceInterface.modifierServeur(idUtilisateur,serveur);
//        return new ResponseEntity<>(serveurMisAJour, HttpStatus.OK);
//    }
    @GetMapping("/getServeur")
    public ResponseEntity<Serveur> getServeur(@RequestParam Long idUtilisateur) {
        Serveur serveur= serveurServiceInterface.getServeur(idUtilisateur);
        return new ResponseEntity<>(serveur, HttpStatus.OK);
    }
//    @GetMapping("/getServeur")
//    public ResponseEntity<?> getServeur(@RequestParam Long idUtilisateur) {
//        Optional<Serveur> serveur = serveurServiceInterface.getServeur(idUtilisateur);
//        if (serveur.isPresent()) {
//            return ResponseEntity.ok(serveur.get());
//        } else {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                    .body("Aucun serveur trouvé pour cet utilisateur.");
//        }
//    }
// to check after this
@GetMapping("/getByHostAndUsername")
public ResponseEntity<Serveur> getServeurByHostAndUsername(
        @RequestParam String host,
        @RequestParam String username,
        @RequestParam Long idUtilisateur) {

    Optional<Serveur> serveurOpt = serveurServiceInterface.getServeurByHostAndUsernameAndUtilisateur(host, username, idUtilisateur);

    return serveurOpt
            .map(serveur -> new ResponseEntity<>(serveur, HttpStatus.OK))
            .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
}

    @GetMapping("/getConnectedServer")
    public ResponseEntity<Serveur> getConnectedServerByUtilisateur(@RequestParam Long idUtilisateur) {
        List<Serveur> serveurs = serveurRepository.findAllByUtilisateurIdUtilisateurAndConnectedTrue(idUtilisateur);

        if (serveurs.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Si plusieurs serveurs sont connectés, on retourne le premier (ou adapte selon besoin)
        return ResponseEntity.ok(serveurs.get(0));
    }

    @GetMapping("/getServeursByUtilisateur")
    public ResponseEntity<List<Serveur>> getServeursByUtilisateur(@RequestParam Long idUtilisateur) {
        List<Serveur> serveurs = serveurServiceInterface.getServeursByUtilisateur(idUtilisateur);
        return new ResponseEntity<>(serveurs, HttpStatus.OK);
    }

    // connexion a un serveur
    @PostMapping("/connectionToServeur")
    public ResponseEntity<?> verifierConnexion(
            @RequestBody Serveur request,
            @RequestParam String password,
            @RequestParam Long idUtilisateur) {

        Optional<Serveur> serveurOpt = serveurServiceInterface.verifierConnexionServeur(
                request.getHost(),
                request.getUser(),
                password,
                idUtilisateur
        );

        if (serveurOpt.isPresent()) {
            return ResponseEntity.ok(serveurOpt.get());
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Échec de connexion : identifiants invalides.");
        }
    }

    @PostMapping("/connect")
    public ResponseEntity<String> connectServeur(
            @RequestParam Long idUtilisateur,
            @RequestParam Long idServeur) {
        try {
            serveurServiceInterface.setServeurConnecte(idUtilisateur, idServeur);
            return ResponseEntity.ok("Serveur connecté avec succès");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur : " + e.getMessage());
        }
    }

    @PostMapping("/deconnect")
    public ResponseEntity<String> deconnecterServeur(
            @RequestParam Long idUtilisateur,
            @RequestParam Long idServeur) {
        try {
            serveurServiceInterface.deconnecterServeur(idUtilisateur, idServeur);
            return ResponseEntity.ok("Serveur déconnecté avec succès");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur : " + e.getMessage());
        }
    }

    @PostMapping("/testerEtatServeur")
    public ResponseEntity<String> testerEtatServeur(@RequestParam Long idServeur) {
        Optional<Serveur> optionalServeur = serveurRepository.findById(idServeur);

        if (optionalServeur.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Serveur avec l'ID " + idServeur + " introuvable.");
        }

        Serveur serveur = optionalServeur.get();
        sshServiceInterface.testerEtatServeurEtMettreAJour(serveur);

        String message = serveur.isState()
                ? "Le serveur fonctionne correctement (state = 1)."
                : "Le serveur ne répond pas (state = 0).";

        return ResponseEntity.ok(message);
    }

    @PutMapping("/update")
    public ResponseEntity<String> updateServeur(@RequestParam Long idServeur,@RequestBody Serveur serveur) {
        String response = serveurServiceInterface.updateServeur(idServeur,serveur);
        if (response.contains("succès")) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(404).body(response);
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteServeur(@RequestParam Long idServeur) {
        String response = serveurServiceInterface.deleteServeur(idServeur);
        if (response.contains("succès")) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(404).body(response);
        }
    }


}
