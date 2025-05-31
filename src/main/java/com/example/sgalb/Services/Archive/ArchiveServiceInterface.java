package com.example.sgalb.Services.Archive;

import com.example.sgalb.Entities.Archivage;
import com.example.sgalb.Enum.Status;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;

public interface ArchiveServiceInterface {
    Archivage addArchive(Archivage archive,Long idUtilisateur);
    // suppression d'archive
    String deleteArchive(Long idArchive);
    // modification d'archive
    String updateArchive(Archivage archive,Long idArchive);
    // selection archive par userId
    Archivage selectOneArchiveById(Long idArchive);
    // selection de tout les archives
    List<Archivage> selectAllArchives();
    List<Archivage> selectArchivesForUser(Long idUtilisateur);
    Long getNumberOfArchives(Long idUser);// use the selectArchivesForUser
    Map<LocalDate,Long> getNombreArchivesOfDates(Long idUSer);
    List<Archivage> getArchivesNonTermineesParUtilisateur(Long idUtilisateur);
    boolean deleteByFilename(String filename);


}
