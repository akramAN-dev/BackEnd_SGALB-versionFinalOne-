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
            Image logo = new Image(imageData).scaleToFit(100, 100).setFixedPosition(50, pdfDoc.getDefaultPageSize().getHeight() - 100);
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
            Serveur serveur = serveurRepository.findByUtilisateurIdUtilisateur(idUtilisateur);
            //Serveur serveur = serveurRepository.findAllByUtilisateurIdUtilisateurAndConnectedTrue(idUtilisateur);
            if (serveur == null) {
                doc.add(new Paragraph("Aucun serveur trouvé pour l'utilisateur " + idUtilisateur));
            } else {
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
        sb.append("Type,Nom,Date,Statut\n");
        for (Archivage a : rapport.getArchivages()) {
            sb.append("Archivage,").append(a.getNomArchive()).append(",").append(a.getDate()).append(",").append(a.getStatuts()).append("\n");
        }
        return sb.toString().getBytes();
    }
    private byte[] generateTxtReport(RapportGlobalDTO rapport, Reporting reporting, Long idUtilisateur) {
        StringBuilder sb = new StringBuilder();
        sb.append("===== RAPPORT GLOBAL SGALB =====\n");
        sb.append(buildHeaderInfo(reporting));
        for (Archivage a : rapport.getArchivages()) {
            sb.append("- ").append(a.getNomArchive()).append(" | ").append(a.getDate()).append(" | ").append(a.getStatuts()).append("\n");
        }
        return sb.toString().getBytes();
    }
    private byte[] generateWordReport(RapportGlobalDTO rapport, Reporting reporting, Long idUtilisateur) throws IOException {
        XWPFDocument document = new XWPFDocument();
        document.createParagraph().createRun().setText(buildHeaderInfo(reporting));

        for (Archivage a : rapport.getArchivages()) {
            document.createParagraph().createRun().setText("Archivage: " + a.getNomArchive() + " | " + a.getDate() + " | " + a.getStatuts());
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
