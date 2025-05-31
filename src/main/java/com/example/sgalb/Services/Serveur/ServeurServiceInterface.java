package com.example.sgalb.Services.Serveur;

import com.example.sgalb.Entities.Serveur;

public interface ServeurServiceInterface {

    Serveur ajouterServeur(Serveur serveur,Long idUser);
    Serveur modifierServeur(Long idUtilisateur, Serveur nouveauServeur);
    Serveur getServeur(Long idUtilisateur);
}
