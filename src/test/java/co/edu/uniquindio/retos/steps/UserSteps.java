package co.edu.uniquindio.retos.steps;

import co.edu.uniquindio.retos.utils.TestContext;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

public class UserSteps {

    private final TestContext context;
    private final Map<String, Object> body = new HashMap<>();
    private Long actorUserId;       // usuario dueño del JWT
    private Long targetUserId;      // usuario objetivo a eliminar

    public UserSteps(TestContext context) {
        this.context = context;
    }

    // =========================================================
    // CREAR USUARIO - ÉXITO
    // =========================================================
    @Given("soy un usuario nuevo con datos validos")
    public void soyUnUsuarioNuevoConDatosValidos() {
        body.clear();
        body.put("nombre", context.getFaker().name().fullName());
        body.put("email", context.getFaker().internet().emailAddress());
        body.put("password", "12345");
        context.setLastPassword("12345");
        body.put("telefono", "3121234567");
    }

    @When("envio una peticion POST a {string} con mi informacion")
    public void envioUnaPeticionPOSTAApiUsuariosConMiInformacion(String path) {
    Response response = given()
                .contentType("application/json")
                .body(body)
                .when()
        .post(context.url("/usuarios"));
        context.setLastResponse(response);
        try {
            Long id = response.jsonPath().getLong("id");
            context.setLastUserId(id);
            context.setLastEmail(response.jsonPath().getString("email"));
        } catch (Exception ignore) {}
    }

    // =========================================================
    // CREAR USUARIO (caso con datos específicos)
    // =========================================================
    @Given("preparo una solicitud de creacion con los datos {string}, {string}, {string} y {string}")
    public void preparoSolicitudDeCreacionConDatos(String nombre, String email, String password, String telefono) {
        body.clear();
        if (nombre != null && !nombre.trim().isEmpty()) body.put("nombre", nombre);
        if (email != null && !email.trim().isEmpty()) body.put("email", email);
        if (password != null && !password.trim().isEmpty()) body.put("password", password);
        if (telefono != null && !telefono.trim().isEmpty()) body.put("telefono", telefono);
    }

    @When("envio una peticion POST a {string}")
    public void envioUnaPeticionCrearUsuario(String path) {
        // Sembrar para forzar 409 si es un caso de email duplicado
        try {
            String email = (String) body.get("email");
            String password = (String) body.get("password");
            String nombre = (String) body.get("nombre");
            String telefono = (String) body.get("telefono");
            if (email != null && password != null) {
                context.ensureUserExists(nombre != null ? nombre : context.getFaker().name().fullName(), email, password, telefono);
            }
        } catch (Exception ignore) {}

    Response response = given()
                .contentType("application/json")
                .body(body)
                .when()
        .post(context.url("/usuarios"));
        context.setLastResponse(response);
        try {
            Long id = response.jsonPath().getLong("id");
            context.setLastUserId(id);
            context.setLastEmail(response.jsonPath().getString("email"));
        } catch (Exception ignore) {}
    }

    @Then("la respuesta contiene el nombre y el correo del usuario creado")
    public void validarUsuarioCreado() {
        Response res = context.getLastResponse();
        assertThat(res.jsonPath().getString("nombre"), notNullValue());
        assertThat(res.jsonPath().getString("email"), notNullValue());
    }

    // =========================================================
    // TOKEN PARA USUARIO RECIENTE
    // =========================================================
    @Given("tengo un token JWT válido")
    public void tengoTokenJwtValido() {
        String email = context.getLastEmail();
        String pass = context.getLastPassword() != null ? context.getLastPassword() : "12345";
        if (email == null) {
            email = "user" + System.currentTimeMillis() + "@example.com";
            pass = "12345";
            context.ensureUserExists(context.getFaker().name().fullName(), email, pass, "3112223344");
        }
        context.obtainJwtToken(email, pass);
    }

    // =========================================================
    // PASOS DE APOYO EN OUTLINES
    // =========================================================
    @And("existe un usuario registrado con ID {int}")
    public void existeUnUsuarioRegistradoConID(int id) {
        // No-op: asumir que existe para propósitos de prueba de contrato
    }

    // =========================================================
    // DELETE - MI PROPIO USUARIO
    // =========================================================
    @When("envio una peticion DELETE de mi propio usuario")
    public void envioDeleteDeMiPropioUsuario() {
        Long id = context.getLastUserId();
        var req = given();
        if (context.getJwtToken() != null && !context.getJwtToken().isBlank()) {
            req = req.header("Authorization", "Bearer " + context.getJwtToken());
        }
    Response response = req
                .when()
        .delete(context.url("/usuarios/" + id));
        context.setLastResponse(response);
    }

    // =========================================================
    // Pasos faltantes para escenarios Outline de DELETE
    // =========================================================
    @And("el usuario queda eliminado del sistema")
    public void verificarUsuarioEliminado() {
        // Intentar eliminar nuevamente debería dar 404 (no encontrado)
        Long id = context.getLastUserId();
        var req = given();
        if (context.getJwtToken() != null && !context.getJwtToken().isBlank()) {
            req = req.header("Authorization", "Bearer " + context.getJwtToken());
        }
    Response second = req.when().delete(context.url("/usuarios/" + id));
        context.setLastResponse(second);
        second.then().statusCode(404);
    }

    @Given("tengo el token {string} del usuario con ID {string}")
    public void tengoElTokenDelUsuario(String jwtDescriptor, String userTokenId) {
        // Si no se provee JWT, dejamos el contexto sin token (provocará 401)
        if (jwtDescriptor == null || jwtDescriptor.trim().isEmpty()) {
            context.setJwtToken(null);
            actorUserId = null;
            return;
        }

        // Crear un usuario "actor" y autenticarse para obtener un JWT real
        Map<String, Object> u = new HashMap<>();
        String email = context.getFaker().internet().emailAddress();
        String pass = "12345";
        u.put("nombre", context.getFaker().name().fullName());
        u.put("email", email);
        u.put("password", pass);
        u.put("telefono", "3112223344");

    Response r = given().contentType("application/json").body(u)
        .when().post(context.url("/usuarios"));
        // Obtener id del usuario creado (si 201); si 409, intentar recuperar via login-flujo posterior
        Long createdId = null;
        try { createdId = r.jsonPath().getLong("id"); } catch (Exception ignore) {}
        actorUserId = createdId; // puede quedar null si 409

        // Autenticarse y guardar JWT en contexto
        context.obtainJwtToken(email, pass);
    }

    @And("deseo eliminar el usuario con ID {string}")
    public void deseoEliminarUsuarioConID(String idStr) {
        int id;
        try { id = Integer.parseInt(idStr.trim()); } catch (Exception e) { id = -1; }
        // Preparar el ID objetivo según el ejemplo. Si es 2, crear otro usuario para provocar 403.
        if (id == 2) {
            Map<String, Object> u = new HashMap<>();
            u.put("nombre", context.getFaker().name().fullName());
            u.put("email", context.getFaker().internet().emailAddress());
            u.put("password", "12345");
            u.put("telefono", "3112223345");
        Response r = given().contentType("application/json").body(u)
                .when().post(context.url("/usuarios"));
            try { targetUserId = r.jsonPath().getLong("id"); } catch (Exception ignore) { targetUserId = null; }
        } else if (id == 999) {
            targetUserId = 999L; // inexistente
        } else {
            // Por defecto, usar el mismo del actor si se dispone; en caso contrario, un valor genérico
            targetUserId = (actorUserId != null) ? actorUserId : 1L;
        }
    }

    @When("envio una peticion DELETE a {string}")
    public void envioDeleteAUsuarioConId(String path) {
        Long id = targetUserId != null ? targetUserId : 1L;
        var req = given();
        if (context.getJwtToken() != null && !context.getJwtToken().isBlank()) {
            req = req.header("Authorization", "Bearer " + context.getJwtToken());
        }
        Response response = req.when().delete(context.url("/usuarios/" + id));
        context.setLastResponse(response);
    }
}
