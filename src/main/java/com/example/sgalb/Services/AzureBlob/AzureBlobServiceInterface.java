package com.example.sgalb.Services.AzureBlob;

import com.azure.storage.blob.models.BlobItem;
import com.example.sgalb.Dtos.FichierArchiveDTO;
import com.example.sgalb.Entities.Archivage;
import com.example.sgalb.Entities.AzureStorageConfig;
import com.example.sgalb.Entities.Utilisateur;
import com.example.sgalb.Enum.Status;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;
import java.util.Map;

public interface AzureBlobServiceInterface {
    //String upload(MultipartFile file,Long idUtilisateur) throws Exception ;
    String upload(String file,Long idUtilisateur) throws Exception ;
    MultipartFile convertToMultipartFile(InputStream inputStream, String fileName) throws IOException;
    List<FichierArchiveDTO> listFiles(Long idUtilisateur);
    boolean delete(String filename,Long idUtilisateur);
    byte[] download(String filename, Long idUtilisateur);
    String archive(String filename, Long idUtilisateur);
    //MultipartFile convertToMultipartFile(File file) throws IOException;
    public File convertBytesToFile(byte[] bytes, String filename) throws IOException;
    AzureStorageConfig saveAzureConfig(AzureStorageConfig azureStorageConfig,Long idUtilisateur);
    AzureStorageConfig updateAzureConfig(AzureStorageConfig azureStorageConfig,Long idUtilisateur);
    List<String> listFilesToArchive(Long idUtilisateur) throws Exception;
    AzureStorageConfig getAzureConfig(Long idUtilisateur);
    Map<String, String> directoryOrFolder(Long idUtilisateur, String pathRelativeToFilesToArchive) throws Exception;
    Object processElement(Long idUtilisateur, String pathRelativeToHome) throws Exception;
    // to update
    Object inspectRemotePath(Long idUtilisateur, String pathRelativeToHome) throws Exception;
}
