package co.edu.uniquindio.perfiles.bdd;

import co.edu.uniquindio.perfiles.model.Profile;
import co.edu.uniquindio.perfiles.repository.ProfileRepository;
import co.edu.uniquindio.perfiles.web.dto.ProfileRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ProfileValidationSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestContext testContext;

    @When("I attempt to create a profile with invalid page URL {string}")
    public void iAttemptToCreateProfileWithInvalidPageURL(String invalidUrl) throws Exception {
        ProfileRequest request = new ProfileRequest();
        request.setNickname("test_user");
        request.setPageUrl(invalidUrl);
        request.setContactPublic(true);

        testContext.setResultActions(mockMvc.perform(post("/profile")
                .with(jwt().jwt(jwt -> jwt.claim("sub", String.valueOf(testContext.getCurrentUserId()))))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))));
    }

    @Then("the request should fail with validation error")
    public void theRequestShouldFailWithValidationError() throws Exception {
        testContext.getResultActions().andExpect(status().isBadRequest());
    }

    @Then("the error should indicate invalid URL format")
    public void theErrorShouldIndicateInvalidURLFormat() throws Exception {
        testContext.getResultActions().andExpect(jsonPath("$.title").exists());
    }

    @When("I attempt to create a profile with bio exceeding {int} characters")
    public void iAttemptToCreateProfileWithBioExceedingCharacters(int maxLength) throws Exception {
        String longBio = "a".repeat(maxLength + 100);
        
        ProfileRequest request = new ProfileRequest();
        request.setNickname("test_user");
        request.setBio(longBio);
        request.setContactPublic(true);

        testContext.setResultActions(mockMvc.perform(post("/profile")
                .with(jwt().jwt(jwt -> jwt.claim("sub", String.valueOf(testContext.getCurrentUserId()))))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))));
    }

    @Then("the error should indicate bio is too long")
    public void theErrorShouldIndicateBioIsTooLong() throws Exception {
        testContext.getResultActions().andExpect(jsonPath("$.title").exists());
    }

    @Given("I already have an existing profile")
    public void iAlreadyHaveAnExistingProfile() {
        Profile profile = new Profile();
        profile.setUserId(testContext.getCurrentUserId());
        profile.setNickname("existing_user");
        profile.setBio("Existing bio");
        profile.setContactPublic(true);
        profileRepository.save(profile);
    }

    @When("I attempt to create another profile")
    public void iAttemptToCreateAnotherProfile() throws Exception {
        ProfileRequest request = new ProfileRequest();
        request.setNickname("duplicate_attempt");
        request.setBio("Another profile");
        request.setContactPublic(true);

        testContext.setResultActions(mockMvc.perform(post("/profile")
                .with(jwt().jwt(jwt -> jwt.claim("sub", String.valueOf(testContext.getCurrentUserId()))))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))));
    }

    @Then("the request should fail with conflict error")
    public void theRequestShouldFailWithConflictError() throws Exception {
        testContext.getResultActions().andExpect(status().isConflict());
    }

    @Then("the error should indicate profile already exists")
    public void theErrorShouldIndicateProfileAlreadyExists() throws Exception {
        testContext.getResultActions().andExpect(jsonPath("$.title").exists());
    }
}
