# Diagramas de Flujo - StoreGo Auth Service

## 1. Flujo de Registro (Sign Up)

```
┌─────────────────────────────────────────────────────────────┐
│                       CLIENTE                                │
│                   (Frontend/App)                             │
└────────────────────┬────────────────────────────────────────┘
                     │ POST /auth/register
                     │ {username, email, password}
                     ▼
        ┌────────────────────────────┐
        │   AuthController           │
        │  register()                │
        └────────┬───────────────────┘
                 │
                 ▼
        ┌────────────────────────────┐
        │    AuthService             │
        │  register()                │
        │  - Check username exists   │
        │  - Check email exists      │
        │  - Encode password (BCrypt)│
        │  - Assign USER role        │
        │  - Save user               │
        └────────┬───────────────────┘
                 │
                 ▼
        ┌────────────────────────────┐
        │   UserRepository           │
        │  save(User)                │
        └────────┬───────────────────┘
                 │
                 ▼
        ┌────────────────────────────┐
        │    PostgreSQL              │
        │  INSERT INTO users         │
        └────────┬───────────────────┘
                 │
                 ▼
        ┌────────────────────────────┐
        │    JwtService              │
        │  generateToken()           │
        │  generateRefreshToken()    │
        └────────┬───────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────────────────────┐
│ 201 Created                                                  │
│ {                                                            │
│   "token": "eyJhbGc...",                                    │
│   "refreshToken": "eyJhbGc...",                            │
│   "username": "newuser",                                    │
│   "roles": [1]                                             │
│ }                                                            │
└─────────────────────────────────────────────────────────────┘
```

---

## 2. Flujo de Login

```
┌─────────────────────────────────────────────────────────────┐
│                       CLIENTE                                │
│                   (Frontend/App)                             │
└────────────────────┬────────────────────────────────────────┘
                     │ POST /auth/login
                     │ {username, password}
                     ▼
        ┌────────────────────────────┐
        │   AuthController           │
        │  login()                   │
        └────────┬───────────────────┘
                 │
                 ▼
        ┌────────────────────────────┐
        │  AuthenticationManager     │
        │  authenticate()            │
        │  - Find user by username   │
        │  - Compare passwords       │
        └────────┬───────────────────┘
                 │
                 ▼
        ┌────────────────────────────┐
        │  CustomUserDetailsService  │
        │  loadUserByUsername()      │
        │  - Get from DB             │
        │  - Create CustomUserDetails│
        └────────┬───────────────────┘
                 │
                 ▼
        ┌────────────────────────────┐
        │   BCryptPasswordEncoder    │
        │  matches()                 │
        │  - Validate password       │
        └────────┬───────────────────┘
                 │ Valid
                 ▼
        ┌────────────────────────────┐
        │    JwtService              │
        │  generateToken()           │
        │  generateRefreshToken()    │
        └────────┬───────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────────────────────┐
│ 200 OK                                                       │
│ {                                                            │
│   "token": "eyJhbGc...",                                    │
│   "refreshToken": "eyJhbGc...",                            │
│   "username": "admin",                                      │
│   "roles": [2]                                             │
│ }                                                            │
└─────────────────────────────────────────────────────────────┘
```

---

## 3. Flujo de Solicitud Protegida (JWT Authentication)

```
┌─────────────────────────────────────────────────────────────┐
│                       CLIENTE                                │
│                   (Frontend/App)                             │
└────────────────────┬────────────────────────────────────────┘
                     │ GET /auth/me
                     │ Authorization: Bearer eyJhbGc...
                     ▼
        ┌────────────────────────────┐
        │   JwtAuthenticationFilter  │
        │  doFilterInternal()        │
        │  - Extract token from header
        │  - Extract userId (subject)│
        │  - Validate token         │
        └────────┬───────────────────┘
                 │
                 ▼
        ┌────────────────────────────┐
        │    JwtService              │
        │  extractSubject()          │
        │  isTokenValid()            │
        │  - Check signature         │
        │  - Check expiration        │
        │  - Check userId match      │
        └────────┬───────────────────┘
                 │ Valid
                 ▼
        ┌────────────────────────────┐
        │  CustomUserDetailsService  │
        │  loadUserById()            │
        │  - Get user from DB        │
        │  - Create CustomUserDetails│
        └────────┬───────────────────┘
                 │
                 ▼
        ┌────────────────────────────┐
        │  SecurityContextHolder     │
        │  setAuthentication()       │
        │  - Set auth in context     │
        └────────┬───────────────────┘
                 │
                 ▼
        ┌────────────────────────────┐
        │   AuthController           │
        │  getCurrentUser()          │
        │  @AuthenticationPrincipal  │
        │    CustomUserDetails       │
        └────────┬───────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────────────────────┐
│ 200 OK                                                       │
│ {                                                            │
│   "username": "admin",                                      │
│   "email": "admin@storego.com",                            │
│   "roles": [2]                                             │
│ }                                                            │
└─────────────────────────────────────────────────────────────┘
```

---

## 4. Flujo de Refresh Token

```
┌─────────────────────────────────────────────────────────────┐
│                       CLIENTE                                │
│                   (Frontend/App)                             │
│            (Token expirado, usa RefreshToken)               │
└────────────────────┬────────────────────────────────────────┘
                     │ POST /auth/refresh
                     │ Authorization: Bearer old_token
                     │ {refreshToken: "eyJhbGc..."}
                     ▼
        ┌────────────────────────────┐
        │   AuthController           │
        │  refreshToken()            │
        │  @PreAuthorize("auth()")   │
        └────────┬───────────────────┘
                 │
                 ▼
        ┌────────────────────────────┐
        │    AuthService             │
        │  refreshToken()            │
        │  - Extract userId from     │
        │    refreshToken            │
        │  - Validate refreshToken   │
        │  - Get user from DB        │
        └────────┬───────────────────┘
                 │
                 ▼
        ┌────────────────────────────┐
        │    JwtService              │
        │  extractSubject()          │
        │  isTokenValid()            │
        │  generateToken()           │
        │  generateRefreshToken()    │
        └────────┬───────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────────────────────┐
│ 200 OK                                                       │
│ {                                                            │
│   "token": "eyJhbGc..." (NUEVO),                           │
│   "refreshToken": "eyJhbGc..." (NUEVO),                   │
│   "username": "admin",                                      │
│   "roles": [2]                                             │
│ }                                                            │
└─────────────────────────────────────────────────────────────┘
```

---

## 5. Arquitectura de Componentes

```
┌───────────────────────────────────────────────────────────────┐
│                     CLIENTE (Frontend)                         │
└───────────────────────────────────────────────────────────────┘
                           │
                    HTTP/REST API
                           │
         ┌─────────────────┼─────────────────┐
         │                 │                 │
    ┌────▼────┐      ┌─────▼─────┐     ┌───▼────┐
    │ Public  │      │ Protected │     │ Swagger│
    │ Endpoints      │ Endpoints │     │  Docs  │
    └────┬────┘      └─────┬─────┘     └───┬────┘
         │                 │                │
         └────────┬────────┴────────┬───────┘
                  │                 │
          ┌───────▼─────────────────▼───────┐
          │    JwtAuthenticationFilter      │
          │  - Validates JWT on every call  │
          └───────┬───────────────────────┬─┘
                  │                       │
          ┌───────▼────────┐      ┌──────▼──────┐
          │   JwtService   │      │ CustomUser  │
          │  - Generate    │      │ Details     │
          │  - Validate    │      │ Service     │
          │  - Extract     │      │             │
          └───┬──────┬─────┘      └──────┬──────┘
              │      │                   │
         ┌────▼──────▼────────────────────▼─────┐
         │      Spring Security Context        │
         │    (@PreAuthorize, @Authentication)  │
         └────┬────────────────────────────────┘
              │
    ┌─────────▼─────────┐
    │   Controllers     │
    │  & Services       │
    │  - AuthService    │
    │  - UserDetails    │
    │  - etc            │
    └─────────┬─────────┘
              │
    ┌─────────▼──────────────┐
    │  Repositories          │
    │  - UserRepository      │
    │  - RoleRepository      │
    └─────────┬──────────────┘
              │
         ┌────▼──────────┐
         │  PostgreSQL   │
         │  - users      │
         │  - roles      │
         └───────────────┘
```

---

## 6. Estados del Token JWT

```
JWT Token Lifecycle
═════════════════════════════════════════════════════

1. GENERACIÓN (Login/Register)
   ├─ Claims: sub (userId), username, roles
   ├─ Algoritmo: HMAC-SHA256
   └─ Expiración: 24 horas desde ahora

2. TRANSMISIÓN
   ├─ Header: Authorization: Bearer <token>
   ├─ Transport: HTTP (HTTPS en producción)
   └─ Storage: Cliente (localStorage, session, etc)

3. VALIDACIÓN (Cada solicitud protegida)
   ├─ Extrae token del header
   ├─ Verifica firma HMAC-SHA256
   ├─ Comprueba expiración
   ├─ Valida que subject == userId
   └─ Si válido → Autentica al usuario

4. EXPIRACIÓN
   ├─ Después de 24 horas
   ├─ Cliente recibe 401 Unauthorized
   ├─ Usa RefreshToken para obtener nuevo token
   └─ RefreshToken válido por 24 días

5. REFRESH (Token expirado pero RefreshToken válido)
   ├─ Cliente envía RefreshToken
   ├─ Backend valida RefreshToken
   ├─ Genera nuevo Token (24h)
   ├─ Genera nuevo RefreshToken (24d)
   └─ Cliente actualiza tokens

6. INVALIDACIÓN
   ├─ Token revocado manualmente
   ├─ Usuario desactivado
   ├─ Cambio de contraseña
   └─ Sesión cerrada
```

---

## 7. Flujo de Validación en Endpoint Protegido

```
Request con JWT
     │
     ▼
┌──────────────────────────────────────────┐
│ 1. JwtAuthenticationFilter               │
│    - Extrae Authorization header        │
│    - Extrae token después de "Bearer "   │
└────────────────┬─────────────────────────┘
                 │
                 ▼ Token encontrado
         ┌───────────────────────┐
         │ 2. JwtService         │
         │    - Valida firma     │
         │    - Valida fecha exp │
         │    - Extrae subject   │
         └────┬────────────┬─────┘
              │ Válido     │ Inválido
              ▼            ▼
       ┌──────────────┐   ┌─────────────────┐
       │ Continuar    │   │ Log error,      │
       │ autenticación│   │ continua sin auth
       └──────┬───────┘   └────────┬────────┘
              │                    │
              ▼                    │
      ┌────────────────────────┐   │
      │ 3. CustomUserDetails   │   │
      │    Service             │   │
      │    loadUserById()      │   │
      └──────┬─────────────────┘   │
             │                      │
             ▼ Usuario encontrado   │
      ┌────────────────────────┐   │
      │ 4. SecurityContextHolder   │
      │    setAuthentication()  │   │
      └──────┬─────────────────┘   │
             │                      │
             ▼                      │
      ┌────────────────────────┐   │
      │ 5. Controller método   │   │
      │    @PreAuthorize       │   │
      │    @AuthenticationPrinc│   │
      └──────┬─────────────────┘   │
             │                      │
    ┌────────┘                      │
    │ Usuario autenticado           │
    │ Disponible en método          │
    │                               │
    └─────────────────────┬─────────┘
                          │
                    ┌─────▼──────────────┐
                    │ Respuesta:         │
                    │ - 200 OK (Auth)    │
                    │ - 401 Unauth (No)  │
                    └────────────────────┘
```

---

## 8. Roles y Permisos

```
┌─────────────────────────────────────────────────────┐
│                    AUTORIZACIÓN                      │
└─────────────────────────────────────────────────────┘

┌────────────────────────┐        ┌──────────────────┐
│      User (id=1)       │        │   Admin (id=2)   │
├────────────────────────┤        ├──────────────────┤
│ ROLE: ROLE_USER        │        │ ROLE: ROLE_ADMIN │
│ Authority: USER        │        │ Authority: ADMIN │
│                        │        │                  │
│ Acceso:                │        │ Acceso:          │
│ ✓ /auth/login          │        │ ✓ /auth/login    │
│ ✓ /auth/register       │        │ ✓ /auth/register │
│ ✓ /auth/validate       │        │ ✓ /auth/validate │
│ ✓ /auth/refresh        │        │ ✓ /auth/refresh  │
│ ✓ /auth/me             │        │ ✓ /auth/me       │
│ ✗ Endpoints privados   │        │ ✓ Todos (admin)  │
└────────────────────────┘        └──────────────────┘

@PreAuthorize("isAuthenticated()") → Todos autenticados
@PreAuthorize("hasRole('ADMIN')") → Solo ADMIN
@PreAuthorize("hasRole('USER')") → Solo USER
@PreAuthorize("hasAnyRole('ADMIN','USER')") → Ambos
```

---

## 9. Manejo de Errores

```
Solicitud
    │
    ▼
┌─────────────────────────────────────────────┐
│ GlobalExceptionHandler                      │
├─────────────────────────────────────────────┤
│                                             │
│ RuntimeException                            │
│ └─ 400 BAD_REQUEST                         │
│    └─ {"error": "mensaje"}                 │
│                                             │
│ BadCredentialsException                     │
│ └─ 401 UNAUTHORIZED                        │
│    └─ {"error": "Invalid credentials"}     │
│                                             │
│ MethodArgumentNotValidException             │
│ └─ 400 BAD_REQUEST                         │
│    └─ {"errors": {"campo": "mensaje"}}     │
│                                             │
│ Exception (genérica)                        │
│ └─ 500 INTERNAL_SERVER_ERROR               │
│    └─ {"error": "Internal server error"}   │
│                                             │
└─────────────────────────────────────────────┘
    │
    ▼
Response JSON
```

---

## 10. Ciclo de Vida de una Request

```
REQUEST CHAIN
═════════════════════════════════════════════════════

1. SERVLET FILTER CHAIN
   ├─ JwtAuthenticationFilter
   │  ├─ Extrae JWT
   │  ├─ Valida firma y expiración
   │  └─ Autentica al usuario
   └─ (Otros filtros Spring Security)

2. DISPATCHER SERVLET
   ├─ Mapea a @RequestMapping
   └─ Selecciona Controller method

3. INTERCEPTORS (si hay)
   ├─ Pre-handle
   └─ Post-handle

4. CONTROLLER METHOD
   ├─ @PreAuthorize verifica permisos
   ├─ Executa método
   └─ Retorna ResponseEntity

5. EXCEPTION HANDLER (si error)
   ├─ GlobalExceptionHandler
   └─ Convierte a JSON

6. RESPONSE
   ├─ Status Code (200, 201, 400, 401, 500)
   ├─ Headers
   └─ JSON Body
```

Todos estos flujos están implementados y testeados en el proyecto.
