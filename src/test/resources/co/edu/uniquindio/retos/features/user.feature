Feature: Gestion de usuarios
  Como usuario del sistema
  Quiero poder registrar, obtener, actualizar, listar y eliminar usuarios
  Para administrar correctamente la información de los mismos

  # =========================================================
  # CREAR USUARIO (POST /api/usuarios)
  # =========================================================

  Scenario: Registrar un nuevo usuario exitosamente
    Given soy un usuario nuevo con datos validos
    When envio una peticion POST a "/api/usuarios" con mi informacion
    Then el sistema devuelve el codigo de estado 201
    And la respuesta contiene el nombre y el correo del usuario creado

  Scenario Outline: Intentar registrar usuario con datos invalidos o duplicados
    Given preparo una solicitud de creacion con los datos "<nombre>", "<email>", "<password>" y "<telefono>"
  When envio una peticion POST a "/api/usuarios"
    Then el sistema devuelve el codigo de estado <status>
    And la respuesta contiene el mensaje "<mensaje>"

    Examples:
      | nombre            | email                   | password | telefono   | status | mensaje         |
      |                 | juan@example.com      | 12345  | 3121112233 | 400 | missing_fields |
      | Carlos Perez    | santiago@example.com  | 12345  | 3114445566 | 409 | email_exists   |



  # =========================================================
  # ELIMINAR USUARIO (DELETE /api/usuarios/{id})
  # =========================================================

  @delete
  Scenario: Eliminar mi cuenta exitosamente
    Given soy un usuario nuevo con datos validos
    When envio una peticion POST a "/api/usuarios" con mi informacion
    And tengo un token JWT válido
    When envio una peticion DELETE de mi propio usuario
    Then el sistema devuelve el codigo de estado 204
    And el usuario queda eliminado del sistema

  Scenario Outline: Intentar eliminar usuario sin permisos o inexistente
    Given tengo el token "<jwt>" del usuario con ID "<userTokenId>"
    And deseo eliminar el usuario con ID "<id>"
  When envio una peticion DELETE a "/api/usuarios/{int}"
    Then el sistema devuelve el codigo de estado <status>
    And la respuesta contiene el mensaje "<mensaje>"

    Examples:
      | jwt         | userTokenId | id | status | mensaje          |
      |             | 1           | 1  | 401    | unauthorized     |
      | valid_jwt   | 1           | 2  | 403    | forbidden        |
      | valid_jwt   | 1           | 999| 404    | user_not_found   |

  # (Escenarios GET/PUT/LIST eliminados al no estar expuestos en el backend)
