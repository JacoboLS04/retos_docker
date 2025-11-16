package com.example.servidor_web_spring.bdd;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class GreetingSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestContext testContext;

    @Before
    public void setup() {
        testContext.reset();
    }

    // ========== STEPS FOR GREETING AUTHENTICATION ==========

    @Given("I am authenticated as user {string}")
    public void iAmAuthenticatedAsUser(String username) {
        testContext.setCurrentUsername(username);
        testContext.setAuthenticated(true);
    }

    @When("I request a greeting with name {string}")
    public void iRequestGreetingWithName(String name) throws Exception {
        testContext.setResultActions(
            mockMvc.perform(get("/saludo")
                .param("nombre", name)
                .with(jwt().jwt(jwt -> jwt.claim("preferred_username", testContext.getCurrentUsername()))))
        );
    }

    @Then("I should receive a successful greeting response")
    public void iShouldReceiveSuccessfulGreetingResponse() throws Exception {
        testContext.getResultActions().andExpect(status().isOk());
    }

    @Then("the greeting should say {string}")
    public void theGreetingShouldSay(String expectedMessage) throws Exception {
        testContext.getResultActions().andExpect(content().string(expectedMessage));
    }

    @Then("the request should be forbidden")
    public void theRequestShouldBeForbidden() throws Exception {
        testContext.getResultActions().andExpect(status().isForbidden());
    }

    @Then("the error should indicate name mismatch with token")
    public void theErrorShouldIndicateNameMismatchWithToken() throws Exception {
        testContext.getResultActions().andExpect(content().string("El nombre no coincide con el del token"));
    }

    @When("I request a greeting without providing a name")
    public void iRequestGreetingWithoutProvidingName() throws Exception {
        testContext.setResultActions(
            mockMvc.perform(get("/saludo")
                .with(jwt().jwt(jwt -> jwt.claim("preferred_username", testContext.getCurrentUsername()))))
        );
    }

    @Then("the request should fail with bad request error")
    public void theRequestShouldFailWithBadRequestError() throws Exception {
        testContext.getResultActions().andExpect(status().isBadRequest());
    }

    @Then("the error should indicate name is required")
    public void theErrorShouldIndicateNameIsRequired() throws Exception {
        testContext.getResultActions().andExpect(content().string("Solicitud no valida: El nombre es obligatorio"));
    }

    @Given("I am not authenticated")
    public void iAmNotAuthenticated() {
        testContext.setAuthenticated(false);
    }

    @When("I attempt to request a greeting")
    public void iAttemptToRequestGreeting() throws Exception {
        testContext.setResultActions(
            mockMvc.perform(get("/saludo").param("nombre", "test"))
        );
    }

    @Then("the request should be unauthorized")
    public void theRequestShouldBeUnauthorized() throws Exception {
        testContext.getResultActions().andExpect(status().isUnauthorized());
    }

    // ========== STEPS FOR RESOURCE NOT FOUND ==========

    @When("I access an unknown endpoint {string}")
    public void iAccessUnknownEndpoint(String path) throws Exception {
        testContext.setResultActions(
            mockMvc.perform(get(path)
                .with(jwt().jwt(jwt -> jwt.claim("preferred_username", testContext.getCurrentUsername()))))
        );
    }

    @Then("the response should indicate resource not found")
    public void theResponseShouldIndicateResourceNotFound() throws Exception {
        testContext.getResultActions().andExpect(content().string("Recurso no encontrado"));
    }

    @Then("the status should be {int}")
    public void theStatusShouldBe(int expectedStatus) throws Exception {
        testContext.getResultActions().andExpect(status().is(expectedStatus));
    }

    @When("I access the root path {string}")
    public void iAccessTheRootPath(String path) throws Exception {
        testContext.setResultActions(
            mockMvc.perform(get(path)
                .with(jwt().jwt(jwt -> jwt.claim("preferred_username", testContext.getCurrentUsername()))))
        );
    }
}
