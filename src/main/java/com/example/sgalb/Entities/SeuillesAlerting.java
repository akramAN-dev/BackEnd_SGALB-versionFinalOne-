package com.example.sgalb.Entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class SeuillesAlerting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idSeuillesAlerting;
    private Long seuilleCPU;
    private Long seuilleRam;
    private Long seuilleDisque;
    private Long seuilleNetworkReceved;
    private Long seuilleNetworkSent;
    private Long seuilleToReceveMail;
    private String mailSender;
    private String passwordMailSender;

    @ManyToOne
    @JoinColumn(name = "id_utilisateur")
    @JsonBackReference
    private Utilisateur utilisateur;
}
