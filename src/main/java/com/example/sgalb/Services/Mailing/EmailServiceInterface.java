package com.example.sgalb.Services.Mailing;

public interface EmailServiceInterface {
    void sendEmail(String toEmail, String subject, String body, String fromEmail, String password);
}
