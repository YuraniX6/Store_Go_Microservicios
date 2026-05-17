# API Reference

## Base URL

```
http://localhost:8080
```

## Authentication

Todos los endpoints protegidos requieren un header JWT:

```
Authorization: Bearer <token>
```

## Endpoints

### 1. Register (Público)

**Endpoint:** `POST /auth/register`

**Descripción:** Registrar nuevo usuario

**Body:**
```json
{
  "username": "string (3-50 caracteres, requerido)",
  "email": "string (email válido, requerido)",
  "password": "string (mínimo 6 caracteres, requerido)"
}
```

**Response (201 Created):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "newuser",
  "roles": [1]
}
```

**Error (400):**
```json
{
  "status": "BAD_REQUEST",
  "error": "Username already exists"
}
```

---

### 2. Login (Público)

**Endpoint:** `POST /auth/login`

**Descripción:** Autenticar usuario y obtener JWT

**Body:**
```json
{
  "username": "string (requerido)",
  "password": "string (requerido)"
}
```

**Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "admin",
  "roles": [2]
}
```

**Error (401):**
```json
{
  "status": "UNAUTHORIZED",
  "error": "Invalid username or password"
}
```

---

### 3. Validate Token (Público)

**Endpoint:** `GET /auth/validate`

**Descripción:** Validar si un JWT es válido

**Headers:**
```
Authorization: Bearer <token>
```

**Response (200 OK):**
```
true  o  false
```

---

### 4. Refresh Token (Protegido)

**Endpoint:** `POST /auth/refresh`

**Descripción:** Generar nuevo JWT usando refresh token

**Headers:**
```
Authorization: Bearer <current_token>
```

**Body:**
```json
{
  "refreshToken": "string (requerido)"
}
```

**Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "admin",
  "roles": [2]
}
```

---

### 5. Get Current User (Protegido)

**Endpoint:** `GET /auth/me`

**Descripción:** Obtener información del usuario actual

**Headers:**
```
Authorization: Bearer <token>
```

**Response (200 OK):**
```json
{
  "username": "admin",
  "email": "admin@storego.com",
  "roles": [2]
}
```

**Error (401):**
```json
{
  "error": "Unauthorized"
}
```

---

## Status Codes

| Código | Descripción |
|--------|------------|
| 200 | OK - Solicitud exitosa |
| 201 | Created - Recurso creado |
| 400 | Bad Request - Error de validación |
| 401 | Unauthorized - No autenticado o token inválido |
| 404 | Not Found - Recurso no encontrado |
| 500 | Internal Server Error - Error del servidor |

---

## Ejemplos cURL

### Register

```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "newuser",
    "email": "newuser@example.com",
    "password": "password123"
  }'
```

### Login

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

### Validate Token

```bash
TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

curl -X GET http://localhost:8080/auth/validate \
  -H "Authorization: Bearer $TOKEN"
```

### Get Current User

```bash
TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

curl -X GET http://localhost:8080/auth/me \
  -H "Authorization: Bearer $TOKEN"
```

### Refresh Token

```bash
REFRESH_TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

curl -X POST http://localhost:8080/auth/refresh \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "refreshToken": "'$REFRESH_TOKEN'"
  }'
```

---

## Ejemplos Python

```python
import requests
import json

BASE_URL = "http://localhost:8080"

# Register
response = requests.post(
    f"{BASE_URL}/auth/register",
    json={
        "username": "newuser",
        "email": "newuser@example.com",
        "password": "password123"
    }
)
print(response.json())

# Login
response = requests.post(
    f"{BASE_URL}/auth/login",
    json={
        "username": "admin",
        "password": "admin123"
    }
)
data = response.json()
token = data['token']

# Get Current User
headers = {"Authorization": f"Bearer {token}"}
response = requests.get(f"{BASE_URL}/auth/me", headers=headers)
print(response.json())

# Validate Token
response = requests.get(
    f"{BASE_URL}/auth/validate",
    headers=headers
)
print(response.text)

# Refresh Token
response = requests.post(
    f"{BASE_URL}/auth/refresh",
    json={"refreshToken": data['refreshToken']},
    headers=headers
)
print(response.json())
```

---

## Ejemplos JavaScript/Axios

```javascript
import axios from 'axios';

const BASE_URL = 'http://localhost:8080';

// Register
async function register(username, email, password) {
  try {
    const response = await axios.post(`${BASE_URL}/auth/register`, {
      username,
      email,
      password
    });
    return response.data;
  } catch (error) {
    console.error(error.response.data);
  }
}

// Login
async function login(username, password) {
  try {
    const response = await axios.post(`${BASE_URL}/auth/login`, {
      username,
      password
    });
    return response.data;
  } catch (error) {
    console.error(error.response.data);
  }
}

// Get Current User
async function getCurrentUser(token) {
  try {
    const response = await axios.get(`${BASE_URL}/auth/me`, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });
    return response.data;
  } catch (error) {
    console.error(error.response.data);
  }
}

// Validate Token
async function validateToken(token) {
  try {
    const response = await axios.get(`${BASE_URL}/auth/validate`, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });
    return response.data;
  } catch (error) {
    return false;
  }
}

// Refresh Token
async function refreshToken(refreshToken, currentToken) {
  try {
    const response = await axios.post(`${BASE_URL}/auth/refresh`, {
      refreshToken
    }, {
      headers: {
        'Authorization': `Bearer ${currentToken}`
      }
    });
    return response.data;
  } catch (error) {
    console.error(error.response.data);
  }
}

// Usage
async function main() {
  // Register
  const register = await register('newuser', 'newuser@example.com', 'password123');
  console.log('Register:', register);
  
  // Login
  const auth = await login('admin', 'admin123');
  console.log('Login:', auth);
  
  // Get Current User
  const user = await getCurrentUser(auth.token);
  console.log('Current User:', user);
  
  // Validate Token
  const isValid = await validateToken(auth.token);
  console.log('Token Valid:', isValid);
  
  // Refresh Token
  const newAuth = await refreshToken(auth.refreshToken, auth.token);
  console.log('New Auth:', newAuth);
}

main();
```

---

## JWT Payload

Estructura del token JWT:

```json
{
  "sub": "550e8400-e29b-41d4-a716-446655440000",  // UUID del usuario
  "username": "admin",
  "roles": [2],                                    // IDs de roles
  "iat": 1234567890,
  "exp": 1234571490
}
```

---

## Errores Comunes

### 400 - Username already exists
```json
{
  "error": "Username already exists",
  "status": "BAD_REQUEST"
}
```

**Solución:** Usar un username único

### 400 - Email already exists
```json
{
  "error": "Email already exists",
  "status": "BAD_REQUEST"
}
```

**Solución:** Usar un email único

### 401 - Invalid username or password
```json
{
  "error": "Invalid username or password",
  "status": "UNAUTHORIZED"
}
```

**Solución:** Verificar credenciales

### 400 - Username must be between 3 and 50 characters
```json
{
  "status": "BAD_REQUEST",
  "errors": {
    "username": "Username must be between 3 and 50 characters"
  }
}
```

**Solución:** Username debe tener entre 3 y 50 caracteres

### 400 - Email should be valid
```json
{
  "status": "BAD_REQUEST",
  "errors": {
    "email": "Email should be valid"
  }
}
```

**Solución:** Usar formato de email válido

---

## Notas Importantes

1. **JWT Expiration:** 24 horas
2. **Refresh Token Expiration:** 24 días
3. **Roles:** ADMIN (id=2), USER (id=1)
4. **Default Admin:** username="admin", password="admin123"
5. **Bearer Token:** Incluir "Bearer " antes del token
6. **Content-Type:** Usar "application/json" en request body
