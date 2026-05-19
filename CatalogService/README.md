# catalog-service

Microservicio del sistema **StoreGo** que expone el catálogo público de skins de CS:GO.
Puerto: `8083`. Base de datos independiente (database-per-service). Sin dependencias HTTP en runtime.

---

## Requisitos

- Java 17
- Maven 3.9+
- Docker + Docker Compose
- PostgreSQL 15 (o usar Docker Compose)

---

## Variables de entorno

| Variable       | Descripción                                             | Default (local)                              |
|----------------|---------------------------------------------------------|----------------------------------------------|
| `DB_URL`       | JDBC URL de PostgreSQL                                  | `jdbc:postgresql://localhost:5432/catalog_db` |
| `DB_USERNAME`  | Usuario de BD                                           | `catalog_user`                               |
| `DB_PASSWORD`  | Contraseña de BD                                        | `catalog_pass`                               |
| `JWT_SECRET`   | Secret Base64 (≥256 bits). **Debe coincidir con auth-service** | —                                   |

---

## Levantar con Docker Compose

```bash
export JWT_SECRET="<tu-secret-base64>"
docker compose up --build
```

El servicio queda disponible en `http://localhost:8083`.
Swagger UI: `http://localhost:8083/swagger-ui.html`

> El puerto de PostgreSQL se expone en `5433` para no colisionar con instancias locales.

---

## Levantar en local (sin Docker)

```bash
# Requiere PostgreSQL corriendo en localhost:5432
export DB_URL=jdbc:postgresql://localhost:5432/catalog_db
export DB_USERNAME=catalog_user
export DB_PASSWORD=catalog_pass
export JWT_SECRET="<tu-secret-base64>"

mvn spring-boot:run
```

---

## Ejecutar tests

```bash
# Unit tests (sin Docker)
mvn test -pl . -Dtest="CatalogSkinControllerTest,CatalogSkinServiceTest"

# Integration tests (requiere Docker para Testcontainers)
mvn test -pl . -Dtest="CatalogSkinIntegrationTest"

# Todos los tests
mvn verify
```

---

## Endpoints

| Método | Path                    | Rol           | Descripción              |
|--------|-------------------------|---------------|--------------------------|
| GET    | `/catalog/skins`        | USER, ADMIN   | Lista paginada con filtros |
| GET    | `/catalog/skins/{id}`   | USER, ADMIN   | Detalle con rawData      |
| POST   | `/catalog/skins`        | ADMIN         | Crear skin               |
| POST   | `/catalog/skins/bulk`   | ADMIN         | Carga masiva             |
| PUT    | `/catalog/skins/{id}`   | ADMIN         | Actualizar skin          |
| DELETE | `/catalog/skins/{id}`   | ADMIN         | Eliminar skin            |

### Query params — GET /catalog/skins

| Param      | Tipo    | Descripción                                     |
|------------|---------|-------------------------------------------------|
| `page`     | int     | Página (default 0)                              |
| `size`     | int     | Tamaño (default 20, máx 100)                    |
| `sort`     | string  | Ej: `name,asc` (default)                        |
| `weapon`   | string  | Filtro por weapon_name (case-insensitive exacto)|
| `category` | string  | Filtro por category_name                        |
| `rarity`   | string  | Filtro por rarity_name                          |
| `stattrak` | boolean | Filtro stattrak                                 |
| `souvenir` | boolean | Filtro souvenir                                 |
| `q`        | string  | Búsqueda parcial en name (case-insensitive)     |

---

## Ejemplos curl

### Login previo (obtener JWT de auth-service)

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"diego123","password":"secret"}' | jq -r '.accessToken')
```

### GET lista (USER o ADMIN)

```bash
curl -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8083/catalog/skins?weapon=AK-47&rarity=Covert&page=0&size=10"
```

### GET detalle (USER o ADMIN)

```bash
curl -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8083/catalog/skins/skin-e757fd7191f9"
```

### POST crear skin (ADMIN)

```bash
curl -X POST http://localhost:8083/catalog/skins \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "id": "skin-e757fd7191f9",
    "name": "★ Hand Wraps | Spruce DDPAT",
    "description": "Preferred by hand-to-hand fighters...",
    "weapon": { "id": "leather_handwraps", "weapon_id": 5032, "name": "Hand Wraps" },
    "category": { "id": "sfui_invpanel_filter_gloves", "name": "Gloves" },
    "pattern": { "id": "handwrap_camo_grey", "name": "Spruce DDPAT" },
    "min_float": 0.06,
    "max_float": 0.8,
    "rarity": { "id": "rarity_ancient", "name": "Extraordinary", "color": "#eb4b4b" },
    "stattrak": false,
    "souvenir": false,
    "paint_index": "10010",
    "wears": [{ "id": "SFUI_InvTooltip_Wear_Amount_0", "name": "Factory New" }],
    "collections": [],
    "crates": [{ "id": "crate-4288", "name": "Glove Case", "image": "https://cdn.example.com/img.png" }],
    "team": { "id": "both", "name": "Both Teams" },
    "legacy_model": false,
    "image": "https://cdn.example.com/skin.png",
    "original": { "name": "leather_handwraps" }
  }'
```

### POST bulk (ADMIN)

```bash
curl -X POST http://localhost:8083/catalog/skins/bulk \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"skins": [ { ...skin1 }, { ...skin2 } ]}'
```

### DELETE (ADMIN)

```bash
curl -X DELETE http://localhost:8083/catalog/skins/skin-e757fd7191f9 \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

---

## Seed futuro

El catálogo completo de CS:GO se importará desde el repositorio público
[nicklvsa/go-csgo](https://github.com/nicklvsa/go-csgo) u equivalente.
El endpoint `POST /catalog/skins/bulk` está diseñado para recibir el JSON
en el formato nativo de ese repositorio sin transformación previa.

El proceso de seed correrá como job independiente en fase posterior y no
afecta la disponibilidad del servicio.

---

## Notas de arquitectura

- `catalog-service` tiene **cero acoplamiento HTTP** con otros microservicios.
- `inventory-service` referencia `catalogSkinId` como string sin validación HTTP.
- Eliminar una skin del catálogo **no afecta** inventarios existentes (consistencia eventual tolerada).
- `raw_data` (JSONB) es source-of-truth para info detallada; columnas planas sirven para queries/filtros.
