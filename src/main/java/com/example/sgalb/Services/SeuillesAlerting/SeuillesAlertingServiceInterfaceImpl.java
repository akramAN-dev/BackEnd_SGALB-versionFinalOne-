package com.example.sgalb.Services.SeuillesAlerting;

import com.example.sgalb.Entities.Serveur;
import com.example.sgalb.Entities.SeuillesAlerting;
import com.example.sgalb.Entities.Utilisateur;
import com.example.sgalb.Repositories.SeuillesAlertingRepository;
import com.example.sgalb.Repositories.UtilisateurRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class SeuillesAlertingServiceInterfaceImpl implements SeuillesAlertingServiceInterface {
    UtilisateurRepository utilisateurRepository;
    SeuillesAlertingRepository seuillesAlertingRepository;



    @Override
    public SeuillesAlerting getTheMetriquesOfUSerConnected(Long idUtilisateur) {
        //Utilisateur userConnected = utilisateurRepository.findByIdUtilisateur(idUtilisateur);
        SeuillesAlerting seuillesAlerting = seuillesAlertingRepository.findByUtilisateurIdUtilisateur(idUtilisateur);
        return seuillesAlerting;
    }

    @Override
    public SeuillesAlerting addSeuilleAlertingOfMetriques(Long idUtilisateur, SeuillesAlerting seuillesAlerting) {
        Utilisateur user = utilisateurRepository.findByIdUtilisateur(idUtilisateur);
        seuillesAlerting.setUtilisateur(user);

        return seuillesAlertingRepository.save(seuillesAlerting);
    }

    @Override
    public SeuillesAlerting modifierSeuilleAlertingOfMetriques(Long idUtilisateur, SeuillesAlerting seuillesAlerting) {
        // Récupérer la seuille de l'utilisateur
        SeuillesAlerting seuilleToUpdate = seuillesAlertingRepository.findByUtilisateurIdUtilisateur(idUtilisateur);
        if (seuilleToUpdate == null) {
            throw new RuntimeException("Aucun seuil trouvé pour l'utilisateur avec l'ID : " + idUtilisateur);
        }

        // Mise à jour des champs sans toucher à l'utilisateur
        seuilleToUpdate.setSeuilleCPU(seuillesAlerting.getSeuilleCPU());
        seuilleToUpdate.setSeuilleDisque(seuillesAlerting.getSeuilleDisque());
        seuilleToUpdate.setSeuilleRam(seuillesAlerting.getSeuilleRam());
        seuilleToUpdate.setSeuilleNetworkReceved(seuillesAlerting.getSeuilleNetworkReceved());
        seuilleToUpdate.setSeuilleNetworkSent(seuillesAlerting.getSeuilleNetworkSent());
        seuilleToUpdate.setSeuilleToReceveMail(seuillesAlerting.getSeuilleToReceveMail());
        seuilleToUpdate.setMailSender(seuillesAlerting.getMailSender());
        seuilleToUpdate.setPasswordMailSender(seuillesAlerting.getPasswordMailSender());

        return seuillesAlertingRepository.save(seuilleToUpdate);
    }

    public SeuillesAlerting getSeuilles(Long idUtilisateur)
    {
        SeuillesAlerting seuilles = seuillesAlertingRepository.findByUtilisateurIdUtilisateur(idUtilisateur);
        return seuilles;
    }

}
