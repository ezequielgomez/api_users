# Diagrama de la solucion

## 1. Arquitectura de componentes

Arquitectura en capas: Spring Security -> Controller -> Service -> Repository -> Base de datos.

```mermaid
flowchart TB
    cliente["Cliente<br/>Postman / curl / Swagger UI"]

    subgraph app["Spring Boot - registro-usuarios-api"]
        direction TB

        subgraph seg["Spring Security - cadena de filtros"]
            filtro["JwtAuthFilter<br/>lee Authorization: Bearer y valida el JWT"]
            entry["AuthenticationEntryPoint<br/>401 en JSON"]
        end

        ctrl["UsuarioController<br/>POST /api/registro<br/>POST /api/login<br/>GET /api/usuarios"]

        subgraph servicio["Capa de servicio"]
            svc["UsuarioService (interfaz)<br/>UsuarioServiceImpl"]
            val["Validadores - Strategy + Chain of Responsibility<br/>1. ValidadorFormatoCorreo<br/>2. ValidadorContrasena<br/>3. ValidadorCorreoUnico"]
            mapper["UsuarioMapper<br/>entidad a DTO"]
            jwt["JwtService (interfaz)<br/>JwtServiceImpl - HS256"]
            bcrypt["PasswordEncoder<br/>BCrypt"]
        end

        repo["UsuarioRepository<br/>Spring Data JPA"]

        subgraph errores["Manejo de errores - siempre {mensaje}"]
            handler["GlobalExceptionHandler<br/>@RestControllerAdvice"]
            errctrl["JsonErrorController<br/>/error"]
        end

        swagger["OpenApiConfig<br/>Swagger UI + esquema Bearer"]
        props[("application.properties<br/>regex de correo y contrasena<br/>secreto y vigencia del JWT")]
    end

    db[("H2 en memoria<br/>USUARIOS / TELEFONOS")]
    consola["Consola H2<br/>/h2-console"]

    cliente -->|"HTTP + JSON"| filtro
    filtro -->|"token valido o ruta publica"| ctrl
    filtro -.->|"sin token o token invalido en /api/usuarios"| entry
    ctrl --> svc
    svc --> val
    svc --> mapper
    svc --> jwt
    svc --> bcrypt
    svc --> repo
    val --> repo
    repo --> db
    filtro --> jwt
    val -.-> props
    jwt -.-> props
    ctrl -.->|"excepciones"| handler
    cliente -.-> swagger
    cliente -.-> consola
    consola -.-> db
```

## 2. Secuencia: registro (`POST /api/registro`)

```mermaid
sequenceDiagram
    autonumber
    actor C as Cliente
    participant F as JwtAuthFilter
    participant UC as UsuarioController
    participant S as UsuarioServiceImpl
    participant V as Validadores
    participant J as JwtService
    participant R as UsuarioRepository
    participant DB as H2

    C->>F: POST /api/registro (JSON)
    F->>UC: ruta publica, sigue la cadena
    UC->>UC: @Valid campos obligatorios
    UC->>S: registrar(request)
    S->>S: normaliza el correo (trim + minusculas)
    S->>V: validar(request) en orden
    alt formato de correo invalido
        V-->>C: 400 {"mensaje": "El formato del correo es inválido"}
    else contrasena no cumple la regex
        V-->>C: 400 {"mensaje": app.validation.password-message}
    else correo ya existe
        V->>R: existsByEmailIgnoreCase
        V-->>C: 409 {"mensaje": "El correo ya registrado"}
    end
    S->>S: UUID, fechas, hash BCrypt de la contrasena
    S->>J: generarToken(id, email)
    J-->>S: JWT
    S->>R: save(usuario con token)
    R->>DB: INSERT USUARIOS + TELEFONOS
    S-->>UC: UsuarioResponse (via UsuarioMapper)
    UC-->>C: 201 Created + usuario, token, created, modified, last_login, isactive
```

## 3. Secuencia: login (`POST /api/login`)

```mermaid
sequenceDiagram
    autonumber
    actor C as Cliente
    participant UC as UsuarioController
    participant S as UsuarioServiceImpl
    participant R as UsuarioRepository
    participant P as PasswordEncoder
    participant J as JwtService

    C->>UC: POST /api/login {email, password}
    UC->>S: login(request)
    S->>R: findByEmailIgnoreCase(email)
    S->>P: matches(password, hash)
    alt correo inexistente o contrasena incorrecta
        S-->>C: 401 {"mensaje": "Correo o contraseña incorrectos"}
    else usuario inactivo
        S-->>C: 403 {"mensaje": "El usuario está inactivo"}
    end
    S->>J: generarToken(id, email)
    S->>S: actualiza last_login y token
    S->>R: save(usuario)
    S-->>UC: LoginResponse (via UsuarioMapper)
    UC-->>C: 200 OK + name, email, phones, token
```

## 4. Secuencia: listado protegido (`GET /api/usuarios`)

```mermaid
sequenceDiagram
    autonumber
    actor C as Cliente
    participant F as JwtAuthFilter
    participant J as JwtService
    participant E as AuthenticationEntryPoint
    participant UC as UsuarioController
    participant S as UsuarioServiceImpl
    participant R as UsuarioRepository

    C->>F: GET /api/usuarios + Authorization: Bearer token
    F->>J: obtenerEmail(token)
    alt sin header Authorization
        F->>E: no autenticado
        E-->>C: 401 {"mensaje": "Falta el token en el header Authorization"}
    else token invalido o vencido
        F->>E: no autenticado
        E-->>C: 401 {"mensaje": "Token inválido o vencido"}
    end
    F->>F: guarda el usuario en el SecurityContext
    F->>UC: request autenticado
    UC->>S: listar()
    S->>R: findAll()
    S-->>UC: lista de UsuarioListadoResponse (sin token)
    UC-->>C: 200 OK + arreglo de usuarios
```

## 5. Modelo de datos

```mermaid
erDiagram
    USUARIOS ||--o{ TELEFONOS : tiene
    USUARIOS {
        UUID id PK
        string name
        string email UK "unico, en minusculas"
        string password "hash BCrypt"
        timestamp created
        timestamp modified
        timestamp last_login
        string token "JWT persistido"
        boolean active "isactive en la API"
    }
    TELEFONOS {
        bigint id PK
        string number
        string citycode
        string contrycode
        UUID usuario_id FK
    }
```

## 6. Estructura de paquetes

```
com.example.usuarios
├── RegistroUsuariosApplication     Punto de entrada de Spring Boot
├── config/        OpenApiConfig            Swagger y esquema Bearer
├── controller/    UsuarioController        Endpoints REST
├── dto/           Requests y responses     Contrato JSON de la API
├── exception/     GlobalExceptionHandler, JsonErrorController, ApiException
├── mapper/        UsuarioMapper            Entidad a DTO
├── model/         Usuario, Telefono        Entidades JPA
├── repository/    UsuarioRepository        Acceso a datos
├── security/      SecurityConfig, JwtAuthFilter, JwtService, JwtServiceImpl
├── service/       UsuarioService (interfaz) y impl/UsuarioServiceImpl
└── validation/    ValidadorRegistro y sus 3 implementaciones
```
