package com.example.sgalb.Repositories;

import com.example.sgalb.Entities.Serveur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ServeurRepository extends JpaRepository<Serveur, Long> {
    //List<Serveur> findByUtilisateurIdUtilisateur(Long utilisateurId);
    Serveur findByUtilisateurIdUtilisateur(Long utilisateurId);
    Serveur findByIdServeur(Long idServeur);
    Optional<Serveur> findByHostAndUser(String host, String username);
    Optional<Serveur> findByHostAndUserAndUtilisateur_IdUtilisateur(String host, String user, Long idUtilisateur);
    List<Serveur> findByUtilisateur_IdUtilisateur(Long utilisateurId);
    List<Serveur> findAllByUtilisateurIdUtilisateurAndConnectedTrue(Long idUtilisateur);
    List<Serveur> findAllByUtilisateurIdUtilisateur(Long idUtilisateur);
}


