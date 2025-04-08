package com.example.haniniferme;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class SGALBApplication {

    public static void main(String[] args) {
        SpringApplication.run(SGALBApplication.class, args);
    }
    @Bean
    public BCryptPasswordEncoder passwordEncoderCoach() {
        return new BCryptPasswordEncoder();
    }

}
