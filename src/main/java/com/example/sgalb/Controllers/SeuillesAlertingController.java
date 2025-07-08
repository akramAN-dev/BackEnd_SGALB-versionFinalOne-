package com.example.sgalb.Controllers;

import com.example.sgalb.Entities.Serveur;
import com.example.sgalb.Entities.SeuillesAlerting;
import com.example.sgalb.Services.SeuillesAlerting.SeuillesAlertingServiceInterface;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/SeuillesAlerting")
@AllArgsConstructor
public class SeuillesAlertingController {
    SeuillesAlertingServiceInterface seuillesAlertingServiceInterface;
    @PostMapping("/addSeuillesAlerting")
    public ResponseEntity<SeuillesAlerting> addSeuillesAlerting(@RequestParam Long idUtilisateur , @Valid @RequestBody SeuillesAlerting seuillesAlerting)
    {
        SeuillesAlerting seuilleAlertsAdded = seuillesAlertingServiceInterface.addSeuilleAlertingOfMetriques(idUtilisateur , seuillesAlerting);
        return new ResponseEntity<>(seuilleAlertsAdded, HttpStatus.CREATED);
    }

    @PostMapping("/updateSeuillesAlerting")
    public ResponseEntity<SeuillesAlerting> modifierServeur(@RequestParam Long idUtilisateur, @RequestBody SeuillesAlerting seuillesAlerting) {
        SeuillesAlerting seuilleAlertsAdded = seuillesAlertingServiceInterface.modifierSeuilleAlertingOfMetriques(idUtilisateur,seuillesAlerting);
        return new ResponseEntity<>(seuilleAlertsAdded, HttpStatus.OK);
    }

    @GetMapping("/getSeuillesAlerting")
    public  ResponseEntity<SeuillesAlerting> getSeuilleAlerting(@RequestParam Long idUtilisateur) {
        SeuillesAlerting seuilleAlerts = seuillesAlertingServiceInterface.getSeuilles(idUtilisateur);
        return new ResponseEntity<>(seuilleAlerts, HttpStatus.OK);
    }

}
