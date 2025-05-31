package com.example.sgalb.Services.Alerting;

import com.example.sgalb.Entities.Alerting;
import com.example.sgalb.Enum.Gravité;

import java.util.List;
import java.util.Map;

public interface AlertingServiceInterface {
    Alerting addAlert(Alerting alert, Long idUtilisateur);
    String deleteAlert(Long idAlerte);
    String updateAlert(Alerting alert, Long idAlerte);
    Alerting selectOneAlertById(Long idAlerte);
    List<Alerting> selectAllAlerts();
    public List<Alerting> selectALertsForUser(Long idUtilisateur);
    Map<Gravité, Long> numberOfEachGravitiesOfAlerts(Long idUtilisateur);// il will use here the selectALertsForUser
}
