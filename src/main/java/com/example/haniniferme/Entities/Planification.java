package com.example.haniniferme.Entities;

import jakarta.persistence.*;

import java.util.Date;

@Entity
public class Planification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPlanification;

    private String fréquence;
    private String typeDArchive;
    private String statut;
    private Date dernièreExecution;
    private String serveur;
    private Date prochaineExecution;

    @ManyToOne
    @JoinColumn(name = "id_utilisateur")
    private Utilisateur utilisateur;

}
