Feature: API de Gestión de Usuarios y Autenticación
  Como cliente del sistema
  Quiero poder registrar, autenticar y administrar usuarios
  Para garantizar el correcto funcionamiento del servicio

  # --- Escenarios de Usuarios ---

  # ---- Preguntar si se deben agregar más escenarios de validación de datos o se autilizar scenario outline ---
  Scenario: Registro de usuario exitoso
    Given soy un usuario nuevo con datos válidos
    When envío una petición POST a /usuarios con mi información
    Then el sistema devuelve un código de estado 201
    And la respuesta contiene el identificador y el correo del usuario creado


  Scenario: Error al registrar un usuario con email duplicado
    Given soy un usuario nuevo con un email ya registrado
    When envío una petición POST a /usuarios con mi información
    Then el sistema devuelve un código de estado 409
    And la respuesta contiene un mensaje de error indicando que el email ya existe

  Scenario: Obtener usuario por ID existente
    Given existe un usuario registrado con un ID válido
    When envío una petición GET a /usuarios/{id}
    Then el sistema devuelve un código de estado 200
    And la respuesta contiene los datos del usuario solicitado

  Scenario: Error al obtener usuario inexistente
    Given envío un ID de usuario que no existe
    When hago una petición GET a /usuarios/{id}
    Then el sistema devuelve un código de estado 404
    And la respuesta contiene un mensaje indicando que el usuario no existe

  Scenario: Actualizar usuario exitosamente
    Given existe un usuario registrado
    When envío una petición PUT a /usuarios/{id} con nuevos datos válidos
    Then el sistema devuelve un código de estado 200
    And la respuesta contiene los datos actualizados del usuario

  Scenario: Eliminar usuario exitosamente
    Given existe un usuario registrado
    When envío una petición DELETE a /usuarios/{id}
    Then el sistema devuelve un código de estado 204
    And el usuario queda eliminado del sistema

  Scenario: Error al eliminar usuario inexistente
    Given envío un ID de usuario que no existe
    When hago una petición DELETE a /usuarios/{id}
    Then el sistema devuelve un código de estado 404
    And la respuesta contiene un mensaje indicando que el usuario no existe

  Scenario: Listar usuarios con token válido
    Given tengo un token JWT válido
    When envío una petición GET a /usuarios con parámetros de paginación
    Then el sistema devuelve un código de estado 200
    And la respuesta incluye una lista paginada de usuarios

  Scenario: Error al listar usuarios sin token
    Given no incluyo un token JWT en la petición
    When envío una petición GET a /usuarios
    Then el sistema devuelve un código de estado 401
    And la respuesta contiene un mensaje indicando falta de autorización

  # --- Escenarios de Autenticación ---


  # --- Escenario de Login con usuarios especificos ---
  Scenario Outline: Check login
    Given el usuario está en la página de login
    When el usuario ingresa el mail como '<email>' y la contraseña como '<password>'
    And el usuario hace clic en el botón de login
    Then el usuario es redirigido a la página principal
    Examples:
      | email               | password |
      | londgav01@gmail.com | 1234     |

  Scenario: Login exitoso
    Given soy un usuario registrado con credenciales válidas
    When envío una petición POST a /auth/login con mi email y contraseña
    Then el sistema devuelve un código de estado 200
    And la respuesta contiene un token JWT válido

  Scenario: Error en login con credenciales inválidas
    Given soy un usuario con credenciales incorrectas
    When envío una petición POST a /auth/login
    Then el sistema devuelve un código de estado 401
    And la respuesta contiene un mensaje de error indicando credenciales inválidas

  Scenario: Solicitud de recuperación de contraseña
    Given soy un usuario registrado con un email válido
    When envío una petición POST a /auth/request-password-reset
    Then el sistema devuelve un código de estado 200
    And recibo confirmación de que el email de recuperación fue enviado

  Scenario: Error en solicitud de recuperación con email inexistente
    Given no soy un usuario registrado
    When envío una petición POST a /auth/request-password-reset con un email inexistente
    Then el sistema devuelve un código de estado 404
    And la respuesta contiene un mensaje de error indicando que el usuario no fue encontrado

  Scenario: Restablecer contraseña exitosamente
    Given recibí un token de recuperación válido
    When envío una petición POST a /auth/reset-password con el token y una nueva contraseña
    Then el sistema devuelve un código de estado 200
    And la contraseña queda actualizada correctamente

  Scenario: Error al restablecer contraseña con token inválido
    Given envío un token de recuperación inválido o expirado
    When hago una petición POST a /auth/reset-password
    Then el sistema devuelve un código de estado 400
    And la respuesta contiene un mensaje indicando que el token no es válido

  # --- Escenarios de Saludos ---

  Scenario: Listar saludos con token válido
    Given tengo un token JWT válido
    When envío una petición GET a /saludos con parámetros de paginación
    Then el sistema devuelve un código de estado 200
    And la respuesta incluye una lista paginada de saludos

  Scenario: Error al listar saludos sin token
    Given no incluyo un token JWT en la petición
    When envío una petición GET a /saludos
    Then el sistema devuelve un código de estado 401
    And la respuesta contiene un mensaje indicando falta de autorización




