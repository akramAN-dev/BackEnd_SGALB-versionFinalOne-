package com.example.sgalb.Services.SshService;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlobProperties;
import com.example.sgalb.Entities.*;
import com.example.sgalb.Enum.Gravité;
import com.example.sgalb.Enum.Status;
import com.example.sgalb.Repositories.*;
import com.example.sgalb.Services.Mailing.EmailServiceInterface;
import com.jcraft.jsch.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    @Override
    public String executeCommand(String host, String user, String password, String command) {
        StringBuilder output = new StringBuilder();

        try {
            JSch jsch = new JSch();
            Session session = jsch.getSession(user, host, 22);
            session.setPassword(password);

            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no"); // éviter la vérification de l’empreinte
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
            e.printStackTrace();
            return "Erreur : " + e.getMessage();
        }

        return output.toString();
    }

    // Récupérer les métriques CPU à distance via SSH
    public double getCpuUsage(String host, String user, String password) {
        String command = "top -bn1 | grep 'Cpu(s)' || top -bn1 | grep '%Cpu'"; // Support pour variantes
        String output = executeCommand(host, user, password, command);

        if (output == null || output.isEmpty()) {
            System.err.println("Aucune sortie de la commande 'top'");
            return -1;
        }

        try {
            // Recherche d'une valeur "id" (idle) dans la sortie
            String[] parts = output.split(",");
            for (String part : parts) {
                part = part.trim(); // Supprimer les espaces
                if (part.toLowerCase().contains("id")) {
                    // Exemple : "88.2 id" -> extraire "88.2"
                    String idleStr = part.replaceAll("[^\\d.]", "");
                    double idle = Double.parseDouble(idleStr);
                    double usage = 100.0 - idle;
                    return Math.round(usage * 100.0) / 100.0; // Arrondi à 2 décimales
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de l’analyse de la sortie CPU : " + e.getMessage());
            e.printStackTrace();
        }

        return -1; // Erreur de parsing
    }



    // Récupérer les informations sur la RAM à distance via SSH
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
            e.printStackTrace();
        }

        return 0.0; // Valeur par défaut si échec
    }


    // Récupérer les statistiques réseau à distance via SSH
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
                    // Exemple ligne : eth0: 1234567 0 0 0 0 0 0 0 7654321 0 0 0 0 0 0 0
                    String[] parts = line.split(":");
                    if (parts.length < 2) continue;

                    String[] data = parts[1].trim().split("\\s+");

                    if (data.length >= 9) {
                        long rx = Long.parseLong(data[0]); // Bytes reçus
                        long tx = Long.parseLong(data[8]); // Bytes envoyés

                        totalRx += rx;
                        totalTx += tx;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        Map<String, Long> stats = new HashMap<>();
        stats.put("bytesRecv", totalRx);
        stats.put("bytesSent", totalTx);

        return stats;
    }


    // Récupérer l'état du stockage à distance via SSH
    @Override
    public double getRemoteStorageStatus(String host, String user, String password) {
        String command = "df -B1 / | tail -1";
        String result = executeCommand(host, user, password, command);

        try {
            String[] parts = result.trim().split("\\s+");
            if (parts.length < 5) {
                throw new IllegalArgumentException("Format de sortie inattendu: " + result);
            }

            long total = Long.parseLong(parts[1]);
            long used = Long.parseLong(parts[2]);

            if (total == 0) return 0.0;

            return ((double) used / total) * 100;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
    // cree une alert a partie de la metrique de CPU > 90% and RAM >90%
    @Override
    public String MetriqueStatusAlerting(String host, String user, String password, Long idUser,
                                         Long seuilleCPU, Long seuilleRam, Long seuilleDisque,
                                         Long seuilleNetworkReceved, Long seuilleNetworkSent,Long seuilleMailing,String mailSender,String passwordSender) {

        Notification notification = new Notification();
        Utilisateur utilisateur = utilisateurRepository.findByIdUtilisateur(idUser);
        if (utilisateur == null) return "Utilisateur non trouvé.";

        // === ALERTE CPU ===
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

        // === ALERTE RAM ===
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

        // === ALERTE DISQUE ===
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

        // === ALERTE RÉSEAU ===
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

        // === NOTIFICATION MAIL ===
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




    // Méthode modifiée avec paramètre de précision
    private String formatBytes(long bytes, boolean forDisplay) {
        double kb = bytes / 1024.0;
        double mb = kb / 1024.0;
        double gb = mb / 1024.0;

        if (gb >= 1) return forDisplay ? String.format("%.4f Go", gb) : ((int) gb) + " Go";
        else if (mb >= 1) return forDisplay ? String.format("%.4f Mo", mb) : ((int) mb) + " Mo";
        else if (kb >= 1) return forDisplay ? String.format("%.4f Ko", kb) : ((int) kb) + " Ko";
        else return bytes + " octets";
    }


    private String formatRoundedTo1DecimalGo(long bytes) {
        double gb = bytes / 1024.0 / 1024.0 / 1024.0;
        return String.format(Locale.US, "%.1f Go", gb);
    }
    public String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName(); // supposé être l’email
    }


    // upload pour tester les l'archivage des elements qui existe dans le VM au azure blob
//    public String uploadFileTest(MultipartFile file, String username, String host, String password) {
//        Session session = null;
//        ChannelSftp sftpChannel = null;
//
//        // ✅ Utiliser un répertoire dans le home
//        String remoteDirectory = "/home/" + username + "/filesToArchive";
//
//        try {
//            // Connexion SSH
//            JSch jsch = new JSch();
//            session = jsch.getSession(username, host, 22);
//            session.setPassword(password);
//            session.setConfig("StrictHostKeyChecking", "no");
//            session.connect();
//
//            // Ouverture du canal SFTP
//            Channel channel = session.openChannel("sftp");
//            channel.connect();
//            sftpChannel = (ChannelSftp) channel;
//
//            // ✅ Créer le dossier s'il n'existe pas
//            try {
//                sftpChannel.cd(remoteDirectory);
//            } catch (SftpException e) {
//                sftpChannel.mkdir(remoteDirectory);
//                sftpChannel.cd(remoteDirectory);
//            }
//
//            // ✅ Transfert du fichier
//            InputStream fileInputStream = file.getInputStream();
//            sftpChannel.put(fileInputStream, file.getOriginalFilename());
//
//            return "✅ Fichier transféré avec succès vers " + remoteDirectory;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return "❌ Échec du transfert : " + e.getMessage();
//        } finally {
//            if (sftpChannel != null) sftpChannel.exit();
//            if (session != null) session.disconnect();
//        }
//    }

    // l'upload du la VM vers azure Blob storage
    public String uploadFileTest(MultipartFile file, String username, String host, String password) {
        Session session = null;
        ChannelSftp sftpChannel = null;

        // Répertoire distant (exemple : /home/username/filesToArchive)
        String remoteDirectory = "/home/" + username + "/filesToArchive";

        try {
            // Connexion SSH
            JSch jsch = new JSch();
            session = jsch.getSession(username, host, 22);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            // Canal SFTP
            Channel channel = session.openChannel("sftp");
            channel.connect();
            sftpChannel = (ChannelSftp) channel;

            // Créer le dossier distant s’il n’existe pas
            try {
                sftpChannel.cd(remoteDirectory);
            } catch (SftpException e) {
                sftpChannel.mkdir(remoteDirectory);
                sftpChannel.cd(remoteDirectory);
            }

            // Transfert du fichier (nom du fichier conservé tel quel)
            try (InputStream fileInputStream = file.getInputStream()) {
                sftpChannel.put(fileInputStream, file.getOriginalFilename());
            }
            // maybe add here the delete elements if uploaded wiht success
            return "Upload réussi du fichier : " + file.getOriginalFilename();

        } catch (Exception e) {
            e.printStackTrace();
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




}
