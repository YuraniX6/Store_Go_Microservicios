# Build Instructions

## Quick Start - Local Development

### 1. Configuración Local de PostgreSQL

```bash
# En Linux/Mac
psql -U postgres
CREATE DATABASE storego_auth;
\q

# En Windows con pgAdmin
# Crear database "storego_auth" manualmente
```

### 2. Variables de Entorno

Copiar `.env.example` a `.env` y configurar:

```bash
cp .env.example .env
```

O establecer variables:

```bash
# Linux/Mac
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=storego_auth
export DB_USER=postgres
export DB_PASSWORD=password
export JWT_SECRET=YXV0aEtzZWNyZXRrZXlmb3JzdG9yZWdvbWljcm9zZXJ2aWNlYXV0aGVudGljYXRpb24=
```

### 3. Compilar y Ejecutar

```bash
# Compilar
mvn clean install

# Ejecutar
mvn spring-boot:run

# O ejecutar JAR compilado
java -jar target/auth-service-1.0.0.jar
```

La app estará en: `http://localhost:8080`

## Docker Deployment

### Usando docker-compose (Recomendado)

```bash
# 1. Copiar variables de entorno
cp .env.example .env

# 2. Editar .env con valores reales (opcional)
# nano .env

# 3. Construir imagen
docker build -t auth-service:latest .

# 4. Ejecutar con docker-compose
docker-compose up -d

# 5. Ver logs
docker-compose logs -f auth-service

# 6. Detener
docker-compose down
```

### Docker Manual

```bash
# Construir imagen
docker build -t storego/auth-service:1.0.0 .

# Ejecutar contenedor
docker run -d \
  --name auth-service \
  -p 8080:8080 \
  -e DB_HOST=postgres \
  -e DB_PORT=5432 \
  -e DB_NAME=storego_auth \
  -e DB_USER=postgres \
  -e DB_PASSWORD=postgres \
  -e JWT_SECRET=YXV0aEtzZWNyZXRrZXlmb3JzdG9yZWdvbWljcm9zZXJ2aWNlYXV0aGVudGljYXRpb24= \
  storego/auth-service:1.0.0
```

## Testing

### Ejecutar Tests

```bash
# Todos los tests
mvn test

# Tests específicos
mvn test -Dtest=AuthServiceTest
mvn test -Dtest=JwtServiceTest

# Con cobertura
mvn test jacoco:report
# Reporte en: target/site/jacoco/index.html
```

### Verificar Cobertura

```bash
# Generar reporte de cobertura
mvn clean test jacoco:report

# Abrir reporte en navegador
open target/site/jacoco/index.html  # Mac
xdg-open target/site/jacoco/index.html  # Linux
start target/site/jacoco/index.html  # Windows
```

## Production Deployment

### Kubernetes (opcional)

```bash
# Crear namespace
kubectl create namespace storego

# Crear secret para variables
kubectl create secret generic auth-service-secrets \
  --from-literal=db-host=postgres.storego.svc.cluster.local \
  --from-literal=db-port=5432 \
  --from-literal=db-name=storego_auth \
  --from-literal=db-user=postgres \
  --from-literal=db-password=<PASSWORD> \
  --from-literal=jwt-secret=<JWT_SECRET> \
  -n storego

# Desplegar imagen
kubectl apply -f k8s/ -n storego
```

## Verificación de Instalación

### 1. Health Check

```bash
curl -X GET http://localhost:8080/swagger-ui.html
```

### 2. Registrar Usuario

```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123"
  }'
```

### 3. Login

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

### 4. Usar Token

```bash
# Obtener token del login anterior
TOKEN="eyJhbGc..."

# Validar token
curl -X GET http://localhost:8080/auth/validate \
  -H "Authorization: Bearer $TOKEN"

# Obtener user info
curl -X GET http://localhost:8080/auth/me \
  -H "Authorization: Bearer $TOKEN"
```

## Troubleshooting

### Error: Cannot connect to PostgreSQL

```
1. Verificar que PostgreSQL está corriendo
   - Windows: Services > PostgreSQL
   - Mac: brew services list
   - Linux: systemctl status postgresql

2. Verificar credenciales en application.properties
3. Verificar puerto (por defecto 5432)
4. Verificar que la BD existe: psql -U postgres -l
```

### Error: JWT_SECRET not set

```
1. Generar secret válido (Base64, min 32 chars)
   - echo -n "your-secret-key-minimum-32-characters" | base64

2. Establecer variable:
   export JWT_SECRET=<base64-value>

3. O editar application.properties
```

### Error: Port 8080 in use

```
1. Cambiar puerto en application.properties:
   server.port=8081

2. O liberar puerto:
   - Linux/Mac: lsof -ti:8080 | xargs kill -9
   - Windows: netstat -ano | findstr :8080
            taskkill /PID <PID> /F
```

## Database Initialization

La aplicación ejecuta automáticamente:

1. **Crear tablas** (JPA DDL auto-update)
2. **Crear roles** (ADMIN, USER)
3. **Crear usuario admin**

Si necesita reinicializar:

```bash
# Limpiar BD
DROP DATABASE storego_auth;
CREATE DATABASE storego_auth;

# La app recreará automáticamente al iniciar
```

## Monitoring

### Logs

```bash
# Ver logs en tiempo real
docker-compose logs -f auth-service

# Ver logs últimas 100 líneas
docker logs -n 100 auth-service

# Guardar logs en archivo
docker logs auth-service > logs.txt 2>&1
```

### Métricas (Opcional)

Para agregar métricas, añadir a pom.xml:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

Endpoints:
- `http://localhost:8080/actuator/health`
- `http://localhost:8080/actuator/metrics`

## Build Pipeline (CI/CD)

### GitHub Actions

```yaml
name: Build and Test

on: [push, pull_request]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
      - name: Build
        run: mvn clean install
      - name: Test
        run: mvn test
```

## Notas Importantes

1. **NUNCA** usar contraseña "admin123" en producción
2. Cambiar JWT_SECRET a valor único y seguro
3. Usar HTTPS en producción
4. Configurar CORS si es necesario
5. Implementar rate limiting
6. Monitorear logs regularmente
7. Hacer backup de BD regularmente
8. Implementar alertas de errores
