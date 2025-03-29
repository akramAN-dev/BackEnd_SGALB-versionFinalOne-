package com.example.haniniferme.Security;

import com.example.haniniferme.Entities.Utilisateur;
import com.example.haniniferme.Services.Utilisateur.UtilisateurServiceInterface;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;


@Service
@Data
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    @Lazy
    private UtilisateurServiceInterface utilisateurServiceInterface;
    private Long userId;
    private String username;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Utilisateur utilisateur = utilisateurServiceInterface.loadByEmail(email);

        if (utilisateur == null) {
            log.error("User with email {} not found", email);
            throw new UsernameNotFoundException("User not found");
        }

        this.userId = utilisateur.getIdUtilisateur();
        this.username = utilisateur.getNom();



        return new org.springframework.security.core.userdetails.User(
                utilisateur.getEmail(),
                utilisateur.getMotDePasse(),
                true, true, true, true,  // Enables the account with no restrictions
                List.of()  // No authorities
        );
    }

}
