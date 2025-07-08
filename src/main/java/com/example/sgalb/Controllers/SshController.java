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

    private final SshServiceInterfaceImpl sshService;
    private final ServeurRepository serveurRepository;
    private final SeuillesAlertingServiceInterface seuillesAlertingServiceInterface;

    // 🔁 Utilise le serveur connecté pour l'utilisateur
    private Serveur getServeurByUtilisateur(Long idUtilisateur) {
        return serveurRepository.findAllByUtilisateurIdUtilisateurAndConnectedTrue(idUtilisateur)
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Aucun serveur connecté trouvé pour cet utilisateur"));
    }

    @GetMapping("/cpu")
    public Map<String, Object> getCpuMetrics(@RequestParam Long idUtilisateur) {
        Serveur serveur = getServeurByUtilisateur(idUtilisateur);
        double cpuUsage = sshService.getCpuUsageByOS(serveur);

        Map<String, Object> response = new HashMap<>();
        response.put("usage", cpuUsage);
        response.put("timestamp", LocalDateTime.now());
        return response;
    }

    @GetMapping("/ram")
    public double getRamMetrics(@RequestParam Long idUtilisateur) {
        Serveur serveur = getServeurByUtilisateur(idUtilisateur);
        return sshService.getRamUsageByOS(serveur);
    }

    @GetMapping("/network")
    public Map<String, Long> getNetworkMetrics(@RequestParam Long idUtilisateur) {
        Serveur serveur = getServeurByUtilisateur(idUtilisateur);
        return sshService.getNetworkStatsByOS(serveur);
    }

    @GetMapping("/storage")
    public double getStorageMetrics(@RequestParam Long idUtilisateur) {
        Serveur serveur = getServeurByUtilisateur(idUtilisateur);
        return sshService.getStorageUsageByOS(serveur);
    }

    @GetMapping("/checkMetrics")
    public String checkMetricsAndGenerateAlert(@RequestParam Long idUtilisateur) {
        Serveur serveur = getServeurByUtilisateur(idUtilisateur);
        SeuillesAlerting seuils = seuillesAlertingServiceInterface.getTheMetriquesOfUSerConnected(idUtilisateur);

        // 🔁 Appelle toujours la méthode centrale (tu peux la refactorer plus tard pour qu'elle utilise aussi ...ByOS)
        return sshService.MetriqueStatusAlerting(
                serveur.getHost(), serveur.getUser(), serveur.getPassword(),
                idUtilisateur,
                seuils.getSeuilleCPU(),
                seuils.getSeuilleRam(),
                seuils.getSeuilleDisque(),
                seuils.getSeuilleNetworkReceved(),
                seuils.getSeuilleNetworkSent(),
                seuils.getSeuilleToReceveMail(),
                seuils.getMailSender(),
                seuils.getPasswordMailSender()
        );
    }

    @PostMapping("/uploadFileForTest")
    public ResponseEntity<String> uploadFile(@RequestParam Long idUtilisateur, @RequestParam("file") MultipartFile file) {
        Serveur serveur = getServeurByUtilisateur(idUtilisateur);
        String result = sshService.uploadFileTest(file, serveur.getUser(), serveur.getHost(), serveur.getPassword());
        if (result.startsWith("✅") || result.toLowerCase().contains("réussi")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(500).body(result);
        }
    }

    // ✅ Monitoring global de tous les serveurs existants (admin)
    @GetMapping("/global-monitoring")
    public ResponseEntity<Map<String, Map<String, Object>>> monitorAllServeurs() {
        Map<String, Map<String, Object>> result = new HashMap<>();
        for (Serveur serveur : serveurRepository.findAll()) {
            try {
                Map<String, Object> metrics = new HashMap<>();
                metrics.put("cpu", sshService.getCpuUsageByOS(serveur));
                metrics.put("ram", sshService.getRamUsageByOS(serveur));
                metrics.put("storage", sshService.getStorageUsageByOS(serveur));
                metrics.put("network", sshService.getNetworkStatsByOS(serveur));
                result.put(serveur.getHost(), metrics);
            } catch (Exception e) {
                result.put(serveur.getHost(), Map.of("error", e.getMessage()));
            }
        }
        return ResponseEntity.ok(result);
    }
}
