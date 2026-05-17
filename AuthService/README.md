# StoreGo Auth Service

Microservicio de autenticación para el sistema StoreGo, construido con Spring Boot 3.3.0, Java 17, PostgreSQL y JWT.

## Características

- Autenticación stateless con JWT
- Registro de usuarios con validación
- Roles de usuario (ADMIN, USER)
- Refresh tokens con expiración de 24 días
- Documentación API con Swagger/OpenAPI 3.0
- Base de datos PostgreSQL
- Tests unitarios completos
- Docker y docker-compose incluidos

## Requisitos

- Java 17 o superior
- Maven 3.9+
- PostgreSQL 12+ (o usar docker-compose)
- Docker y Docker Compose (opcional)

## Configuración

### Variables de Entorno

```bash
DB_HOST=localhost          # Host de PostgreSQL
DB_PORT=5432             # Puerto de PostgreSQL
DB_NAME=storego_auth     # Nombre de la base de datos
DB_USER=postgres         # Usuario de PostgreSQL
DB_PASSWORD=password     # Contraseña de PostgreSQL
JWT_SECRET=<base64-string> # Clave secreta JWT (mínimo 32 caracteres en Base64)
```

### Ejecutar Localmente

1. **Configurar Base de Datos (PostgreSQL local):**
```bash
psql -U postgres
CREATE DATABASE storego_auth;
```

2. **Configurar variables de entorno:**
```bash
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=storego_auth
export DB_USER=postgres
export DB_PASSWORD=password
export JWT_SECRET=YXV0aEtzZWNyZXRrZXlmb3JzdG9yZWdvbWljcm9zZXJ2aWNlYXV0aGVudGljYXRpb24=
```

3. **Compilar y ejecutar:**
```bash
mvn clean install
mvn spring-boot:run
```

La aplicación estará disponible en `http://localhost:8080`

### Usando Docker Compose

```bash
# Copiar archivo de ejemplo
cp .env.example .env

# Editar variables si es necesario
nano .env

# Construir y ejecutar
docker-compose up -d
```

## API Endpoints

### Públicos (sin autenticación)

- **POST /auth/register** - Registrar nuevo usuario
- **POST /auth/login** - Iniciar sesión
- **GET /auth/validate** - Validar token
- **GET /swagger-ui/** - Documentación Swagger
- **GET /v3/api-docs/** - OpenAPI JSON

### Protegidos (requieren JWT)

- **POST /auth/refresh** - Refrescar token
- **GET /auth/me** - Obtener información del usuario actual

## Estructura del Proyecto

```
auth-service/
├── src/
│   ├── main/
│   │   ├── java/com/storego/authservice/
│   │   │   ├── AuthServiceApplication.java
│   │   │   ├── config/           # Configuración de seguridad y Swagger
│   │   │   ├── controller/       # Endpoints REST
│   │   │   ├── dto/              # Data Transfer Objects
│   │   │   ├── entity/           # Entidades JPA
│   │   │   ├── exception/        # Manejo de excepciones
│   │   │   ├── repository/       # Acceso a datos
│   │   │   ├── security/         # Componentes de seguridad
│   │   │   └── service/          # Lógica de negocio
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/com/storego/authservice/
├── pom.xml
├── Dockerfile
├── docker-compose.yml
└── README.md
```

## Roles de Usuario

- **ADMIN** - Acceso total al sistema
- **USER** - Usuario estándar (asignado por defecto)

## Usuario Administrativo por Defecto

- **Username:** admin
- **Email:** admin@storego.com
- **Password:** admin123

**Importante:** Cambiar credenciales en producción.

## Flujo de Autenticación

1. **Registro:**
   ```bash
   POST /auth/register
   {
     "username": "newuser",
     "email": "newuser@example.com",
     "password": "password123"
   }
   ```

2. **Login:**
   ```bash
   POST /auth/login
   {
     "username": "newuser",
     "password": "password123"
   }
   ```
   Respuesta:
   ```json
   {
     "token": "eyJhbGc...",
     "refreshToken": "eyJhbGc...",
     "username": "newuser",
     "roles": [1]
   }
   ```

3. **Usar Token:**
   Incluir en header Authorization:
   ```
   Authorization: Bearer eyJhbGc...
   ```

4. **Refrescar Token:**
   ```bash
   POST /auth/refresh
   {
     "refreshToken": "eyJhbGc..."
   }
   ```

## Validación de Entrada

### RegisterRequest
- **username:** 3-50 caracteres, obligatorio
- **email:** Email válido, obligatorio
- **password:** Mínimo 6 caracteres, obligatorio

### LoginRequest
- **username:** Obligatorio
- **password:** Obligatorio

## Tests

Ejecutar todos los tests:
```bash
mvn test
```

Ejecutar tests específicos:
```bash
mvn test -Dtest=AuthServiceTest
```

Con cobertura:
```bash
mvn test jacoco:report
```

## Dependencias Principales

- Spring Boot 3.3.0
- Spring Security 6.1.x
- Spring Data JPA
- JJWT 0.11.5
- PostgreSQL Driver
- SpringDoc OpenAPI 2.6.0
- Validation API
- Lombok

## Configuración de JWT

- **Expiración:** 24 horas (86400000 ms)
- **Refresh Token:** 24 días (2073600000 ms)
- **Algoritmo:** HMAC-SHA256
- **Claims:** subject (userId), username, roles

## Seguridad

- Contraseñas hasheadas con BCrypt
- CSRF deshabilitado (stateless)
- Sesiones deshabilitadas (stateless)
- CORS configurado si es necesario
- Validación de entrada en DTOs
- Manejo centralizado de excepciones

## Logging

- Log level por defecto: INFO
- Log level para authservice: DEBUG
- Logs en consola
- Logs de acceso SQL deshabilitados por defecto

## Swagger/OpenAPI

Documentación interactiva disponible en:
```
http://localhost:8080/swagger-ui.html
```

JSON OpenAPI:
```
http://localhost:8080/v3/api-docs
```

## Desarrollo

### Agregar Nueva Funcionalidad

1. Crear entidad en `entity/`
2. Crear repositorio en `repository/`
3. Crear DTO en `dto/`
4. Crear servicio en `service/`
5. Crear endpoint en `controller/`
6. Agregar tests en `src/test/`

### Convenciones de Código

- Usar camelCase para variables y métodos
- Usar PascalCase para clases
- UPPER_CASE para constantes
- DTOs con @Data, @Builder, @NoArgsConstructor, @AllArgsConstructor
- Servicios con @Service, @RequiredArgsConstructor
- Controllers con @RestController, @RequestMapping

## Troubleshooting

### Error de conexión a BD
```
Asegurarse que PostgreSQL está corriendo y credenciales son correctas
```

### Token expirado
```
Usar endpoint /auth/refresh con el refresh token
```

### CORS issues
```
Configurar CORS en SecurityConfig si es necesario
```

## Contacto y Soporte

Para reportar issues o sugerencias, contactar al equipo de desarrollo.

## Licencia

Propietario de StoreGo
