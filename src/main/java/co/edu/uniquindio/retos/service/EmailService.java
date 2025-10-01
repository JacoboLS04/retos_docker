package co.edu.uniquindio.retos.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;


@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // Método genérico
    public void enviarCorreo(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("londgav01@gmail.com");
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }

    // Método especializado para reset de contraseña
    public void sendResetEmail(String to, String resetToken) {
        String resetLink = String.format("http://localhost:8080/reset-password?token=%s", resetToken);

        String body = "Para recuperar su contraseña use este token: " + resetToken +
                "\n\nO haga click en el siguiente enlace (solo para desarrollo): " + resetLink;

        enviarCorreo(to, "Recuperación de contraseña", body);
    }
}
