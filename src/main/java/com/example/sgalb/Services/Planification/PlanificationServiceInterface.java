package com.example.sgalb.Services.Planification;

import com.example.sgalb.Entities.Planification;

import java.util.List;

public interface PlanificationServiceInterface {
    Planification addPlanification(Planification planification, Long idUtilisateur);
    String deletePlanification(Long idPlanification);
    String updatePlanification(Planification planification, Long idPlanification);
    Planification selectOnePlanification(Long idPlanification);
    List<Planification> selectAllPlanifications();
    List<Planification> selectPanificationForUser(Long idUtilisateur);
//    String planificationOfArchive(Long idPlanification);
    String planificationOfArchives();
}
