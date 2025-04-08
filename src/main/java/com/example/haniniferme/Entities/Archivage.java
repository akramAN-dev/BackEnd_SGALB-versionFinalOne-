package com.example.haniniferme.Entities;

import com.example.haniniferme.Enum.Status;
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
    private String archive;

    @Enumerated(EnumType.STRING)
    private Status statuts = Status.En_cours;

    private Long taille;
    private String serveurs;
    private Long duree;

    @ManyToOne
    @JoinColumn(name = "id_utilisateur")
    private Utilisateur utilisateur;


}