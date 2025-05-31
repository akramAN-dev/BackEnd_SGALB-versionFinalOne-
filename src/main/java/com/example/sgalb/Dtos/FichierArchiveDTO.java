package com.example.sgalb.Dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
public class FichierArchiveDTO {
    private String nomFichier;
    private Date dateArchivage;
    private String tailleMo;
    private String serveur;
    private long dureeSecondes;
}