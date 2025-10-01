package co.edu.uniquindio.retos.service;

import co.edu.uniquindio.retos.config.RabbitConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
public class EventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public EventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    private void send(Map<String, Object> event) {
        event.put("origin", "user-service");
        event.put("version", 1);
        rabbitTemplate.convertAndSend(
                RabbitConfig.USER_EVENTS_EXCHANGE,
                RabbitConfig.USER_EVENTS_ROUTING_KEY,
                event
        );
    }

    public void publishUserRegisteredEvent(Long userId, String email, String nombre, String telefono) {
        Map<String,Object> ev = new HashMap<>();
        ev.put("type", "USER_REGISTERED");
        ev.put("userId", userId);
        ev.put("email", email);
        ev.put("nombre", nombre);
        ev.put("telefono", telefono);
        ev.put("timestamp", Instant.now().toString());
        send(ev);
    }

    public void publishUserLoginEvent(Long userId, String email, String telefono) {
        Map<String,Object> ev = new HashMap<>();
        ev.put("type", "USER_LOGIN");
        ev.put("userId", userId);
        ev.put("email", email);
        ev.put("telefono", telefono);
        ev.put("timestamp", Instant.now().toString());
        send(ev);
    }

    public void publishPasswordResetRequestedEvent(Long userId, String email, String token, String telefono) {
        Map<String,Object> ev = new HashMap<>();
        ev.put("type", "PASSWORD_RESET_REQUESTED");
        ev.put("userId", userId);
        ev.put("email", email);
        ev.put("token", token);
        ev.put("telefono", telefono);
        ev.put("timestamp", Instant.now().toString());
        send(ev);
    }

    public void publishPasswordChangedEvent(Long userId, String email, String telefono) {
        Map<String,Object> ev = new HashMap<>();
        ev.put("type", "PASSWORD_CHANGED");
        ev.put("userId", userId);
        ev.put("email", email);
        ev.put("telefono", telefono);
        ev.put("timestamp", Instant.now().toString());
        send(ev);
    }
}
