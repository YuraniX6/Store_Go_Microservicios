# Project Structure & File Guide

## Resumen Ejecutivo

✅ **Microservicio de Autenticación Completo**
- Spring Boot 3.3.0 + Java 17
- PostgreSQL + JPA/Hibernate
- JWT Stateless Authentication
- 8 Tests Unitarios Completos
- Docker & docker-compose
- Swagger/OpenAPI 3.0

---

## Estructura de Directorios

```
AuthService/
│
├── 📄 pom.xml                          # Dependencias Maven
├── 📄 Dockerfile                       # Imagen Docker
├── 📄 docker-compose.yml               # Orquestación contenedores
├── 📄 .gitignore                       # Archivos ignorados Git
├── 📄 .env.example                     # Variables de entorno ejemplo
│
├── 📚 Documentación
│   ├── 📄 README.md                    # Guía principal
│   ├── 📄 BUILD.md                     # Instrucciones de construcción
│   ├── 📄 API_REFERENCE.md             # Referencia de endpoints
│   └── 📄 PROJECT_STRUCTURE.md         # Este archivo
│
├── 📂 scripts/
│   ├── 📄 utility.sh                   # Script utilidad (Linux/Mac)
│   └── 📄 utility.bat                  # Script utilidad (Windows)
│
└── 📂 src/
    ├── 📂 main/
    │   ├── 📂 java/com/storego/authservice/
    │   │   │
    │   │   ├── 📄 AuthServiceApplication.java
    │   │   │   └── Clase principal con CommandLineRunner
    │   │   │   └── Inicializa roles y usuario admin
    │   │   │
    │   │   ├── 📂 config/
    │   │   │   ├── 📄 SecurityConfig.java
    │   │   │   │   └── Configuración de seguridad Spring
    │   │   │   │   └── Beans: PasswordEncoder, AuthenticationManager
    │   │   │   │   └── Configuración de filtros JWT
    │   │   │   │
    │   │   │   └── 📄 SwaggerConfig.java
    │   │   │       └── Configuración OpenAPI 3.0
    │   │   │       └── Definición esquema de seguridad
    │   │   │
    │   │   ├── 📂 controller/
    │   │   │   └── 📄 AuthController.java
    │   │   │       └── @RestController /auth
    │   │   │       └── Endpoints: register, login, validate, refresh, me
    │   │   │
    │   │   ├── 📂 service/
    │   │   │   ├── 📄 JwtService.java
    │   │   │   │   └── Generación de JWT y refresh tokens
    │   │   │   │   └── Validación y extracción de claims
    │   │   │   │   └── HMAC-SHA256 con Base64
    │   │   │   │
    │   │   │   ├── 📄 AuthService.java
    │   │   │   │   └── Lógica de negocio de autenticación
    │   │   │   │   └── Métodos: register, login, refreshToken, validateToken
    │   │   │   │   └── BCrypt para contraseñas
    │   │   │   │
    │   │   │   └── 📄 CustomUserDetailsService.java
    │   │   │       └── Implementa UserDetailsService
    │   │   │       └── loadUserByUsername, loadUserById
    │   │   │       └── Retorna CustomUserDetails
    │   │   │
    │   │   ├── 📂 security/
    │   │   │   ├── 📄 CustomUserDetails.java
    │   │   │   │   └── Implementa UserDetails
    │   │   │   │   └── Mapeo de authorities (ROLE_USER, ROLE_ADMIN)
    │   │   │   │   └── Accesores para usuario y roles
    │   │   │   │
    │   │   │   └── 📄 JwtAuthenticationFilter.java
    │   │   │       └── OncePerRequestFilter
    │   │   │       └── Extrae token de header Authorization
    │   │   │       └── Autentica usando JWT
    │   │   │
    │   │   ├── 📂 entity/
    │   │   │   ├── 📄 User.java
    │   │   │   │   └── @Entity tabla "users"
    │   │   │   │   └── id: UUID, username: unique, email: unique
    │   │   │   │   └── ManyToOne hacia Role
    │   │   │   │
    │   │   │   └── 📄 Role.java
    │   │   │       └── @Entity tabla "roles"
    │   │   │       └── id: Long, name: unique (ADMIN, USER)
    │   │   │       └── OneToMany desde User
    │   │   │
    │   │   ├── 📂 repository/
    │   │   │   ├── 📄 UserRepository.java
    │   │   │   │   └── JpaRepository<User, UUID>
    │   │   │   │   └── findByUsername, findByEmail
    │   │   │   │   └── existsByUsername, existsByEmail
    │   │   │   │
    │   │   │   └── 📄 RoleRepository.java
    │   │   │       └── JpaRepository<Role, Long>
    │   │   │       └── findByName
    │   │   │
    │   │   ├── 📂 dto/
    │   │   │   ├── 📄 RegisterRequest.java
    │   │   │   │   └── username (@NotBlank, @Size 3-50)
    │   │   │   │   └── email (@NotBlank, @Email)
    │   │   │   │   └── password (@NotBlank, @Size min 6)
    │   │   │   │
    │   │   │   ├── 📄 LoginRequest.java
    │   │   │   │   └── username, password
    │   │   │   │
    │   │   │   ├── 📄 RefreshRequest.java
    │   │   │   │   └── refreshToken
    │   │   │   │
    │   │   │   ├── 📄 AuthResponse.java
    │   │   │   │   └── token, refreshToken, username, roles
    │   │   │   │
    │   │   │   └── 📄 UserInfo.java
    │   │   │       └── username, email, roles
    │   │   │
    │   │   └── 📂 exception/
    │   │       └── 📄 GlobalExceptionHandler.java
    │   │           └── @RestControllerAdvice
    │   │           └── Manejo de excepciones globales
    │   │           └── RuntimeException → 400 BAD_REQUEST
    │   │           └── BadCredentialsException → 401 UNAUTHORIZED
    │   │           └── MethodArgumentNotValidException → 400
    │   │
    │   └── 📂 resources/
    │       └── 📄 application.properties
    │           ├── server.port=8080
    │           ├── spring.datasource.url
    │           ├── spring.datasource.username
    │           ├── spring.datasource.password
    │           ├── spring.jpa.hibernate.ddl-auto=update
    │           ├── jwt.secret=${JWT_SECRET}
    │           ├── jwt.expiration=86400000
    │           └── Logging configuration
    │
    └── 📂 test/
        └── 📂 java/com/storego/authservice/
            │
            ├── 📂 service/
            │   ├── 📄 AuthServiceTest.java (6 test methods)
            │   │   └── testRegister_Success
            │   │   └── testRegister_UsernameTaken
            │   │   └── testRegister_EmailTaken
            │   │   └── testLogin_Success
            │   │   └── testRefreshToken_Success
            │   │   └── testRefreshToken_InvalidToken
            │   │   └── testValidateToken_Valid
            │   │   └── testValidateToken_Invalid
            │   │
            │   ├── 📄 JwtServiceTest.java (8 test methods)
            │   │   └── testGenerateToken
            │   │   └── testGenerateRefreshToken
            │   │   └── testExtractSubject
            │   │   └── testExtractUsername
            │   │   └── testExtractRoles
            │   │   └── testIsTokenValid_Valid
            │   │   └── testIsTokenValid_InvalidUserId
            │   │   └── testIsTokenValid_InvalidToken
            │   │   └── testTokenExpiration
            │   │
            │   └── 📄 CustomUserDetailsServiceTest.java (5 test methods)
            │       └── testLoadUserByUsername_Success
            │       └── testLoadUserByUsername_NotFound
            │       └── testLoadUserById_Success
            │       └── testLoadUserById_NotFound
            │       └── testLoadUserByUsername_WithRole
            │
            ├── 📂 security/
            │   ├── 📄 JwtAuthenticationFilterTest.java (4 test methods)
            │   │   └── testDoFilterInternal_WithValidToken
            │   │   └── testDoFilterInternal_WithoutToken
            │   │   └── testDoFilterInternal_WithInvalidToken
            │   │   └── testDoFilterInternal_WithMalformedAuthHeader
            │   │
            │   └── 📄 CustomUserDetailsTest.java (9 test methods)
            │       └── testGetAuthorities_WithRole
            │       └── testGetAuthorities_WithoutRole
            │       └── testGetAuthorities_AdminRole
            │       └── testGetPassword
            │       └── testGetUsername
            │       └── testGetUserId
            │       └── testGetUser
            │       └── testIsAccountNonExpired
            │       └── testIsEnabled
            │
            ├── 📂 exception/
            │   └── 📄 GlobalExceptionHandlerTest.java (5 test methods)
            │       └── testHandleRuntimeException
            │       └── testHandleBadCredentialsException
            │       └── testHandleMethodArgumentNotValidException
            │       └── testHandleException
            │
            ├── 📂 controller/
            │   └── 📄 AuthControllerTest.java (9 test methods)
            │       └── testRegister_Success
            │       └── testLogin_Success
            │       └── testValidateToken_Valid
            │       └── testValidateToken_Invalid
            │       └── testValidateToken_NoToken
            │       └── testRegister_InvalidUsername
            │       └── testRegister_InvalidEmail
            │       └── testRegister_ShortPassword
            │
            └── 📂 config/
                └── 📄 SecurityConfigTest.java (4 test methods)
                    └── testPasswordEncoderBean
                    └── testAuthenticationManagerBean
                    └── testPasswordEncoding
                    └── testPasswordMatchingWithDifferentPassword

```

---

## Dependencias Principales

| Dependencia | Versión | Propósito |
|------------|---------|----------|
| Spring Boot | 3.3.0 | Framework principal |
| Java | 17 | Lenguaje |
| Spring Security | 6.1.x | Autenticación/Autorización |
| Spring Data JPA | 3.1.x | ORM |
| JJWT | 0.11.5 | JWT |
| PostgreSQL | 42.6.0 | Base de datos |
| SpringDoc OpenAPI | 2.6.0 | Swagger/API Docs |
| Lombok | 1.18.x | Anotaciones |
| Mockito | 5.2.x | Testing |

---

## Endpoints API

### Públicos
- `POST /auth/register` - Registrar usuario
- `POST /auth/login` - Login
- `GET /auth/validate` - Validar token
- `GET /swagger-ui.html` - Documentación

### Protegidos
- `POST /auth/refresh` - Refrescar token
- `GET /auth/me` - Información del usuario

---

## Base de Datos

### Tabla: roles
```
id (BIGINT, PK, AUTO_INCREMENT)
name (VARCHAR UNIQUE)
```

### Tabla: users
```
id (UUID, PK)
username (VARCHAR UNIQUE)
email (VARCHAR UNIQUE)
password (VARCHAR)
role_id (BIGINT, FK)
created_at (TIMESTAMP)
```

---

## Configuración de Seguridad

✅ CSRF deshabilitado (stateless)
✅ Sesiones deshabilitadas (STATELESS)
✅ Contraseñas con BCrypt
✅ JWT en header Authorization
✅ Validación de entrada en DTOs
✅ Manejo centralizado de excepciones
✅ Endpoints públicos y protegidos
✅ Roles (ADMIN, USER)

---

## Testing

- **Total Tests:** 45
- **Coverage:** Servicios, Controllers, Security, Exceptions
- **Framework:** JUnit 5 + Mockito
- **Cobertura Esperada:** 80%+

---

## Instrucciones Rápidas

### Desarrollo Local
```bash
# 1. Variables de entorno
export JWT_SECRET=<base64-secret>

# 2. Compilar
mvn clean install

# 3. Ejecutar
mvn spring-boot:run

# 4. Tests
mvn test

# 5. Acceder
http://localhost:8080/swagger-ui.html
```

### Docker
```bash
# Build
docker build -t auth-service:latest .

# Run
docker-compose up -d

# Stop
docker-compose down
```

---

## Características Implementadas

✅ Registro de usuarios con validación
✅ Login con JWT
✅ Refresh tokens (24 días)
✅ Validación de tokens
✅ Información del usuario autenticado
✅ Roles ADMIN y USER
✅ BCrypt para contraseñas
✅ PostgreSQL con JPA
✅ Documentación Swagger/OpenAPI
✅ Pruebas unitarias
✅ Docker & docker-compose
✅ Manejo global de excepciones
✅ Logging configurado
✅ Scripts de utilidad

---

## Próximos Pasos (Opcional)

- [ ] Implementar rate limiting
- [ ] Agregar refresh token en BD
- [ ] Implementar logout
- [ ] Auditoría de accesos
- [ ] Integración con OAuth2
- [ ] 2FA (Two-Factor Authentication)
- [ ] Tokens de API
- [ ] Integración con otros microservicios

---

## Notas Importantes

1. **Usuario Admin Default:**
   - Username: `admin`
   - Password: `admin123`
   - ⚠️ Cambiar en producción

2. **JWT Secret:**
   - Mínimo 32 caracteres en Base64
   - Cambiar en producción
   - Debe ser único y seguro

3. **Base de Datos:**
   - Tablas se crean automáticamente (DDL auto-update)
   - Roles e usuario admin se inicializan en startup
   - PostgreSQL requerido

4. **Seguridad:**
   - HTTPS recomendado en producción
   - CORS si es necesario
   - Rate limiting recomendado
   - Monitoreo de logs

---

## Soporte

Para preguntas o issues, contactar al equipo de desarrollo de StoreGo.
