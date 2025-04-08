package com.example.haniniferme.Repositories;

import com.example.haniniferme.Entities.Archivage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArchiveRepository extends JpaRepository<Archivage,Long> {
    Archivage findByIdArchivage(Long idArchivage);

}
