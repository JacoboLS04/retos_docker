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
        try {
            mensaje = res.jsonPath().getString("message");
        } catch (Exception ignore) {
            // El cuerpo puede no ser JSON; intentamos texto plano
            try { mensaje = res.getBody().asString(); } catch (Exception ignored) {}
        }
        assertThat("El mensaje de respuesta no coincide", mensaje, equalTo(mensajeEsperado));
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
