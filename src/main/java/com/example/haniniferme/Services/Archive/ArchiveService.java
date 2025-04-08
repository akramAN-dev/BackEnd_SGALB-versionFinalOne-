package com.example.haniniferme.Services.Archive;

import com.example.haniniferme.Entities.Archivage;

public interface ArchiveService {
    Archivage addArchive(Archivage archivage);
    String deleteArchive(Long idArchive);
}
