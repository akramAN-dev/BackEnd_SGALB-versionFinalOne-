package com.example.sgalb.Controllers;

import com.azure.core.annotation.Get;
import com.example.sgalb.Entities.Serveur;
import com.example.sgalb.Repositories.ServeurRepository;
import com.example.sgalb.Services.Serveur.ServeurServiceInterface;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/serveurs")
@AllArgsConstructor
public class ServeurController {
    ServeurRepository serveurRepository;
    ServeurServiceInterface serveurServiceInterface;

    @PostMapping("/addServeur")
    public ResponseEntity<Serveur> ajouterServeur(@RequestParam Long idUtilisateur,@RequestBody Serveur serveur) {
        Serveur nouveauServeur = serveurServiceInterface.ajouterServeur(serveur,idUtilisateur);
        return new ResponseEntity<>(nouveauServeur, HttpStatus.CREATED);
    }

    // 🔄 Modifier un serveur existant
    @PostMapping("/updateServeur")
    public ResponseEntity<Serveur> modifierServeur(@RequestParam Long idUtilisateur, @RequestBody Serveur serveur) {
        Serveur serveurMisAJour = serveurServiceInterface.modifierServeur(idUtilisateur,serveur);
        return new ResponseEntity<>(serveurMisAJour, HttpStatus.OK);
    }
    @GetMapping("/getServeur")
    public ResponseEntity<Serveur> getServeur(@RequestParam Long idUtilisateur) {
        Serveur serveur= serveurServiceInterface.getServeur(idUtilisateur);
        return new ResponseEntity<>(serveur, HttpStatus.OK);
    }


}
