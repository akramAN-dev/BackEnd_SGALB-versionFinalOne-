package com.example.sgalb.Entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Serveur {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idServeur;

    private String host;
    private String user;
    private String password;

    @ManyToOne
    @JoinColumn(name = "id_utilisateur")
    @JsonBackReference
    private Utilisateur utilisateur;

}