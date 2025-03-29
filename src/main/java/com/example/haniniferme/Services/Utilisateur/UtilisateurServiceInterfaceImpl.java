package com.example.haniniferme.Services.Utilisateur;


import com.example.haniniferme.Entities.Utilisateur;
import com.example.haniniferme.Repositories.UtilisateurRepository;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;


@Service
@AllArgsConstructor
@NoArgsConstructor

public class UtilisateurServiceInterfaceImpl implements UtilisateurServiceInterface {
    @Autowired
    UtilisateurRepository utilisateurRepository;
    @Override
    public Utilisateur loadByEmail(String email) {
        return utilisateurRepository.findByEmail(email);
    }

    @Override
    public Utilisateur creationCompte(Utilisateur utilisateur) {
        if (utilisateur.getNom() == null || utilisateur.getNom().isEmpty()) {
            throw new IllegalArgumentException("Name is required");
        }
        if (utilisateur.getPrenom() == null || utilisateur.getPrenom().isEmpty()) {
            throw new IllegalArgumentException("Prenom is required");
        }
        if (utilisateur.getEmail() == null || utilisateur.getEmail().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (utilisateur.getMotDePasse() == null || utilisateur.getMotDePasse().isEmpty()) {
            throw new IllegalArgumentException("password is required");
        }
        Utilisateur existingByEmail = utilisateurRepository.findByEmail(utilisateur.getEmail());
        if (existingByEmail != null) {
            throw new IllegalArgumentException("Un utilisateur existant deja avec cette email");
        }

        // Check if a client already exists with the same name and first name
        Utilisateur existingByNameAndFirstName = utilisateurRepository.findByPrenomAndNom(utilisateur.getPrenom(),utilisateur.getNom());
        if (existingByNameAndFirstName != null) {
            throw new IllegalArgumentException("Un utilisateur existant deja avec ce nom");
        }

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String encryptedPassword = encoder.encode(utilisateur.getMotDePasse());
        utilisateur.setMotDePasse(encryptedPassword);
        return utilisateurRepository.save(utilisateur);
    }

    @Override
    public Utilisateur userInfo(Long id) {
        Utilisateur userInfo = utilisateurRepository.findByIdUtilisateur(id);
        return userInfo;
    }


}
