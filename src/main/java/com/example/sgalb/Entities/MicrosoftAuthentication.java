package com.example.sgalb.Entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity
@Data
public class MicrosoftAuthentication {
    @Id
    @Column(unique = true)
    private String idMicrosoft;

    @ManyToOne
    @JoinColumn(name = "id_utilisateur")
    @JsonBackReference
    private Utilisateur utilisateur;

    private Date dateConnexion;

    private String avatar;
    private String role = "CLIENT";

}
