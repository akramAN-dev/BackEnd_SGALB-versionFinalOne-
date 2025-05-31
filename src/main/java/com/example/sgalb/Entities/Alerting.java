package com.example.sgalb.Entities;

import com.example.sgalb.Enum.Gravité;
import com.example.sgalb.Enum.Status;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity
@Data
public class Alerting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idAlerte;

    private String typeDAlerte;

    @Enumerated(EnumType.STRING)
    private Status statut;
    @Enumerated(EnumType.STRING)
    private Gravité gravité;
    private String serveurBase;
    private Date dateEtHeure;


    @ManyToOne
    @JoinColumn(name = "id_utilisateur")
    @JsonBackReference
    private Utilisateur utilisateur;

    // Getters and setters
}
