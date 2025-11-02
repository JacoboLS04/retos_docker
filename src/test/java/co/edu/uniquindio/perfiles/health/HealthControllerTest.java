package co.edu.uniquindio.perfiles.health;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class HealthControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void healthRootIsAccessibleAndContainsChecks() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.checks[0].name").exists())
                .andExpect(jsonPath("$.checks[0].data.from").exists())
                .andExpect(jsonPath("$.checks[0].data.version").exists());
    }

    @Test
    void readinessEndpointOk() throws Exception {
        mockMvc.perform(get("/health/ready"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Readiness check"))
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.data.status").value("READY"));
    }

    @Test
    void livenessEndpointOk() throws Exception {
        mockMvc.perform(get("/health/live"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Liveness check"))
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.data.status").value("ALIVE"));
    }
}
