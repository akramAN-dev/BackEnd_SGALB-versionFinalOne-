package com.example.sgalb.Repositories;

import com.example.sgalb.Entities.Archivage;
import com.example.sgalb.Enum.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface ArchiveRepository extends JpaRepository<Archivage,Long> {
    Archivage findByIdArchivage(Long idArchivage);
    boolean existsByDateAndServeurs(Date date, String serveur);
    List<Archivage> findByUtilisateurIdUtilisateur(Long idUser);
   // List<Archivage> findByDate(Date date);
   List<Archivage> findByUtilisateurIdUtilisateurAndStatutsNot(Long idUtilisateur, Status statuts);
    Optional<Archivage> findByNomArchive(String nomArchive);

}
