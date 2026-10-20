# API de registro de usuarios

API RESTful (Spring Boot 3, Java 17, Maven, H2 en memoria) para registrar usuarios, hacer login
y listarlos. Todo entra y sale en JSON, incluidos los errores, con el formato `{"mensaje": "..."}`.

## Requisitos

- JDK 17 o superior.
- Maven 3.9+ (o un IDE con soporte Maven: Eclipse, STS o IntelliJ).
- No necesita instalar base de datos: usa H2 en memoria (los datos se pierden al reiniciar).

## 1. Levantar la aplicacion

**Desde la consola** (en la carpeta del proyecto):

```bash
mvn spring-boot:run
```

**Desde Eclipse / STS:**

1. `File > Import... > Maven > Existing Maven Projects`, seleccionar la carpeta `registro-usuarios-api` y `Finish`.
2. Clic derecho al proyecto > `Maven > Update Project...` (descarga las dependencias).
3. Clic derecho a `RegistroUsuariosApplication` > `Run As > Spring Boot App` (o `Java Application`).

La aplicacion esta lista cuando la consola muestra `Started RegistroUsuariosApplication`.
Queda escuchando en `http://localhost:8080`.

## 2. Probar con Swagger (lo mas rapido)

1. Abrir `http://localhost:8080/swagger-ui/index.html`.
2. **Registrar un usuario:** `POST /api/registro` > `Try it out`, pegar el JSON de ejemplo
   (seccion 3.1) y `Execute`. Debe responder `201`. Copiar el valor de `token`.
3. **Autorizarse:** boton **Authorize** (arriba a la derecha), pegar solo el token (sin la palabra
   `Bearer`) y `Authorize`.
4. **Listar usuarios:** `GET /api/usuarios` > `Try it out` > `Execute`. Debe responder `200` con
   el usuario creado.
5. **Login:** `POST /api/login` con el correo y la contrasena del paso 2. Devuelve un token nuevo,
   que tambien sirve para el paso 3.

## 3. Probar con curl o Postman

Los ejemplos usan `curl` (bash, Git Bash o WSL). En Postman: metodo y URL iguales, body `raw` de
tipo `JSON` y, donde corresponda, la pestana `Authorization` con tipo `Bearer Token`.

### 3.1 Registro: `POST /api/registro`

```bash
curl -i -X POST http://localhost:8080/api/registro \
  -H "Content-Type: application/json" \
  -d '{
        "name": "Juan Rodriguez",
        "email": "juan@rodriguez.org",
        "password": "Hunter2",
        "phones": [ { "number": "1234567", "citycode": "1", "contrycode": "57" } ]
      }'
```

Respuesta esperada `201 Created`:

```json
{
  "id": "0d7b1c0e-8a9e-4f5b-9a47-2f3a1c6d5e10",
  "name": "Juan Rodriguez",
  "email": "juan@rodriguez.org",
  "phones": [ { "number": "1234567", "citycode": "1", "contrycode": "57" } ],
  "created": "2026-10-18T13:24:00.123",
  "modified": "2026-10-18T13:24:00.123",
  "last_login": "2026-10-18T13:24:00.123",
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "isactive": true
}
```

Que verificar:
- `id` es un UUID.
- En un usuario nuevo, `created`, `modified` y `last_login` son iguales.
- La contrasena nunca aparece en la respuesta.

### 3.2 Login: `POST /api/login`

```bash
curl -i -X POST http://localhost:8080/api/login \
  -H "Content-Type: application/json" \
  -d '{ "email": "juan@rodriguez.org", "password": "Hunter2" }'
```

Respuesta esperada `200 OK`. Genera un token nuevo, lo guarda en la base y actualiza `last_login`:

```json
{
  "name": "Juan Rodriguez",
  "email": "juan@rodriguez.org",
  "phones": [ { "number": "1234567", "citycode": "1", "contrycode": "57" } ],
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

### 3.3 Listado: `GET /api/usuarios` (requiere token)

```bash
curl -i http://localhost:8080/api/usuarios \
  -H "Authorization: Bearer <TOKEN>"
```

Respuesta esperada `200 OK`: un arreglo con los usuarios (mismos campos del registro, sin `token`).

### 3.4 Casos de error

Todos responden JSON con el formato `{"mensaje": "..."}`.

| Que probar | Como | Status esperado |
|---|---|---|
| Correo ya registrado | Repetir el registro de 3.1 | `409` "El correo ya registrado" |
| Correo con formato invalido | Registro con `"email": "esto-no-es-un-correo"` | `400` "El formato del correo es inválido" |
| Contrasena que no cumple la regex | Registro con `"password": "hunter2"` (sin mayuscula), `"Secreto"` (sin numero) o `"Ab1"` (corta) | `400` con el mensaje de `app.validation.password-message` |
| Falta un campo obligatorio | Registro sin `name` | `400` "El nombre es obligatorio" |
| JSON mal formado | Body `{ esto no es json` | `400` "El cuerpo de la petición no es un JSON válido" |
| Content-Type que no es JSON | `-H "Content-Type: text/plain"` | `415` |
| Metodo no permitido | `GET /api/registro` | `405` |
| Ruta inexistente | `GET /api/no-existe` | `404` "Recurso no encontrado" |
| Login con datos incorrectos | Login con una contrasena equivocada | `401` "Correo o contraseña incorrectos" |
| Listado sin token | `GET /api/usuarios` sin header `Authorization` | `401` "Falta el token en el header Authorization" |
| Listado con token invalido | `-H "Authorization: Bearer abc"` | `401` "Token inválido o vencido" |

## Ver la base de datos (consola H2)

Con la aplicacion levantada, abrir `http://localhost:8080/h2-console` y conectarse con:

| Campo | Valor |
|---|---|
| JDBC URL | `jdbc:h2:mem:usuariosdb` |
| User Name | `sa` |
| Password | (vacio) |

Tablas: `USUARIOS` (incluye la columna `TOKEN` persistida y la contrasena hasheada con BCrypt) y `TELEFONOS`.

## 4. Tests automaticos

```bash
mvn test
```

En Eclipse/STS: clic derecho al proyecto > `Run As > Maven test` (o `JUnit Test`).

- `RegistroControllerTest`: registro exitoso, correo duplicado, validaciones y formato JSON de los errores.
- `LoginYListadoTest`: correo con formato `aaaaaaa@dominio.cl`, login correcto e incorrecto,
  listado sin token, con token invalido y con token valido.

## Configuracion (`src/main/resources/application.properties`)

| Propiedad | Para que |
|---|---|
| `app.validation.email-regex` | Expresion regular del correo |
| `app.validation.password-regex` | Expresion regular de la contrasena. Por defecto `^(?=.*[A-Z])(?=.*\d).{6,64}$`: minimo 6 caracteres, al menos una mayuscula y al menos un numero (ej. `Hunter2`) |
| `app.validation.password-message` | Mensaje de error cuando la contrasena no cumple la regex. Si se cambia la regex, actualizarlo tambien |
| `app.jwt.secret` | Secreto para firmar el JWT (minimo 32 caracteres). Se puede pasar con la variable de entorno `JWT_SECRET` |
| `app.jwt.expiration-minutes` | Vigencia del token, en minutos |

Para usar MySQL en vez de H2 hay un bloque comentado en el mismo archivo
(agregar tambien `mysql-connector-j` al `pom.xml`).

## Diagrama de la solucion

Ver [docs/diagrama-solucion.md](docs/diagrama-solucion.md): arquitectura de componentes,
secuencia de cada endpoint (registro, login, listado) y modelo de datos.

## Patrones de diseno

| Patron | Donde | Para que |
|---|---|---|
| Arquitectura en capas / MVC | `controller` -> `service` -> `repository` | Cada capa tiene una sola responsabilidad |
| Facade + inversion de dependencias | `UsuarioService` (interfaz) y `UsuarioServiceImpl` | El controller depende de la interfaz, no de la implementacion |
| Strategy | `validation/ValidadorRegistro` y sus implementaciones | Cada regla del registro (formato de correo, contrasena, correo unico) es una clase aparte |
| Chain of Responsibility | Lista de `ValidadorRegistro` en `UsuarioServiceImpl`; `JwtAuthFilter` en la cadena de Spring Security | Las reglas y filtros se ejecutan en orden; el primero que falla corta la cadena. Para agregar una regla nueva alcanza con crear otra clase |
| Mapper (DTO Assembler) | `mapper/UsuarioMapper` | Convierte entidades a DTOs en un solo lugar; la entidad (y la contrasena hasheada) nunca se expone |
| DTO | paquete `dto` | Separa el contrato JSON de la API de las entidades JPA |
| Repository | `UsuarioRepository` (Spring Data JPA) | Abstrae el acceso a datos |
| Template Method | `JwtAuthFilter` extiende `OncePerRequestFilter` | Spring define el algoritmo del filtro; la clase solo implementa `doFilterInternal` |
| Inyeccion de dependencias | Constructores de todos los beans; `PasswordEncoder` como `@Bean` en `SecurityConfig` | Nada se crea con `new` en los servicios; facilita cambiar implementaciones y testear |
| Manejo centralizado de errores | `GlobalExceptionHandler` (`@RestControllerAdvice`) y `JsonErrorController` | Un solo lugar traduce excepciones a `{"mensaje": "..."}` |

## Decisiones de diseno

- `id` es un UUID generado por la aplicacion.
- La contrasena se guarda con BCrypt y nunca se devuelve.
- El correo se normaliza a minusculas; no se puede registrar el mismo correo con distintas mayusculas.
- `token` es un JWT firmado (HS256) con el correo y el id. Se persiste junto con el usuario.
- Seguridad con Spring Security: un filtro (`JwtAuthFilter`) valida el JWT del header
  `Authorization`. Solo `/api/usuarios` lo exige (`SecurityConfig`).
- Los errores que no pasan por los controllers (filtros, firewall de Spring Security) los atiende
  `JsonErrorController`, para que tambien salgan como `{"mensaje": "..."}`.
