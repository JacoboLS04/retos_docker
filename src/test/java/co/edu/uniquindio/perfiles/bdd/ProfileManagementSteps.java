package co.edu.uniquindio.perfiles.bdd;

import co.edu.uniquindio.perfiles.model.Profile;
import co.edu.uniquindio.perfiles.repository.ProfileRepository;
import co.edu.uniquindio.perfiles.web.dto.ProfilePatchRequest;
import co.edu.uniquindio.perfiles.web.dto.ProfileRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class ProfileManagementSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestContext testContext;

    private Profile existingProfile;

    @Before
    public void setup() {
        // Clean database before each scenario
        profileRepository.deleteAll();
        testContext.reset();
        existingProfile = null;
    }

    // ========== STEPS FOR PROFILE MANAGEMENT ==========

    @Given("I am authenticated as user with ID {long}")
    public void iAmAuthenticatedAsUserWithId(Long userId) {
        testContext.setCurrentUserId(userId);
    }

    @When("I create a profile with nickname {string} and bio {string}")
    public void iCreateProfileWithNicknameAndBio(String nickname, String bio) throws Exception {
        ProfileRequest request = new ProfileRequest();
        request.setNickname(nickname);
        request.setBio(bio);
        request.setContactPublic(true);

        testContext.setResultActions(mockMvc.perform(post("/profile")
                .with(jwt().jwt(jwt -> jwt.claim("sub", String.valueOf(testContext.getCurrentUserId()))))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))));
    }

    @Then("the profile should be created successfully")
    public void theProfileShouldBeCreatedSuccessfully() throws Exception {
        testContext.getResultActions().andExpect(status().isCreated());
    }

    @Then("the profile should have nickname {string}")
    public void theProfileShouldHaveNickname(String expectedNickname) throws Exception {
        testContext.getResultActions().andExpect(jsonPath("$.nickname").value(expectedNickname));
    }

    @Then("the profile should have bio {string}")
    public void theProfileShouldHaveBio(String expectedBio) throws Exception {
        testContext.getResultActions().andExpect(jsonPath("$.bio").value(expectedBio));
    }

    @Given("I have an existing profile with nickname {string}")
    public void iHaveExistingProfileWithNickname(String nickname) {
        Profile profile = new Profile();
        profile.setUserId(testContext.getCurrentUserId());
        profile.setNickname(nickname);
        profile.setBio("Test bio");
        profile.setContactPublic(true);
        existingProfile = profileRepository.save(profile);
    }

    @When("I request to view my profile")
    public void iRequestToViewMyProfile() throws Exception {
        testContext.setResultActions(mockMvc.perform(get("/profile")
                .with(jwt().jwt(jwt -> jwt.claim("sub", String.valueOf(testContext.getCurrentUserId()))))));
    }

    @Then("I should receive my profile information")
    public void iShouldReceiveMyProfileInformation() throws Exception {
        testContext.getResultActions()
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists());
    }

    @Then("the profile should contain my nickname {string}")
    public void theProfileShouldContainMyNickname(String expectedNickname) throws Exception {
        testContext.getResultActions().andExpect(jsonPath("$.nickname").value(expectedNickname));
    }

    @Given("I have an existing profile with bio {string}")
    public void iHaveExistingProfileWithBio(String bio) {
        Profile profile = new Profile();
        profile.setUserId(testContext.getCurrentUserId());
        profile.setNickname("test_user");
        profile.setBio(bio);
        profile.setContactPublic(true);
        existingProfile = profileRepository.save(profile);
    }

    @When("I update my profile with new bio {string}")
    public void iUpdateMyProfileWithNewBio(String newBio) throws Exception {
        ProfileRequest request = new ProfileRequest();
        request.setNickname("test_user");
        request.setBio(newBio);
        request.setContactPublic(true);

        testContext.setResultActions(mockMvc.perform(put("/profile")
                .with(jwt().jwt(jwt -> jwt.claim("sub", String.valueOf(testContext.getCurrentUserId()))))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))));
    }

    @Then("the profile should be updated successfully")
    public void theProfileShouldBeUpdatedSuccessfully() throws Exception {
        testContext.getResultActions().andExpect(status().isOk());
    }

    @Then("the bio should be {string}")
    public void theBioShouldBe(String expectedBio) throws Exception {
        testContext.getResultActions().andExpect(jsonPath("$.bio").value(expectedBio));
    }

    @Given("I have an existing profile with country {string}")
    public void iHaveExistingProfileWithCountry(String country) {
        Profile profile = new Profile();
        profile.setUserId(testContext.getCurrentUserId());
        profile.setNickname("test_user");
        profile.setCountry(country);
        profile.setContactPublic(true);
        existingProfile = profileRepository.save(profile);
    }

    @When("I partially update only the country to {string}")
    public void iPartiallyUpdateOnlyTheCountryTo(String newCountry) throws Exception {
        ProfilePatchRequest request = new ProfilePatchRequest();
        request.setCountry(newCountry);

        testContext.setResultActions(mockMvc.perform(patch("/profile")
                .with(jwt().jwt(jwt -> jwt.claim("sub", String.valueOf(testContext.getCurrentUserId()))))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))));
    }

    @Then("the country should be {string}")
    public void theCountryShouldBe(String expectedCountry) throws Exception {
        testContext.getResultActions().andExpect(jsonPath("$.country").value(expectedCountry));
    }

    @When("I request to delete my profile")
    public void iRequestToDeleteMyProfile() throws Exception {
        testContext.setResultActions(mockMvc.perform(delete("/profile")
                .with(jwt().jwt(jwt -> jwt.claim("sub", String.valueOf(testContext.getCurrentUserId()))))));
    }

    @Then("the profile should be deleted successfully")
    public void theProfileShouldBeDeletedSuccessfully() throws Exception {
        testContext.getResultActions().andExpect(status().isNoContent());
    }

    @Then("the profile should no longer exist")
    public void theProfileShouldNoLongerExist() {
        Optional<Profile> found = profileRepository.findByUserId(testContext.getCurrentUserId());
        assertThat(found).isEmpty();
    }
}
