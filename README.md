# Proyecto Retos - Spring Boot + Docker + JWT + OpenAPI

Este proyecto implementa una API en **Spring Boot (Gradle)** que resuelve los retos propuestos:

1. **Persistencia en Base de Datos**  
   - Se eligió **PostgreSQL** como motor de base de datos.  
   - Se despliega con **Docker Compose**.  
   - El servicio de saludo almacena el nombre de la persona y la fecha/hora en que accede al sistema.

2. **Federación de identidad / Autenticación**  
   - Se implementó autenticación mediante **JWT** directamente en el backend.  
   - No se utilizó Auth0 ni servicios externos: todo se gestiona desde la aplicación.  
   - Los tokens son generados al iniciar sesión y verificados en cada request.

3. **Protección de endpoints**  
   - Los endpoints de registro y login son públicos.  
   - Los demás endpoints requieren un **token JWT válido** en el header `Authorization: Bearer <token>`.

4. **Paginación de saludos**  
   - Se añadió un método para listar los registros de saludos guardados en la base de datos, con **paginación**.

---

## Tecnologías utilizadas

- **Spring Boot 3** (Gradle)
- **Spring Security** (con JWT)
- **Spring Data JPA**
- **PostgreSQL** (contenedor Docker)
- **Docker Compose**
- **Springdoc OpenAPI** (Swagger UI)

---

