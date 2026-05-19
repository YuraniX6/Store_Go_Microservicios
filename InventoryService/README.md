# Inventory Service — StoreGo Microservicio

API REST de inventario de skins para StoreGo. Cada usuario gestiona sus propias skins de CS:GO con autenticación JWT stateless.

## 📋 Stack Técnico

- **Lenguaje**: Java 17
- **Framework**: Spring Boot 3.2.4
- **Build**: Maven
- **Database**: PostgreSQL 15+
- **Auth**: Spring Security + JWT (jjwt 0.12.3)
- **Mapper**: MapStruct 1.6.0
- **Validación**: Jakarta Bean Validation
- **Docs**: SpringDoc OpenAPI (Swagger)
- **ORM**: Spring Data JPA + Hibernate
- **Migrations**: Flyway
- **Container**: Docker (multi-stage)

## 🚀 Quick Start

### 1. Requisitos Previos

- Java 17+
- Maven 3.9+
- Docker & Docker Compose (opcional)
- PostgreSQL 15+ (si no usas Docker)

### 2. Variables de Entorno

Copiar y editar `.env.example` (si existe) o configurar directamente:

```bash
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=storego_inventory
export DB_USER=postgres
export DB_PASSWORD=postgres
export JWT_SECRET=your-storego-jwt-secret-change-in-production
```

**Importante**: `JWT_SECRET` debe coincidir con el de `auth-service`.

### 3. Ejecutar Localmente

#### Opción A: Con Docker Compose (Recomendado)

```bash
docker-compose up -d
```

Esto levanta:
- PostgreSQL 15 en puerto 5432
- inventory-service en puerto 8082

Healthcheck automático. La DB se migra automáticamente con Flyway.

#### Opción B: Con BD Local

```bash
# 1. Crear BD
createdb -U postgres storego_inventory

# 2. Compilar
mvn clean package -DskipTests

# 3. Ejecutar
java -jar target/inventory-service-1.0.0.jar
```

### 4. Verificar Salud

```bash
curl http://localhost:8082/actuator/health
```

Respuesta esperada:
```json
{
  "status": "UP"
}
```

### 5. Swagger UI

Abre en navegador:
```
http://localhost:8082/swagger-ui.html
```

## 🔐 Autenticación JWT

Todos los endpoints (excepto `/swagger-ui/**`, `/v3/api-docs/**`, `/actuator/health`) requieren token JWT en header:

```bash
Authorization: Bearer <jwt_token>
```

**Flujo esperado:**

1. Usuario hace login en `auth-service` → recibe JWT
2. Usuario envía JWT en header Authorization a `inventory-service`
3. `JwtAuthenticationFilter` valida firma + expiración
4. Extrae `sub` (UUID) como `ownerId`
5. Extrae `role` como `GrantedAuthority`

**Estructura esperada del JWT:**

```json
{
  "sub": "550e8400-e29b-41d4-a716-446655440000",
  "username": "diego123",
  "role": "USER",
  "iat": 1234567890,
  "exp": 1234567890
}
```

## 📡 Endpoints

Todos requieren autenticación (Bearer token) excepto donde se indique.

### 1. Crear Skin

**POST** `/skins`

```bash
curl -X POST http://localhost:8082/skins \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "AWP | Dragon Lore",
    "weapon": "AWP",
    "rarity": "COVERT",
    "wear": "FACTORY_NEW",
    "floatValue": 0.012345,
    "imageUrl": "https://example.com/skin.png"
  }'
```

**Response (201 Created):**

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440001",
  "ownerId": "550e8400-e29b-41d4-a716-446655440000",
  "name": "AWP | Dragon Lore",
  "weapon": "AWP",
  "rarity": "COVERT",
  "wear": "FACTORY_NEW",
  "floatValue": 0.012345,
  "imageUrl": "https://example.com/skin.png",
  "createdAt": "2026-05-17T12:34:56Z",
  "updatedAt": "2026-05-17T12:34:56Z"
}
```

### 2. Listar Mis Skins

**GET** `/skins/me`

```bash
curl -X GET http://localhost:8082/skins/me \
  -H "Authorization: Bearer <token>"
```

**Response (200 OK):**

```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440001",
    "ownerId": "550e8400-e29b-41d4-a716-446655440000",
    "name": "AWP | Dragon Lore",
    "weapon": "AWP",
    "rarity": "COVERT",
    "wear": "FACTORY_NEW",
    "floatValue": 0.012345,
    "imageUrl": "https://example.com/skin.png",
    "createdAt": "2026-05-17T12:34:56Z",
    "updatedAt": "2026-05-17T12:34:56Z"
  }
]
```

### 3. Obtener Skin por ID

**GET** `/skins/{id}`

```bash
curl -X GET http://localhost:8082/skins/550e8400-e29b-41d4-a716-446655440001 \
  -H "Authorization: Bearer <token>"
```

**Response (200 OK):** Misma estructura que arriba.

**Códigos posibles:**
- `200` — Skin existe y es del usuario
- `400` — UUID inválido
- `403` — Skin pertenece a otro usuario
- `404` — Skin no encontrada

### 4. Actualizar Skin

**PUT** `/skins/{id}`

Reemplaza TODOS los campos editables. `id`, `ownerId`, `createdAt`, `updatedAt` son inmutables.

```bash
curl -X PUT http://localhost:8082/skins/550e8400-e29b-41d4-a716-446655440001 \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "AWP | Dragon Lore FN",
    "weapon": "AWP",
    "rarity": "COVERT",
    "wear": "FACTORY_NEW",
    "floatValue": 0.011,
    "imageUrl": "https://example.com/updated.png"
  }'
```

**Response (200 OK):** Skin actualizada.

### 5. Eliminar Skin

**DELETE** `/skins/{id}`

```bash
curl -X DELETE http://localhost:8082/skins/550e8400-e29b-41d4-a716-446655440001 \
  -H "Authorization: Bearer <token>"
```

**Response (204 No Content)** — Sin body.

## 🔍 Códigos HTTP

| Endpoint | Método | 200/201 | 400 | 401 | 403 | 404 | 500 |
|----------|--------|---------|-----|-----|-----|-----|-----|
| POST /skins | POST | 201 | ✓ | ✓ | - | - | - |
| GET /skins/me | GET | 200 | - | ✓ | - | - | - |
| GET /skins/{id} | GET | 200 | ✓ | ✓ | ✓ | ✓ | - |
| PUT /skins/{id} | PUT | 200 | ✓ | ✓ | ✓ | ✓ | - |
| DELETE /skins/{id} | DELETE | 204 | ✓ | ✓ | ✓ | ✓ | - |

## 📊 Modelo de Datos

### Entidad Skin

```
id (UUID)                  — PK, auto-generado
ownerId (UUID)             — FK (del JWT.sub), indexed
name (VARCHAR 150)         — NOT NULL, ej: "AWP | Dragon Lore"
weapon (VARCHAR 50)        — NOT NULL, ej: "AWP"
rarity (ENUM)              — NOT NULL, valores: CONSUMER, INDUSTRIAL, MILSPEC, RESTRICTED, CLASSIFIED, COVERT, CONTRABAND
wear (ENUM)                — NOT NULL, valores: FACTORY_NEW, MINIMAL_WEAR, FIELD_TESTED, WELL_WORN, BATTLE_SCARRED
floatValue (NUMERIC 9,8)   — NOT NULL, rango [0.0, 1.0]
imageUrl (VARCHAR 500)     — nullable
createdAt (TIMESTAMP)      — NOT NULL, auto
updatedAt (TIMESTAMP)      — NOT NULL, auto
```

### Validaciones en BD

```sql
CHECK (float_value >= 0.0 AND float_value <= 1.0)
INDEX owner_id
```

## 🧪 Testing

### Unit Tests (SkinService)

```bash
mvn test -Dtest=SkinServiceTest
```

Cubre:
- create OK, getMySkins OK (vacío y con datos), getSkin OK, get ajeno → 403, update OK, update inexistente → 404, delete OK, delete ajeno → 403

### Integration Tests (SkinController)

```bash
mvn test -Dtest=SkinControllerTest
```

Cubre:
- POST 201, POST validación 400, GET /me 200 (vacío/con datos), GET /{id} 200/404/403, GET UUID inválido 400, PUT 200/404/403, DELETE 204/404/403, sin token → 401

### Ejecutar Todos

```bash
mvn test
```

## 📝 Configuración

### application.properties (Producción y Desarrollo)

## 🐳 Docker

### Build

```bash
docker build -t storego/inventory-service:latest .
```

Multi-stage:
- **Stage 1 (maven:3.9-eclipse-temurin-17)**: Compila
- **Stage 2 (eclipse-temurin:17-jre-alpine)**: JRE mínimo, user no-root, healthcheck

### Docker Compose

```bash
docker-compose up -d
docker-compose logs -f inventory-service
docker-compose down
```

Variables por defecto en `docker-compose.yml` → editar si es necesario.

## 🔧 Desarrollo

### Estructura de Carpetas

```
src/main/java/com/storego/inventoryservice/
├── config/          → SecurityConfig
├── controller/      → SkinController (REST)
├── dto/             → CreateSkinRequest, UpdateSkinRequest, SkinResponse
├── entity/          → Skin, Rarity, Wear (JPA entities)
├── exception/       → SkinNotFoundException, SkinAccessDeniedException, GlobalExceptionHandler
├── mapper/          → SkinMapper (MapStruct)
├── repository/      → SkinRepository (JPA)
├── security/        → JwtAuthenticationFilter, JwtService
└── service/         → SkinService (lógica)

src/main/resources/
├── application.properties
└── db/migration/    → Flyway migrations

src/test/java/com/storego/inventoryservice/
├── controller/      → SkinControllerTest
└── service/         → SkinServiceTest
```

### Dependencias Clave

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.3</version>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct</artifactId>
    <version>1.6.0</version>
</dependency>
```

## 🚨 Troubleshooting

### Error: "JWT_SECRET not set"

```
Solución: Exportar variable
export JWT_SECRET=your-secret-key
```

### Error: "Connection refused" (PostgreSQL)

```
Solución: Verificar BD está corriendo
docker ps | grep postgres
```

### Error: "Validating JWT token" (logs)

```
Solución: Token expirado o secret incorrecto
- Verificar JWT_SECRET coincide con auth-service
- Renovar token (refresh)
```

### Error: "Cannot acquire a connection"

```
Solución: Pool agotado, revisar application.properties
spring.datasource.hikari.maximum-pool-size
```

## 📚 Referencias

- [Spring Boot 3.x Docs](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring Security](https://docs.spring.io/spring-security/reference/)
- [jjwt](https://github.com/jwtk/jjwt)
- [MapStruct](https://mapstruct.org/)
- [Flyway](https://flywaydb.org/)

## 🤝 Contribuir

1. Branch descriptivo: `feature/xxx` o `bugfix/xxx`
2. Tests + logs con SLF4J
3. Validaciones Bean Validation
4. Sin System.out.println — usar logger
5. PR con cobertura de tests

## 📄 Licencia

Interno — StoreGo.
