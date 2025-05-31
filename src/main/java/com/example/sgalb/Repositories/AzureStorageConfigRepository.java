package com.example.sgalb.Repositories;

import com.example.sgalb.Entities.Archivage;
import com.example.sgalb.Entities.AzureStorageConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AzureStorageConfigRepository extends JpaRepository<AzureStorageConfig, Long> {
    //Optional<AzureStorageConfig> findTopByOrderByDateAjoutDesc();
    AzureStorageConfig findByUtilisateurIdUtilisateur(Long idUser);
}

