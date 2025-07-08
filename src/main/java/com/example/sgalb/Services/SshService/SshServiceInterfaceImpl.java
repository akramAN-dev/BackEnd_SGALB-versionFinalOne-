package com.example.sgalb.Services.SshService;

import com.example.sgalb.Entities.*;
import com.example.sgalb.Enum.Gravité;
import com.example.sgalb.Enum.Status;
import com.example.sgalb.Repositories.*;
import com.example.sgalb.Services.Mailing.EmailServiceInterface;
import com.example.sgalb.Services.Serveur.ServeurServiceInterface;
import com.example.sgalb.Utils.EncryptionUtils;
import com.jcraft.jsch.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.*;

@Slf4j
@Service
@AllArgsConstructor
public class SshServiceInterfaceImpl implements SshServiceInterface {
    AlertingRepository alertingRepository;
    UtilisateurRepository utilisateurRepository;
    NotificationRepository notificationRepository;
    EmailServiceInterface emailService;
    SeuillesAlertingRepository seuillesAlertingRepository;
    ServeurRepository serveurRepository;
    ServeurServiceInterface serveurServiceInterface;

    /**
     * Récupère un Serveur avec mot de passe décrypté (pour l'utilisateur donné)
     */
//    private Serveur getServeurCredentials(Long idUtilisateur) {
//        return serveurServiceInterface.getServeurWithDecryptedPassword(idUtilisateur);
//    }
    public List<Serveur> getServeurCredentials(Long idUtilisateur) {
        return serveurServiceInterface.getServeursWithDecryptedPasswords(idUtilisateur);
    }


//    public String executeCommandByUserId(Long idUtilisateur, String command) {
//        Serveur serveur = getServeurCredentials(idUtilisateur);
//        if (serveur == null) {
//            return "Serveur introuvable pour l'utilisateur " + idUtilisateur;
//        }
//        return executeCommand(serveur.getHost(), serveur.getUser(), serveur.getPassword(), command);
//    }

    public String executeCommandEncrypted(String host, String user, String encryptedPassword, String command) {
        String decryptedPassword = EncryptionUtils.decrypt(encryptedPassword);
        return executeCommand(host, user, decryptedPassword, command);
    }

    @Override
    public String executeCommand(String host, String user, String password, String command) {
        StringBuilder output = new StringBuilder();
        try {
            password = EncryptionUtils.decrypt(password); // toujours déchiffrer ici

            JSch jsch = new JSch();
            Session session = jsch.getSession(user, host, 22);
            session.setPassword(password);

            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect();

            ChannelExec channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);
            channel.setErrStream(System.err);
            InputStream in = channel.getInputStream();

            channel.connect();

            byte[] buffer = new byte[1024];
            while (true) {
                while (in.available() > 0) {
                    int i = in.read(buffer, 0, 1024);
                    if (i < 0) break;
                    output.append(new String(buffer, 0, i));
                }
                if (channel.isClosed()) break;
                Thread.sleep(100);
            }

            channel.disconnect();
            session.disconnect();

        } catch (Exception e) {
            log.error("Erreur exécution commande SSH", e);
            return "Erreur : " + e.getMessage();
        }
        return output.toString();
    }


    // Métriques CPU
    public double getCpuUsage(String host, String user, String password) {
        String command = "top -bn1 | grep 'Cpu(s)' || top -bn1 | grep '%Cpu'";
        String output = executeCommand(host, user, password, command);

        if (output == null || output.isEmpty()) {
            log.error("Aucune sortie pour la commande 'top'");
            return -1;
        }

        try {
            String[] parts = output.split(",");
            for (String part : parts) {
                part = part.trim();
                if (part.toLowerCase().contains("id")) {
                    String idleStr = part.replaceAll("[^\\d.]", "");
                    double idle = Double.parseDouble(idleStr);
                    return Math.round((100.0 - idle) * 100.0) / 100.0;
                }
            }
        } catch (Exception e) {
            log.error("Erreur analyse CPU : ", e);
        }
        return -1;
    }

    // Métriques RAM
    public double getRemoteRamUsage(String host, String user, String password) {
        String command = "free -b | grep Mem";
        String result = executeCommand(host, user, password, command);

        try (Scanner scanner = new Scanner(result)) {
            if (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split("\\s+");
                long total = Long.parseLong(parts[1]);
                long used = Long.parseLong(parts[2]);
                return (double) used / total * 100;
            }
        } catch (Exception e) {
            log.error("Erreur analyse RAM", e);
        }
        return 0.0;
    }

    // Statistiques réseau
    @Override
    public Map<String, Long> getNetworkStats(String host, String user, String password) {
        String command = "cat /proc/net/dev | grep -v lo | tail -n +3";
        String result = executeCommand(host, user, password, command);

        long totalRx = 0;
        long totalTx = 0;

        try (Scanner scanner = new Scanner(result)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (!line.isEmpty()) {
                    String[] parts = line.split(":");
                    if (parts.length < 2) continue;
                    String[] data = parts[1].trim().split("\\s+");
                    if (data.length >= 9) {
                        totalRx += Long.parseLong(data[0]);
                        totalTx += Long.parseLong(data[8]);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Erreur analyse stats réseau", e);
        }

        Map<String, Long> stats = new HashMap<>();
        stats.put("bytesRecv", totalRx);
        stats.put("bytesSent", totalTx);
        return stats;
    }

    // Usage stockage disque
    @Override
    public double getRemoteStorageStatus(String host, String user, String password) {
        String command = "df -B1 / | tail -1";
        String result = executeCommand(host, user, password, command);

        try {
            String[] parts = result.trim().split("\\s+");
            if (parts.length < 5) throw new IllegalArgumentException("Sortie inattendue: " + result);
            long total = Long.parseLong(parts[1]);
            long used = Long.parseLong(parts[2]);
            if (total == 0) return 0.0;
            return ((double) used / total) * 100;
        } catch (Exception e) {
            log.error("Erreur analyse stockage disque", e);
            return -1;
        }
    }

    // Création d’alerte selon métriques
    @Override
    public String MetriqueStatusAlerting(String host, String user, String password, Long idUser,
                                         Long seuilleCPU, Long seuilleRam, Long seuilleDisque,
                                         Long seuilleNetworkReceved, Long seuilleNetworkSent,
                                         Long seuilleMailing, String mailSender, String passwordSender) {

        Notification notification = new Notification();
        Utilisateur utilisateur = utilisateurRepository.findByIdUtilisateur(idUser);
        if (utilisateur == null) return "Utilisateur non trouvé.";

        // ALERTE CPU
        double cpu = getCpuUsage(host, user, password);
        if (cpu > seuilleCPU) {
            String cpuMsg = "Surcharge CPU (" + Math.round(cpu) + "%)";
            if (!alertingRepository.existsByTypeDAlerte(cpuMsg)) {
                Alerting cpuAlert = new Alerting();
                cpuAlert.setDateEtHeure(new Date());
                cpuAlert.setServeurBase(host);
                cpuAlert.setStatut(Status.Terminé);
                cpuAlert.setUtilisateur(utilisateur);
                cpuAlert.setTypeDAlerte(cpuMsg);
                cpuAlert.setGravité(Gravité.CRITIQUE);
                alertingRepository.save(cpuAlert);

                if (!notificationRepository.existsByMessage(cpuMsg)) {
                    notification.setDateNotif(new Date());
                    notification.setMessage(cpuMsg);
                    notification.setUtilisateur(utilisateur);
                    notificationRepository.save(notification);
                }
            }
        }

        // ALERTE RAM
        double ram = getRemoteRamUsage(host, user, password);
        if (ram > seuilleRam) {
            String ramMsg = "Surcharge RAM (" + Math.round(ram) + "%)";
            if (!alertingRepository.existsByTypeDAlerte(ramMsg)) {
                Alerting ramAlert = new Alerting();
                ramAlert.setDateEtHeure(new Date());
                ramAlert.setServeurBase(host);
                ramAlert.setStatut(Status.Terminé);
                ramAlert.setUtilisateur(utilisateur);
                ramAlert.setTypeDAlerte(ramMsg);
                ramAlert.setGravité(Gravité.CRITIQUE);
                alertingRepository.save(ramAlert);

                if (!notificationRepository.existsByMessage(ramMsg)) {
                    notification.setDateNotif(new Date());
                    notification.setMessage(ramMsg);
                    notification.setUtilisateur(utilisateur);
                    notificationRepository.save(notification);
                }
            }
        }

        // ALERTE DISQUE
        double disk = getRemoteStorageStatus(host, user, password);
        if (disk > seuilleDisque) {
            String diskMsg = "Espace disque critique (" + Math.round(disk) + "% utilisé)";
            if (!alertingRepository.existsByTypeDAlerte(diskMsg)) {
                Alerting diskAlert = new Alerting();
                diskAlert.setDateEtHeure(new Date());
                diskAlert.setServeurBase(host);
                diskAlert.setStatut(Status.Terminé);
                diskAlert.setUtilisateur(utilisateur);
                diskAlert.setTypeDAlerte(diskMsg);
                diskAlert.setGravité(Gravité.CRITIQUE);
                alertingRepository.save(diskAlert);

                if (!notificationRepository.existsByMessage(diskMsg)) {
                    notification.setDateNotif(new Date());
                    notification.setMessage(diskMsg);
                    notification.setUtilisateur(utilisateur);
                    notificationRepository.save(notification);
                }
            }
        }

        // ALERTE RÉSEAU
        Map<String, Long> netStats = getNetworkStats(host, user, password);
        long rx = netStats.getOrDefault("bytesRecv", 0L);
        long tx = netStats.getOrDefault("bytesSent", 0L);

        if (rx > seuilleNetworkReceved || tx > seuilleNetworkSent) {
            String rxFormatted = formatBytes(rx, false);
            String txFormatted = formatBytes(tx, false);
            String typeAlerte = "Saturation réseau (Rx: " + rxFormatted + ", Tx: " + txFormatted + ")";
            String notifMsg = "Saturation réseau détectée : Rx = " + rxFormatted + ", Tx = " + txFormatted;

            if (!alertingRepository.existsByTypeDAlerte(typeAlerte)) {
                Alerting networkAlert = new Alerting();
                networkAlert.setTypeDAlerte(typeAlerte);
                networkAlert.setGravité(Gravité.MOYENNE);
                networkAlert.setStatut(Status.En_cours);
                networkAlert.setDateEtHeure(new Date());
                networkAlert.setServeurBase(host);
                networkAlert.setUtilisateur(utilisateur);
                alertingRepository.save(networkAlert);

                if (!notificationRepository.existsByMessage(notifMsg)) {
                    Notification networkNotification = new Notification();
                    networkNotification.setDateNotif(new Date());
                    networkNotification.setMessage(notifMsg);
                    networkNotification.setUtilisateur(utilisateur);
                    notificationRepository.save(networkNotification);
                }
            }
        }

        // Notification par mail si seuil dépassé
        List<Notification> notifications = notificationRepository.findByUtilisateurIdUtilisateur(idUser);
        int sizeOfAlerts = notifications.size();
        if (sizeOfAlerts >= seuilleMailing) {
            Date now = new Date();
            Date lastSent = utilisateur.getLastAlertMailSent();

            StringBuilder message = new StringBuilder("Bonjour,\n\nVoici les alertes critiques détectées sur le serveur " + host + " :\n\n");
            for (Notification notif : notifications) {
                message.append("- ").append(notif.getMessage()).append("\n");
            }
            message.append("\nMerci de prendre les mesures nécessaires.\n\nCordialement,\nSGALB Monitoring System");

            emailService.sendEmail(
                    utilisateur.getEmail(),
                    "Alerte système : " + notifications.size() + " anomalies détectées",
                    message.toString(),
                    mailSender,
                    passwordSender
            );

            notificationRepository.deleteAll(notifications);
            utilisateur.setLastAlertMailSent(now);
            utilisateurRepository.save(utilisateur);
        }

        return notifications.isEmpty() ? "Aucune alerte déclenchée." : notifications.size() + " alertes générées.";
    }

    // Formatage des octets en Ko, Mo, Go
    private String formatBytes(long bytes, boolean forDisplay) {
        double kb = bytes / 1024.0;
        double mb = kb / 1024.0;
        double gb = mb / 1024.0;

        if (gb >= 1) return forDisplay ? String.format("%.4f Go", gb) : ((int) gb) + " Go";
        else if (mb >= 1) return forDisplay ? String.format("%.4f Mo", mb) : ((int) mb) + " Mo";
        else if (kb >= 1) return forDisplay ? String.format("%.4f Ko", kb) : ((int) kb) + " Ko";
        else return bytes + " octets";
    }

    // Récupérer email de l’utilisateur courant (spring security)
    public String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }

    // Upload SFTP vers VM distante
    public String uploadFileTest(MultipartFile file, String username, String host, String password) {
        Session session = null;
        ChannelSftp sftpChannel = null;

        String remoteDirectory = "/home/" + username + "/filesToArchive";

        try {
            JSch jsch = new JSch();
            session = jsch.getSession(username, host, 22);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            Channel channel = session.openChannel("sftp");
            channel.connect();
            sftpChannel = (ChannelSftp) channel;

            try {
                sftpChannel.cd(remoteDirectory);
            } catch (SftpException e) {
                sftpChannel.mkdir(remoteDirectory);
                sftpChannel.cd(remoteDirectory);
            }

            try (InputStream fileInputStream = file.getInputStream()) {
                sftpChannel.put(fileInputStream, file.getOriginalFilename());
            }

            return "Upload réussi du fichier : " + file.getOriginalFilename();

        } catch (Exception e) {
            log.error("Erreur upload SFTP", e);
            return "Erreur lors de l’upload via SFTP : " + e.getMessage();
        } finally {
            if (sftpChannel != null && sftpChannel.isConnected()) {
                sftpChannel.exit();
                sftpChannel.disconnect();
            }
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        }
    }
    public boolean testerConnexionServeur(Serveur serveur) {
        Session session = null;
        try {
            String passwordDechiffré = EncryptionUtils.decrypt(serveur.getPassword());
            JSch jsch = new JSch();
            session = jsch.getSession(serveur.getUser(), serveur.getHost(), 22);
            session.setPassword(passwordDechiffré);

            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect(5000); // timeout de 5 secondes

            return session.isConnected();

        } catch (Exception e) {
            log.error("Impossible de se connecter au serveur SSH : " + serveur.getHost(), e);
            return false;
        } finally {
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        }
    }

    public void testerEtatServeurEtMettreAJour(Serveur serveur) {
        boolean estConnecté = testerConnexionServeur(serveur);
        serveur.setState(estConnecté); // true = fonctionne, false = ne fonctionne pas
        serveurRepository.save(serveur); // Mise à jour en base
    }


    @Scheduled(fixedRate = 30000) // toutes les 5 minutes (300 000 ms)
    public void verifierEtatsServeursTousUtilisateurs() {
        List<Utilisateur> utilisateurs = utilisateurRepository.findAll();

        for (Utilisateur utilisateur : utilisateurs) {
            List<Serveur> serveurs = serveurRepository.findAllByUtilisateurIdUtilisateur(utilisateur.getIdUtilisateur());

            for (Serveur serveur : serveurs) {
                testerEtatServeurEtMettreAJour(serveur);
            }
        }

        log.info("Vérification automatique des états de tous les serveurs terminée.");
    }


    // ici partie de serveur si il est windows
    public double getWindowsCpuUsage(String host, String user, String password) {
        String command = "wmic cpu get loadpercentage";
        String output = executeCommand(host, user, password, command);

        try (Scanner scanner = new Scanner(output)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.matches("\\d+")) {
                    return Double.parseDouble(line);
                }
            }
        } catch (Exception e) {
            log.error("Erreur lecture CPU Windows : " + output, e);
        }
        return -1;
    }


    public double getWindowsRamUsage(String host, String user, String password) {
        String command = "wmic OS get FreePhysicalMemory,TotalVisibleMemorySize /Value";
        String output = executeCommand(host, user, password, command);

        try {
            long total = 0, free = 0;
            for (String line : output.split("\n")) {
                if (line.contains("TotalVisibleMemorySize"))
                    total = Long.parseLong(line.split("=")[1].trim());
                if (line.contains("FreePhysicalMemory"))
                    free = Long.parseLong(line.split("=")[1].trim());
            }
            return total == 0 ? -1 : (1.0 - ((double) free / total)) * 100.0;
        } catch (Exception e) {
            log.error("Erreur lecture RAM Windows : " + output, e);
            return -1;
        }
    }

    public double getWindowsStorageUsage(String host, String user, String password) {
        String command = "wmic logicaldisk where DriveType=3 get Size,FreeSpace";
        String output = executeCommand(host, user, password, command);

        try {
            long total = 0, free = 0;
            for (String line : output.split("\n")) {
                String[] parts = line.trim().split("\\s+");
                if (parts.length == 2 && parts[0].matches("\\d+")) {
                    free += Long.parseLong(parts[0]);
                    total += Long.parseLong(parts[1]);
                }
            }
            return total == 0 ? -1 : ((double) (total - free) / total) * 100.0;
        } catch (Exception e) {
            log.error("Erreur lecture disque Windows : " + output, e);
            return -1;
        }
    }

    public Map<String, Long> getWindowsNetworkStats(String host, String user, String password) {
        String command = "netstat -e";
        String output = executeCommand(host, user, password, command);

        long bytesReceived = 0;
        long bytesSent = 0;

        try (Scanner scanner = new Scanner(output)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.toLowerCase().startsWith("bytes")) {
                    String[] parts = line.split("\\s+");
                    if (parts.length >= 3) {
                        bytesReceived = Long.parseLong(parts[1]);
                        bytesSent = Long.parseLong(parts[2]);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            log.error("Erreur lecture réseau Windows : " + output, e);
        }

        Map<String, Long> map = new HashMap<>();
        map.put("bytesRecv", bytesReceived);
        map.put("bytesSent", bytesSent);
        return map;
    }


    // ici des fonctions pour utiliser dans le controller par OS
    public double getCpuUsageByOS(Serveur serveur) {
        log.info("getCpuUsageByOS - Serveur: " + serveur.getHost() + ", osType: " + serveur.getOsType());
        if ("windows".equalsIgnoreCase(serveur.getOsType())) {
            log.info("Exécution getWindowsCpuUsage");
            return getWindowsCpuUsage(serveur.getHost(), serveur.getUser(), serveur.getPassword());
        } else {
            log.info("Exécution getCpuUsage Unix");
            return getCpuUsage(serveur.getHost(), serveur.getUser(), serveur.getPassword());
        }
    }


    public double getRamUsageByOS(Serveur serveur) {
        return serveur.getOsType().equalsIgnoreCase("windows")
                ? getWindowsRamUsage(serveur.getHost(), serveur.getUser(), serveur.getPassword())
                : getRemoteRamUsage(serveur.getHost(), serveur.getUser(), serveur.getPassword());
    }

    public double getStorageUsageByOS(Serveur serveur) {
        return serveur.getOsType().equalsIgnoreCase("windows")
                ? getWindowsStorageUsage(serveur.getHost(), serveur.getUser(), serveur.getPassword())
                : getRemoteStorageStatus(serveur.getHost(), serveur.getUser(), serveur.getPassword());
    }

    public Map<String, Long> getNetworkStatsByOS(Serveur serveur) {
        return serveur.getOsType().equalsIgnoreCase("windows")
                ? getWindowsNetworkStats(serveur.getHost(), serveur.getUser(), serveur.getPassword())
                : getNetworkStats(serveur.getHost(), serveur.getUser(), serveur.getPassword());
    }



}
