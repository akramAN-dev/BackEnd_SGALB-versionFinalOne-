package com.example.sgalb.Entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Data
@Entity
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idNotif ;
    private String message ;
    private Boolean lu =false;
    private Date dateNotif;


    @ManyToOne
    @JoinColumn(name = "id_utilisateur")
    @JsonBackReference
    private Utilisateur utilisateur;
}
