package co.edu.uniquindio.retos.utils;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import net.datafaker.Faker;
import java.util.HashMap;
import java.util.Map;
import static io.restassured.RestAssured.given;

public class TestContext {

    // 🔹 Base URL configurable por variable de entorno o propiedad de sistema
    private final String baseUrl = System.getProperty(
            "baseUrl",
            System.getenv().getOrDefault("BASE_URL", "http://localhost:8080/api")
    );

    private Response lastResponse;
    private String jwtToken;
    private final Faker faker = new Faker();

    public String getBaseUrl() {
        return baseUrl;
    }

    public Response getLastResponse() {
        return lastResponse;
    }

    public void setLastResponse(Response lastResponse) {
        this.lastResponse = lastResponse;
    }

    public String getJwtToken() {
        return jwtToken;
    }

    public void setJwtToken(String jwtToken) {
        this.jwtToken = jwtToken;
    }

    public Faker getFaker() {
        return faker;
    }

    // 🔹 Método útil para depurar respuestas
    public void logLastResponse() {
        if (lastResponse != null) {
            System.out.println("🔹 STATUS: " + lastResponse.getStatusCode());
            System.out.println("🔹 BODY:\n" + lastResponse.getBody().asPrettyString());
        } else {
            System.out.println("⚠️ No hay respuesta registrada aún.");
        }
    }

    // 🔹 Helpers para flujos de autenticación en pruebas E2E
    public void ensureUserExists(String nombre, String email, String password, String telefono) {
        Map<String, Object> body = new HashMap<>();
        body.put("nombre", nombre != null ? nombre : faker.name().fullName());
        body.put("email", email);
        body.put("password", password);
        if (telefono != null) body.put("telefono", telefono);

        try {
            given()
                    .contentType(ContentType.JSON)
                    .body(body)
                    .when()
                    .post(getBaseUrl() + "/usuarios")
                    .then()
                    .statusCode(org.hamcrest.Matchers.anyOf(org.hamcrest.Matchers.is(201), org.hamcrest.Matchers.is(409)));
        } catch (AssertionError ignored) {
            // Si no es 201/409, continuamos: el caso de prueba lo evidenciará más adelante
        }
    }

    public String obtainJwtToken(String email, String password) {
        Map<String, Object> login = new HashMap<>();
        login.put("email", email);
        login.put("password", password);
        Response res = given()
                .contentType(ContentType.JSON)
                .body(login)
                .when()
                .post(getBaseUrl() + "/auth/login");
        setLastResponse(res);
        String token = null;
        try {
            token = res.jsonPath().getString("token");
        } catch (Exception ignore) {
            // no-json or different shape
        }
        if (token == null || token.isBlank()) {
            try { token = res.jsonPath().getString("jwt"); } catch (Exception ignore) {}
        }
        if (token != null) setJwtToken(token);
        return token;
    }
}
