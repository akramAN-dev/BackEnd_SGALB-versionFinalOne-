package com.example.sgalb.Services.Serveur;

import com.example.sgalb.Entities.Serveur;
import com.example.sgalb.Entities.Utilisateur;
import com.example.sgalb.Repositories.ServeurRepository;
import com.example.sgalb.Repositories.UtilisateurRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ServeurServiceInterfaceImpl implements ServeurServiceInterface {
    ServeurRepository serveurRepository;
    UtilisateurRepository utilisateurRepository;
    @Override
    public Serveur ajouterServeur(Serveur serveur, Long idUser) {
        Utilisateur utilisateur = utilisateurRepository.findById(idUser)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // Lier les deux côtés
        serveur.setUtilisateur(utilisateur);

        return serveurRepository.save(serveur);
    }

    @Override
    public Serveur modifierServeur(Long idUtilisateur,Serveur nouveauServeur) {
        Serveur serveurToUpdate = serveurRepository.findByUtilisateurIdUtilisateur(idUtilisateur);
        // Mise à jour des champs, sans modifier l'ID ni la relation utilisateur
        serveurToUpdate.setHost(nouveauServeur.getHost());
        serveurToUpdate.setUser(nouveauServeur.getUser());
        serveurToUpdate.setPassword(nouveauServeur.getPassword());
        return serveurRepository.save(serveurToUpdate);
    }

    @Override
    public Serveur getServeur(Long idUtilisateur) {
        return serveurRepository.findByUtilisateurIdUtilisateur(idUtilisateur);
    }


}
