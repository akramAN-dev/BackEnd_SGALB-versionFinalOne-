package com.example.sgalb.Repositories;

import com.example.sgalb.Entities.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UtilisateurRepository extends JpaRepository<Utilisateur,Long> {
    Utilisateur findByEmail(String email);
    Utilisateur findByIdUtilisateur(Long id);
    Utilisateur findByPrenomAndNom(String nom , String prenom);
}
