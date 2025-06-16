package com.example.sgalb.Controllers;

import com.example.sgalb.Dtos.ServiceInfoDTO;
import com.example.sgalb.Services.MachineServices.MachineServicesServiceInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/services")
public class MachineServicesController {

    @Autowired
    private MachineServicesServiceInterface machineService;

    // Liste tous les services systemd pour un utilisateur donné
    @GetMapping("/listServices")
    public ResponseEntity<List<ServiceInfoDTO>> getServices(@RequestParam Long idUtilisateur) {
        List<ServiceInfoDTO> services = machineService.listDesServices(idUtilisateur);
        return ResponseEntity.ok(services);
    }

    // Status d'un service pour un utilisateur donné
    @GetMapping("/statusService")
    public String status(@RequestParam String serviceName, @RequestParam Long idUtilisateur) {
        return machineService.status(serviceName, idUtilisateur);
    }

    // Démarrer un service pour un utilisateur donné
    @GetMapping("/startService")
    public String start(@RequestParam String serviceName, @RequestParam Long idUtilisateur) {
        return machineService.start(serviceName, idUtilisateur);
    }

    // Stopper un service pour un utilisateur donné
    @GetMapping("/stopService")
    public String stop(@RequestParam String serviceName, @RequestParam Long idUtilisateur) {
        return machineService.stop(serviceName, idUtilisateur);
    }

    // Redémarrer un service pour un utilisateur donné
    @GetMapping("/restartService")
    public String restart(@RequestParam String serviceName, @RequestParam Long idUtilisateur) {
        return machineService.restart(serviceName, idUtilisateur);
    }
}
