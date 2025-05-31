package com.example.sgalb.Services.SshService;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface SshServiceInterface {
    String executeCommand(String host, String user, String password, String command);
    double getCpuUsage(String host, String user, String password);
    double getRemoteRamUsage(String host, String user, String password);
    Map<String, Long> getNetworkStats(String host, String user, String password);
    double getRemoteStorageStatus(String host, String user, String password);
    String MetriqueStatusAlerting(String host, String user, String password, Long idUser,Long seuilleCPU,Long seuilleRam,Long seuilleDisque,Long seuilleNetworkReceved,Long seuilleNetworkSent,Long seuilleMailing,String mailSender,String passwordSender);
    //String MetriqueStatusAlerting(String host, String user, String password, Long idUser);
    String uploadFileTest(MultipartFile file, String username, String host, String password);
}
