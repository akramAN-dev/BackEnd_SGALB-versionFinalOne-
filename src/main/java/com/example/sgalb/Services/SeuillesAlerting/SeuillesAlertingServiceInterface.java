package com.example.sgalb.Services.SeuillesAlerting;

import com.example.sgalb.Entities.SeuillesAlerting;

public interface SeuillesAlertingServiceInterface {
    SeuillesAlerting getTheMetriquesOfUSerConnected(Long idUtilisateur);
    SeuillesAlerting addSeuilleAlertingOfMetriques(Long idUtilisateur, SeuillesAlerting seuillesAlerting);
    SeuillesAlerting getSeuilles(Long idUtilisateur);
    SeuillesAlerting modifierSeuilleAlertingOfMetriques(Long idUtilisateur, SeuillesAlerting seuillesAlerting);
}
