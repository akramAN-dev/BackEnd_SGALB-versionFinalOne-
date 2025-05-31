package com.example.sgalb.Services.Planification;

import com.example.sgalb.Entities.Archivage;
import com.example.sgalb.Entities.Planification;
import com.example.sgalb.Entities.Serveur;
import com.example.sgalb.Entities.Utilisateur;
import com.example.sgalb.Enum.Status;
import com.example.sgalb.Repositories.ArchiveRepository;
import com.example.sgalb.Repositories.PlanificationRepository;
import com.example.sgalb.Repositories.ServeurRepository;
import com.example.sgalb.Repositories.UtilisateurRepository;
import com.example.sgalb.Services.AzureBlob.AzureBlobServiceInterface;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.apache.commons.compress.harmony.pack200.Archive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class PlanificationServiceInterfaceImpl implements PlanificationServiceInterface {
    private PlanificationRepository planificationRepository;
    ArchiveRepository archivageRepository;
    private UtilisateurRepository utilisateurRepository;
    AzureBlobServiceInterface azureBlobService;
    ServeurRepository serveurRepository;

    @Override
    public Planification addPlanification(Planification planification, Long idUtilisateur) {
        if (planification == null || planification.getFréquence() == null || planification.getServeur() == null) {
            throw new IllegalArgumentException("Champs obligatoires manquants.");
        }

        Utilisateur utilisateur = utilisateurRepository.findByIdUtilisateur(idUtilisateur);
        //Archivage archivage = archivageRepository.findByIdArchivage(idArchive);
        planification.setUtilisateur(utilisateur);

        return planificationRepository.save(planification);
    }

    @Override
    public String deletePlanification(Long idPlanification) {
        Planification planif = planificationRepository.findById(idPlanification)
                .orElseThrow(() -> new IllegalArgumentException("Aucune planification trouvée avec l'ID: " + idPlanification));
        planificationRepository.delete(planif);
        return "Planification supprimée avec succès.";
    }

    @Override
    public String updatePlanification(Planification updated, Long idPlanification) {
        Planification planif = planificationRepository.findByIdPlanification(idPlanification);

        planif.setFréquence(updated.getFréquence());
        planif.setTypeDArchive(updated.getTypeDArchive());
        planif.setStatut(updated.getStatut());
        planif.setDernièreExecution(updated.getDernièreExecution());
        planif.setProchaineExecution(updated.getProchaineExecution());
        planif.setServeur(updated.getServeur());

        planificationRepository.save(planif);
        return "Planification mise à jour.";
    }

    @Override
    public Planification selectOnePlanification(Long idPlanification) {
        return planificationRepository.findById(idPlanification).orElse(null);
    }

    @Override
    public List<Planification> selectAllPlanifications() {
        return planificationRepository.findAll();
    }
    @Override
    public List<Planification> selectPanificationForUser(Long idUtilisateur) {
        return planificationRepository.findByUtilisateurIdUtilisateur(idUtilisateur);
    }

    public InputStream downloadFile(String remoteFilePath,Long idUtilisateur) throws Exception {
        Serveur serveur = serveurRepository.findByUtilisateurIdUtilisateur(idUtilisateur);
        JSch jsch = new JSch();
        Session session = jsch.getSession(serveur.getUser(), serveur.getHost(), 22);
        session.setPassword(serveur.getPassword());
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect();

        ChannelSftp sftpChannel = (ChannelSftp) session.openChannel("sftp");
        sftpChannel.connect();

        return sftpChannel.get(remoteFilePath); // Renvoyer l'InputStream
    }

//    @Transactional
//    @Scheduled(fixedRate = 2000)
//    public String planificationOfArchives() {
//        List<Planification> planifications = planificationRepository.findAll();
//        Date now = new Date();
//        int executedCount = 0;
//        StringBuilder resultMessage = new StringBuilder();
//
//        for (Planification planification : planifications) {
//            Date prochaineExecution = planification.getProchaineExecution();
//
//            if (prochaineExecution != null && !prochaineExecution.after(now)) {
//
//                String chemin = planification.getCheminFichier();
//                String nomFichier = planification.getNomFichier();
//                File fichier = new File(chemin, nomFichier);
//                //File fichier = new File(nomFichier);
//
//                try {
//                    if (!fichier.exists()) {
//                        planification.setStatut(Status.Échoué);
//                        resultMessage.append("❌ Fichier introuvable : ")
//                                .append(fichier.getAbsolutePath()).append("\n");
//                    } else {
//                        MultipartFile multipartFile = azureBlobService.convertToMultipartFile(fichier);
//
//                    // 👤 Récupérer l’utilisateur lié à cette planification
//                        Utilisateur utilisateur = planification.getUtilisateur();
//                        if (utilisateur == null) {
//                            planification.setStatut(Status.Échoué);
//                            resultMessage.append("❌ Aucun utilisateur associé à la planification ID ")
//                                    .append(planification.getIdPlanification()).append("\n");
//                            continue;
//                        }
//
//                        //String url = azureBlobService.upload(multipartFile, utilisateur.getIdUtilisateur());
//                        String url = azureBlobService.upload(multipartFile.getName(), utilisateur.getIdUtilisateur());
//                        //  Suppression après upload
//                        if (fichier.delete()) {
//                            System.out.println("Fichier supprimé : " + fichier.getAbsolutePath());
//                        } else {
//                            System.out.println("Échec suppression fichier : " + fichier.getAbsolutePath());
//                        }
//                        planification.setStatut(Status.Terminé);
//                        planification.setDernièreExecution(prochaineExecution);
//                        resultMessage.append("Fichier archivé automatiquement : ").append(nomFichier).append("\n");
//                    }
//                } catch (Exception e) {
//                    planification.setStatut(Status.Échoué);
//                    resultMessage.append("Erreur lors de l'archivage de ")
//                            .append(nomFichier).append(" : ").append(e.getMessage()).append("\n");
//                }
//
//                // 🔁 Calcul de la prochaine exécution
//                Calendar cal = Calendar.getInstance();
//                cal.setTime(prochaineExecution);
//
//                String freq = planification.getFréquence().trim().toLowerCase();
//
//                switch (freq) {
//                    case "jour":
//                    case "1 jour":
//                        cal.add(Calendar.DAY_OF_MONTH, 1);
//                        break;
//                    case "semaine":
//                    case "1 semaine":
//                        cal.add(Calendar.WEEK_OF_YEAR, 1);
//                        break;
//                    case "2 semaines":
//                    case "2 semaine":
//                        cal.add(Calendar.WEEK_OF_YEAR, 2);
//                        break;
//                    case "mois":
//                    case "1 mois":
//                        cal.add(Calendar.MONTH, 1);
//                        break;
//                    default:
//                        resultMessage.append("⚠️ Fréquence inconnue pour planif ID ")
//                                .append(planification.getIdPlanification()).append(": ").append(freq).append("\n");
//                        continue; // skip save
//                }
//
//                planification.setProchaineExecution(cal.getTime());
//
//                // 💾 Force la mise à jour immédiate
//                planificationRepository.saveAndFlush(planification);
//
//                // 🔍 Log de contrôle
//                System.out.println("📌 Mise à jour : ID " + planification.getIdPlanification() +
//                        " | Statut=" + planification.getStatut() +
//                        " | DernièreExec=" + planification.getDernièreExecution() +
//                        " | ProchaineExec=" + planification.getProchaineExecution());
//
//                executedCount++;
//            }
//        }
//
//        return executedCount == 0
//                ? "⏳ Aucune planification à exécuter pour le moment."
//                : resultMessage.toString();
//    }

//    @Transactional
//    @Scheduled(fixedRate = 2000)
//    public String planificationOfArchives() {
//        List<Planification> planifications = planificationRepository.findAll();
//        Date now = new Date();
//        int executedCount = 0;
//        StringBuilder resultMessage = new StringBuilder();
//
//        for (Planification planification : planifications) {
//            Date prochaineExecution = planification.getProchaineExecution();
//
//            if (prochaineExecution != null && !prochaineExecution.after(now)) {
//                String nomFichier = planification.getNomFichier();
//
//                try {
//                    // Récupérer l’utilisateur lié
//                    Utilisateur utilisateur = planification.getUtilisateur();
//                    if (utilisateur == null) {
//                        planification.setStatut(Status.Échoué);
//                        resultMessage.append(" Aucun utilisateur pour planification ID ")
//                                .append(planification.getIdPlanification()).append("\n");
//                        continue;
//                    }
//
//                    // Appeler ta méthode upload qui fait SFTP + upload Azure + sauvegarde métadonnées
//                    String url = azureBlobService.upload(nomFichier, utilisateur.getIdUtilisateur());
//
//                    // Mise à jour de la planification
//                    planification.setStatut(Status.Terminé);
//                    planification.setDernièreExecution(prochaineExecution);
//
//                    resultMessage.append("✅ Fichier archivé : ").append(nomFichier).append("\n");
//
//                } catch (Exception e) {
//                    planification.setStatut(Status.Échoué);
//                    resultMessage.append("❌ Erreur : ").append(nomFichier).append(" -> ")
//                            .append(e.getMessage()).append("\n");
//                }
//
//                // Calcul de la prochaine exécution
//                Calendar cal = Calendar.getInstance();
//                cal.setTime(prochaineExecution);
//                String freq = planification.getFréquence().trim().toLowerCase();
//
//                switch (freq) {
//                    case "jour":
//                    case "1 jour":
//                        cal.add(Calendar.DAY_OF_MONTH, 1);
//                        break;
//                    case "semaine":
//                    case "1 semaine":
//                        cal.add(Calendar.WEEK_OF_YEAR, 1);
//                        break;
//                    case "2 semaines":
//                    case "2 semaine":
//                        cal.add(Calendar.WEEK_OF_YEAR, 2);
//                        break;
//                    case "mois":
//                    case "1 mois":
//                        cal.add(Calendar.MONTH, 1);
//                        break;
//                    default:
//                        resultMessage.append("⚠️ Fréquence inconnue : ").append(freq).append("\n");
//                        continue;
//                }
//
//                planification.setProchaineExecution(cal.getTime());
//                planificationRepository.saveAndFlush(planification);
//                executedCount++;
//            }
//        }
//
//        return executedCount == 0 ? "⏳ Aucune planification à exécuter." : resultMessage.toString();
//    }

    @Transactional
    @Scheduled(fixedRate = 2000)
    public String planificationOfArchives() {
        List<Planification> planifications = planificationRepository.findAll();
        Date now = new Date();
        int executedCount = 0;
        StringBuilder resultMessage = new StringBuilder();

        for (Planification planification : planifications) {
            Date prochaineExecution = planification.getProchaineExecution();

            if (prochaineExecution != null && !prochaineExecution.after(now)) {
                String nomFichier = planification.getNomFichier();

                try {
                    Utilisateur utilisateur = planification.getUtilisateur();
                    if (utilisateur == null) {
                        planification.setStatut(Status.Échoué);
                        resultMessage.append("❌ Aucun utilisateur pour la planification ID ")
                                .append(planification.getIdPlanification()).append("\n");
                        continue;
                    }

                    Object result = azureBlobService.processElement(utilisateur.getIdUtilisateur(), nomFichier);

                    if (result instanceof Map<?, ?> resultMap) {
                        if ("uploaded".equals(resultMap.get("status"))) {
                            planification.setStatut(Status.Terminé);
                            resultMessage.append("✅ Fichier archivé : ").append(nomFichier).append("\n");
                        } else {
                            planification.setStatut(Status.Échoué);
                            resultMessage.append("❌ Archivage échoué : ").append(nomFichier).append("\n");
                        }
                    } else if (result instanceof List<?> resultList) {
                        planification.setStatut(Status.Échoué);
                        resultMessage.append("📁 '").append(nomFichier).append("' est un dossier. Contenu :\n");
                        for (Object obj : resultList) {
                            if (obj instanceof Map<?, ?> entry) {
                                resultMessage.append(" - ")
                                        .append(entry.get("name")).append(" (")
                                        .append(entry.get("type")).append(")\n");
                            }
                        }
                    } else {
                        planification.setStatut(Status.Échoué);
                        resultMessage.append("❌ Résultat inattendu pour : ").append(nomFichier).append("\n");
                    }

                    planification.setDernièreExecution(prochaineExecution);

                } catch (Exception e) {
                    planification.setStatut(Status.Échoué);
                    resultMessage.append("❌ Erreur : ").append(nomFichier).append(" -> ")
                            .append(e.getMessage()).append("\n");
                }

                // Recalcul de la prochaine exécution
                Calendar cal = Calendar.getInstance();
                cal.setTime(prochaineExecution);
                String freq = planification.getFréquence().trim().toLowerCase();

                switch (freq) {
                    case "jour", "1 jour" -> cal.add(Calendar.DAY_OF_MONTH, 1);
                    case "semaine", "1 semaine" -> cal.add(Calendar.WEEK_OF_YEAR, 1);
                    case "2 semaines", "2 semaine" -> cal.add(Calendar.WEEK_OF_YEAR, 2);
                    case "mois", "1 mois" -> cal.add(Calendar.MONTH, 1);
                    default -> {
                        resultMessage.append("⚠️ Fréquence inconnue : ").append(freq).append("\n");
                        continue;
                    }
                }

                planification.setProchaineExecution(cal.getTime());
                planificationRepository.saveAndFlush(planification);
                executedCount++;
            }
        }

        return executedCount == 0
                ? "⏳ Aucune planification à exécuter."
                : resultMessage.toString();
    }












}
