package com.example.haniniferme.Entities;

import com.example.haniniferme.Enum.Gravité;
import com.example.haniniferme.Enum.Status;
import jakarta.persistence.*;

import java.util.Date;

@Entity
public class Alerting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idAlerte;

    private String typeDAlerte;
    private Status statut;
    @Enumerated(EnumType.STRING)
    private Gravité gravité;
    private String serveurBase;
    private Date dateEtHeure;

    @ManyToOne
    @JoinColumn(name = "id_utilisateur")
    private Utilisateur utilisateur;

    // Getters and setters
}
