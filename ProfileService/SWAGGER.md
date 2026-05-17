# Profile Service - Documentación de API

## Acceso a Swagger UI

La documentación interactiva de la API está disponible en Swagger UI, donde puedes explorar todos los endpoints, ver ejemplos de request/response y probar la API directamente.

### URL de Swagger UI

```
http://localhost:8081/api/v1/swagger-ui.html
```

### URL del archivo OpenAPI (JSON)

```
http://localhost:8081/api/v1/v3/api-docs
```

## Características de la Documentación

- **Documentación completa de endpoints**: Todos los endpoints disponibles están documentados con descripciones detalladas
- **Esquemas de request/response**: Cada DTO está completamente documentado con ejemplos y validaciones
- **Seguridad JWT**: La documentación incluye autenticación JWT Bearer
- **Códigos de respuesta**: Se documentan todos los códigos HTTP posibles (201, 200, 400, 401, 404, 409)

## Endpoints Documentados

### 1. Crear Perfil
- **POST** `/api/v1/users`
- **Autenticación**: JWT Bearer requerida
- **Descripción**: Crea un nuevo perfil para el usuario autenticado
- **Validaciones**:
  - RUT único (no puede repetirse)
  - Nombre completo requerido (1-150 caracteres)
  - Idioma ISO 639-1 (ej: es, en, pt)
  
### 2. Obtener Mi Perfil
- **GET** `/api/v1/users/me`
- **Autenticación**: JWT Bearer requerida
- **Descripción**: Obtiene el perfil completo del usuario autenticado (incluye RUT)

### 3. Obtener Perfil Público
- **GET** `/api/v1/users/{uuid}`
- **Autenticación**: JWT Bearer requerida
- **Descripción**: Obtiene el perfil público de otro usuario (sin RUT)

### 4. Actualizar Mi Perfil
- **PATCH** `/api/v1/users/me`
- **Autenticación**: JWT Bearer requerida
- **Descripción**: Actualiza el perfil del usuario autenticado (todos los campos opcionales, RUT no se puede modificar)

## Cómo Usar Swagger UI

1. **Accede a Swagger**: Abre http://localhost:8081/api/v1/swagger-ui.html en tu navegador

2. **Autenticación con JWT**:
   - Haz clic en el botón "Authorize" en la parte superior derecha
   - Selecciona "Bearer JWT"
   - Pega tu token JWT en el campo "Value"
   - Haz clic en "Authorize"

3. **Prueba un endpoint**:
   - Haz clic en un endpoint para expandirlo
   - Haz clic en "Try it out"
   - Completa los parámetros necesarios
   - Haz clic en "Execute"
   - Verás la respuesta, headers y curl command

## Configuración

La documentación está habilitada por defecto. Para desabilitar, agrega a `application.yml`:

```yaml
springdoc:
  swagger-ui:
    enabled: false
```

## Acceso sin Autenticación

Los siguientes endpoints son accesibles sin autenticación:
- `GET /actuator/health` - Health check
- `GET /api/v1/swagger-ui/**` - Swagger UI
- `GET /api/v1/v3/api-docs/**` - OpenAPI spec

## Información Adicional

- **Versión Spring Boot**: 3.2.3
- **Versión Springdoc OpenAPI**: 2.3.0
- **Formato de documentación**: OpenAPI 3.0.0

