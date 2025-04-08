package com.example.haniniferme.Entities;

import jakarta.persistence.*;

import java.util.Date;

@Entity
public class Reporting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idRapport;

    private String nomDuRapport;
    private String typeDuRapport;
    private Date périodeDuRapport;
    private String format;
    private String personnalisationDuContenu;

    @ManyToOne
    @JoinColumn(name = "id_utilisateur")
    private Utilisateur utilisateur;


}
