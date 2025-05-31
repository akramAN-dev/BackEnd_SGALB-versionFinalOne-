package com.example.sgalb.Controllers;

import com.example.sgalb.Entities.Alerting;
import com.example.sgalb.Entities.Archivage;
import com.example.sgalb.Enum.Gravité;
import com.example.sgalb.Services.Alerting.AlertingServiceInterfaceImpl;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/alerting")
@AllArgsConstructor

public class AlertController {

    AlertingServiceInterfaceImpl alertingServiceInterface;
    //add Alert
    @PostMapping("/addAlert")
    public ResponseEntity<String> addAlert(@RequestBody Alerting alerting, @RequestParam Long idUtilisateur)
    {
        try {
            Alerting newAlert = alertingServiceInterface.addAlert(alerting,idUtilisateur);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("alert cree avec succee");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Une erreur inattendue s'est produite : " + e.getMessage());
        }
    }

    //update Alert
    @PostMapping("/updateAlert")
    public ResponseEntity<String> updateAlert(@RequestParam Long idAlert, @RequestBody Alerting alert) {
        try {
            String message = alertingServiceInterface.updateAlert(alert,idAlert);
            return ResponseEntity.ok(message);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Une erreur inattendue s'est produite : " + e.getMessage());
        }
    }
    @DeleteMapping("/deleteAlert")
    public ResponseEntity<String> deleteAlert(@RequestParam Long idAlert)
    {
        try {
            String message = alertingServiceInterface.deleteAlert(idAlert);
            return ResponseEntity.ok(message);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Une erreur s'est produite lors de la suppression : " + e.getMessage());
        }
    }
    @GetMapping("/getAlertsOfUser")
    public ResponseEntity<?> getAlertsOfUser(@RequestParam Long idUtilisateur) {
        try {
            List<Alerting> alerts = alertingServiceInterface.selectALertsForUser(idUtilisateur);
            return ResponseEntity.ok(alerts);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Utilisateur introuvable : " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Une erreur s'est produite lors de la récupération des alert : " + e.getMessage());
        }
//        @GetMapping("/getOneArchive/{id}")
//        public ResponseEntity<?> getOneArchive(@PathVariable Long id) {
//            try {
//                Archivage archive = archiveServiceInterface.selectOneArchiveById(id);
//                return ResponseEntity.ok(archive);
//            } catch (Exception e) {
//                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Erreur : " + e.getMessage());
//            }
//        }
    }
    // stats part
   // numberOfEachGravitiesOfAlerts
    @GetMapping("/getGravityInAlerts")
    public ResponseEntity<Map<Gravité, Long>> getNumberOfEachGravityInAlerts(@RequestParam Long idUtilisateur) {
        Map<Gravité, Long> result = alertingServiceInterface.numberOfEachGravitiesOfAlerts(idUtilisateur);
        return ResponseEntity.ok(result);
    }


}