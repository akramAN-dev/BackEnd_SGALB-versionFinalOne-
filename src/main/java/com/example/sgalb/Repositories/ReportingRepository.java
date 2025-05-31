package com.example.sgalb.Repositories;

import com.example.sgalb.Entities.Alerting;
import com.example.sgalb.Entities.Reporting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportingRepository extends JpaRepository<Reporting,Long> {
    List<Reporting> findByUtilisateurIdUtilisateur(Long idUser);
}
