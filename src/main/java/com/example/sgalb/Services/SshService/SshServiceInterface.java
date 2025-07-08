package com.example.sgalb.Services.SshService;

import com.example.sgalb.Entities.Serveur;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface SshServiceInterface {
    String executeCommand(String host, String user, String password, String command);
    double getCpuUsage(String host, String user, String password);
    double getRemoteRamUsage(String host, String user, String password);
    Map<String, Long> getNetworkStats(String host, String user, String password);
    double getRemoteStorageStatus(String host, String user, String password);
    String MetriqueStatusAlerting(String host, String user, String password, Long idUser,Long seuilleCPU,Long seuilleRam,Long seuilleDisque,Long seuilleNetworkReceved,Long seuilleNetworkSent,Long seuilleMailing,String mailSender,String passwordSender);
    //String MetriqueStatusAlerting(String host, String user, String password, Long idUser);
    //String uploadFileTest(MultipartFile file, String username, String host, String password);
    boolean testerConnexionServeur(Serveur serveur);
    void testerEtatServeurEtMettreAJour(Serveur serveur);
//    void verifierTousLesServeurs();
    // ici partie os windows
    double getWindowsCpuUsage(String host, String username, String password);
    double getWindowsRamUsage(String host, String username, String password);
    Map<String, Long> getWindowsNetworkStats(String host, String user, String password);
    double getWindowsStorageUsage(String host, String user, String password);
    double getCpuUsageByOS(Serveur serveur);
    double getRamUsageByOS(Serveur serveur);
    double getStorageUsageByOS(Serveur serveur);
    Map<String, Long> getNetworkStatsByOS(Serveur serveur);
    List<Serveur> getServeurCredentials(Long idUtilisateur);
}
