package com.example.sgalb.Repositories;

import com.example.sgalb.Entities.GoogleAuthentication;
import com.example.sgalb.Entities.MicrosoftAuthentication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MicrosoftAuthenticationRepository extends JpaRepository<MicrosoftAuthentication,Long> {
    Optional<MicrosoftAuthentication> findByIdMicrosoft(String id);

}
