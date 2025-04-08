package com.example.haniniferme.Services.Archive;

import com.example.haniniferme.Entities.Archivage;
import com.example.haniniferme.Repositories.ArchiveRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ArchiveServiceImpl implements ArchiveService {
    ArchiveRepository archiveRepository;
    @Override
    public Archivage addArchive(Archivage archivage) {

        if (archivage.getDate() == null) {
            throw new IllegalArgumentException("La date est obligatoire.");
        }

        if (archivage.getArchive() == null || archivage.getArchive().trim().isEmpty()) {
            throw new IllegalArgumentException("L'archive ne peut pas être vide.");
        }

        if (archivage.getTaille() == null || archivage.getTaille() <= 0) {
            throw new IllegalArgumentException("La taille doit être supérieure à zéro.");
        }

        if (archivage.getServeurs() == null) {
            throw new IllegalArgumentException("Le nom des serveurs est requis.");
        }

        if (archivage.getDuree() == null || archivage.getDuree() <= 0) {
            throw new IllegalArgumentException("La durée doit être positive.");
        }

        if (archivage.getUtilisateur() == null) {
            throw new IllegalArgumentException("Un utilisateur est requis.");
        }

        return archiveRepository.save(archivage);
    }

    @Override
    public String deleteArchive(Long idArchive) {

        return "";
    }

}
