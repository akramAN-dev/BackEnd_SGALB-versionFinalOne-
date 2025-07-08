package com.example.sgalb.Services.Reporting;

import com.example.sgalb.Entities.Archivage;
import com.example.sgalb.Entities.Reporting;

import java.io.IOException;
import java.util.List;

public interface ReportingServiceInterface {

    // here a function to select all elements of the user and like
byte[] genererRapportGlobal(Long idUtilisateur, Reporting reporting) throws IOException;
    Long getNumberOfRepports(Long idUser);
    List<Reporting> selectRapportsForUser(Long idUtilisateur);

}
