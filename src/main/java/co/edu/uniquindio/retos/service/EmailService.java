package co.edu.uniquindio.retos.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private final JavaMailSender mailSender;
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendResetEmail(String to, String resetToken) {
        String resetLink = String.format("http://localhost:8080/reset-password?token=%s", resetToken);
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject("Recuperación de contraseña");
        msg.setText("Para recuperar su contraseña haga click o use este token: " + resetToken + "\nLink (dev): " + resetLink);
        mailSender.send(msg);
    }
}
