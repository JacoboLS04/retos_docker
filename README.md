## API Gateway

Este repositorio contiene el microservicio "API Gateway" (Node.js + Express) que actúa como puerta de entrada para los servicios de autenticación y perfiles. Proporciona rutas públicas para registro, login y operaciones combinadas sobre usuarios (combinando datos de Auth y Profile). Además publica eventos a RabbitMQ cuando se elimina un usuario.

### Qué hace
- Reenvía las peticiones de autenticación (`/api/auth/*`) al servicio de Auth.
- Reenvía las peticiones de perfiles (`/api/profiles/*`) al servicio de Profiles.
- Expone endpoints combinados para usuarios:
  - `GET /api/users/:userId` — recupera datos de Auth y Profile y los combina en una sola respuesta.
  - `PUT /api/users/:userId` — permite actualizar datos de Auth y/o Profile en paralelo.
  - `DELETE /api/users/:userId` — borra usuario en Auth y Profile y publica el evento `USER_DELETED` en RabbitMQ.
- Publica eventos a RabbitMQ (cola por defecto: `user.events.queue`).

### Endpoints principales
- `GET /health` — estado del servicio.
- `POST /api/auth/register` — proxy al Auth service.
- `POST /api/auth/login` — proxy al Auth service.
- `DELETE /api/auth/delete` — proxy al Auth service.
- `ANY /api/profiles/*` — proxy al Profile service (prefijo `/profiles`).
- `GET|PUT|DELETE /api/users/:userId` — endpoints combinados que llaman a Auth y Profile y devuelven una vista unificada.

### Comportamiento en tests
- Cuando `NODE_ENV=test` el gateway no mantiene una conexión real a RabbitMQ (la publicación de eventos es ignorada). Además, los manejadores de `/api/auth/*` usan llamadas HTTP directas (axios) para que `nock` pueda interceptarlas fácilmente en los tests.

### Variables de entorno importantes
- `PORT` — puerto donde corre el gateway (por defecto `3001`).
- `AUTH_SERVICE_URL` — URL base del servicio de Auth (ej.: `http://auth-service:8080`).
- `PROFILE_SERVICE_URL` — URL base del servicio de Profiles (ej.: `http://profile-service:8081`).
- `RABBIT_URL` — URL de conexión a RabbitMQ (ej.: `amqp://guest:guest@rabbitmq:5672`).
- `USER_EVENTS_QUEUE` — nombre de la cola donde se publican eventos de usuario (por defecto `user.events.queue`).
- `NODE_ENV` — en `test` activa comportamientos de prueba (skip RabbitMQ, axios handlers para auth).

### Ejecutarlo localmente (PowerShell)
1. Desde la carpeta `api-gateway`, instala dependencias:

```powershell
cd C:\Users\londg\IdeaProjects\microservicios\api-gateway
npm install
```

2. Ejecutar en modo desarrollo:

```powershell
# (opcional) ajustar variables de entorno para apuntar a servicios locales / docker
$env:AUTH_SERVICE_URL = 'http://localhost:8080'
$env:PROFILE_SERVICE_URL = 'http://localhost:8081'
$env:RABBIT_URL = 'amqp://guest:guest@localhost:5672'

npm start
```

3. Ejecutar tests unitarios (Jest + supertest + nock):

```powershell
cd C:\Users\londg\IdeaProjects\microservicios\api-gateway
npm test
```

Los tests están preparados para ejecutarse de forma aislada (usando `nock` para simular Auth y Profile). Cuando ejecutes `npm test` el servidor se inicia en modo de prueba y las llamadas externas son interceptadas por `nock`.

### Ejecutarlo con docker-compose (integración)
En este proyecto hay un `docker-compose.yml` a nivel del repositorio (`reto-automatizacion/docker-compose.yml`) que orquesta los servicios: Auth, Profiles, RabbitMQ, api-gateway, notification-orchestrator, etc. Para pruebas de integración end-to-end puedes levantar el stack:

```powershell
cd ..\reto-automatizacion
docker-compose up -d
# esperar que los servicios estén up, luego probar el endpoint
```

Si levantas con `docker-compose`, asegúrate de que las variables de entorno en el servicio `api-gateway` apunten a los nombres de servicio correctos definidos en el compose (p. ej. `auth-service`, `profile-service`, `rabbitmq`).

### CI / Jenkins
Se incluye un `Jenkinsfile` con etapas básicas para instalar dependencias, ejecutar tests y validar `/health`. Puedes ampliar el pipeline para publicar artefactos o reportes JUnit.

### Problemas comunes y cómo resolverlos
- `Nock: Disallowed net connect` — ocurre si `nock` bloquea conexiones reales. Asegúrate de que `NODE_ENV=test` esté activo durante los tests y que las URLs en los mocks coincidan exactamente con `AUTH_SERVICE_URL` y `PROFILE_SERVICE_URL` usadas por el gateway.
- Logs del proxy (http-proxy-middleware) — son informativos; si quieres silenciarlos durante los tests, modifica `src/index.js` para condicionar `morgan` o las opciones del `createProxyMiddleware`.
- RabbitMQ no disponible — el gateway intenta reconectar. Para desarrollo local, asegúrate de tener RabbitMQ corriendo o setear `NODE_ENV=test` si sólo quieres ejecutar tests sin broker.

### Contribuir
- Añade tests cuando cambies comportamiento o rutas.
- Mantén los mocks de `nock` sincronizados con las rutas reales de los servicios (auth/profile).

Fin README
