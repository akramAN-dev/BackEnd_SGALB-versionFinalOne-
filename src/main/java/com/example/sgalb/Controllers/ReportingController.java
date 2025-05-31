package com.example.sgalb.Controllers;

import com.example.sgalb.Entities.Reporting;
import com.example.sgalb.Entities.Utilisateur;
import com.example.sgalb.Repositories.ReportingRepository;
import com.example.sgalb.Repositories.UtilisateurRepository;
import com.example.sgalb.Services.Reporting.ReportingServiceInterface;
import lombok.AllArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@RequestMapping("/api/v1/reporting")
@AllArgsConstructor
public class ReportingController {

     ReportingServiceInterface reportingService;
     ReportingRepository reportingRepository;
     UtilisateurRepository utilisateurRepository;


    @PostMapping("/addRapport")
    public ResponseEntity<byte[]> genererRapport(
            @RequestParam("idUtilisateur") Long idUtilisateur,
            @RequestBody Reporting reporting) {
        try {
            byte[] rapportBytes = reportingService.genererRapportGlobal(idUtilisateur, reporting);

            String extension = reporting.getFormat().toLowerCase();
            String fileName = "rapport_" + System.currentTimeMillis() + "." + extension;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(getMediaTypeForFormat(extension));
            headers.setContentDisposition(ContentDisposition.builder("attachment")
                    .filename(fileName)
                    .build());
            Utilisateur userSelected= utilisateurRepository.findByIdUtilisateur(idUtilisateur);
            reporting.setUtilisateur(userSelected);

            // Sauvegarder le rapport dans la base de données
            reportingRepository.save(reporting);

            return new ResponseEntity<>(rapportBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    private MediaType getMediaTypeForFormat(String format) {
        switch (format) {
            case "pdf":
                return MediaType.APPLICATION_PDF;
            case "xlsx":
                return MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            case "csv":
                return MediaType.TEXT_PLAIN;
            case "txt":
                return MediaType.TEXT_PLAIN;
            case "docx":
                return MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            default:
                return MediaType.APPLICATION_OCTET_STREAM;
        }
    }

    @GetMapping("/getNumberOfRepports")
    public ResponseEntity<Long> getNumberOfRepports(@RequestParam Long idUtilisateur) {
        Long nombre = reportingService.getNumberOfRepports(idUtilisateur);
        return ResponseEntity.ok(nombre);
    }
}
