package co.edu.uniquindio.perfiles.health;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/health")
public class HealthController {

    private final StartupInfo startupInfo;

    public HealthController(StartupInfo startupInfo) {
        this.startupInfo = startupInfo;
    }

    private String getVersion() {
        // Try Implementation-Version from manifest (set by Spring Boot when packaged)
        String impl = HealthController.class.getPackage().getImplementationVersion();
        if (impl != null && !impl.isBlank()) return impl;
        // Fallback to environment/system property if provided
        String env = System.getProperty("app.version", System.getenv().getOrDefault("APP_VERSION", "dev"));
        return (env == null || env.isBlank()) ? "dev" : env;
    }

    private Map<String, Object> checkData(String status) {
        Map<String, Object> data = new HashMap<>();
        Instant from = startupInfo.getStartedAt();
        data.put("from", from.toString());
        data.put("status", status);
        data.put("version", getVersion());
        data.put("uptimeSeconds", startupInfo.getUptimeSeconds());
        return data;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> body = new HashMap<>();
        body.put("status", "UP");

        Map<String, Object> readiness = new HashMap<>();
        readiness.put("name", "Readiness check");
        readiness.put("status", "UP");
        readiness.put("data", checkData("READY"));

        Map<String, Object> liveness = new HashMap<>();
        liveness.put("name", "Liveness check");
        liveness.put("status", "UP");
        liveness.put("data", checkData("ALIVE"));

        body.put("checks", List.of(readiness, liveness));
        return ResponseEntity.ok(body);
    }

    @GetMapping("/ready")
    public ResponseEntity<Map<String, Object>> ready() {
        Map<String, Object> body = new HashMap<>();
        body.put("name", "Readiness check");
        body.put("status", "UP");
        body.put("data", checkData("READY"));
        return ResponseEntity.ok(body);
    }

    @GetMapping("/live")
    public ResponseEntity<Map<String, Object>> live() {
        Map<String, Object> body = new HashMap<>();
        body.put("name", "Liveness check");
        body.put("status", "UP");
        body.put("data", checkData("ALIVE"));
        return ResponseEntity.ok(body);
    }
}
