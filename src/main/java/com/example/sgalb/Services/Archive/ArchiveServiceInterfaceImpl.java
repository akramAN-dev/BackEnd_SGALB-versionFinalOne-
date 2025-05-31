package com.example.sgalb.Services.Archive;

import com.azure.storage.blob.models.BlobItem;
import com.example.sgalb.Entities.Archivage;
import com.example.sgalb.Entities.Utilisateur;
import com.example.sgalb.Enum.Status;
import com.example.sgalb.Repositories.ArchiveRepository;
import com.example.sgalb.Repositories.UtilisateurRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Service
@AllArgsConstructor
public class ArchiveServiceInterfaceImpl implements ArchiveServiceInterface{
    ArchiveRepository archiveRepository;
    UtilisateurRepository utilisateurRepository;
    //Add archive
    @Override
    public Archivage addArchive(Archivage archive, Long idUtilisateur) {
        if (archive == null) {
            throw new IllegalArgumentException("L'archive ne peut pas être null.");
        }
        if (archive.getDate() == null) {
            throw new IllegalArgumentException("La date de l'archive est obligatoire.");
        }
        if (archive.getTaille() == null || archive.getTaille() <= 0) {
            throw new IllegalArgumentException("La taille de l'archive doit être positive.");
        }
        if (archive.getServeurs() == null || archive.getServeurs().trim().isEmpty()) {
            throw new IllegalArgumentException("Le serveur d'archive est obligatoire.");
        }

        // Vérifier l'existence de l'utilisateur
        Utilisateur utilisateur = utilisateurRepository.findById(idUtilisateur)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé avec l'ID : " + idUtilisateur));

        archive.setUtilisateur(utilisateur);
//        // Vérifier s'il existe déjà une archive pour cette date et ce serveur
//        boolean exists = archiveRepository.existsByDateAndServeurs(archive.getDate(), archive.getServeurs());
//        if (exists) {
//            throw new IllegalArgumentException("Une archive avec cette date et ce serveur existe déjà.");
//        }

        // Associer l'utilisateur à l'archive
        archive.setUtilisateur(utilisateur);

        // Sauvegarder
        return archiveRepository.save(archive);
    }

    @Override
    public String deleteArchive(Long idArchive) {
        // Vérifier si l'archive existe
        Archivage archiveToDelete = archiveRepository.findById(idArchive)
                .orElseThrow(() -> new IllegalArgumentException("Aucune archive trouvée avec l'ID: " + idArchive));
        // Supprimer l'archive
        archiveRepository.delete(archiveToDelete);
        return "L'archive " + archiveToDelete.getNomArchive() + " a été supprimée avec succès.";
    }

    @Override
    public String updateArchive(Archivage archive,Long IdArchive) {
        // Vérifier si l'archive existe
        Archivage archiveToUpdate = archiveRepository.findById(IdArchive)
                .orElseThrow(() -> new IllegalArgumentException("Aucune archive trouvée avec l'ID: " + archive.getIdArchivage()));

        // Mise à jour des champs de l'archive
        archiveToUpdate.setNomArchive(archive.getNomArchive());
        archiveToUpdate.setDate(archive.getDate());
        archiveToUpdate.setTaille(archive.getTaille());
        archiveToUpdate.setStatuts(archive.getStatuts());
        archiveToUpdate.setServeurs(archive.getServeurs());
        archiveToUpdate.setDuree(archive.getDuree());

        // Sauvegarder les modifications
        archiveRepository.save(archiveToUpdate);

        // Retourner un message de succès
        return "L'archive avec l'ID " + archive.getIdArchivage() + " a été mise à jour avec succès.";
    }


    @Override
    public Archivage selectOneArchiveById(Long idArchive) {

        return archiveRepository.findByIdArchivage(idArchive);
    }

    @Override
    public List<Archivage> selectAllArchives() {
        return archiveRepository.findAll();
    }

    @Override
    public List<Archivage> selectArchivesForUser(Long idUtilisateur) {

        return archiveRepository.findByUtilisateurIdUtilisateur(idUtilisateur);
    }

    public List<Archivage> getArchivesNonTermineesParUtilisateur(Long idUtilisateur) {
    return archiveRepository.findByUtilisateurIdUtilisateurAndStatutsNot(idUtilisateur, Status.Terminé);
    }

    public boolean deleteByFilename(String filename) {
        Optional<Archivage> archiveOpt = archiveRepository.findByNomArchive(filename);
        if (archiveOpt.isPresent()) {
            archiveRepository.delete(archiveOpt.get());
            return true; // suppression réussie
        }
        return false; // pas trouvé => suppression impossible
    }


    @Override
    public Long getNumberOfArchives(Long idUser) {
        List<Archivage> archivesOfUser = selectArchivesForUser(idUser);
        return (archivesOfUser != null) ? (long) archivesOfUser.size() : 0L;
    }

    @Override
    public Map<LocalDate, Long> getNombreArchivesOfDates(Long idUser) {
        List<Archivage> archivesOfUser = selectArchivesForUser(idUser);

        if (archivesOfUser == null || archivesOfUser.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<LocalDate, Long> countByDate = new HashMap<>();

        for (Archivage archivage : archivesOfUser) {
            // Convertir java.util.Date → java.time.LocalDate (ignorer l'heure)
            Instant instant = archivage.getDate().toInstant();
            LocalDate localDate = instant.atZone(ZoneId.systemDefault()).toLocalDate();

            countByDate.put(localDate, countByDate.getOrDefault(localDate, 0L) + 1);
        }

        return countByDate;
    }


}