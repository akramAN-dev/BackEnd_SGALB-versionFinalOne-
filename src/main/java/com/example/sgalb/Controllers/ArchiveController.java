package com.example.sgalb.Controllers;

import com.example.sgalb.Entities.Archivage;
import com.example.sgalb.Services.Archive.ArchiveServiceInterfaceImpl;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/archivage")
@AllArgsConstructor

public class ArchiveController {

    ArchiveServiceInterfaceImpl archiveServiceInterface;
    //add archive
    @PostMapping("/addArchive")
    public ResponseEntity<String> addArchive(@RequestBody Archivage archive,@RequestParam Long idUtilisateur)
    {
        try {
            Archivage newArchive = archiveServiceInterface.addArchive(archive,idUtilisateur);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("Archive cree avec succee");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Une erreur inattendue s'est produite : " + e.getMessage());
        }
    }

    //update archive
    @PostMapping("/updateArchive")
    public ResponseEntity<String> updateArchive(@RequestParam Long idArchive, @RequestBody Archivage archive) {
        try {
            String message = archiveServiceInterface.updateArchive(archive,idArchive);
            return ResponseEntity.ok(message);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Une erreur inattendue s'est produite : " + e.getMessage());
        }
    }
    // delete anArchive
    @DeleteMapping("/deleteArchive")
    public ResponseEntity<String> deleteArchive(@RequestParam Long idArchive)
    {
        try {
            String message = archiveServiceInterface.deleteArchive(idArchive);
            return ResponseEntity.ok(message);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Une erreur s'est produite lors de la suppression : " + e.getMessage());
        }
    }
    // get one archive by id
    @GetMapping("/getOneArchive/{id}")
    public ResponseEntity<?> getOneArchive(@PathVariable Long id) {
        try {
            Archivage archive = archiveServiceInterface.selectOneArchiveById(id);
            return ResponseEntity.ok(archive);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Erreur : " + e.getMessage());
        }
    }

    // get all archives for a user
    @GetMapping("/getArchivesOfUser")
    public ResponseEntity<?> getArchivesOfUser(@RequestParam Long idUtilisateur) {
        try {
            List<Archivage> archives = archiveServiceInterface.selectArchivesForUser(idUtilisateur);
            return ResponseEntity.ok(archives);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Utilisateur introuvable : " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Une erreur s'est produite lors de la récupération des archives : " + e.getMessage());
        }
    }

    // get Number Of Archives for a user
    @GetMapping("/getNumberOfArchives")
    public ResponseEntity<Long> getNumberOfArchives(@RequestParam Long idUtilisateur) {
        Long nombre = archiveServiceInterface.getNumberOfArchives(idUtilisateur);
        return ResponseEntity.ok(nombre);
    }

    @GetMapping("/getNombreArchivesOfDates")
    public ResponseEntity<Map<LocalDate, Long>> getNombreArchivesOfDates(@RequestParam Long idUtilisateur) {
        Map<LocalDate, Long> archivesParDate = archiveServiceInterface.getNombreArchivesOfDates(idUtilisateur);
        return ResponseEntity.ok(archivesParDate);
    }

    @GetMapping("/getArchivesNotTerminerOfUser")
    public ResponseEntity<?> getArchivesNotArchivedOfUser(@RequestParam Long idUtilisateur) {
        try {
            List<Archivage> archives = archiveServiceInterface.getArchivesNonTermineesParUtilisateur(idUtilisateur);
            return ResponseEntity.ok(archives);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Utilisateur introuvable : " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Une erreur s'est produite lors de la récupération des archives : " + e.getMessage());
        }
    }

}