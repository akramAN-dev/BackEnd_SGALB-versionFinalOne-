package com.example.sgalb.Services.Serveur;

import com.example.sgalb.Entities.Serveur;

import java.util.List;
import java.util.Optional;

public interface ServeurServiceInterface {

    Serveur ajouterServeur(Serveur serveur,Long idUser);
    Serveur modifierServeur(Long idUtilisateur, Serveur nouveauServeur);
   Serveur getServeur(Long idUtilisateur);
   //Optional<Serveur> getServeur(Long idUtilisateur);
   Optional<Serveur> verifierConnexionServeur(String host, String username, String rawPassword, Long idUtilisateur);
    Optional<Serveur> getServeurByHostAndUsernameAndUtilisateur(String host, String username, Long idUtilisateur);
    //public Serveur getServeurWithDecryptedPassword(Long idUtilisateur);
    public List<Serveur> getServeursByUtilisateur(Long idUtilisateur);

    //
    void setServeurConnecte(Long idUtilisateur, Long idServeurConnecte);
    void deconnecterServeur(Long idUtilisateur, Long idServeur);
    List<Serveur> getServeursWithDecryptedPasswords(Long idUtilisateur);
    String updateServeur(Long idServeur,Serveur serveurUpated);
    String deleteServeur(Long idServeur);

}
