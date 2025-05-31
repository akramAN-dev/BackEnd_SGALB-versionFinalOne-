package com.example.sgalb.Controllers;

import com.example.sgalb.Dtos.FichierArchiveDTO;
import com.example.sgalb.Entities.AzureStorageConfig;
import com.example.sgalb.Entities.Serveur;
import com.example.sgalb.Entities.SeuillesAlerting;
import com.example.sgalb.Repositories.AzureStorageConfigRepository;
import com.example.sgalb.Repositories.ServeurRepository;
import com.example.sgalb.Repositories.SeuillesAlertingRepository;
import com.example.sgalb.Services.Archive.ArchiveServiceInterface;
import com.example.sgalb.Services.AzureBlob.AzureBlobServiceInterface;
import com.example.sgalb.Services.AzureBlob.AzureBlobServiceInterfaceImpl;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/blob")
@AllArgsConstructor
public class AzureBlobController {
    AzureBlobServiceInterface azureBlobService;
    ArchiveServiceInterface archiveServiceInterface;
    AzureStorageConfigRepository azureStorageConfigRepository;
    ServeurRepository serveurRepository;
    SeuillesAlertingRepository seuillesAlertingRepository;

//    @PostMapping("/upload")
//    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) throws Exception {
//        String url = azureBlobService.upload(file);
//        return ResponseEntity.ok("Fichier uploadé avec succès : " + url);
//    }

    @GetMapping("/list")
    public ResponseEntity<List<FichierArchiveDTO>> listFiles(Long idUtilisateur) {
        return ResponseEntity.ok(azureBlobService.listFiles(idUtilisateur));
    }

//    @DeleteMapping("/delete/{filename}")
//    public ResponseEntity<String> deleteFile(@PathVariable String filename) {
//        boolean deleted = azureBlobService.delete(filename);
//        return deleted ?
//                ResponseEntity.ok("Fichier supprimé : " + filename) :
//                ResponseEntity.status(HttpStatus.NOT_FOUND).body("Fichier introuvable");
//    }
    @DeleteMapping("/delete/{filename}")
    public ResponseEntity<String> deleteFileAndDbRecord(@PathVariable String filename,@RequestParam Long idUtilisateur) {
        boolean deletedBlob = azureBlobService.delete(filename,idUtilisateur);

        // Supposons que tu as un service ou repo pour supprimer l'archive en BDD via filename
        boolean deletedDb = archiveServiceInterface.deleteByFilename(filename);

        if (deletedBlob && deletedDb) {
            return ResponseEntity.ok("Fichier et enregistrement supprimés : " + filename);
        } else if (!deletedBlob) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Fichier introuvable dans Azure Blob");
        } else if (!deletedDb) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Enregistrement BDD introuvable pour : " + filename);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de la suppression");
        }
    }

    @GetMapping("/download/{filename}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable String filename,@RequestParam Long idUtilisateur) {
        byte[] content = azureBlobService.download(filename,idUtilisateur);
        if (content == null) return ResponseEntity.notFound().build();

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(content);
    }

    @PostMapping("/archive/{filename}")
    public ResponseEntity<String> archiveFile(@PathVariable String filename,@RequestParam Long idUtilisateur) {
        String url = azureBlobService.archive(filename,idUtilisateur);
        return url != null ?
                ResponseEntity.ok("Archivé vers : " + url) :
                ResponseEntity.status(HttpStatus.NOT_FOUND).body("Fichier introuvable");
    }

    // lister les fichiers existant dans un path
    @GetMapping("/lister")
    public ResponseEntity<?> listerFichiers(@RequestParam String path) {
        File dossier = new File(path);

        // Vérifie si le chemin est valide et correspond à un dossier
        if (!dossier.exists() || !dossier.isDirectory()) {
            return ResponseEntity
                    .badRequest()
                    .body("Le chemin fourni n'est pas valide ou ne correspond pas à un dossier.");
        }

        // Liste les fichiers
        File[] fichiers = dossier.listFiles();

        if (fichiers == null || fichiers.length == 0) {
            return ResponseEntity.ok(Collections.emptyList()); // Aucun fichier
        }

        // Convertir en une liste de noms
        List<String> nomsFichiers = Arrays.stream(fichiers)
                .filter(File::isFile)
                .map(File::getName)
                .collect(Collectors.toList());

        return ResponseEntity.ok(nomsFichiers);
    }

    // deplacer le ficher vers avec le nom du fichier dans le serveur d'archivage ==> l'upload manuel (archivage manuel d'un fichier)
//    @PostMapping("/upload")
//    public ResponseEntity<String> uploadFileDirect(
//            @RequestParam("file") MultipartFile file,
//            @RequestParam Long idUtilisateur) {
//
//        try {
//            String url = azureBlobService.upload(file, idUtilisateur);
//            return ResponseEntity.ok("✅ Fichier uploadé avec succès : " + url);
//        } catch (Exception e) {
//            return ResponseEntity.status(500)
//                    .body("Erreur lors de l'upload : " + e.getMessage());
//        }
//    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFileFromVM(
            @RequestParam("fileName") String fileName,
            @RequestParam("idUtilisateur") Long idUtilisateur) {

        try {
            String url = azureBlobService.upload(fileName, idUtilisateur);
            return ResponseEntity.ok("✅ Fichier uploadé avec succès depuis la VM : " + url);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body("❌ Erreur lors de l'upload depuis la VM : " + e.getMessage());
        }
    }

    @PostMapping("/addAzureConfig")
    public ResponseEntity<String> saveAzureConfig(@RequestBody AzureStorageConfig  azureStorageConfig,@RequestParam Long idUtilisateur) {
        azureBlobService.saveAzureConfig(azureStorageConfig,idUtilisateur);
        return ResponseEntity.ok("Configuration Azure enregistrée avec succès.");
    }
    @PostMapping("/updateAzureConfig")
    public ResponseEntity<String> updateAzureConfig(@RequestBody AzureStorageConfig  azureStorageConfig,@RequestParam Long idUtilisateur) {
        azureBlobService.updateAzureConfig(azureStorageConfig,idUtilisateur);
        return ResponseEntity.ok("Configuration Azure updated avec succès.");
    }

    @GetMapping("/getAzureConfig")
    public ResponseEntity<AzureStorageConfig> getAzureConfig(@RequestParam Long idUtilisateur) {
        AzureStorageConfig azureConfig = azureBlobService.getAzureConfig(idUtilisateur);
        return new ResponseEntity<>(azureConfig, HttpStatus.OK);
    }


    // global stuff about if the user has the metriques or no
    @GetMapping("/globalStuff")
    public ResponseEntity<Boolean> seuillesMetriquesServeurBlobStorage(@RequestParam Long idUtilisateur) {
        AzureStorageConfig azureStorageConfig = azureStorageConfigRepository.findByUtilisateurIdUtilisateur(idUtilisateur);
        SeuillesAlerting seuillesAlerting = seuillesAlertingRepository.findByUtilisateurIdUtilisateur(idUtilisateur);
        Serveur serveur = serveurRepository.findByUtilisateurIdUtilisateur(idUtilisateur);

        if (azureStorageConfig == null || seuillesAlerting == null || serveur == null) {
            return ResponseEntity.ok(false);
        }
        return ResponseEntity.ok(true);
    }
    // lister des fichiers a archiver
    @GetMapping("/filesToArchive")
    public ResponseEntity<List<String>> listFilesToArchive(@RequestParam Long idUtilisateur) {
        try {
            List<String> fichiers = azureBlobService.listFilesToArchive(idUtilisateur);
            return ResponseEntity.ok(fichiers);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonList("Erreur : " + e.getMessage()));
        }
    }

    // check if it s a folder or a directory by the name
    @GetMapping("/check-type")
    public Map<String, String> checkType(@RequestParam Long idUtilisateur, @RequestParam String path) throws Exception {
        return azureBlobService.directoryOrFolder(idUtilisateur, path);
    }
    // upload or list the elements of a folder
    @GetMapping("/handle-path")
    public ResponseEntity<?> handlePath(@RequestParam Long idUtilisateur, @RequestParam String path) {
        try {
            Object result = azureBlobService.processElement(idUtilisateur, path);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    @GetMapping("/explore")
    public ResponseEntity<?> explorePath(
            @RequestParam Long idUtilisateur,
            @RequestParam String path
    ) {
        try {
            Object result = azureBlobService.inspectRemotePath(idUtilisateur, path);

            Map<String, Object> pathData = new HashMap<>();
            if (result instanceof List) {
                pathData.put("contents", result);
            } else if (result instanceof Map) {
                // C'est un fichier isolé
                List<Map<String, String>> singleFileList = new ArrayList<>();
                singleFileList.add((Map<String, String>) result);
                pathData.put("contents", singleFileList);
            }

            Map<String, Object> finalResponse = new HashMap<>();
            finalResponse.put("/" + path, pathData);

            return ResponseEntity.ok(finalResponse);

        } catch (FileNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de l'exploration du chemin : " + e.getMessage()));
        }
    }

}
