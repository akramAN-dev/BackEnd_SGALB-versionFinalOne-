package com.example.haniniferme.Dtos;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Entity
public class UserDto {
    @Id
    private Long idUser;
    private String email;
}
