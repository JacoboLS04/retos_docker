package co.edu.uniquindio.perfiles.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import co.edu.uniquindio.perfiles.web.dto.ProfileRequest;
import co.edu.uniquindio.perfiles.web.dto.ProfilePatchRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ProfileControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void unauthorizedWithoutToken() throws Exception {
        mockMvc.perform(get("/profile"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createAndGetProfile() throws Exception {
        ProfileRequest req = new ProfileRequest();
        req.setNickname("jdoe");
        req.setContactPublic(true);
        String body = objectMapper.writeValueAsString(req);

        // create
        mockMvc.perform(post("/profile")
                        .with(jwt().jwt(jwt -> jwt.subject("1")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nickname").value("jdoe"))
                .andExpect(jsonPath("$.contactPublic").value(true));

        // get
        mockMvc.perform(get("/profile").with(jwt().jwt(jwt -> jwt.subject("1"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value("jdoe"));
    }

    @Test
    void putReplaceCreatesAndUpdatesProfile() throws Exception {
    // Use different subject to isolate test data
    String sub = "2";

    // First PUT should create if not exists
    ProfileRequest createReq = new ProfileRequest();
    createReq.setNickname("first");
    createReq.setContactPublic(false);
    String createBody = objectMapper.writeValueAsString(createReq);

    mockMvc.perform(put("/profile")
            .with(jwt().jwt(jwt -> jwt.subject(sub)))
            .contentType(MediaType.APPLICATION_JSON)
            .content(createBody))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.nickname").value("first"))
        .andExpect(jsonPath("$.contactPublic").value(false));

    // Second PUT should update existing
    ProfileRequest updateReq = new ProfileRequest();
    updateReq.setNickname("second");
    updateReq.setContactPublic(true);
    String updateBody = objectMapper.writeValueAsString(updateReq);

    mockMvc.perform(put("/profile")
            .with(jwt().jwt(jwt -> jwt.subject(sub)))
            .contentType(MediaType.APPLICATION_JSON)
            .content(updateBody))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.nickname").value("second"))
        .andExpect(jsonPath("$.contactPublic").value(true));

    // GET should reflect updated values
    mockMvc.perform(get("/profile").with(jwt().jwt(jwt -> jwt.subject(sub))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.nickname").value("second"))
        .andExpect(jsonPath("$.contactPublic").value(true));
    }

    @Test
    void patchUpdatesPartialFields() throws Exception {
    String sub = "3";

    // Seed with POST
    ProfileRequest seed = new ProfileRequest();
    seed.setNickname("nick");
    seed.setBio("initial");
    String seedBody = objectMapper.writeValueAsString(seed);

    mockMvc.perform(post("/profile")
            .with(jwt().jwt(jwt -> jwt.subject(sub)))
            .contentType(MediaType.APPLICATION_JSON)
            .content(seedBody))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.bio").value("initial"));

    // Patch only bio
    ProfilePatchRequest patch = new ProfilePatchRequest();
    patch.setBio("patched");
    String patchBody = objectMapper.writeValueAsString(patch);

    mockMvc.perform(patch("/profile")
            .with(jwt().jwt(jwt -> jwt.subject(sub)))
            .contentType(MediaType.APPLICATION_JSON)
            .content(patchBody))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.bio").value("patched"))
        .andExpect(jsonPath("$.nickname").value("nick"));
    }

    @Test
    void deleteRemovesProfile() throws Exception {
    String sub = "4";

    // Seed with POST
    ProfileRequest seed = new ProfileRequest();
    seed.setNickname("to-delete");
    String seedBody = objectMapper.writeValueAsString(seed);

    mockMvc.perform(post("/profile")
            .with(jwt().jwt(jwt -> jwt.subject(sub)))
            .contentType(MediaType.APPLICATION_JSON)
            .content(seedBody))
        .andExpect(status().isCreated());

    // DELETE should return 204
    mockMvc.perform(delete("/profile")
            .with(jwt().jwt(jwt -> jwt.subject(sub))))
        .andExpect(status().isNoContent());

    // Optional: GET should now be 404 (not strictly happy path of GET, but validates delete effect)
    mockMvc.perform(get("/profile").with(jwt().jwt(jwt -> jwt.subject(sub))))
        .andExpect(status().isNotFound());
    }
}
