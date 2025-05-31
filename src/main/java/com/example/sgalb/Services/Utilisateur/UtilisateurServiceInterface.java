package com.example.sgalb.Services.Utilisateur;

import com.example.sgalb.Entities.Utilisateur;

public interface UtilisateurServiceInterface {
    Utilisateur loadByEmail(String username);
    Utilisateur creationCompte(Utilisateur utilisateur);
    Utilisateur userInfo(Long id);


}
