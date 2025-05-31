package com.example.sgalb.Controllers;

import com.example.sgalb.Entities.Serveur;
import com.example.sgalb.Entities.SeuillesAlerting;
import com.example.sgalb.Repositories.ServeurRepository;
import com.example.sgalb.Services.SeuillesAlerting.SeuillesAlertingServiceInterface;
import com.example.sgalb.Services.SshService.SshServiceInterfaceImpl;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/serveurUnix/metrics")
@AllArgsConstructor
public class SshController {

    SshServiceInterfaceImpl sshService;
    ServeurRepository serveurRepository;
    SeuillesAlertingServiceInterface seuillesAlertingServiceInterface;

    // Méthode utilitaire pour charger les infos SSH du serveur
//    private Serveur getServeurByUtilisateur(Long idUtilisateur) {
//        return serveurRepository.findByUtilisateurIdUtilisateur(idUtilisateur).stream()
//                .findFirst()
//                .orElseThrow(() -> new RuntimeException("Aucun serveur trouvé pour cet utilisateur"));
//    }

    private Serveur getServeurByUtilisateur(Long idUtilisateur) {
        return serveurRepository.findByUtilisateurIdUtilisateur(idUtilisateur);
    }

    @GetMapping("/cpu")
    public Map<String, Object> getCpuMetrics(@RequestParam Long idUtilisateur) {
        Serveur serveur = getServeurByUtilisateur(idUtilisateur);
        double cpuUsage = sshService.getCpuUsage(serveur.getHost(), serveur.getUser(), serveur.getPassword());

        Map<String, Object> response = new HashMap<>();
        response.put("usage", cpuUsage);
        response.put("timestamp", LocalDateTime.now());
        return response;
    }

    @GetMapping("/ram")
    public double getRamMetrics(@RequestParam Long idUtilisateur) {
        Serveur serveur = getServeurByUtilisateur(idUtilisateur);
        return sshService.getRemoteRamUsage(serveur.getHost(), serveur.getUser(), serveur.getPassword());
    }

    @GetMapping("/network")
    public Map<String, Long> getNetworkMetrics(@RequestParam Long idUtilisateur) {
        Serveur serveur = getServeurByUtilisateur(idUtilisateur);
        return sshService.getNetworkStats(serveur.getHost(), serveur.getUser(), serveur.getPassword());
    }

    @GetMapping("/storage")
    public double getStorageMetrics(@RequestParam Long idUtilisateur) {
        Serveur serveur = getServeurByUtilisateur(idUtilisateur);
        return sshService.getRemoteStorageStatus(serveur.getHost(), serveur.getUser(), serveur.getPassword());
    }

    @GetMapping("/checkMetrics")
    public String checkMetricsAndGenerateAlert(@RequestParam Long idUtilisateur) {

        Serveur serveur = getServeurByUtilisateur(idUtilisateur);
        SeuillesAlerting seuillesAlerting = seuillesAlertingServiceInterface.getTheMetriquesOfUSerConnected(idUtilisateur);
        return sshService.MetriqueStatusAlerting(serveur.getHost(),
                                                serveur.getUser(), serveur.getPassword(),
                                                idUtilisateur,
                                                seuillesAlerting.getSeuilleCPU(),
                                                seuillesAlerting.getSeuilleRam(),
                                                seuillesAlerting.getSeuilleDisque(),
                                                seuillesAlerting.getSeuilleNetworkReceved(),
                                                seuillesAlerting.getSeuilleNetworkSent(),
                                                seuillesAlerting.getSeuilleToReceveMail(),
                                                seuillesAlerting.getMailSender(),
                                                seuillesAlerting.getPasswordMailSender());
    }
    // controller pour uploader  les fichiers dans la VM only for testing the use of archive things
    @PostMapping("/uploadFileForTest")
    public ResponseEntity<String> uploadFile(@RequestParam Long idUtilisateur ,@RequestParam("file") MultipartFile file) {
        Serveur serveur = getServeurByUtilisateur(idUtilisateur);
        String result = sshService.uploadFileTest(file,serveur.getUser(),serveur.getHost(),serveur.getPassword());
        if (result.startsWith("✅")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(500).body(result);
        }
    }
}
