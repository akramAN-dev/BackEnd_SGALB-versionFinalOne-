package com.example.sgalb.Dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;
@Data
@AllArgsConstructor
public class PlanificationUploadDTO {
        private String fréquence;
        private String typeDArchive;
        private String serveur;
        private Date prochaineExecution;
    }

