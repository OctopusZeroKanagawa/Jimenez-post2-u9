# Unidad 9 — Seguridad en Aplicaciones Web · Post-Contenido 2

**Programación Web · Ingeniería de Sistemas · UFPS · 2026**  
**Autor:** Andres Felipe Jimenez Ramirez

> Extensión del Post-Contenido 1. Requiere el proyecto anterior funcionando con login, registro y roles ADMIN/USER.

---

## Descripción

Verificación activa de las protecciones de seguridad extendidas con: autorización a nivel de método mediante `@PreAuthorize` con distintas expresiones SpEL, página de error 403 personalizada, mitigación de XSS comprobada con Thymeleaf `th:text`, cabecera Content-Security-Policy y verificación de protección CSRF.

---

## Tecnologías utilizadas

| Tecnología | Versión | Rol |
|---|---|---|
| Java | 17 | Lenguaje principal |
| Spring Boot | 3.2.5 | Framework base |
| Spring Security 6 | 6.2.4 | Autenticación, autorización por método y URL |
| `@PreAuthorize` / SpEL | — | Autorización a nivel de método en la capa de servicio |
| `@EnableMethodSecurity` | — | Habilita las anotaciones de método |
| Thymeleaf `th:text` | 3.x | Escape automático de HTML — mitigación XSS |
| Content-Security-Policy | — | Cabecera HTTP de defensa adicional contra XSS |
| CSRF Token | — | Protección automática en formularios POST |
| MySQL 8 | 8.x | Base de datos relacional |
| Maven | 3.x | Gestión de dependencias |

---

## Cambios respecto al Post-Contenido 1

### 1. UsuarioService — 5 métodos con @PreAuthorize

| Método | Expresión SpEL | Descripción |
|---|---|---|
| `listarTodos()` | `hasRole('ADMIN')` | Solo ADMIN puede listar usuarios |
| `buscarPorEmail(email)` | `hasRole('ADMIN') or #email == authentication.name` | ADMIN o el propio usuario |
| `cambiarRol(id, rol)` | `hasRole('ADMIN')` | Solo ADMIN puede cambiar roles |
| `actualizarNombre(usuario)` | `#usuario.email == authentication.name or hasRole('ADMIN')` | Propietario o ADMIN |
| `contarUsuarios()` | `hasAnyRole('ADMIN', 'USER')` | Cualquier autenticado |

### 2. SecurityConfig — nuevas configuraciones

- `.exceptionHandling()` → redirige a `/error/403` al denegar acceso
- `.headers().contentSecurityPolicy()` → cabecera CSP en todas las respuestas

### 3. Nueva vista error/403.html

Muestra el nombre del usuario autenticado con `sec:authentication="name"`.

### 4. Panel admin con cambio de rol

El endpoint `/admin/cambiarRol` invoca `cambiarRol()` que tiene `@PreAuthorize` como segunda capa.

---

## Ejecución

```bash
podman start mysql-estudiantes
mvn spring-boot:run
```

Usuarios de prueba:

| Email | Contraseña | Rol |
|---|---|---|
| `admin@universidad.edu` | `admin123` | ADMIN |
| `user@universidad.edu` | `user123` | USER |

---

## Pruebas de seguridad realizadas

### Prueba 1 — @PreAuthorize bloquea acceso no autorizado

1. Iniciar sesión como `user@universidad.edu` (rol USER).
2. Navegar a `http://localhost:8080/admin`.
3. **Resultado esperado:** página `error/403.html` con el nombre del usuario autenticado.

### Prueba 2 — XSS mitigado por th:text

1. Registrar un usuario con nombre: `<script>alert("XSS")</script>`
2. Iniciar sesión con ese usuario y ver el dashboard.
3. **Resultado esperado:** el texto se muestra literalmente, nunca se ejecuta.
4. Inspeccionar con F12 → Elements: el HTML debe mostrar `&lt;script&gt;` en lugar de `<script>`.

> **Por qué funciona:** `th:text` convierte `<` en `&lt;` y `>` en `&gt;`, impidiendo que el navegador interprete el contenido como HTML ejecutable. `th:utext` nunca debe usarse con datos de usuario.

### Prueba 3 — Cabecera Content-Security-Policy

```bash
curl -I http://localhost:8080/login | grep -i content-security
```

O en Chrome: F12 → Network → GET /login → Response Headers → `Content-Security-Policy`.

**Resultado esperado:**
```
Content-Security-Policy: default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline' ...
```

### Prueba 4 — CSRF bloquea POST sin token

Desde la consola del navegador (F12 → Console), con sesión activa en `/dashboard`:

```javascript
fetch("/logout", { method: "POST" })
  .then(r => console.log("Status:", r.status));
// Resultado esperado: Status: 403
```

O con curl:

```bash
curl -X POST http://localhost:8080/logout -v 2>&1 | grep "< HTTP"
# Resultado esperado: < HTTP/1.1 403
```

---

## Estructura de archivos nuevos/modificados

```
src/main/java/.../
├── config/SecurityConfig.java       ← + exceptionHandling y CSP header
├── controller/AuthController.java   ← + endpoint cambiarRol
├── controller/ErrorController.java  ← NUEVO
└── service/UsuarioService.java      ← + 5 métodos con @PreAuthorize

src/main/resources/templates/
├── dashboard.html                   ← + sección verificación XSS
├── admin/panel.html                 ← + formulario cambio de rol
└── error/403.html                   ← NUEVO
```

---

## Commits sugeridos

```
1. feat: agregar @PreAuthorize con 5 expresiones SpEL en UsuarioService
2. feat: página 403 personalizada y CSP header en SecurityConfig
3. feat: verificar XSS con th:text, CSRF y documentar pruebas
```

---

## Autor

**Andres Felipe Jimenez Ramirez**  
Ingeniería de Sistemas — Universidad Francisco de Paula Santander  
Programación Web · 2026
