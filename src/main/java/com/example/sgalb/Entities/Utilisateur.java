package com.example.sgalb.Entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;
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

    @Temporal(TemporalType.TIMESTAMP)
    private Date lastAlertMailSent;

    @OneToMany(mappedBy = "utilisateur", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<Notification> notifications;

    @OneToMany(mappedBy = "utilisateur", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<Serveur> serveur;

    @OneToMany(mappedBy = "utilisateur", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<SeuillesAlerting> seuillesAlertings;

    @OneToMany(mappedBy = "utilisateur", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<AzureStorageConfig> azureStorageConfigs;





}
