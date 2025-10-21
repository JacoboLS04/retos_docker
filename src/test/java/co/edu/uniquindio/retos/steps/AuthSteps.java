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
        String useEmail = email.isEmpty() ? faker.internet().emailAddress() : email;
        signup.put("nombre", faker.name().fullName());
        signup.put("email", useEmail);
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

        context.setLastEmail(useEmail);
        context.setLastPassword(password);

        requestBody.clear();
        requestBody.put("email", useEmail);
        requestBody.put("password", password);
    }

    @When("envio una peticion POST a {string} con esas credenciales")
    public void envioPeticionLogin(String path) {
    Response response = given()
                .contentType("application/json")
                .body(requestBody)
                .when()
        .post(context.url(path));
        context.setLastResponse(response);
    }

    // Alias con acento en "petición"
    @When("envío una petición POST a {string} con esas credenciales")
    public void envioPeticionLoginConAcento(String path) {
        envioPeticionLogin(path);
    }

    @Given("preparo la solicitud con correo {string} y contrasena {string}")
    public void prepararLoginConCredenciales(String email, String password) {
        requestBody.clear();
        requestBody.put("email", email);
        requestBody.put("password", password);
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
        String emailToUse = email.isEmpty() ? faker.internet().emailAddress() : email;
        signup.put("email", emailToUse);
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

        // Obtener un JWT para este usuario si el backend requiere autorización para el endpoint
        try {
            String token = context.obtainJwtToken(emailToUse, "12345");
            if (token == null || token.isBlank()) {
                // Fallback: crear un usuario temporal para autenticarse y obtener un JWT válido
                Map<String, Object> tmp = new HashMap<>();
                tmp.put("nombre", faker.name().fullName());
                tmp.put("email", faker.internet().emailAddress());
                tmp.put("password", "12345");
                tmp.put("telefono", faker.phoneNumber().cellPhone());
                try { given().contentType("application/json").body(tmp).when().post(context.url("/usuarios")); } catch (Exception ignore) {}
                context.obtainJwtToken((String) tmp.get("email"), "12345");
            }
        } catch (Exception ignore) { }
    }

    @When("envio una peticion POST a {string} con ese correo")
    public void envioPeticionRequestPasswordReset(String path) {
        var req = given().contentType("application/json").body(requestBody);
        if (context.getJwtToken() != null && !context.getJwtToken().isBlank()) {
            req = req.header("Authorization", "Bearer " + context.getJwtToken());
        }
        Response response = req.when().post(context.url(path));
        context.setLastResponse(response);
    }

    @Then("la respuesta contiene un token temporal de restablecimiento")
    public void validarTokenTemporal() {
        assertThat(context.getLastResponse().jsonPath().getString("resetToken"), notNullValue());
    }

    @Then("un token temporal de restablecimiento")
    public void aliasTokenTemporal() {
        validarTokenTemporal();
    }

    @And("guardo el token de restablecimiento de la respuesta")
    public void guardoResetTokenDeRespuesta() {
        String resetToken = null;
        try { resetToken = context.getLastResponse().jsonPath().getString("resetToken"); } catch (Exception ignore) {}
        assertThat("No se encontró resetToken en la respuesta", resetToken, notNullValue());
        requestBody.put("token", resetToken);
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

    @When("envio una peticion POST a {string} con esos datos")
    public void envioPeticionResetPassword(String path) {
        var req = given().contentType("application/json").body(requestBody);
        if (context.getJwtToken() != null && !context.getJwtToken().isBlank()) {
            req = req.header("Authorization", "Bearer " + context.getJwtToken());
        }
        Response response = req.when().post(context.url(path));
        context.setLastResponse(response);
    }

    @Given("preparo la solicitud con token {string} y nueva contrasena {string}")
    public void prepararSolicitudResetNegativa(String token, String newPassword) {
        requestBody.clear();
        requestBody.put("token", token);
        requestBody.put("newPassword", newPassword);
    }

    @Given("tengo un usuario aleatorio con contrasena {string}")
    public void tengoUsuarioAleatorioConContrasena(String password) {
        String email = faker.internet().emailAddress();
        Map<String, Object> signup = new HashMap<>();
        signup.put("nombre", faker.name().fullName());
        signup.put("email", email);
        signup.put("password", password);
        signup.put("telefono", faker.phoneNumber().cellPhone());

        try {
            given().contentType("application/json").body(signup)
                    .when().post(context.getBaseUrl() + "/usuarios")
                    .then().statusCode(anyOf(is(201), is(409)));
        } catch (AssertionError ignored) {}

        context.setLastEmail(email);
        context.setLastPassword(password);
        requestBody.clear();
        requestBody.put("email", email);
        requestBody.put("password", password);
    }

}
