package co.edu.uniquindio.retos.steps;

import co.edu.uniquindio.retos.utils.TestContext;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import net.datafaker.Faker;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

public class UserSteps {

    private final TestContext context;
    private final Map<String, Object> body = new HashMap<>();
    private final Faker faker;

    public UserSteps(TestContext context) {
        this.context = context;
        this.faker = context.getFaker();
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
        body.put("telefono", "3121234567");
    }

    @When("^envio una peticion POST a /api/usuarios con mi informacion$")
    public void envioUnaPeticionPOSTAApiUsuariosConMiInformacion() {
        Response response = given()
                .contentType("application/json")
                .body(body)
                .when()
                .post(context.getBaseUrl() + "/usuarios");
        context.setLastResponse(response);
    }

    // =========================================================
    // CREAR USUARIO (caso con datos específicos)
    // =========================================================
    @Given("preparo una solicitud de creacion con los datos {string}, {string}, {string} y {string}")
    public void preparoSolicitudDeCreacionConDatos(String nombre, String email, String password, String telefono) {
        body.clear();
        if (nombre != null && !nombre.trim().isEmpty()) {
            body.put("nombre", faker.name().fullName());
        }
        if (email != null && !email.trim().isEmpty()) {
            body.put("email", faker.internet().emailAddress());
        }
        if (password != null && !password.trim().isEmpty()) {
            body.put("password", faker.internet().password(8, 12));
        }
        if (telefono != null && !telefono.trim().isEmpty()) {
            body.put("telefono", faker.phoneNumber().cellPhone());
        }
    }

    @When("envio una peticion POST a \\/api\\/usuarios")
    public void envioUnaPeticionCrearUsuario() {
        Response response = given()
                .contentType("application/json")
                .body(body)
                .when()
                .post(context.getBaseUrl() + "/usuarios");
        context.setLastResponse(response);
    }

    @Then("la respuesta contiene el nombre y el correo del usuario creado")
    public void validarUsuarioCreado() {
        Response res = context.getLastResponse();
        assertThat(res.jsonPath().getString("nombre"), notNullValue());
        assertThat(res.jsonPath().getString("email"), notNullValue());
    }

    // =========================================================
    // OBTENER USUARIO
    // =========================================================
    @Given("tengo un token JWT válido")
    public void tengoTokenJwtValido() {
        // Asegurar usuario y obtener JWT real del backend
        String email = "santiago@example.com";
        String pass = "12345";
        context.ensureUserExists(context.getFaker().name().fullName(), email, pass, "3112223344");
        context.obtainJwtToken(email, pass);
    }

    @When("envio una peticion GET a \\/api\\/usuarios\\/{int}")
    public void envioPeticionGetUsuarioPorId(int id) {
        var req = given();
        if (context.getJwtToken() != null && !context.getJwtToken().isBlank()) {
            req = req.header("Authorization", "Bearer " + context.getJwtToken());
        }
        Response response = req
                .when()
                .get(context.getBaseUrl() + "/usuarios/" + id);
        context.setLastResponse(response);
    }

    @Then("la respuesta contiene los datos del usuario solicitado")
    public void validarDatosUsuarioSolicitado() {
        Response res = context.getLastResponse();
        assertThat(res.jsonPath().getString("email"), notNullValue());
        assertThat(res.jsonPath().getString("nombre"), notNullValue());
    }

    // =========================================================
    // ACTUALIZAR USUARIO
    // =========================================================
    @Given("tengo un token JWT valido correspondiente al usuario con ID {int}")
    public void tengoTokenValidoCorrespondiente(int id) {
        String email = "user" + id + "@example.com";
        String pass = "12345";
        context.ensureUserExists("Usuario " + id, email, pass, "300000000" + id);
        context.obtainJwtToken(email, pass);
    }

    @Given("tengo un token JWT válido correspondiente al usuario con ID {int}")
    public void tengoTokenValidoCorrespondienteConAcento(int id) {
        tengoTokenValidoCorrespondiente(id);
    }

    @When("envio una petición PUT a \\/api\\/usuarios\\/{int} con un nuevo nombre y correo")
    public void envioPeticionActualizarUsuario(int id) {
        body.clear();
        body.put("nombre", faker.name().firstName());
        body.put("email", faker.internet().emailAddress());

        var req = given().contentType("application/json").body(body);
        if (context.getJwtToken() != null && !context.getJwtToken().isBlank()) {
            req = req.header("Authorization", "Bearer " + context.getJwtToken());
        }
        Response response = req
                .when()
                .put(context.getBaseUrl() + "/usuarios/" + id);
        context.setLastResponse(response);
    }

    // Alias sin acento
    @When("envio una peticion PUT a \\/api\\/usuarios\\/{int} con un nuevo nombre y correo")
    public void envioPeticionActualizarUsuarioSinAcento(int id) {
        envioPeticionActualizarUsuario(id);
    }

    // Paso para PUT sin cuerpo (escenarios negativos)
    @When("envio una petición PUT a \\/api\\/usuarios\\/{int}")
    public void envioPeticionPutSinCuerpo(int id) {
        var req = given();
        if (context.getJwtToken() != null && !context.getJwtToken().isBlank()) {
            req = req.header("Authorization", "Bearer " + context.getJwtToken());
        }
        Response response = req.when().put(context.getBaseUrl() + "/usuarios/" + id);
        context.setLastResponse(response);
    }

    // Alias sin acento para cubrir escenarios
    @When("envio una peticion PUT a \\/api\\/usuarios\\/{int}")
    public void envioPeticionPutSinCuerpoSinAcento(int id) {
        envioPeticionPutSinCuerpo(id);
    }

    @Then("la respuesta contiene los datos actualizados del usuario")
    public void validarDatosActualizadosUsuario() {
        Response res = context.getLastResponse();
        assertThat(res.jsonPath().getString("nombre"), notNullValue());
    }

    // =========================================================
    // ELIMINAR USUARIO
    // =========================================================
    @When("envio una peticion DELETE a \\/api\\/usuarios\\/{int}")
    public void envioPeticionDeleteUsuario(int id) {
        var req = given();
        if (context.getJwtToken() != null && !context.getJwtToken().isBlank()) {
            req = req.header("Authorization", "Bearer " + context.getJwtToken());
        }
        Response response = req
                .when()
                .delete(context.getBaseUrl() + "/usuarios/" + id);
        context.setLastResponse(response);
    }

    @Then("el usuario queda eliminado del sistema")
    public void usuarioEliminadoDelSistema() {
        context.getLastResponse().then().statusCode(204);
    }

    // =========================================================
    // LISTAR USUARIOS
    // =========================================================
    @When("envio una peticion GET a \\/api\\/usuarios?page={int}&size={int}")
    public void envioPeticionListarUsuarios(int page, int size) {
        var req = given();
        if (context.getJwtToken() != null && !context.getJwtToken().isBlank()) {
            req = req.header("Authorization", "Bearer " + context.getJwtToken());
        }
        Response response = req
                .when()
                .get(context.getBaseUrl() + "/usuarios?page=" + page + "&size=" + size);
        context.setLastResponse(response);
    }

    // Alias para escenarios con acentos (listado)
    @When("^envío una petición GET a /api/usuarios\\?page=0&size=5$")
    public void listadoUsuariosConAcentos() {
        envioPeticionListarUsuarios(0, 5);
    }

    @When("^envio una petición GET a /api/usuarios\\?page=0&size=5$")
    public void listadoUsuariosConAcentoEnPeticion() {
        envioPeticionListarUsuarios(0, 5);
    }

    @Then("la respuesta incluye una lista paginada de usuarios")
    public void validarListaUsuarios() {
        context.getLastResponse().then().body("data", notNullValue());
    }

    // =========================================================
    // PASOS DE APOYO EN OUTLINES
    // =========================================================
    @And("existe un usuario registrado con ID {int}")
    public void existeUnUsuarioRegistradoConID(int id) {
        // No-op: asumir que existe para propósitos de prueba de contrato
    }

    @Given("tengo el token {string}")
    public void tengoElToken(String jwt) {
        if (jwt == null || jwt.isBlank()) {
            context.setJwtToken(null);
        } else if ("valid_jwt".equalsIgnoreCase(jwt)) {
            context.setJwtToken("mocked_valid_jwt");
        } else {
            context.setJwtToken(jwt);
        }
    }

    @Given("tengo el token {string} del usuario con ID {string}")
    public void tengoElTokenDelUsuarioConID(String jwt, String userTokenId) {
        tengoElToken(jwt);
    }

    @And("el ID solicitado es {string}")
    public void elIDSolicitadoEs(String id) {
        // No-op
    }

    @And("deseo modificar el usuario con ID {string}")
    public void deseoModificarUsuarioConID(String id) {
        // No-op
    }

    @And("deseo eliminar el usuario con ID {string}")
    public void deseoEliminarUsuarioConID(String id) {
        // No-op
    }
}
