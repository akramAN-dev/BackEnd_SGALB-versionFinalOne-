package com.example.sgalb.Repositories;

import com.example.sgalb.Entities.Serveur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServeurRepository extends JpaRepository<Serveur, Long> {
    //List<Serveur> findByUtilisateurIdUtilisateur(Long utilisateurId);
    Serveur findByUtilisateurIdUtilisateur(Long utilisateurId);
    Serveur findByIdServeur(Long idServeur);
}


