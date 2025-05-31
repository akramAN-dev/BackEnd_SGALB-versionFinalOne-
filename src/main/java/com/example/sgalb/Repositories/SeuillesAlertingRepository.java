package com.example.sgalb.Repositories;

import com.example.sgalb.Entities.SeuillesAlerting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeuillesAlertingRepository extends JpaRepository<SeuillesAlerting,Long> {
    SeuillesAlerting findByUtilisateurIdUtilisateur(Long utilisateurId);
    SeuillesAlerting findByIdSeuillesAlerting(Long idSeuille);
}
