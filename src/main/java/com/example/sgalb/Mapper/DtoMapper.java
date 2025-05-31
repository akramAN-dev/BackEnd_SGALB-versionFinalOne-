package com.example.sgalb.Mapper;

import com.example.sgalb.Dtos.UserDto;
import com.example.sgalb.Entities.Utilisateur;

public class DtoMapper {

    // Convertir un Utilisateur en UserDto
    public static UserDto toUserDto(Utilisateur utilisateur) {
        // Vérifie que l'utilisateur n'est pas null
        if (utilisateur == null) {
            return null;
        }
        return new UserDto(utilisateur.getIdUtilisateur(), utilisateur.getEmail()); // Par exemple, tu ne prends que l'email
    }

    // Convertir un UserDto en Utilisateur
    public static Utilisateur toUtilisateur(UserDto userDto) {
        // Vérifie que le UserDto n'est pas null
        if (userDto == null) {
            return null;
        }
        // Créer un nouvel Utilisateur avec les informations du UserDto
        // Ici, on peut étendre cette méthode pour mapper d'autres champs si nécessaire
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setEmail(userDto.getEmail());
        // Assure-toi de compléter avec d'autres champs si nécessaire
        return utilisateur;
    }
}
