package com.example.sgalb.Services.Mailing;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Service // ✅ AJOUTE CETTE ANNOTATION
public class EmailServiceInterfaceImpl implements EmailServiceInterface {

    @Autowired
    private JavaMailSender mailSender;

    public void sendEmail(String toEmail, String subject, String body, String fromEmail, String password) {
        JavaMailSenderImpl customMailSender = new JavaMailSenderImpl();
        customMailSender.setHost("smtp.gmail.com");
        customMailSender.setPort(587);
        customMailSender.setUsername(fromEmail);
        customMailSender.setPassword(password);

        Properties props = customMailSender.getJavaMailProperties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);

        try {
            customMailSender.send(message);
            System.out.println("Email envoyé depuis " + fromEmail + " à " + toEmail);
        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi de l'email : " + e.getMessage());
        }
    }

}
