package com.example.sgalb.Dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceInfoDTO {
    private String name;
    private String status;
    private String type;
}
