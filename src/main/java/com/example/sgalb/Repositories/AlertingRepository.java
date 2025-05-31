package com.example.sgalb.Repositories;

import com.example.sgalb.Entities.Alerting;
import com.example.sgalb.Entities.Archivage;
import com.example.sgalb.Entities.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface AlertingRepository extends JpaRepository<Alerting,Long> {
    Alerting findByIdAlerte(Long idArchivage);
    List<Alerting> findByUtilisateurIdUtilisateur(Long idUser);
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Alerting a WHERE a.typeDAlerte = :typeDAlerte")
    boolean existsByTypeDAlerte(@Param("typeDAlerte") String typeDAlerte);
    @Query("SELECT a FROM Alerting a WHERE a.utilisateur.idUtilisateur = :idUtilisateur")
    List<Alerting> findAllByUtilisateurId(@Param("idUtilisateur") Long idUtilisateur);


}
