package com.example.sgalb.Controllers;

import com.example.sgalb.Entities.Archivage;
import com.example.sgalb.Entities.Planification;
import com.example.sgalb.Entities.Serveur;
import com.example.sgalb.Entities.Utilisateur;
import com.example.sgalb.Enum.Status;
import com.example.sgalb.Repositories.PlanificationRepository;
import com.example.sgalb.Repositories.ServeurRepository;
import com.example.sgalb.Repositories.UtilisateurRepository;
import com.example.sgalb.Services.AzureBlob.AzureBlobServiceInterfaceImpl;
import com.example.sgalb.Services.Planification.PlanificationServiceInterfaceImpl;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/v1/planification")
@AllArgsConstructor

public class PlanificationController {

    PlanificationServiceInterfaceImpl planificationServiceInterface;
    AzureBlobServiceInterfaceImpl azureBlobServiceInterface;
    UtilisateurRepository utilisateurRepository;
    PlanificationRepository planificationRepository;
    //add planification
//    @PostMapping("/addPlanification")
//    public ResponseEntity<String> addPlanification(
//            @RequestParam("file") MultipartFile file,
//            @RequestParam("frequence") String frequence,
//            @RequestParam("prochaineExecution") String prochaineExecutionStr,
//            @RequestParam("idUtilisateur") Long idUtilisateur) {
//
//        try {
//            // 1. Convertir la date
//            Date prochaineExecution = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm").parse(prochaineExecutionStr);
//
//            // 2. Créer dossier de stockage local (s’il n’existe pas)
//            String dossierStockage = "C:/fichiers_planification/";
//            File dossier = new File(dossierStockage);
//            if (!dossier.exists()) {
//                dossier.mkdirs();
//            }
//
//            // 3. Sauvegarder le fichier sur le disque
//            String originalFilename = file.getOriginalFilename();
//            File fichierLocal = new File(dossier, originalFilename);
//            file.transferTo(fichierLocal); // sauvegarde
//
//            // 4. Créer l’objet Planification
//            Planification planification = new Planification();
//            planification.setFréquence(frequence);
//            planification.setProchaineExecution(prochaineExecution);
//            planification.setServeur("20.199.25.198");
//
//            // Extension du fichier
//            String extension = originalFilename.substring(originalFilename.lastIndexOf('.') + 1);
//            planification.setTypeDArchive(extension.toUpperCase());
//
//            // 🔥 Enregistrer chemin + nom
//            planification.setCheminFichier(fichierLocal.getParent());
//            planification.setNomFichier(fichierLocal.getName());
//
//            // Lien avec utilisateur
//            Utilisateur utilisateur = utilisateurRepository.findByIdUtilisateur(idUtilisateur);
//            planification.setUtilisateur(utilisateur);
//
//            // Statut initial
//            planification.setStatut(Status.En_cours);
//
//            // Enregistrer dans la base
//            planificationRepository.save(planification);
//
//            return ResponseEntity.status(HttpStatus.CREATED)
//                    .body("✅ Planification enregistrée. Fichier stocké localement : " + fichierLocal.getAbsolutePath());
//
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body("❌ Erreur lors de la planification : " + e.getMessage());
//        }
//    }
    ServeurRepository serveurRepository;
    @PostMapping("/addPlanification")
    public ResponseEntity<String> addPlanification(
            @RequestParam("nomFichier") String nomFichier,
            @RequestParam("frequence") String frequence,
            @RequestParam("prochaineExecution") String prochaineExecutionStr,
            @RequestParam("idUtilisateur") Long idUtilisateur) {

        try {
            Date prochaineExecution = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm").parse(prochaineExecutionStr);

            Utilisateur utilisateur = utilisateurRepository.findByIdUtilisateur(idUtilisateur);
            if (utilisateur == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("❌ Utilisateur introuvable avec l'ID : " + idUtilisateur);
            }

            // ✅ Récupérer le(s) serveur(s) connectés
            List<Serveur> serveurs = serveurRepository.findAllByUtilisateurIdUtilisateurAndConnectedTrue(idUtilisateur);
            if (serveurs == null || serveurs.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("❌ Aucun serveur connecté associé à cet utilisateur !");
            }

            Serveur serveur = serveurs.get(0); // ✅ Utiliser le premier serveur connecté

            Planification planification = new Planification();
            planification.setNomFichier(nomFichier); // juste le nom relatif (ex : "fichier.txt" ou "logs/dossier/file.log")
            planification.setCheminFichier("/home/" + serveur.getUser() + "/"); // chemin de base
            planification.setFréquence(frequence);
            planification.setProchaineExecution(prochaineExecution);
            planification.setServeur(serveur.getHost());

            // Déduire l'extension du fichier
            String extension = nomFichier.contains(".") ?
                    nomFichier.substring(nomFichier.lastIndexOf('.') + 1) : "inconnu";
            planification.setTypeDArchive(extension.toUpperCase());

            planification.setUtilisateur(utilisateur);
            planification.setStatut(Status.En_cours);

            planificationRepository.save(planification);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("✅ Planification ajoutée pour fichier distant : " + nomFichier);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("❌ Erreur lors de l'ajout de la planification : " + e.getMessage());
        }
    }



    //update Planification
    @PostMapping("/updatePlanification")
    public ResponseEntity<String> updatePlanification(@RequestParam Long idPlanification, @RequestBody Planification planification) {
        try {
            String message = planificationServiceInterface.updatePlanification(planification,idPlanification);
            return ResponseEntity.ok(message);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Une erreur inattendue s'est produite : " + e.getMessage());
        }
    }
    @DeleteMapping("/deletePlanification")
    public ResponseEntity<String> deletePlanification(@RequestParam Long idPlanification)
    {
        try {
            String message = planificationServiceInterface.deletePlanification(idPlanification);
            return ResponseEntity.ok(message);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Une erreur s'est produite lors de la suppression : " + e.getMessage());
        }
    }

    @GetMapping("/getPlanificationsOfUser")
    public ResponseEntity<?> getArchivesOfUser(@RequestParam Long idUtilisateur) {
        try {
            List<Planification> archives = planificationServiceInterface.selectPanificationForUser(idUtilisateur);
            return ResponseEntity.ok(archives);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Utilisateur introuvable : " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Une erreur s'est produite lors de la récupération des archives : " + e.getMessage());
        }
    }

//    @GetMapping("/executerPlanification")
//    public ResponseEntity<String> executerPlanification(@RequestParam Long idPlanification) {
//        String result = planificationServiceInterface.planificationOfArchive(idPlanification);
//        return ResponseEntity.ok(result);
//    }



}