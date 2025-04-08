package com.example.haniniferme.Entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@SuperBuilder
public class Utilisateur {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idUtilisateur;

    private String nom;
    private String prenom;
    private String email;
    private String motDePasse;
    private String role;
    private Date dateInscription;
    private String statut;

    @OneToMany(mappedBy = "utilisateur")
    private List<GoogleAuthentication> googleAuthentications;

    @OneToMany(mappedBy = "utilisateur")
    private List<Archivage> archivages;

    @OneToMany(mappedBy = "utilisateur")
    private List<Planification> planifications;

    @OneToMany(mappedBy = "utilisateur")
    private List<Reporting> reportings;

    @OneToMany(mappedBy = "utilisateur")
    private List<Alerting> alertings;

}
