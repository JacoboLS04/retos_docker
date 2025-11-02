# Gestión de Perfiles (Spring Boot + Gradle)

Microservicio para gestionar el perfil del usuario autenticado.

- Stack: Java 17, Spring Boot 3, JPA (H2 dev), Security (JWT), springdoc OpenAPI.
- Endpoints (todas requieren autenticación):
  - GET `/profile` → devuelve tu perfil.
  - POST `/profile` → crea tu perfil (409 si ya existe).
  - PUT `/profile` → reemplaza/crea tu perfil.
  - PATCH `/profile` → actualización parcial de tu perfil.
  - DELETE `/profile` → elimina tu perfil.

Swagger UI: `http://localhost:8083/swagger-ui` (expuesto sin auth para pruebas locales).

## Configuración

application.yml incluye H2 en memoria para desarrollo. Para JWT se usa una clave simétrica de ejemplo:

- `jwt.secret` (por defecto `dev-secret-please-change`). Cambia esto en producción o configura un decodificador JWT por JWK/issuer.

## Compilar y probar

Puedes reutilizar el wrapper de Gradle del proyecto `reto-automatizacion` pasando `-p` para apuntar a este proyecto:

```
..\reto-automatizacion\gradlew -p . clean test
```

En Windows PowerShell (desde `gestion-perfiles`):

```
..\reto-automatizacion\gradlew -p . clean test
```

## Ejecutar local

```
..\reto-automatizacion\gradlew -p . bootRun
```

Luego abre `http://localhost:8083/swagger-ui`.

## Docker

Primero construye el JAR:

```
..\reto-automatizacion\gradlew -p . clean bootJar
```

Luego construye la imagen:

```
docker build -t gestion-perfiles:local .
```

Y ejecuta:

```
docker run --rm -p 8083:8083 -e JWT_SECRET=dev-secret-please-change gestion-perfiles:local
```

## Notas de seguridad

- Este servicio actúa como Resource Server. Espera un JWT con `sub` numérico que corresponde al `id` del usuario (Long) del servicio de autenticación (`reto-automatizacion`).
- En producción, configura `spring.security.oauth2.resourceserver.jwt.jwk-set-uri` o `issuer-uri` en lugar de la clave simétrica de ejemplo.
