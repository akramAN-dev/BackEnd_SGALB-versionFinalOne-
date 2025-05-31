package com.example.sgalb.Dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserDto {
    private Long idUser;
    private String email;
}
