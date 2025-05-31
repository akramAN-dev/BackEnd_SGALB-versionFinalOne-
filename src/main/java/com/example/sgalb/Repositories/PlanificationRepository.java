package com.example.sgalb.Repositories;

import com.example.sgalb.Entities.Archivage;
import com.example.sgalb.Entities.Planification;
import com.example.sgalb.Enum.Status;
import org.apache.commons.compress.harmony.pack200.Archive;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;

public interface PlanificationRepository extends JpaRepository<Planification,Long> {
    Planification findByIdPlanification(Long idPlanification);
    List<Planification> findByUtilisateurIdUtilisateur(Long idUser);

    //List<Planification> findByStatutNotAndProchaineExecutionBefore(Status status, Date now);

    List<Planification> findByStatutNotAndProchaineExecutionLessThanEqual(Status status, Date now);
}
