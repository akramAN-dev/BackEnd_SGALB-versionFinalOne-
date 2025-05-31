package com.example.sgalb.Entities;

import com.example.sgalb.Enum.Status;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity
@Data
public class Archivage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idArchivage;

    private Date date;
    private String nomArchive;

    @Enumerated(EnumType.STRING)
    private Status statuts = Status.En_cours;

    private Long taille;
    private String serveurs;
    private Long duree;

    @ManyToOne
    @JoinColumn(name = "id_utilisateur")
    @JsonBackReference
    private Utilisateur utilisateur;

//    @ManyToOne
//    @JoinColumn(name = "id_planification")
//    @JsonBackReference
//    private Planification planification;


}