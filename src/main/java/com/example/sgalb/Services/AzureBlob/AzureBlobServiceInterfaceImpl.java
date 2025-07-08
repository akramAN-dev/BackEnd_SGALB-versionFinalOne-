//package com.example.sgalb.Services.AzureBlob;
//
//import com.azure.storage.blob.BlobClient;
//import com.azure.storage.blob.BlobContainerClient;
//import com.azure.storage.blob.BlobServiceClient;
//import com.azure.storage.blob.BlobServiceClientBuilder;
//import com.azure.storage.blob.models.BlobItem;
//import com.azure.storage.blob.models.BlobProperties;
//import com.example.sgalb.Dtos.FichierArchiveDTO;
//import com.example.sgalb.Entities.Archivage;
//import com.example.sgalb.Entities.AzureStorageConfig;
//import com.example.sgalb.Entities.Serveur;
//import com.example.sgalb.Entities.Utilisateur;
//import com.example.sgalb.Enum.Status;
//import com.example.sgalb.Repositories.ArchiveRepository;
//import com.example.sgalb.Repositories.AzureStorageConfigRepository;
//import com.example.sgalb.Repositories.ServeurRepository;
//import com.example.sgalb.Repositories.UtilisateurRepository;
//import com.example.sgalb.Utils.EncryptionUtils;
//import com.jcraft.jsch.*;
//import lombok.AllArgsConstructor;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//import org.springframework.beans.factory.annotation.Value;
//
//import java.io.*;
//import java.net.InetAddress;
//import java.net.UnknownHostException;
//import java.nio.file.Files;
//import java.nio.file.Paths;
//import java.nio.file.StandardCopyOption;
//import java.nio.file.StandardOpenOption;
//import java.util.*;
//import java.util.stream.Collectors;
//
//@Service
//
//public class AzureBlobServiceInterfaceImpl implements AzureBlobServiceInterface {
////    private BlobServiceClient blobServiceClient;
////    private BlobContainerClient containerClient;
//    @Autowired
//    AzureStorageConfigRepository azureStorageConfigRepository;
//
//    @Autowired
//    ArchiveRepository archiveRepository;
//    @Autowired
//    UtilisateurRepository utilisateurRepository;
//    @Autowired
//    ServeurRepository serveurRepository;
//
//    // ghir methode d'initialisation dynamique par idUtilisateur
//    private BlobContainerClient initClientFromDB(Long idUtilisateur) {
//        AzureStorageConfig config = azureStorageConfigRepository.findByUtilisateurIdUtilisateur(idUtilisateur);
//        if (config == null) throw new RuntimeException("Configuration Azure introuvable pour l'utilisateur");
//
//        BlobServiceClient serviceClient = new BlobServiceClientBuilder()
//                .connectionString(config.getConnectionString())
//                .buildClient();
//
//        BlobContainerClient container = serviceClient.getBlobContainerClient(config.getContainerName());
//        if (!container.exists()) {
//            container.create();
//        }
//
//        return container;
//    }
//
////public String upload(String fileName, Long idUtilisateur) throws Exception {
////    Serveur serveur = serveurRepository.findByUtilisateurIdUtilisateur(idUtilisateur);
////    String remoteFilePath = "/home/" + serveur.getUser() + "/filesToArchive/" + fileName;
////    String sftpPassword = serveur.getPassword();
////
////    JSch jsch = new JSch();
////    Session session = jsch.getSession(serveur.getUser(), serveur.getHost(), 22);
////    session.setPassword(sftpPassword);
////    session.setConfig("StrictHostKeyChecking", "no");
////    session.connect();
////
////    Channel channel = session.openChannel("sftp");
////    channel.connect();
////    ChannelSftp sftpChannel = (ChannelSftp) channel;
////
////    try {
////        InputStream inputStream = sftpChannel.get(remoteFilePath);
////        long fileSize = sftpChannel.lstat(remoteFilePath).getSize();
////
////        BlobContainerClient containerClient = initClientFromDB(idUtilisateur);
////        BlobClient blobClient = containerClient.getBlobClient(fileName);
////        blobClient.upload(inputStream, fileSize, true);
////
////        BlobProperties props = blobClient.getProperties();
////        Date dateArchivage = Date.from(props.getLastModified().toInstant());
////        long dureeSecondes = (new Date().getTime() - dateArchivage.getTime()) / 1000;
////        long tailleOctets = props.getBlobSize();
////
////        Archivage archivage = new Archivage();
////        archivage.setDate(dateArchivage);
////        archivage.setNomArchive(fileName);
////        archivage.setStatuts(Status.Terminé);
////        archivage.setTaille(tailleOctets);
////        archivage.setDuree(dureeSecondes);
////        archivage.setServeurs(serveur.getHost());
////
////        Utilisateur utilisateur = utilisateurRepository.findById(idUtilisateur)
////                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
////        archivage.setUtilisateur(utilisateur);
////
////        archiveRepository.save(archivage);
////
////        sftpChannel.rm(remoteFilePath);
////
////        inputStream.close();
////        return blobClient.getBlobUrl();
////    } catch (Exception e) {
////        throw new RuntimeException("Erreur durant l'upload ou la suppression du fichier : " + e.getMessage(), e);
////    } finally {
////        if (sftpChannel.isConnected()) sftpChannel.exit();
////        if (session.isConnected()) session.disconnect();
////    }
////}
//
//
//    public String upload(String fullRelativePath, Long idUtilisateur) throws Exception {
//        Serveur serveur = serveurRepository.findByUtilisateurIdUtilisateur(idUtilisateur);
//        String remoteFilePath = "/home/" + serveur.getUser() + "/" + fullRelativePath;
//        String sftpPassword = EncryptionUtils.decrypt(serveur.getPassword());
//
//        JSch jsch = new JSch();
//        Session session = jsch.getSession(serveur.getUser(), serveur.getHost(), 22);
//        session.setPassword(sftpPassword);
//        session.setConfig("StrictHostKeyChecking", "no");
//        session.connect();
//
//        Channel channel = session.openChannel("sftp");
//        channel.connect();
//        ChannelSftp sftpChannel = (ChannelSftp) channel;
//
//        try {
//            InputStream inputStream = sftpChannel.get(remoteFilePath);
//            long fileSize = sftpChannel.lstat(remoteFilePath).getSize();
//
//            String fileName = Paths.get(fullRelativePath).getFileName().toString();
//
//            BlobContainerClient containerClient = initClientFromDB(idUtilisateur);
//            BlobClient blobClient = containerClient.getBlobClient(fileName);
//            blobClient.upload(inputStream, fileSize, true);
//
//            BlobProperties props = blobClient.getProperties();
//            Date dateArchivage = Date.from(props.getLastModified().toInstant());
//            long dureeSecondes = (new Date().getTime() - dateArchivage.getTime()) / 1000;
//            long tailleOctets = props.getBlobSize();
//
//            Archivage archivage = new Archivage();
//            archivage.setDate(dateArchivage);
//            archivage.setNomArchive(fileName);
//            archivage.setStatuts(Status.Terminé);
//            archivage.setTaille(tailleOctets);
//            archivage.setDuree(dureeSecondes);
//            archivage.setServeurs(serveur.getHost());
//
//            Utilisateur utilisateur = utilisateurRepository.findById(idUtilisateur)
//                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
//            archivage.setUtilisateur(utilisateur);
//
//            archiveRepository.save(archivage);
//
//            sftpChannel.rm(remoteFilePath);
//
//            inputStream.close();
//            return blobClient.getBlobUrl();
//        } catch (Exception e) {
//            throw new RuntimeException("Erreur durant l'upload ou la suppression du fichier : " + e.getMessage(), e);
//        } finally {
//            if (sftpChannel.isConnected()) sftpChannel.exit();
//            if (session.isConnected()) session.disconnect();
//        }
//    }
//
//@Override
//public List<FichierArchiveDTO> listFiles(Long idUtilisateur) {
//    BlobContainerClient containerClient = initClientFromDB(idUtilisateur);
//    List<FichierArchiveDTO> fichiers = new ArrayList<>();
//
//
//    String serveur = serveurRepository.findByUtilisateurIdUtilisateur(idUtilisateur).getHost(); // ou tu peux le rendre dynamique
//
//    for (BlobItem blobItem : containerClient.listBlobs()) {
//        String nomFichier = blobItem.getName();
//
//        BlobClient blobClient = containerClient.getBlobClient(nomFichier);
//        BlobProperties props = blobClient.getProperties();
//
//        Date dateArchivage = Date.from(props.getLastModified().toInstant());
//        long dureeSecondes = (new Date().getTime() - dateArchivage.getTime()) / 1000;
//        double tailleMo = (double) props.getBlobSize() / (1024 * 1024);
//
//        fichiers.add(new FichierArchiveDTO(
//                nomFichier,
//                dateArchivage,
//                String.format(Locale.US, "%.2f", tailleMo),
//                serveur,
//                dureeSecondes
//        ));
//    }
//
//    return fichiers;
//}
//
//
//    public boolean delete(String filename,Long idUtilisateur) {
//        BlobContainerClient containerClient = initClientFromDB(idUtilisateur);
//        BlobClient blobClient = containerClient.getBlobClient(filename);
//        if (blobClient.exists()) {
//            blobClient.delete();
//            return true;
//        }
//        return false;
//    }
//
//    public byte[] download(String filename, Long idUtilisateur) {
//        BlobContainerClient containerClient = initClientFromDB(idUtilisateur);
//        BlobClient blobClient = containerClient.getBlobClient(filename);
//        if (blobClient.exists()) {
//            ByteArrayOutputStream os = new ByteArrayOutputStream();
//            blobClient.download(os);
//            return os.toByteArray();
//        }
//        return null;
//    }
//
//
//
//    public String archive(String filename, Long idUtilisateur) {
//        BlobContainerClient containerClient = initClientFromDB(idUtilisateur);
//        BlobClient originalBlob = containerClient.getBlobClient(filename);
//        BlobClient archivedBlob = containerClient.getBlobClient("archive/" + filename);
//
//        if (originalBlob.exists()) {
//            archivedBlob.beginCopy(originalBlob.getBlobUrl(), null);
//            originalBlob.delete(); // suppression après copie
//            return archivedBlob.getBlobUrl();
//        }
//        return null;
//    }
//
////    public MultipartFile convertToMultipartFile(File file) throws IOException {
////
////
////        return new MultipartFile() {
////            @Override
////            public String getName() {
////                return file.getName();
////            }
////
////            @Override
////            public String getOriginalFilename() {
////                return file.getName();
////            }
////
////            @Override
////            public String getContentType() {
////                return "application/octet-stream";
////            }
////
////            @Override
////            public boolean isEmpty() {
////                return file.length() == 0;
////            }
////
////            @Override
////            public long getSize() {
////                return file.length();
////            }
////
////            @Override
////            public byte[] getBytes() throws IOException {
////                try (FileInputStream input = new FileInputStream(file)) {
////                    return input.readAllBytes();
////                }
////            }
////
////            @Override
////            public InputStream getInputStream() throws IOException {
////                return new FileInputStream(file);
////            }
////
////            @Override
////            public void transferTo(File dest) throws IOException, IllegalStateException {
////                Files.copy(file.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
////            }
////        };
////    }
//public MultipartFile convertToMultipartFile(InputStream inputStream, String fileName) throws IOException {
//    byte[] content = inputStream.readAllBytes();
//
//    return new MultipartFile() {
//        @Override
//        public String getName() {
//            return fileName;
//        }
//
//        @Override
//        public String getOriginalFilename() {
//            return fileName;
//        }
//
//        @Override
//        public String getContentType() {
//            return "application/octet-stream";
//        }
//
//        @Override
//        public boolean isEmpty() {
//            return content.length == 0;
//        }
//
//        @Override
//        public long getSize() {
//            return content.length;
//        }
//
//        @Override
//        public byte[] getBytes() {
//            return content;
//        }
//
//        @Override
//        public InputStream getInputStream() {
//            return new ByteArrayInputStream(content);
//        }
//
//        @Override
//        public void transferTo(File dest) throws IOException, IllegalStateException {
//            Files.write(dest.toPath(), content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
//        }
//    };
//}
//
//    public File convertBytesToFile(byte[] bytes, String filename) throws IOException {
//        File file = new File(System.getProperty("java.io.tmpdir") + "/" + filename);
//        try (FileOutputStream fos = new FileOutputStream(file)) {
//            fos.write(bytes);
//        }
//        return file;
//    }
//
//
//    @Override
//    public AzureStorageConfig saveAzureConfig(AzureStorageConfig azureStorageConfig,Long idUtilisateur) {
//        azureStorageConfig.setUtilisateur(utilisateurRepository.findByIdUtilisateur(idUtilisateur));
//        return azureStorageConfigRepository.save(azureStorageConfig);
//    }
//
//    @Override
//    public AzureStorageConfig updateAzureConfig(AzureStorageConfig azureStorageConfig, Long idUtilisateur) {
//        AzureStorageConfig azureStorageConfigToUpdate =azureStorageConfigRepository.findByUtilisateurIdUtilisateur(idUtilisateur);
//        azureStorageConfigToUpdate.setContainerName(azureStorageConfig.getContainerName());
//        azureStorageConfigToUpdate.setConnectionString(azureStorageConfig.getConnectionString());
//        return azureStorageConfigRepository.save(azureStorageConfigToUpdate);
//    }
//
//    @Override
//    public AzureStorageConfig getAzureConfig(Long idUtilisateur) {
//        return azureStorageConfigRepository.findByUtilisateurIdUtilisateur(idUtilisateur);
//    }
//
//
//    // liste des fichier a archiver
//    public List<String> listFilesToArchive(Long idUtilisateur) throws Exception {
//        // 🔍 Récupérer le serveur lié à l'utilisateur
//        Serveur serveur = serveurRepository.findByUtilisateurIdUtilisateur(idUtilisateur);
//        if (serveur == null) {
//            throw new RuntimeException("Aucun serveur associé à cet utilisateur.");
//        }
//
//        String sftpUser = serveur.getUser();
//        String sftpHost = serveur.getHost();
//        String sftpPassword = EncryptionUtils.decrypt(serveur.getPassword());
//        String remoteDirectory = "/home/" + sftpUser + "/filesToArchive";
//
//        List<String> fileNames = new ArrayList<>();
//
//        JSch jsch = new JSch();
//        Session session = jsch.getSession(sftpUser, sftpHost, 22);
//        session.setPassword(EncryptionUtils.decrypt(serveur.getPassword()));
//        session.setConfig("StrictHostKeyChecking", "no");
//        session.connect();
//
//        Channel channel = session.openChannel("sftp");
//        channel.connect();
//        ChannelSftp sftpChannel = (ChannelSftp) channel;
//
//        // 📂 Lister les fichiers du répertoire
//        Vector<ChannelSftp.LsEntry> files = sftpChannel.ls(remoteDirectory);
//
//        for (ChannelSftp.LsEntry entry : files) {
//            String fileName = entry.getFilename();
//            // Ignorer "." et ".."
//            if (!fileName.equals(".") && !fileName.equals("..")) {
//                fileNames.add(fileName);
//            }
//        }
//
//        // 🧹 Nettoyage
//        sftpChannel.exit();
//        session.disconnect();
//
//        return fileNames;
//    }
//
//    // public ResponseEntity<List<String>> listFilesToArchive(@RequestParam Long idUtilisateur)
//    public Map<String, String> directoryOrFolder(Long idUtilisateur, String pathRelativeToFilesToArchive) throws Exception {
//        Serveur serveur = serveurRepository.findByUtilisateurIdUtilisateur(idUtilisateur);
//        if (serveur == null) {
//            throw new RuntimeException("Aucun serveur associé à cet utilisateur.");
//        }
//
//        String basePath = "/home/" + serveur.getUser();
//        String fullPath = basePath + "/" + pathRelativeToFilesToArchive;
//
//        JSch jsch = new JSch();
//        Session session = jsch.getSession(serveur.getUser(), serveur.getHost(), 22);
//        session.setPassword(EncryptionUtils.decrypt(serveur.getPassword()));
//        session.setConfig("StrictHostKeyChecking", "no");
//        session.connect();
//
//        Channel channel = session.openChannel("sftp");
//        channel.connect();
//        ChannelSftp sftpChannel = (ChannelSftp) channel;
//
//        SftpATTRS attrs;
//        try {
//            attrs = sftpChannel.lstat(fullPath);
//        } catch (Exception e) {
//            throw new RuntimeException("Fichier ou dossier non trouvé : " + fullPath);
//        }
//
//        Map<String, String> result = new HashMap<>();
//        result.put("path", pathRelativeToFilesToArchive);
//        result.put("type", attrs.isDir() ? "folder" : "file");
//        result.put("size", String.valueOf(attrs.getSize()));
//
//        sftpChannel.exit();
//        session.disconnect();
//
//        return result;
//    }
//
//    //ici la fonction va pemetre de lister les elements si il s'ajit d'un path directory et archiver si le pathe est un fichier
//    public Object processElement(Long idUtilisateur, String pathRelativeToHome) throws Exception {
//        Map<String, String> info = directoryOrFolder(idUtilisateur, pathRelativeToHome);
//        String type = info.get("type");
//
//        if ("file".equals(type)) {
//            // Si c'est un fichier, on archive (upload)
//            String blobUrl = upload(pathRelativeToHome, idUtilisateur);
//            Map<String, String> result = new HashMap<>();
//            result.put("status", "uploaded");
//            result.put("blobUrl", blobUrl);
//            return result;
//        } else {
//            // Si c'est un dossier, on liste son contenu
//            Serveur serveur = serveurRepository.findByUtilisateurIdUtilisateur(idUtilisateur);
//            String basePath = "/home/" + serveur.getUser();
//            String fullPath = basePath + "/" + pathRelativeToHome;
//
//            JSch jsch = new JSch();
//            Session session = jsch.getSession(serveur.getUser(), serveur.getHost(), 22);
//            session.setPassword(EncryptionUtils.decrypt(serveur.getPassword()));
//            session.setConfig("StrictHostKeyChecking", "no");
//            session.connect();
//
//            Channel channel = session.openChannel("sftp");
//            channel.connect();
//            ChannelSftp sftpChannel = (ChannelSftp) channel;
//
//            List<ChannelSftp.LsEntry> entries = sftpChannel.ls(fullPath);
//            List<Map<String, String>> results = new ArrayList<>();
//
//            for (ChannelSftp.LsEntry entry : entries) {
//                String name = entry.getFilename();
//                if (!name.equals(".") && !name.equals("..")) {
//                    String elementType = entry.getAttrs().isDir() ? "folder" : "file";
//                    Map<String, String> item = new HashMap<>();
//                    item.put("name", name);
//                    item.put("type", elementType);
//                    results.add(item);
//                }
//            }
//
//            sftpChannel.exit();
//            session.disconnect();
//
//            return results;
//        }
//    }
//
//    // to update maybe
//    public Object inspectRemotePath(Long idUtilisateur, String pathRelativeToHome) throws Exception {
//        Serveur serveur = serveurRepository.findByUtilisateurIdUtilisateur(idUtilisateur);
//        String basePath = "/home/" + serveur.getUser();
//        String fullPath = basePath + "/" + pathRelativeToHome;
//
//        JSch jsch = new JSch();
//        Session session = jsch.getSession(serveur.getUser(), serveur.getHost(), 22);
//        session.setPassword(EncryptionUtils.decrypt(serveur.getPassword()));
//        session.setConfig("StrictHostKeyChecking", "no");
//        session.connect();
//
//        Channel channel = session.openChannel("sftp");
//        channel.connect();
//        ChannelSftp sftpChannel = (ChannelSftp) channel;
//
//        SftpATTRS attrs;
//        try {
//            attrs = sftpChannel.lstat(fullPath);
//        } catch (SftpException e) {
//            sftpChannel.exit();
//            session.disconnect();
//            throw new FileNotFoundException("Chemin introuvable : " + pathRelativeToHome);
//        }
//
//        if (attrs.isDir()) {
//            Vector<ChannelSftp.LsEntry> entries = sftpChannel.ls(fullPath);
//            List<Map<String, String>> contents = new ArrayList<>();
//
//            for (ChannelSftp.LsEntry entry : entries) {
//                String name = entry.getFilename();
//                if (!name.equals(".") && !name.equals("..")) {
//                    Map<String, String> fileInfo = new HashMap<>();
//                    fileInfo.put("name", name);
//                    fileInfo.put("type", entry.getAttrs().isDir() ? "folder" : "file");
//                    contents.add(fileInfo);
//                }
//            }
//
//            sftpChannel.exit();
//            session.disconnect();
//            return contents;
//        } else {
//            sftpChannel.exit();
//            session.disconnect();
//
//            // Fichier unique
//            Map<String, String> file = new HashMap<>();
//            file.put("name", pathRelativeToHome);
//            file.put("type", "file");
//            return file;
//        }
//    }
//
//
//}
package com.example.sgalb.Services.AzureBlob;

import com.azure.storage.blob.*;
import com.azure.storage.blob.models.*;
import com.example.sgalb.Dtos.FichierArchiveDTO;
import com.example.sgalb.Entities.*;
import com.example.sgalb.Enum.Status;
import com.example.sgalb.Repositories.*;
import com.example.sgalb.Utils.EncryptionUtils;
import com.jcraft.jsch.*;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AzureBlobServiceInterfaceImpl implements AzureBlobServiceInterface {

    @Autowired
    AzureStorageConfigRepository azureStorageConfigRepository;

    @Autowired
    ArchiveRepository archiveRepository;
    @Autowired
    UtilisateurRepository utilisateurRepository;
    @Autowired
    ServeurRepository serveurRepository;

    private Serveur getServeurConnecte(Long idUtilisateur) {
        List<Serveur> serveurs = serveurRepository.findAllByUtilisateurIdUtilisateurAndConnectedTrue(idUtilisateur);
        if (serveurs.isEmpty()) {
            throw new RuntimeException("Aucun serveur connecté pour cet utilisateur.");
        }
        return serveurs.get(0);
    }

    private BlobContainerClient initClientFromDB(Long idUtilisateur) {
        AzureStorageConfig config = azureStorageConfigRepository.findByUtilisateurIdUtilisateur(idUtilisateur);
        if (config == null) throw new RuntimeException("Configuration Azure introuvable pour l'utilisateur");

        BlobServiceClient serviceClient = new BlobServiceClientBuilder()
                .connectionString(config.getConnectionString())
                .buildClient();

        BlobContainerClient container = serviceClient.getBlobContainerClient(config.getContainerName());
        if (!container.exists()) {
            container.create();
        }

        return container;
    }

    public String upload(String fullRelativePath, Long idUtilisateur) throws Exception {
        Serveur serveur = getServeurConnecte(idUtilisateur);
        String remoteFilePath = "/home/" + serveur.getUser() + "/" + fullRelativePath;
        String sftpPassword = EncryptionUtils.decrypt(serveur.getPassword());

        JSch jsch = new JSch();
        Session session = jsch.getSession(serveur.getUser(), serveur.getHost(), 22);
        session.setPassword(sftpPassword);
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect();

        Channel channel = session.openChannel("sftp");
        channel.connect();
        ChannelSftp sftpChannel = (ChannelSftp) channel;

        try {
            InputStream inputStream = sftpChannel.get(remoteFilePath);
            long fileSize = sftpChannel.lstat(remoteFilePath).getSize();

            String fileName = Paths.get(fullRelativePath).getFileName().toString();

            BlobContainerClient containerClient = initClientFromDB(idUtilisateur);
            BlobClient blobClient = containerClient.getBlobClient(fileName);
            blobClient.upload(inputStream, fileSize, true);

            BlobProperties props = blobClient.getProperties();
            Date dateArchivage = Date.from(props.getLastModified().toInstant());
            long dureeSecondes = (new Date().getTime() - dateArchivage.getTime()) / 1000;
            long tailleOctets = props.getBlobSize();

            Archivage archivage = new Archivage();
            archivage.setDate(dateArchivage);
            archivage.setNomArchive(fileName);
            archivage.setStatuts(Status.Terminé);
            archivage.setTaille(tailleOctets);
            archivage.setDuree(dureeSecondes);
            archivage.setServeurs(serveur.getHost());

            Utilisateur utilisateur = utilisateurRepository.findById(idUtilisateur)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            archivage.setUtilisateur(utilisateur);

            archiveRepository.save(archivage);

            sftpChannel.rm(remoteFilePath);

            inputStream.close();
            return blobClient.getBlobUrl();
        } catch (Exception e) {
            throw new RuntimeException("Erreur durant l'upload ou la suppression du fichier : " + e.getMessage(), e);
        } finally {
            if (sftpChannel.isConnected()) sftpChannel.exit();
            if (session.isConnected()) session.disconnect();
        }
    }

    @Override
    public List<FichierArchiveDTO> listFiles(Long idUtilisateur) {
        BlobContainerClient containerClient = initClientFromDB(idUtilisateur);
        List<FichierArchiveDTO> fichiers = new ArrayList<>();

        Serveur serveur = getServeurConnecte(idUtilisateur);

        for (BlobItem blobItem : containerClient.listBlobs()) {
            String nomFichier = blobItem.getName();

            BlobClient blobClient = containerClient.getBlobClient(nomFichier);
            BlobProperties props = blobClient.getProperties();

            Date dateArchivage = Date.from(props.getLastModified().toInstant());
            long dureeSecondes = (new Date().getTime() - dateArchivage.getTime()) / 1000;
            double tailleMo = (double) props.getBlobSize() / (1024 * 1024);

            fichiers.add(new FichierArchiveDTO(
                    nomFichier,
                    dateArchivage,
                    String.format(Locale.US, "%.2f", tailleMo),
                    serveur.getHost(),
                    dureeSecondes
            ));
        }

        return fichiers;
    }

    public boolean delete(String filename, Long idUtilisateur) {
        BlobContainerClient containerClient = initClientFromDB(idUtilisateur);
        BlobClient blobClient = containerClient.getBlobClient(filename);
        if (blobClient.exists()) {
            blobClient.delete();
            return true;
        }
        return false;
    }

    public byte[] download(String filename, Long idUtilisateur) {
        BlobContainerClient containerClient = initClientFromDB(idUtilisateur);
        BlobClient blobClient = containerClient.getBlobClient(filename);
        if (blobClient.exists()) {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            blobClient.download(os);
            return os.toByteArray();
        }
        return null;
    }

    public String archive(String filename, Long idUtilisateur) {
        BlobContainerClient containerClient = initClientFromDB(idUtilisateur);
        BlobClient originalBlob = containerClient.getBlobClient(filename);
        BlobClient archivedBlob = containerClient.getBlobClient("archive/" + filename);

        if (originalBlob.exists()) {
            archivedBlob.beginCopy(originalBlob.getBlobUrl(), null);
            originalBlob.delete();
            return archivedBlob.getBlobUrl();
        }
        return null;
    }

    public MultipartFile convertToMultipartFile(InputStream inputStream, String fileName) throws IOException {
        byte[] content = inputStream.readAllBytes();

        return new MultipartFile() {
            @Override public String getName() { return fileName; }
            @Override public String getOriginalFilename() { return fileName; }
            @Override public String getContentType() { return "application/octet-stream"; }
            @Override public boolean isEmpty() { return content.length == 0; }
            @Override public long getSize() { return content.length; }
            @Override public byte[] getBytes() { return content; }
            @Override public InputStream getInputStream() { return new ByteArrayInputStream(content); }
            @Override public void transferTo(File dest) throws IOException { Files.write(dest.toPath(), content); }
        };
    }

    public File convertBytesToFile(byte[] bytes, String filename) throws IOException {
        File file = new File(System.getProperty("java.io.tmpdir") + "/" + filename);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(bytes);
        }
        return file;
    }

    @Override
    public AzureStorageConfig saveAzureConfig(AzureStorageConfig azureStorageConfig, Long idUtilisateur) {
        azureStorageConfig.setUtilisateur(utilisateurRepository.findByIdUtilisateur(idUtilisateur));
        return azureStorageConfigRepository.save(azureStorageConfig);
    }

    @Override
    public AzureStorageConfig updateAzureConfig(AzureStorageConfig azureStorageConfig, Long idUtilisateur) {
        AzureStorageConfig existing = azureStorageConfigRepository.findByUtilisateurIdUtilisateur(idUtilisateur);
        existing.setContainerName(azureStorageConfig.getContainerName());
        existing.setConnectionString(azureStorageConfig.getConnectionString());
        return azureStorageConfigRepository.save(existing);
    }

    @Override
    public AzureStorageConfig getAzureConfig(Long idUtilisateur) {
        return azureStorageConfigRepository.findByUtilisateurIdUtilisateur(idUtilisateur);
    }

    public List<String> listFilesToArchive(Long idUtilisateur) throws Exception {
        Serveur serveur = getServeurConnecte(idUtilisateur);
        String remoteDirectory = "/home/" + serveur.getUser() + "/filesToArchive";

        List<String> fileNames = new ArrayList<>();

        JSch jsch = new JSch();
        Session session = jsch.getSession(serveur.getUser(), serveur.getHost(), 22);
        session.setPassword(EncryptionUtils.decrypt(serveur.getPassword()));
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect();

        Channel channel = session.openChannel("sftp");
        channel.connect();
        ChannelSftp sftpChannel = (ChannelSftp) channel;

        Vector<ChannelSftp.LsEntry> files = sftpChannel.ls(remoteDirectory);
        for (ChannelSftp.LsEntry entry : files) {
            String fileName = entry.getFilename();
            if (!fileName.equals(".") && !fileName.equals("..")) {
                fileNames.add(fileName);
            }
        }

        sftpChannel.exit();
        session.disconnect();

        return fileNames;
    }

    public Map<String, String> directoryOrFolder(Long idUtilisateur, String pathRelativeToFilesToArchive) throws Exception {
        Serveur serveur = getServeurConnecte(idUtilisateur);
        String fullPath = "/home/" + serveur.getUser() + "/" + pathRelativeToFilesToArchive;

        JSch jsch = new JSch();
        Session session = jsch.getSession(serveur.getUser(), serveur.getHost(), 22);
        session.setPassword(EncryptionUtils.decrypt(serveur.getPassword()));
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect();

        Channel channel = session.openChannel("sftp");
        channel.connect();
        ChannelSftp sftpChannel = (ChannelSftp) channel;

        SftpATTRS attrs = sftpChannel.lstat(fullPath);

        Map<String, String> result = new HashMap<>();
        result.put("path", pathRelativeToFilesToArchive);
        result.put("type", attrs.isDir() ? "folder" : "file");
        result.put("size", String.valueOf(attrs.getSize()));

        sftpChannel.exit();
        session.disconnect();

        return result;
    }

    public Object processElement(Long idUtilisateur, String pathRelativeToHome) throws Exception {
        Map<String, String> info = directoryOrFolder(idUtilisateur, pathRelativeToHome);
        String type = info.get("type");

        if ("file".equals(type)) {
            String blobUrl = upload(pathRelativeToHome, idUtilisateur);
            Map<String, String> result = new HashMap<>();
            result.put("status", "uploaded");
            result.put("blobUrl", blobUrl);
            return result;
        } else {
            Serveur serveur = getServeurConnecte(idUtilisateur);
            String fullPath = "/home/" + serveur.getUser() + "/" + pathRelativeToHome;

            JSch jsch = new JSch();
            Session session = jsch.getSession(serveur.getUser(), serveur.getHost(), 22);
            session.setPassword(EncryptionUtils.decrypt(serveur.getPassword()));
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            Channel channel = session.openChannel("sftp");
            channel.connect();
            ChannelSftp sftpChannel = (ChannelSftp) channel;

            List<ChannelSftp.LsEntry> entries = sftpChannel.ls(fullPath);
            List<Map<String, String>> results = new ArrayList<>();

            for (ChannelSftp.LsEntry entry : entries) {
                String name = entry.getFilename();
                if (!name.equals(".") && !name.equals("..")) {
                    Map<String, String> item = new HashMap<>();
                    item.put("name", name);
                    item.put("type", entry.getAttrs().isDir() ? "folder" : "file");
                    results.add(item);
                }
            }

            sftpChannel.exit();
            session.disconnect();

            return results;
        }
    }

    public Object inspectRemotePath(Long idUtilisateur, String pathRelativeToHome) throws Exception {
        Serveur serveur = getServeurConnecte(idUtilisateur);
        String fullPath = "/home/" + serveur.getUser() + "/" + pathRelativeToHome;

        JSch jsch = new JSch();
        Session session = jsch.getSession(serveur.getUser(), serveur.getHost(), 22);
        session.setPassword(EncryptionUtils.decrypt(serveur.getPassword()));
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect();

        Channel channel = session.openChannel("sftp");
        channel.connect();
        ChannelSftp sftpChannel = (ChannelSftp) channel;

        SftpATTRS attrs = sftpChannel.lstat(fullPath);

        if (attrs.isDir()) {
            Vector<ChannelSftp.LsEntry> entries = sftpChannel.ls(fullPath);
            List<Map<String, String>> contents = new ArrayList<>();
            for (ChannelSftp.LsEntry entry : entries) {
                String name = entry.getFilename();
                if (!name.equals(".") && !name.equals("..")) {
                    Map<String, String> fileInfo = new HashMap<>();
                    fileInfo.put("name", name);
                    fileInfo.put("type", entry.getAttrs().isDir() ? "folder" : "file");
                    contents.add(fileInfo);
                }
            }
            sftpChannel.exit();
            session.disconnect();
            return contents;
        } else {
            sftpChannel.exit();
            session.disconnect();
            Map<String, String> file = new HashMap<>();
            file.put("name", pathRelativeToHome);
            file.put("type", "file");
            return file;
        }
    }
}

