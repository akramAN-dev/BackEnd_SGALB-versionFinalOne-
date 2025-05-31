package com.example.sgalb.Controllers;

import com.example.sgalb.Services.Metriques.MetriqueServiceInterface;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/metriques")  // Préfixe commun pour les métriques
@AllArgsConstructor
public class MetriqueController {

    MetriqueServiceInterface metriqueServiceInterface;

    @GetMapping("/cpu")
    public double getCpuStatus() {
        return metriqueServiceInterface.getCpuUsage();
    }
    @GetMapping("/ram")
    public double getRamStatus() {
        return metriqueServiceInterface.getRamUsage();
    }
    @GetMapping("/network")
    public Map<String, Long> getNetworkStatus() {
        return metriqueServiceInterface.getNetworkStats();
    }
    @GetMapping("/storage")
    public double getStorageStatus() {
        return metriqueServiceInterface.getStorageStatus();
    }

}
