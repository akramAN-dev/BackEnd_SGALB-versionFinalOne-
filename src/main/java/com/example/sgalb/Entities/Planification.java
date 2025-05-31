package com.example.sgalb.Entities;

import com.example.sgalb.Enum.Status;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Entity
@Data
public class Planification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPlanification;

    private String fréquence;
    private String typeDArchive;
    private Status statut= Status.En_cours;
    private Date dernièreExecution;
    private String serveur;
    private Date prochaineExecution;
    private String cheminFichier;
    private String nomFichier;



    @ManyToOne
    @JoinColumn(name = "id_utilisateur")
    @JsonBackReference
    private Utilisateur utilisateur;


//    @OneToOne
//    @JoinColumn(name = "id_archivage")
//    private Archivage archivage;

}
