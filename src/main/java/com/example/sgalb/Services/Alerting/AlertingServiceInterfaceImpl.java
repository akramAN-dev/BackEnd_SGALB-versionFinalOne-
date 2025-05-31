package com.example.sgalb.Services.Alerting;
import com.example.sgalb.Entities.Alerting;
import com.example.sgalb.Entities.Archivage;
import com.example.sgalb.Entities.Utilisateur;
import com.example.sgalb.Enum.Gravité;
import com.example.sgalb.Repositories.AlertingRepository;
import com.example.sgalb.Repositories.UtilisateurRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class AlertingServiceInterfaceImpl implements AlertingServiceInterface {
     AlertingRepository alertingRepository;
     UtilisateurRepository utilisateurRepository;


    @Override
    public Alerting addAlert(Alerting alert, Long idUtilisateur) {
        if (alert == null || alert.getTypeDAlerte() == null || alert.getDateEtHeure() == null || alert.getServeurBase() == null) {
            throw new IllegalArgumentException("Champs obligatoires manquants.");
        }

        Utilisateur utilisateur = utilisateurRepository.findById(idUtilisateur)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé avec ID : " + idUtilisateur));

        alert.setUtilisateur(utilisateur);
        return alertingRepository.save(alert);
    }

    @Override
    public String deleteAlert(Long idAlerte) {
        Alerting alert = alertingRepository.findById(idAlerte)
                .orElseThrow(() -> new IllegalArgumentException("Alerte non trouvée avec ID : " + idAlerte));
        alertingRepository.delete(alert);
        return "Alerte supprimée avec succès.";
    }

    @Override
    public String updateAlert(Alerting alert, Long idAlerte) {
        Alerting existing = alertingRepository.findById(idAlerte)
                .orElseThrow(() -> new IllegalArgumentException("Alerte non trouvée avec ID : " + idAlerte));

        existing.setTypeDAlerte(alert.getTypeDAlerte());
        existing.setDateEtHeure(alert.getDateEtHeure());
        existing.setGravité(alert.getGravité());
        existing.setStatut(alert.getStatut());
        existing.setServeurBase(alert.getServeurBase());

        alertingRepository.save(existing);
        return "Alerte mise à jour.";
    }

    @Override
    public Alerting selectOneAlertById(Long idAlerte) {
        return alertingRepository.findByIdAlerte(idAlerte);
    }

    @Override
    public List<Alerting> selectAllAlerts() {
        return alertingRepository.findAll();
    }

    public List<Alerting> selectALertsForUser(Long idUtilisateur) {
        return alertingRepository.findByUtilisateurIdUtilisateur(idUtilisateur);
    }

    @Override
    public Map<Gravité, Long> numberOfEachGravitiesOfAlerts(Long idUtilisateur) {
        List<Alerting> listeDesAlerts = selectALertsForUser(idUtilisateur);

        Map<Gravité, Long> graviteCount = new HashMap<>();

        for (Alerting alert : listeDesAlerts) {
            Gravité gravite = alert.getGravité();
            graviteCount.put(gravite, graviteCount.getOrDefault(gravite, 0L) + 1);
        }
        return graviteCount;
    }


}
