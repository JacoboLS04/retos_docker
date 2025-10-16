Feature: Gestion de usuarios
  Como usuario del sistema
  Quiero poder registrar, obtener, actualizar, listar y eliminar usuarios
  Para administrar correctamente la información de los mismos

  # =========================================================
  # CREAR USUARIO (POST /api/usuarios)
  # =========================================================

  Scenario: Registrar un nuevo usuario exitosamente
    Given soy un usuario nuevo con datos validos
    When envio una peticion POST a /api/usuarios con mi informacion
    Then el sistema devuelve el codigo de estado 201
    And la respuesta contiene el nombre y el correo del usuario creado

  Scenario Outline: Intentar registrar usuario con datos invalidos o duplicados
    Given preparo una solicitud de creacion con los datos "<nombre>", "<email>", "<password>" y "<telefono>"
    When envio una peticion POST a /api/usuarios
    Then el sistema devuelve el codigo de estado <status>
    And la respuesta contiene el mensaje "<mensaje>"

    Examples:
      | nombre            | email                   | password | telefono   | status | mensaje         |
      |                 | juan@example.com      | 12345  | 3121112233 | 400 | missing_fields |
      | Carlos Perez    | santiago@example.com  | 12345  | 3114445566 | 409 | email_exists   |

  # =========================================================
  # OBTENER USUARIO (GET /api/usuarios/{id})
  # =========================================================

  Scenario: Consultar un usuario existente por ID
    Given tengo un token JWT válido
    And existe un usuario registrado con ID 1
    When envio una peticion GET a /api/usuarios/1
    Then el sistema devuelve el codigo de estado 200
    And la respuesta contiene los datos del usuario solicitado

  Scenario Outline: Consultar usuario sin autorización o inexistente
    Given tengo el token "<jwt>"
    And el ID solicitado es "<id>"
  When envio una peticion GET a /api/usuarios/<id>
    Then el sistema devuelve el codigo de estado <status>
    And la respuesta contiene el mensaje "<mensaje>"

    Examples:
      | jwt         | id  | status | mensaje          |
      |           | 1   | 401    | unauthorized   |
      | valid_jwt | 999 | 404    | user_not_found |

  # =========================================================
  # ACTUALIZAR USUARIO (PUT /api/usuarios/{id})
  # =========================================================

  Scenario: Actualizar mis datos exitosamente
    Given tengo un token JWT valido correspondiente al usuario con ID 1
    When envio una peticion PUT a /api/usuarios/1 con un nuevo nombre y correo
    Then el sistema devuelve el codigo de estado 200
    And la respuesta contiene los datos actualizados del usuario

  Scenario Outline: Intentar actualizar usuario con errores o restricciones
    Given tengo el token "<jwt>" del usuario con ID "<userTokenId>"
    And deseo modificar el usuario con ID "<id>"
    When envio una petición PUT a /api/usuarios/<id>
    Then el sistema devuelve el codigo de estado <status>
    And la respuesta contiene el mensaje "<mensaje>"

    Examples:
      | jwt         | userTokenId | id | status | mensaje         |
      |           | 1           | 1  | 401    | unauthorized  |
      | valid_jwt | 1           | 2  | 403    | forbidden     |
      | valid_jwt | 1           | 999| 404    | user_not_found|

  # =========================================================
  # ELIMINAR USUARIO (DELETE /api/usuarios/{id})
  # =========================================================

  Scenario: Eliminar mi cuenta exitosamente
    Given tengo un token JWT válido correspondiente al usuario con ID 1
    When envio una peticion DELETE a /api/usuarios/1
    Then el sistema devuelve el codigo de estado 204
    And el usuario queda eliminado del sistema

  Scenario Outline: Intentar eliminar usuario sin permisos o inexistente
    Given tengo el token "<jwt>" del usuario con ID "<userTokenId>"
    And deseo eliminar el usuario con ID "<id>"
    When envio una peticion DELETE a /api/usuarios/<id>
    Then el sistema devuelve el codigo de estado <status>
    And la respuesta contiene el mensaje "<mensaje>"

    Examples:
      | jwt         | userTokenId | id | status | mensaje          |
      |           | 1           | 1  | 401    | unauthorized   |
      | valid_jwt | 1           | 2  | 403    | forbidden      |
      | valid_jwt | 1           | 999| 404    | user_not_found |

  # =========================================================
  # LISTAR USUARIOS (GET /api/usuarios?page=&size=)
  # =========================================================

  Scenario: Listar usuarios con token válido
    Given tengo un token JWT válido
    When envío una petición GET a /api/usuarios?page=0&size=5
    Then el sistema devuelve el codigo de estado 200
    And la respuesta incluye una lista paginada de usuarios

  Scenario Outline: Intentar listar usuarios sin token o con error
    Given tengo el token "<jwt>"
    When envio una petición GET a /api/usuarios?page=0&size=5
    Then el sistema devuelve el codigo de estado <status>
    And la respuesta contiene el mensaje "<mensaje>"

    Examples:
      | jwt | status | mensaje        |
      |   | 401    | unauthorized |
