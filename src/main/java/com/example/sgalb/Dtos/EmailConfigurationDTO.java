package com.example.sgalb.Dtos;


import lombok.Data;

@Data
public class EmailConfigurationDTO {
    private String email;
    private String password;
    private String toEmail;
    private String subject;
    private String body;

}
