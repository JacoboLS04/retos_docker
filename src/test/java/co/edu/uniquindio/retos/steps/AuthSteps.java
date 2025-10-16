package co.edu.uniquindio.retos.steps;

import co.edu.uniquindio.retos.utils.TestContext;
import io.cucumber.java.en.*;
import io.restassured.response.Response;
import net.datafaker.Faker;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class AuthSteps {

    private final TestContext context;
    private final Map<String, Object> requestBody = new HashMap<>();
    private final Faker faker;

    public AuthSteps(TestContext context) {
        this.context = context;
        this.faker = context.getFaker();
    }

    // =========================================================
    // LOGIN
    // =========================================================
    @Given("tengo un usuario registrado con el correo {string} y contrasena {string}")
    public void tengoUnUsuarioRegistrado(String email, String password) {
        // Asegurar que el usuario exista en el backend antes de intentar login
        Map<String, Object> signup = new HashMap<>();
        signup.put("nombre", faker.name().fullName());
        signup.put("email", email.isEmpty() ? faker.internet().emailAddress() : email);
        signup.put("password", password);
        signup.put("telefono", faker.phoneNumber().cellPhone());

        try {
            given()
                    .contentType("application/json")
                    .body(signup)
                    .when()
                    .post(context.getBaseUrl() + "/usuarios")
                    .then()
                    .statusCode(anyOf(is(201), is(409))); // 409 si el correo ya existe
        } catch (AssertionError ignored) {
            // Si falla por otro motivo, seguimos para que el login reporte adecuadamente
        }

        requestBody.clear();
        requestBody.put("email", signup.get("email"));
        requestBody.put("password", password);
    }

    @When("^envio una peticion POST a /api/auth/login con esas credenciales$")
    public void envioPeticionLogin() {
        Response response = given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post(context.getBaseUrl() + "/auth/login");
        context.setLastResponse(response);
    }

    // Alias con acento en "petición"
    @When("^envío una petición POST a /api/auth/login con esas credenciales$")
    public void envioPeticionLoginConAcento() {
        envioPeticionLogin();
    }

    @Given("preparo la solicitud con correo {string} y contrasena {string}")
    public void prepararLoginConCredenciales(String email, String password) {
        requestBody.clear();
        requestBody.put("email", email);
        requestBody.put("password", password);
    }

    @When("^envio una peticion POST a /api/auth/login$")
    public void envioPeticionLoginGenerica() {
        Response response = given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post(context.getBaseUrl() + "/auth/login");
        context.setLastResponse(response);
    }

    @Given("preparo la solicitud con el correo {string}")
    public void prepararSolicitudConCorreo(String email) {
        requestBody.clear();
        requestBody.put("email", email);
    }

    // =========================================================
    // REQUEST PASSWORD RESET
    // =========================================================
    @Given("existe un usuario con el correo {string}")
    public void existeUsuarioConCorreo(String email) {
        // Asegurar existencia del usuario antes de solicitar reset
        Map<String, Object> signup = new HashMap<>();
        signup.put("nombre", faker.name().fullName());
        signup.put("email", email.isEmpty() ? faker.internet().emailAddress() : email);
        signup.put("password", "12345");
        signup.put("telefono", faker.phoneNumber().cellPhone());

        try {
            given()
                    .contentType("application/json")
                    .body(signup)
                    .when()
                    .post(context.getBaseUrl() + "/usuarios")
                    .then()
                    .statusCode(anyOf(is(201), is(409)));
        } catch (AssertionError ignored) { }

        requestBody.clear();
        requestBody.put("email", signup.get("email"));
    }

    @When("^envio una peticion POST a /api/auth/request-password-reset con ese correo$")
    public void envioPeticionRequestPasswordReset() {
        Response response = given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post(context.getBaseUrl() + "/auth/request-password-reset");
        context.setLastResponse(response);
    }

    // Alias sin "con ese correo"
    @When("^envio una peticion POST a /api/auth/request-password-reset$")
    public void envioPeticionRequestPasswordResetGenerica() {
        envioPeticionRequestPasswordReset();
    }

    @Then("la respuesta contiene un token temporal de restablecimiento")
    public void validarTokenTemporal() {
        assertThat(context.getLastResponse().jsonPath().getString("resetToken"), notNullValue());
    }

    @Then("un token temporal de restablecimiento")
    public void aliasTokenTemporal() {
        validarTokenTemporal();
    }

    // =========================================================
    // RESET PASSWORD
    // =========================================================
    @Given("tengo un token de restablecimiento valido {string}")
    public void tengoTokenValido(String token) {
        requestBody.clear();
        requestBody.put("token", token);
    }

    @And("un nuevo password {string}")
    public void nuevoPassword(String newPassword) {
        requestBody.put("newPassword", newPassword);
    }

    @When("^envio una peticion POST a /api/auth/reset-password con esos datos$")
    public void envioPeticionResetPassword() {
        Response response = given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post(context.getBaseUrl() + "/auth/reset-password");
        context.setLastResponse(response);
    }

    // Alias genérico sin "con esos datos"
    @When("^envio una peticion POST a /api/auth/reset-password$")
    public void envioPeticionResetPasswordGenerica() {
        envioPeticionResetPassword();
    }

    @Given("preparo la solicitud con token {string} y nueva contrasena {string}")
    public void prepararSolicitudResetNegativa(String token, String newPassword) {
        requestBody.clear();
        requestBody.put("token", token);
        requestBody.put("newPassword", newPassword);
    }

    

}
