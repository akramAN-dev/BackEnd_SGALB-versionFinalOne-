package com.example.sgalb.Services.Reporting;

import com.example.sgalb.Dtos.RapportGlobalDTO;
import com.example.sgalb.Entities.*;
import com.example.sgalb.Repositories.*;
import com.example.sgalb.Services.SshService.SshServiceInterfaceImpl;
import com.example.sgalb.Utils.EncryptionUtils;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.AllArgsConstructor;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Service
public class ReportingServiceInterfaceImpl implements ReportingServiceInterface {
    ArchiveRepository archiveRepository;
    UtilisateurRepository utilisateurRepository;
    AlertingRepository alertingRepository;
    PlanificationRepository planificationRepository;
    ReportingRepository reportingRepository;
    SshServiceInterfaceImpl sshServiceInterface;
    ServeurRepository serveurRepository;

    @Override
    public byte[] genererRapportGlobal(Long idUtilisateur, Reporting reporting) throws IOException {
        List<Archivage> archivages = archiveRepository.findByUtilisateurIdUtilisateur(idUtilisateur);
        List<Alerting> alertes = alertingRepository.findByUtilisateurIdUtilisateur(idUtilisateur);
        List<Planification> plans = planificationRepository.findByUtilisateurIdUtilisateur(idUtilisateur);

        RapportGlobalDTO rapport = new RapportGlobalDTO();
        rapport.setArchivages(archivages);
        rapport.setAlertes(alertes);
        rapport.setPlanifications(plans);

        switch (reporting.getFormat().toLowerCase()) {
            case "pdf":
                return generatePdfReport(rapport, reporting, idUtilisateur);
            case "csv":
                return generateCsvReport(rapport, reporting, idUtilisateur);
            case "txt":
                return generateTxtReport(rapport, reporting, idUtilisateur);
            case "docx":
                return generateWordReport(rapport, reporting, idUtilisateur);
            default:
                throw new IllegalArgumentException("Format non supporté : " + reporting.getFormat());
        }
    }

    private byte[] generatePdfReport(RapportGlobalDTO rapport, Reporting reporting, Long idUtilisateur) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(out);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document doc = new Document(pdfDoc);

        // Ajouter logo
        try {
            ImageData imageData = ImageDataFactory.create("src/main/resources/static/logo.png");
            Image logo = new Image(imageData)
                    .scaleToFit(100, 100)
                    .setFixedPosition(50, pdfDoc.getDefaultPageSize().getHeight() - 100);
            doc.add(logo);
        } catch (Exception ignored) {}

        // Couleur violette
        DeviceRgb violet = new DeviceRgb(115, 91, 157);

        // Titre
        doc.add(new Paragraph("RAPPORT GLOBAL SGALB")
                .setBold()
                .setFontSize(18)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(violet));

        // Infos de l'en-tête
        doc.add(new Paragraph(buildHeaderInfo(reporting)));

        // Table ARCHIVAGES
        doc.add(new Paragraph("\nArchivages").setBold().setFontSize(14).setFontColor(violet));

        float[] archCols = {4f, 4f, 4f};
        Table tableArch = new Table(archCols).setWidth(UnitValue.createPercentValue(100));
        tableArch.addHeaderCell("Nom").addHeaderCell("Date").addHeaderCell("Statut");

        for (Archivage a : rapport.getArchivages()) {
            tableArch.addCell(a.getNomArchive());
            tableArch.addCell(a.getDate().toString());
            tableArch.addCell(String.valueOf(a.getStatuts()));
        }
        doc.add(tableArch);

        // Table ALERTES
        doc.add(new Paragraph("\nAlertes").setBold().setFontSize(14).setFontColor(violet));

        float[] alertCols = {4f, 4f, 4f};
        Table tableAlert = new Table(alertCols).setWidth(UnitValue.createPercentValue(100));
        tableAlert.addHeaderCell("Type").addHeaderCell("Gravité").addHeaderCell("Serveur/Base");

        for (Alerting a : rapport.getAlertes()) {
            tableAlert.addCell(a.getTypeDAlerte());
            tableAlert.addCell(String.valueOf(a.getGravité()));
            tableAlert.addCell(a.getServeurBase());
        }
        doc.add(tableAlert);

        // Table PLANIFICATIONS
        doc.add(new Paragraph("\nPlanifications").setBold().setFontSize(14).setFontColor(violet));

        float[] planCols = {4f, 4f, 4f};
        Table tablePlan = new Table(planCols).setWidth(UnitValue.createPercentValue(100));
        tablePlan.addHeaderCell("Type Archive").addHeaderCell("Dernière Exécution").addHeaderCell("Serveur");

        for (Planification p : rapport.getPlanifications()) {
            tablePlan.addCell(p.getTypeDArchive());
            tablePlan.addCell(String.valueOf(p.getDernièreExecution()));
            tablePlan.addCell(p.getServeur());
        }
        doc.add(tablePlan);

        // MÉTRIQUES
        doc.add(new Paragraph("\nMétriques système").setBold().setFontSize(14).setFontColor(violet));

        try {
            List<Serveur> serveurs = serveurRepository.findAllByUtilisateurIdUtilisateurAndConnectedTrue(idUtilisateur);
            if (serveurs == null || serveurs.isEmpty()) {
                doc.add(new Paragraph("Aucun serveur connecté trouvé pour l'utilisateur " + idUtilisateur));
            } else {
                Serveur serveur = serveurs.get(0); // On prend le premier serveur connecté
                String password = serveur.getPassword();

                double cpu = sshServiceInterface.getCpuUsage(serveur.getHost(), serveur.getUser(), password);
                double ram = sshServiceInterface.getRemoteRamUsage(serveur.getHost(), serveur.getUser(), password);
                double disk = sshServiceInterface.getRemoteStorageStatus(serveur.getHost(), serveur.getUser(), password);
                Map<String, Long> net = sshServiceInterface.getNetworkStats(serveur.getHost(), serveur.getUser(), password);

                float[] metricCols = {4f, 4f};
                Table metricTable = new Table(metricCols).setWidth(UnitValue.createPercentValue(100));
                metricTable.addHeaderCell("Ressource").addHeaderCell("Utilisation");

                metricTable.addCell("CPU").addCell((int) cpu + "%");
                metricTable.addCell("RAM").addCell((int) ram + "%");
                metricTable.addCell("Disque").addCell((int) disk + "%");
                metricTable.addCell("Réseau Reçus").addCell((net.get("bytesRecv") / (1024 * 1024)) + " MB");
                metricTable.addCell("Réseau Envoyés").addCell((net.get("bytesSent") / (1024 * 1024)) + " MB");

                doc.add(metricTable);
            }
        } catch (Exception e) {
            doc.add(new Paragraph("Erreur récupération métriques : " + e.getMessage()));
        }

        doc.close();
        return out.toByteArray();
    }



    private byte[] generateCsvReport(RapportGlobalDTO rapport, Reporting reporting, Long idUtilisateur) {
        StringBuilder sb = new StringBuilder();

        // Header info
        sb.append("Titre,").append(reporting.getNomDuRapport()).append("\n");
        sb.append("Description,").append(reporting.getPersonnalisationDuContenu()).append("\n");
        sb.append("Date début,").append(reporting.getDateDebut() != null ? reporting.getDateDebut() : "N/A").append("\n");
        sb.append("Date fin,").append(reporting.getDateFin() != null ? reporting.getDateFin() : "N/A").append("\n");
        sb.append("Date génération,").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))).append("\n\n");

        // Archivages
        sb.append("Type,Nom,Date,Statut\n");
        for (Archivage a : rapport.getArchivages()) {
            sb.append("Archivage,")
                    .append(a.getNomArchive()).append(",")
                    .append(a.getDate()).append(",")
                    .append(a.getStatuts())
                    .append("\n");
        }
        sb.append("\n");

        // Alertes
        sb.append("Type,Gravité,Serveur/Base\n");
        for (Alerting a : rapport.getAlertes()) {
            sb.append(a.getTypeDAlerte()).append(",")
                    .append(a.getGravité()).append(",")
                    .append(a.getServeurBase())
                    .append("\n");
        }
        sb.append("\n");

        // Planifications
        sb.append("Type Archive,Dernière Exécution,Serveur\n");
        for (Planification p : rapport.getPlanifications()) {
            sb.append(p.getTypeDArchive()).append(",")
                    .append(p.getDernièreExecution()).append(",")
                    .append(p.getServeur())
                    .append("\n");
        }
        sb.append("\n");

        // Métriques système (récupération sécurisée)
        try {
            List<Serveur> serveurs = serveurRepository.findAllByUtilisateurIdUtilisateurAndConnectedTrue(idUtilisateur);
            if (serveurs == null || serveurs.isEmpty()) {
                sb.append("Métriques système,Aucun serveur connecté trouvé pour l'utilisateur ").append(idUtilisateur).append("\n");
            } else {
                Serveur serveur = serveurs.get(0);
                String password = serveur.getPassword();

                double cpu = sshServiceInterface.getCpuUsage(serveur.getHost(), serveur.getUser(), password);
                double ram = sshServiceInterface.getRemoteRamUsage(serveur.getHost(), serveur.getUser(), password);
                double disk = sshServiceInterface.getRemoteStorageStatus(serveur.getHost(), serveur.getUser(), password);
                Map<String, Long> net = sshServiceInterface.getNetworkStats(serveur.getHost(), serveur.getUser(), password);

                sb.append("Ressource,Utilisation\n");
                sb.append("CPU,").append((int) cpu).append("%\n");
                sb.append("RAM,").append((int) ram).append("%\n");
                sb.append("Disque,").append((int) disk).append("%\n");
                sb.append("Réseau Reçus,").append(net.get("bytesRecv") / (1024 * 1024)).append(" MB\n");
                sb.append("Réseau Envoyés,").append(net.get("bytesSent") / (1024 * 1024)).append(" MB\n");
            }
        } catch (Exception e) {
            sb.append("Erreur récupération métriques : ").append(e.getMessage()).append("\n");
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private byte[] generateTxtReport(RapportGlobalDTO rapport, Reporting reporting, Long idUtilisateur) {
        StringBuilder sb = new StringBuilder();

        // En-tête
        sb.append("===== RAPPORT GLOBAL SGALB =====\n");
        sb.append(buildHeaderInfo(reporting)).append("\n");

        // Archivages
        sb.append("Archivages:\n");
        for (Archivage a : rapport.getArchivages()) {
            sb.append("- ").append(a.getNomArchive())
                    .append(" | ").append(a.getDate())
                    .append(" | ").append(a.getStatuts())
                    .append("\n");
        }
        sb.append("\n");

        // Alertes
        sb.append("Alertes:\n");
        for (Alerting a : rapport.getAlertes()) {
            sb.append("- ").append(a.getTypeDAlerte())
                    .append(" | ").append(a.getGravité())
                    .append(" | ").append(a.getServeurBase())
                    .append("\n");
        }
        sb.append("\n");

        // Planifications
        sb.append("Planifications:\n");
        for (Planification p : rapport.getPlanifications()) {
            sb.append("- ").append(p.getTypeDArchive())
                    .append(" | ").append(p.getDernièreExecution())
                    .append(" | ").append(p.getServeur())
                    .append("\n");
        }
        sb.append("\n");

        // Métriques système
        sb.append("Métriques système:\n");
        try {
            List<Serveur> serveurs = serveurRepository.findAllByUtilisateurIdUtilisateurAndConnectedTrue(idUtilisateur);
            if (serveurs == null || serveurs.isEmpty()) {
                sb.append("Aucun serveur connecté trouvé pour l'utilisateur ").append(idUtilisateur).append("\n");
            } else {
                Serveur serveur = serveurs.get(0);
                String password =serveur.getPassword();

                double cpu = sshServiceInterface.getCpuUsage(serveur.getHost(), serveur.getUser(), password);
                double ram = sshServiceInterface.getRemoteRamUsage(serveur.getHost(), serveur.getUser(), password);
                double disk = sshServiceInterface.getRemoteStorageStatus(serveur.getHost(), serveur.getUser(), password);
                Map<String, Long> net = sshServiceInterface.getNetworkStats(serveur.getHost(), serveur.getUser(), password);

                sb.append("CPU: ").append((int) cpu).append("%\n");
                sb.append("RAM: ").append((int) ram).append("%\n");
                sb.append("Disque: ").append((int) disk).append("%\n");
                sb.append("Réseau Reçus: ").append(net.get("bytesRecv") / (1024 * 1024)).append(" MB\n");
                sb.append("Réseau Envoyés: ").append(net.get("bytesSent") / (1024 * 1024)).append(" MB\n");
            }
        } catch (Exception e) {
            sb.append("Erreur récupération métriques : ").append(e.getMessage()).append("\n");
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private byte[] generateWordReport(RapportGlobalDTO rapport, Reporting reporting, Long idUtilisateur) throws IOException {
        XWPFDocument document = new XWPFDocument();

        // En-tête
        XWPFParagraph header = document.createParagraph();
        XWPFRun runHeader = header.createRun();
        runHeader.setBold(true);
        runHeader.setFontSize(16);
        runHeader.setText("RAPPORT GLOBAL SGALB");
        runHeader.addBreak();

        runHeader.setText(buildHeaderInfo(reporting));
        runHeader.addBreak();

        // Archivages
        XWPFParagraph archivagesTitle = document.createParagraph();
        XWPFRun runArchTitle = archivagesTitle.createRun();
        runArchTitle.setBold(true);
        runArchTitle.setFontSize(14);
        runArchTitle.setColor("735B9D");
        runArchTitle.setText("Archivages");
        runArchTitle.addBreak();

        for (Archivage a : rapport.getArchivages()) {
            XWPFParagraph p = document.createParagraph();
            p.createRun().setText("Nom: " + a.getNomArchive() + " | Date: " + a.getDate() + " | Statut: " + a.getStatuts());
        }

        // Alertes
        XWPFParagraph alertesTitle = document.createParagraph();
        XWPFRun runAlertTitle = alertesTitle.createRun();
        runAlertTitle.setBold(true);
        runAlertTitle.setFontSize(14);
        runAlertTitle.setColor("735B9D");
        runAlertTitle.setText("Alertes");
        runAlertTitle.addBreak();

        for (Alerting a : rapport.getAlertes()) {
            XWPFParagraph p = document.createParagraph();
            p.createRun().setText("Type: " + a.getTypeDAlerte() + " | Gravité: " + a.getGravité() + " | Serveur/Base: " + a.getServeurBase());
        }

        // Planifications
        XWPFParagraph planifTitle = document.createParagraph();
        XWPFRun runPlanifTitle = planifTitle.createRun();
        runPlanifTitle.setBold(true);
        runPlanifTitle.setFontSize(14);
        runPlanifTitle.setColor("735B9D");
        runPlanifTitle.setText("Planifications");
        runPlanifTitle.addBreak();

        for (Planification p : rapport.getPlanifications()) {
            XWPFParagraph pParagraph = document.createParagraph();
            pParagraph.createRun().setText("Type Archive: " + p.getTypeDArchive() + " | Dernière Exécution: " + p.getDernièreExecution() + " | Serveur: " + p.getServeur());
        }

        // Métriques système
        XWPFParagraph metricsTitle = document.createParagraph();
        XWPFRun runMetricsTitle = metricsTitle.createRun();
        runMetricsTitle.setBold(true);
        runMetricsTitle.setFontSize(14);
        runMetricsTitle.setColor("735B9D");
        runMetricsTitle.setText("Métriques système");
        runMetricsTitle.addBreak();

        try {
            List<Serveur> serveurs = serveurRepository.findAllByUtilisateurIdUtilisateurAndConnectedTrue(idUtilisateur);
            if (serveurs == null || serveurs.isEmpty()) {
                XWPFParagraph p = document.createParagraph();
                p.createRun().setText("Aucun serveur connecté trouvé pour l'utilisateur " + idUtilisateur);
            } else {
                Serveur serveur = serveurs.get(0);
                String password = serveur.getPassword();

                double cpu = sshServiceInterface.getCpuUsage(serveur.getHost(), serveur.getUser(), password);
                double ram = sshServiceInterface.getRemoteRamUsage(serveur.getHost(), serveur.getUser(), password);
                double disk = sshServiceInterface.getRemoteStorageStatus(serveur.getHost(), serveur.getUser(), password);
                Map<String, Long> net = sshServiceInterface.getNetworkStats(serveur.getHost(), serveur.getUser(), password);

                XWPFParagraph pCpu = document.createParagraph();
                pCpu.createRun().setText("CPU: " + (int) cpu + "%");

                XWPFParagraph pRam = document.createParagraph();
                pRam.createRun().setText("RAM: " + (int) ram + "%");

                XWPFParagraph pDisk = document.createParagraph();
                pDisk.createRun().setText("Disque: " + (int) disk + "%");

                XWPFParagraph pNetRecv = document.createParagraph();
                pNetRecv.createRun().setText("Réseau Reçus: " + (net.get("bytesRecv") / (1024 * 1024)) + " MB");

                XWPFParagraph pNetSent = document.createParagraph();
                pNetSent.createRun().setText("Réseau Envoyés: " + (net.get("bytesSent") / (1024 * 1024)) + " MB");
            }
        } catch (Exception e) {
            XWPFParagraph p = document.createParagraph();
            p.createRun().setText("Erreur récupération métriques : " + e.getMessage());
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        document.write(out);
        return out.toByteArray();
    }



    private String buildHeaderInfo(Reporting reporting) {
        String dateDebutStr = (reporting.getDateDebut() != null) ? reporting.getDateDebut().toString() : "N/A";
        String dateFinStr = (reporting.getDateFin() != null) ? reporting.getDateFin().toString() : "N/A";

        return "\n \n \n Titre: " + reporting.getNomDuRapport() + "\n"
                + "Description: " + reporting.getPersonnalisationDuContenu() + "\n"
                + "Date de début: " + dateDebutStr + "\n"
                + "Date de fin: " + dateFinStr + "\n"
                + "Date de génération: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) + "\n\n";
    }

    // Les autres méthodes sont inchangées, mais on les stylise dans des tableaux dans les formats PDF et Word

    @Override
    public List<Reporting> selectRapportsForUser(Long idUtilisateur) {
        return reportingRepository.findByUtilisateurIdUtilisateur(idUtilisateur);
    }

    @Override
    public Long getNumberOfRepports(Long idUser) {
        List<Reporting> rapportsOfUser = selectRapportsForUser(idUser);
        return (rapportsOfUser != null) ? (long) rapportsOfUser.size() : 0L;
    }
}
