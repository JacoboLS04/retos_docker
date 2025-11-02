package co.edu.uniquindio.perfiles.health;

import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.time.Instant;
import java.time.Duration;

@Component
public class StartupInfo {
    private Instant startedAt;

    @PostConstruct
    public void init() {
        this.startedAt = Instant.now();
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public long getUptimeSeconds() {
        return Duration.between(startedAt, Instant.now()).getSeconds();
    }
}
