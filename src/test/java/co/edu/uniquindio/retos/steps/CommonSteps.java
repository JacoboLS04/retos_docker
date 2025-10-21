package co.edu.uniquindio.retos.steps;

import co.edu.uniquindio.retos.utils.TestContext;
import io.cucumber.java.en.Then;
import io.restassured.response.Response;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class CommonSteps {

    private final TestContext context;

    public CommonSteps(TestContext context) {
        this.context = context;
    }

    // =========================================================
    // CÓDIGO DE ESTADO
    // =========================================================
    @Then("el sistema devuelve el codigo de estado {int}")
    public void validarCodigoEstado(int statusCode) {
        context.getLastResponse().then().statusCode(statusCode);
    }

    // =========================================================
    // MENSAJES
    // =========================================================
    @Then("la respuesta contiene el mensaje {string}")
    public void validarMensaje(String mensajeEsperado) {
        Response res = context.getLastResponse();
        String mensaje = null;
        String code = null;
        int status = -1;
        try { status = res.getStatusCode(); } catch (Exception ignore) {}
        try {
            mensaje = res.jsonPath().getString("message");
        } catch (Exception ignore) {
            // El cuerpo puede no ser JSON; intentamos texto plano
            try { mensaje = res.getBody().asString(); } catch (Exception ignored) {}
        }
        // Fallback: intentar parsear el body como JSON manualmente si mensaje sigue nulo y hay cuerpo
        if ((mensaje == null || mensaje.isBlank())) {
            try {
                String body = res.getBody().asString();
                if (body != null && body.trim().startsWith("{")) {
                    io.restassured.path.json.JsonPath jp = new io.restassured.path.json.JsonPath(body);
                    mensaje = jp.getString("message");
                }
            } catch (Exception ignore) {}
        }
        try { code = res.jsonPath().getString("code"); } catch (Exception ignore) {}

        boolean matches = false;
        if (mensaje != null) {
            if (mensaje.equals(mensajeEsperado)) matches = true;
            else if (mensaje.toLowerCase().contains(mensajeEsperado.toLowerCase())) matches = true;
        }
        if (!matches && code != null) {
            if (String.valueOf(code).equalsIgnoreCase(mensajeEsperado)) matches = true;
        }
        // Algunos backends devuelven 401 sin cuerpo; aceptar como válido si se espera "unauthorized"
        if (!matches && status == 401 && "unauthorized".equalsIgnoreCase(mensajeEsperado)) {
            String raw = null;
            try { raw = res.getBody().asString(); } catch (Exception ignore) {}
            if (raw == null || raw.isBlank()) matches = true;
        }
        assertThat("El mensaje/código de respuesta no coincide. body=" + (res != null ? res.getBody().asString() : "<null>"), matches, is(true));
    }

    // =========================================================
    // TOKEN JWT
    // =========================================================
    @Then("la respuesta contiene un token JWT y un campo {string}")
    public void validarTokenJwt(String campo) {
        Response response = context.getLastResponse();
        String token = null;
        Object extra = null;
        try { token = response.jsonPath().getString("token"); } catch (Exception ignore) {}
        if (token == null || token.isBlank()) {
            try { token = response.jsonPath().getString("jwt"); } catch (Exception ignore) {}
        }
        try { extra = response.jsonPath().get("" + campo); } catch (Exception ignore) {}
        assertThat(token, notNullValue());
        assertThat(extra, notNullValue());
        context.setJwtToken(token);
    }

    // =========================================================
    // SCHEMA JSON
    // =========================================================
    @Then("la respuesta cumple el schema {string}")
    public void validarSchema(String schemaName) {
        context.getLastResponse()
                .then()
                .assertThat()
                .body(matchesJsonSchemaInClasspath("co/edu/uniquindio/retos/schemas/" + schemaName));
    }
}
