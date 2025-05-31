package com.example.sgalb.Entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity
@Data
public class Reporting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idRapport;

    private String nomDuRapport;
    private String typeDuRapport;

    @Temporal(TemporalType.DATE)
    private Date dateDebut;

    @Temporal(TemporalType.DATE)
    private Date dateFin;
    private String format;
    private String personnalisationDuContenu;

    @ManyToOne
    @JoinColumn(name = "id_utilisateur")
    @JsonBackReference
    private Utilisateur utilisateur;


}
