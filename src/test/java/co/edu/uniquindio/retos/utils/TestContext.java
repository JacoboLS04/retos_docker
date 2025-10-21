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
    private String lastEmail;
    private String lastPassword;
    private Long lastUserId;

    public String getBaseUrl() {
        return baseUrl;
    }

    // Build a full URL from baseUrl and a step-provided path, avoiding duplicate "/api" and slashes
    public String url(String path) {
        String base = getBaseUrl();
        String p = path == null ? "" : path.trim();
        if (p.isEmpty()) return base;
        // If base ends with /api or /api/ and path starts with /api or /api/ then drop the leading /api from path
        boolean baseEndsWithApi = base.endsWith("/api") || base.endsWith("/api/");
        boolean pathStartsWithApi = p.equals("/api") || p.startsWith("/api/");
        if (baseEndsWithApi && pathStartsWithApi) {
            if (p.equals("/api")) {
                p = "/"; // points to api root
            } else {
                p = p.substring(4); // remove leading /api
            }
        }
        // Normalize slashes
        boolean baseEndsWithSlash = base.endsWith("/");
        boolean pathStartsWithSlash = p.startsWith("/");
        if (baseEndsWithSlash && pathStartsWithSlash) {
            return base + p.substring(1);
        } else if (!baseEndsWithSlash && !pathStartsWithSlash) {
            return base + "/" + p;
        } else {
            return base + p;
        }
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

    public String getLastEmail() { return lastEmail; }
    public void setLastEmail(String lastEmail) { this.lastEmail = lastEmail; }

    public String getLastPassword() { return lastPassword; }
    public void setLastPassword(String lastPassword) { this.lastPassword = lastPassword; }

    public Long getLastUserId() { return lastUserId; }
    public void setLastUserId(Long lastUserId) { this.lastUserId = lastUserId; }

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
                    .post(url("/usuarios"))
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
        .post(url("/auth/login"));
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
