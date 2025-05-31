package com.example.sgalb.Services.Reporting;
import com.example.sgalb.Dtos.RapportGlobalDTO;
import com.example.sgalb.Entities.*;
import com.example.sgalb.Repositories.*;
import com.example.sgalb.Services.SshService.SshServiceInterfaceImpl;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import lombok.AllArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.stereotype.Service;

import com.itextpdf.layout.Document;

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
    UtilisateurRepository  utilisateurRepository;
    AlertingRepository alertingRepository;
    PlanificationRepository planificationRepository;
    ReportingRepository reportingRepository;
    SshServiceInterfaceImpl sshServiceInterface;
    @Override
    public byte[] genererRapportGlobal(Long idUtilisateur, Reporting reporting) throws IOException {
        // Vérification utilisateur
        List<Archivage> archivages = archiveRepository.findByUtilisateurIdUtilisateur(idUtilisateur);
        List<Alerting> alertes = alertingRepository.findByUtilisateurIdUtilisateur(idUtilisateur);
        List<Planification> plans = planificationRepository.findByUtilisateurIdUtilisateur(idUtilisateur);

        RapportGlobalDTO rapport = new RapportGlobalDTO();
        rapport.setArchivages(archivages);
        rapport.setAlertes(alertes);
        rapport.setPlanifications(plans);
        // rapport.setServeurs(...) si besoin

        // Choix du format de génération
        switch (reporting.getFormat().toLowerCase()) {
            case "pdf":
                return generatePdfReport(rapport,reporting);
//            case "xlsx":
//                return generateExcelReport(rapport,reporting);
            case "csv":
                return generateCsvReport(rapport,reporting);
            case "txt":
                return generateTxtReport(rapport,reporting);
            case "docx":
                return generateWordReport(rapport,reporting);
            default:
                throw new IllegalArgumentException("Format non supporté");
        }
    }



    // les fonctions de regenerations de reapport
    private byte[] generateTxtReport(RapportGlobalDTO rapport, Reporting reporting) {
        StringBuilder sb = new StringBuilder();
        sb.append("===== ").append(reporting.getNomDuRapport()).append(" =====\n\n");
        sb.append("Description: ").append(reporting.getPersonnalisationDuContenu()).append("\n");
        sb.append("Généré le : ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))).append("\n\n");
        sb.append("Archivages:\n");
        for (Archivage a : rapport.getArchivages()) {
            sb.append("- ").append(a.getNomArchive()).append(" | ").append(a.getDate()).append(" | ").append(a.getStatuts()).append("\n");
        }

        sb.append("\nAlertes:\n");
        for (Alerting a : rapport.getAlertes()) {
            sb.append("- ").append(a.getTypeDAlerte()).append(" | ").append(a.getGravité()).append(" | ").append(a.getServeurBase()).append("\n");
        }

        sb.append("\nPlanifications:\n");
        for (Planification p : rapport.getPlanifications()) {
            sb.append("- ").append(p.getTypeDArchive()).append(" | ").append(p.getDernièreExecution()).append(" | ").append(p.getServeur()).append("\n");
        }

        // Ajouter les éléments des métriques formatées
        String host = "20.199.25.198";
        String user = "akram";
        String password = "Akram001002*$*";
        double cpuUsage = sshServiceInterface.getCpuUsage(host, user, password);
        double ramUsage = sshServiceInterface.getRemoteRamUsage(host, user, password);
        Map<String, Long> networkStats = sshServiceInterface.getNetworkStats(host, user, password);
        double storageStatus = sshServiceInterface.getRemoteStorageStatus(host, user, password);

        sb.append("\nMétriques:\n");
        sb.append("CPU Usage: ").append((int) cpuUsage).append("%\n");  // Arrondi à l'entier inférieur
        sb.append("RAM Usage: ").append((int) ramUsage).append("%\n");  // Arrondi à l'entier inférieur
        sb.append("Network status - reçus: \n").append(networkStats.get("bytesRecv") / (1024 * 1024)).append("MB, ")
                .append("\t\nenvoyé: ").append(networkStats.get("bytesSent") / (1024 * 1024)).append("MB\n"); // Converti en MB
        sb.append("Storage Usage: ").append((int) storageStatus).append("%\n");  // Arrondi à l'entier inférieur

        return sb.toString().getBytes();
    }


    private byte[] generateCsvReport(RapportGlobalDTO rapport, Reporting reporting) {
        StringBuilder sb = new StringBuilder();
        sb.append("# ").append(reporting.getNomDuRapport()).append("\n");
        sb.append("# Description: ").append(reporting.getPersonnalisationDuContenu()).append("\n");
        sb.append("# Generer le : ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))).append("\n\n");

        sb.append("Type,Nom,Date,Statut/Details\n");

        for (Archivage a : rapport.getArchivages()) {
            sb.append("Archivage,").append(a.getNomArchive()).append(",").append(a.getDate()).append(",").append(a.getStatuts()).append("\n");
        }

        for (Alerting a : rapport.getAlertes()) {
            sb.append("Alerte,").append(a.getTypeDAlerte()).append(",").append(a.getDateEtHeure()).append(",").append(a.getGravité()).append("\n");
        }

        for (Planification p : rapport.getPlanifications()) {
            sb.append("Planification,").append(p.getTypeDArchive()).append(",").append(p.getDernièreExecution()).append(",").append(p.getStatut()).append("\n");
        }

        // Ajouter les éléments des métriques formatées
        String host = "20.199.25.198";
        String user = "akram";
        String password = "Akram001002*$*";
        double cpuUsage = sshServiceInterface.getCpuUsage(host, user, password);
        double ramUsage = sshServiceInterface.getRemoteRamUsage(host, user, password);
        Map<String, Long> networkStats = sshServiceInterface.getNetworkStats(host, user, password);
        double storageStatus = sshServiceInterface.getRemoteStorageStatus(host, user, password);

        sb.append("\nMetriques:\n");
        sb.append("CPU Usage: ").append((int) cpuUsage).append("%\n");  // Arrondi à l'entier inférieur
        sb.append("RAM Usage: ").append((int) ramUsage).append("%\n");  // Arrondi à l'entier inférieur
        sb.append("Network Stats - Bytes Received: ").append(networkStats.get("bytesRecv") / (1024 * 1024)).append("MB, ")
                .append("Bytes Sent: ").append(networkStats.get("bytesSent") / (1024 * 1024)).append("MB\n"); // Converti en MB
        sb.append("Storage Usage: ").append((int) storageStatus).append("%\n");  // Arrondi à l'entier inférieur

        return sb.toString().getBytes();
    }


    private byte[] generatePdfReport(RapportGlobalDTO rapport, Reporting reporting) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(out);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document doc = new Document(pdfDoc);

        // ===== Image d'en-tête =====
        String imagePath = "src/main/resources/static/logo.png"; // Chemin de l’image
        ImageData imageData = ImageDataFactory.create(imagePath);
        Image logo = new Image(imageData).scaleToFit(100, 100).setFixedPosition(50, pdfDoc.getDefaultPageSize().getHeight() - 100);
        doc.add(logo);

        // ===== Couleur personnalisée pour le titre =====
        DeviceRgb customColor = new DeviceRgb(115, 91, 157); // #735B9D

        Paragraph titre = new Paragraph(reporting.getNomDuRapport())
                .setBold()
                .setFontSize(20)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(customColor)
                .setMarginTop(35)
                .setMarginBottom(30);
        doc.add(titre);

        // ===== ARCHIVAGES =====
        doc.add(new Paragraph("Archivages")
                .setBold().setFontSize(14).setFontColor(ColorConstants.DARK_GRAY).setUnderline());

        for (Archivage a : rapport.getArchivages()) {
            doc.add(new Paragraph("• " + a.getNomArchive() + " | " + a.getDate() + " | " + a.getStatuts())
                    .setFontSize(11).setMarginLeft(20));
        }

        doc.add(new Paragraph("\n"));

        // ===== ALERTES =====
        doc.add(new Paragraph("Alertes")
                .setBold().setFontSize(14).setFontColor(ColorConstants.DARK_GRAY).setUnderline());

        for (Alerting a : rapport.getAlertes()) {
            doc.add(new Paragraph("• " + a.getTypeDAlerte() + " | " + a.getGravité() + " | " + a.getServeurBase())
                    .setFontSize(11).setMarginLeft(20));
        }

        doc.add(new Paragraph("\n"));

        // ===== PLANIFICATIONS =====
        doc.add(new Paragraph("Planifications")
                .setBold().setFontSize(14).setFontColor(ColorConstants.DARK_GRAY).setUnderline());

        for (Planification p : rapport.getPlanifications()) {
            doc.add(new Paragraph("• " + p.getTypeDArchive() + " | " + p.getDernièreExecution() + " | " + p.getServeur())
                    .setFontSize(11).setMarginLeft(20));
        }

        // Ajouter les métriques formatées
        String host = "20.199.25.198";
        String user = "akram";
        String password = "Akram001002*$*";
        double cpuUsage = sshServiceInterface.getCpuUsage(host, user, password);
        double ramUsage = sshServiceInterface.getRemoteRamUsage(host, user, password);
        Map<String, Long> networkStats = sshServiceInterface.getNetworkStats(host, user, password);
        double storageStatus = sshServiceInterface.getRemoteStorageStatus(host, user, password);

        doc.add(new Paragraph("\nMétriques:")
                .setBold().setFontSize(14).setFontColor(ColorConstants.DARK_GRAY).setUnderline());
        doc.add(new Paragraph("CPU Usage: " + (int) cpuUsage + "%")
                .setFontSize(11).setMarginLeft(20));
        doc.add(new Paragraph("RAM Usage: " + (int) ramUsage + "%")
                .setFontSize(11).setMarginLeft(20));
        doc.add(new Paragraph("Network Stats - Bytes Received: " + networkStats.get("bytesRecv") / (1024 * 1024) + "MB, Bytes Sent: " + networkStats.get("bytesSent") / (1024 * 1024) + "MB")
                .setFontSize(11).setMarginLeft(20));
        doc.add(new Paragraph("Storage Usage: " + (int) storageStatus + "%")
                .setFontSize(11).setMarginLeft(20));

        doc.close();
        return out.toByteArray();
    }



    private byte[] generateWordReport(RapportGlobalDTO rapport, Reporting reporting) throws IOException {
        XWPFDocument document = new XWPFDocument();

        XWPFParagraph meta = document.createParagraph();
        XWPFRun runMeta = meta.createRun();
        runMeta.setText("Titre : " + reporting.getNomDuRapport());
        runMeta.addBreak();
        runMeta.setText("Description : " + reporting.getPersonnalisationDuContenu());
        runMeta.addBreak();
        runMeta.setText("Généré le : " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        runMeta.addBreak();
        runMeta.setFontSize(11);

        XWPFParagraph archivages = document.createParagraph();
        archivages.createRun().setText("Archivages:");
        for (Archivage a : rapport.getArchivages()) {
            archivages.createRun().addBreak();
            archivages.createRun().setText("- " + a.getNomArchive() + " | " + a.getDate());
        }

        XWPFParagraph alertes = document.createParagraph();
        alertes.createRun().setText("Alertes:");
        for (Alerting a : rapport.getAlertes()) {
            alertes.createRun().addBreak();
            alertes.createRun().setText("- " + a.getTypeDAlerte() + " | " + a.getGravité());
        }

        XWPFParagraph plans = document.createParagraph();
        plans.createRun().setText("Planifications:");
        for (Planification p : rapport.getPlanifications()) {
            plans.createRun().addBreak();
            plans.createRun().setText("- " + p.getTypeDArchive() + " | " + p.getDernièreExecution());
        }

        // Ajouter les métriques formatées
        String host = "20.199.25.198";
        String user = "akram";
        String password = "Akram001002*$*";
        double cpuUsage = sshServiceInterface.getCpuUsage(host, user, password);
        double ramUsage = sshServiceInterface.getRemoteRamUsage(host, user, password);
        Map<String, Long> networkStats = sshServiceInterface.getNetworkStats(host, user, password);
        double storageStatus = sshServiceInterface.getRemoteStorageStatus(host, user, password);

        XWPFParagraph metrics = document.createParagraph();
        metrics.createRun().setText("Métriques:");
        metrics.createRun().addBreak();
        metrics.createRun().setText("CPU Usage: " + (int) cpuUsage + "%");
        metrics.createRun().addBreak();
        metrics.createRun().setText("RAM Usage: " + (int) ramUsage + "%");
        metrics.createRun().addBreak();
        metrics.createRun().setText("Network Stats - Bytes Received: " + networkStats.get("bytesRecv") / (1024 * 1024) + "MB, Bytes Sent: " + networkStats.get("bytesSent") / (1024 * 1024) + "MB");
        metrics.createRun().addBreak();
        metrics.createRun().setText("Storage Usage: " + (int) storageStatus + "%");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        document.write(out);
        return out.toByteArray();
    }


//    private byte[] generateExcelReport(RapportGlobalDTO rapport, Reporting reporting) throws IOException {
//        XSSFWorkbook workbook = new XSSFWorkbook();
//
//        // Feuille Résumé
//        Sheet metaSheet = workbook.createSheet("Résumé");
//        Row row0 = metaSheet.createRow(0);
//        row0.createCell(0).setCellValue("Titre");
//        row0.createCell(1).setCellValue(reporting.getNomDuRapport());
//
//        Row row1 = metaSheet.createRow(1);
//        row1.createCell(0).setCellValue("Description");
//        row1.createCell(1).setCellValue(reporting.getPersonnalisationDuContenu());
//
//        Row row2 = metaSheet.createRow(2);
//        row2.createCell(0).setCellValue("Date de génération");
//        row2.createCell(1).setCellValue(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
//
//        // Feuille Archivages
//        Sheet sheet1 = workbook.createSheet("Archivages");
//        Row header1 = sheet1.createRow(0);
//        header1.createCell(0).setCellValue("Nom Archive");
//        header1.createCell(1).setCellValue("Date");
//        header1.createCell(2).setCellValue("Statut");
//        int rowNum1 = 1;
//        for (Archivage a : rapport.getArchivages()) {
//            Row row = sheet1.createRow(rowNum1++);
//            row.createCell(0).setCellValue(a.getNomArchive());
//            row.createCell(1).setCellValue(a.getDate().toString());
//            row.createCell(2).setCellValue(String.valueOf(a.getStatuts()));
//        }
//
//        // Feuille Alertes
//        Sheet sheet2 = workbook.createSheet("Alertes");
//        Row header2 = sheet2.createRow(0);
//        header2.createCell(0).setCellValue("Type Alerte");
//        header2.createCell(1).setCellValue("Gravité");
//        header2.createCell(2).setCellValue("Serveur/Base");
//        int rowNum2 = 1;
//        for (Alerting a : rapport.getAlertes()) {
//            Row row = sheet2.createRow(rowNum2++);
//            row.createCell(0).setCellValue(a.getTypeDAlerte());
//            row.createCell(1).setCellValue(String.valueOf(a.getGravité()));
//            row.createCell(2).setCellValue(a.getServeurBase());
//        }
//
//        // Feuille Planifications
//        Sheet sheet3 = workbook.createSheet("Planifications");
//        Row header3 = sheet3.createRow(0);
//        header3.createCell(0).setCellValue("Type d'Archive");
//        header3.createCell(1).setCellValue("Dernière Exécution");
//        header3.createCell(2).setCellValue("Serveur");
//        int rowNum3 = 1;
//        for (Planification p : rapport.getPlanifications()) {
//            Row row = sheet3.createRow(rowNum3++);
//            row.createCell(0).setCellValue(p.getTypeDArchive());
//            row.createCell(1).setCellValue(p.getDernièreExecution().toString());
//            row.createCell(2).setCellValue(p.getServeur());
//        }
//
//        // Feuille Métriques
//        Sheet sheet4 = workbook.createSheet("Métriques");
//        Row header4 = sheet4.createRow(0);
//        header4.createCell(0).setCellValue("Métrique");
//        header4.createCell(1).setCellValue("Valeur");
//        String host = "20.199.25.198";
//        String user = "akram";
//        String password = "Akram001002*$*";
//        // Récupération des données de métriques
//        double cpuUsage = sshServiceInterface.getCpuUsage(host, user, password);
//        double ramUsage = sshServiceInterface.getRemoteRamUsage(host, user, password);
//        Map<String, Long> networkStats = sshServiceInterface.getNetworkStats(host, user, password);
//        double storageStatus = sshServiceInterface.getRemoteStorageStatus(host, user, password);
//
//        // Remplir la feuille des métriques
//        int rowNum4 = 1;
//        Row row4 = sheet4.createRow(rowNum4++);
//        row4.createCell(0).setCellValue("CPU Usage");
//        row4.createCell(1).setCellValue((int) cpuUsage + "%");
//
//        Row row5 = sheet4.createRow(rowNum4++);
//        row5.createCell(0).setCellValue("RAM Usage");
//        row5.createCell(1).setCellValue((int) ramUsage + "%");
//
//        Row row6 = sheet4.createRow(rowNum4++);
//        row6.createCell(0).setCellValue("Network - Bytes Received");
//        row6.createCell(1).setCellValue(networkStats.get("bytesRecv") / (1024 * 1024) + "MB");
//
//        Row row7 = sheet4.createRow(rowNum4++);
//        row7.createCell(0).setCellValue("Network - Bytes Sent");
//        row7.createCell(1).setCellValue(networkStats.get("bytesSent") / (1024 * 1024) + "MB");
//
//        Row row8 = sheet4.createRow(rowNum4++);
//        row8.createCell(0).setCellValue("Storage Usage");
//        row8.createCell(1).setCellValue((int) storageStatus + "%");
//
//        ByteArrayOutputStream out = new ByteArrayOutputStream();
//        workbook.write(out);
//        workbook.close();
//
//        return out.toByteArray();
//    }




    @Override
    public List<Reporting> selectRapportsForUser(Long idUtilisateur) {
        return reportingRepository.findByUtilisateurIdUtilisateur(idUtilisateur);
    }
    // number of repports
    @Override
    public Long getNumberOfRepports(Long idUser) {
        List<Reporting> rapportsOfUser = selectRapportsForUser(idUser);
        return (rapportsOfUser != null) ? (long) rapportsOfUser.size() : 0L;
    }



}
