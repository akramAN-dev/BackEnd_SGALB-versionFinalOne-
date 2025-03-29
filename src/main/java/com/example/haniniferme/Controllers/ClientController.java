package com.example.haniniferme.Controllers;


import com.example.haniniferme.Dtos.UserDto;
import com.example.haniniferme.Entities.Utilisateur;
import com.example.haniniferme.Mapper.DtoMapper;
import com.example.haniniferme.Services.Utilisateur.UtilisateurServiceInterfaceImpl;
import jdk.jshell.execution.Util;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/utilisateur")
@AllArgsConstructor
public class ClientController {
    UtilisateurServiceInterfaceImpl utilisateurServiceInterface;

    @PostMapping("/addUtilisateur")
    public ResponseEntity<String> addUtilisateur(@RequestBody Utilisateur utilisateur) {
        try {
            Utilisateur newUser = utilisateurServiceInterface.creationCompte(utilisateur);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("Utilisateur cree avec succee");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred");
        }
    }

    @GetMapping("/user-info")
    public Utilisateur userInfos(@RequestBody UserDto userDto) {
        // Mapper le UserDto en Utilisateur
        Utilisateur utilisateur = utilisateurServiceInterface.userInfo(userDto.getIdUser());

        // Mapper l'Utilisateur en UserDto
        return utilisateur;
    }




}
