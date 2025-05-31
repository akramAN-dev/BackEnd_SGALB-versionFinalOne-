package com.example.sgalb.Repositories;

import com.example.sgalb.Entities.GoogleAuthentication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GoogleAuthenticationRepository extends JpaRepository<GoogleAuthentication,Long> {
    Optional<GoogleAuthentication> findByIdGoogle(String id);

}
