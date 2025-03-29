package com.example.haniniferme.Services.Utilisateur;

import com.example.haniniferme.Entities.Utilisateur;

public interface UtilisateurServiceInterface {
    Utilisateur loadByEmail(String username);
    Utilisateur creationCompte(Utilisateur utilisateur);
    Utilisateur userInfo(Long id);


}
