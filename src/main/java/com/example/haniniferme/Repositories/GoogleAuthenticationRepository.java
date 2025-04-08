package com.example.haniniferme.Repositories;

import com.example.haniniferme.Entities.GoogleAuthentication;
import com.example.haniniferme.Entities.Reporting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GoogleAuthenticationRepository extends JpaRepository<GoogleAuthentication,Long> {

}
