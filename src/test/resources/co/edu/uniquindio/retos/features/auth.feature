Feature: Autenticacion y recuperacion de contrasenas
  Como usuario del sistema
  Quiero poder autenticarme y recuperar mi contrasena
  Para acceder de forma segura a mis servicios y restablecer el acceso cuando lo necesite

  # =========================================================
  # LOGIN (POST /api/auth/login)
  # =========================================================

  Scenario: Autenticacion exitosa con credenciales validas
    Given tengo un usuario registrado con el correo "santiago@example.com" y contrasena "12345"
    When envio una peticion POST a /api/auth/login con esas credenciales
    Then el sistema devuelve el codigo de estado 200
    And la respuesta contiene un token JWT y un campo "expiresIn"

  Scenario Outline: Intentar autenticarse con credenciales invalidas o incompletas
    Given preparo la solicitud con correo "<email>" y contrasena "<password>"
    When envio una peticion POST a /api/auth/login
    Then el sistema devuelve el codigo de estado <status>
    And la respuesta contiene el mensaje "<mensaje>"

    Examples:
      | email                  | password | status | mensaje               |
      | noexiste@example.com | 12345  | 401    | invalid_credentials |
      | santiago@example.com | wrong  | 401    | invalid_credentials |
      |                      | 12345  | 401    | invalid_credentials |

  # =========================================================
  # REQUEST PASSWORD RESET (POST /api/auth/request-password-reset)
  # =========================================================

  Scenario: Solicitar restablecimiento de contrasena exitosamente
    Given existe un usuario con el correo "santiago@example.com"
    When envio una peticion POST a /api/auth/request-password-reset con ese correo
    Then el sistema devuelve el codigo de estado 200
    And la respuesta contiene el mensaje "reset_email_sent"
    And un token temporal de restablecimiento

  Scenario Outline: Intentar solicitar restablecimiento con correo inexistente o invalido
    Given preparo la solicitud con el correo "<email>"
    When envio una peticion POST a /api/auth/request-password-reset
    Then el sistema devuelve el codigo de estado <status>
    And la respuesta contiene el mensaje "<mensaje>"

    Examples:
      | email                   | status | mensaje         |
      | noexiste@example.com  | 404    | user_not_found |
      |                       | 404    | user_not_found |

  # =========================================================
  # RESET PASSWORD (POST /api/auth/reset-password)
  # =========================================================

  Scenario: Restablecer contrasena exitosamente con token valido
    Given tengo un token de restablecimiento valido "abc123resettoken"
    And un nuevo password "nuevaContrasena123"
    When envio una peticion POST a /api/auth/reset-password con esos datos
    Then el sistema devuelve el codigo de estado 200
    And la respuesta contiene el mensaje "password_reset"

  Scenario Outline: Intentar restablecer contrasena con token invalido o expirado
    Given preparo la solicitud con token "<token>" y nueva contrasena "<newPassword>"
    When envio una peticion POST a /api/auth/reset-password
    Then el sistema devuelve el codigo de estado <status>
    And la respuesta contiene el mensaje "<mensaje>"

    Examples:
      | token                | newPassword         | status | mensaje          |
      | invalidToken123    | pass123           | 400    | invalid_token  |
      | expiredToken456    | pass123           | 400    | token_expired  |
