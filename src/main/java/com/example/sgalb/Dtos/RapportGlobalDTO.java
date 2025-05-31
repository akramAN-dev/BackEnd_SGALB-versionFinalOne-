package com.example.sgalb.Dtos;

import com.example.sgalb.Entities.Alerting;
import com.example.sgalb.Entities.Archivage;
import com.example.sgalb.Entities.Planification;
import com.example.sgalb.Entities.Utilisateur;
import lombok.Data;

import java.util.List;
@Data
public class RapportGlobalDTO {
        private List<Archivage> archivages;
        private List<Alerting> alertes;
        private List<Planification> planifications;
        private List<Utilisateur> utilisateurs;

}
