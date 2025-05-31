package com.example.sgalb.Entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity
@Data
public class AzureStorageConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String connectionString;
    private String containerName;
    @ManyToOne
    @JoinColumn(name = "id_utilisateur")
    @JsonBackReference
    private Utilisateur utilisateur;

}
