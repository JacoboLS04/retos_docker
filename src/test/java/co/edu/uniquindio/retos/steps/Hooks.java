package co.edu.uniquindio.retos.steps;

import co.edu.uniquindio.retos.utils.TestContext;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;

public class Hooks {

    private final TestContext context;

    public Hooks(TestContext context) {
        this.context = context;
    }

    @Before
    public void beforeEach(Scenario scenario) {
        // Siempre loguear request/response cuando falle una validación
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        // Si se activa -Dhttp.debug=true, loguear TODO el tráfico HTTP
        if (Boolean.getBoolean("http.debug")) {
            RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
            System.out.println("[HTTP DEBUG] Logging de request/response habilitado para: " + scenario.getName());
        }
    }

    @After
    public void afterEach(Scenario scenario) {
        if (scenario.isFailed()) {
            System.out.println("===== Scenario FAILED: " + scenario.getName() + " =====");
            context.logLastResponse();
            System.out.println("==============================================");
        }
    }
}
