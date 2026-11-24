# 🛠️ Cache – Backend Spring Boot

API REST de la tienda **Cache** (periféricos gamer y mangas), hecha con **un solo proyecto Spring Boot** y base de datos **MySQL**.
Reemplaza lo que hoy el frontend `cache-react` guarda en `localStorage` (`at_usuarios`, `at_productos`, `at_pedidos`, `at_sesion`)
y lo que calcula el `CarritoContext` (cupones y costo de envío).

| Qué | Cómo se cumple |
|---|---|
| Base de datos relacional | MySQL: USUARIO → PEDIDO → DETALLE_PEDIDO ← PRODUCTO ← CATEGORIA |
| Capas | `model`, `repository`, `service` (lógica de negocio), `controller`, `dto`, `exception` |
| API REST con CRUD | Rutas versionadas `/api/v1/...` |
| Documentación | Swagger en `http://localhost:8080/swagger-ui.html` |
| Seguridad | Spring Security + JWT (access 15 min, refresh 7 días), BCrypt y 3 tipos de usuario |
| Pruebas | JUnit 5 + Mockito sobre los servicios (`mvn test`, no necesitan MySQL) |

## 1. Requisitos

- **Java 21** (JDK) y **MySQL 8** en el puerto 3306 (XAMPP, Laragon o Workbench). Maven viene incluido en IntelliJ y VS Code.
- La base `cache_db` se crea sola (`createDatabaseIfNotExist=true`). Revisa usuario y contraseña en `application.properties`
  (`DB_PASSWORD`, por defecto `root`; si no tienes contraseña usa `${DB_PASSWORD:}`).

## 2. Cómo ejecutarlo

```bash
mvn spring-boot:run        # o ejecuta CacheBackendApplication desde el IDE
mvn test                   # pruebas unitarias
```

También con Docker (sin instalar Java ni MySQL): `docker compose up -d --build` (API en `8080`, MySQL en `3307`).

Al primer inicio `DataLoader` carga: 3 usuarios, 6 categorías, **los 72 productos de `src/data/productos.js`** (con el mismo `slug`,
por ejemplo `mouse-1`) y 2 pedidos de ejemplo.

### Cuentas de prueba

| tipoUsuario | Correo | Clave | Puede |
|---|---|---|---|
| administrador | `admin@cache.cl` | `admin123` | Todo |
| vendedor | `vendedor@cache.cl` | `vendedor123` | Ver productos, categorías y pedidos (sin modificar) |
| cliente | `pedro@gmail.com` | `pedro123` | Comprar, ver sus pedidos y su perfil |

Prueba en Swagger: `POST /api/v1/auth/login` → copia el `accessToken` → **Authorize** → pégalo.

## 3. Endpoints

| Método y ruta | Acceso | Reemplaza en el frontend |
|---|---|---|
| `POST /api/v1/auth/login` `{correo, clave}` | Público | `iniciarSesion` / `obtenerUsuarioPorCorreo` |
| `POST /api/v1/auth/registro` | Público (crea siempre un cliente) | `crearUsuario` en `Registro.js` |
| `POST /api/v1/auth/refresh` · `GET /api/v1/auth/me` | Refresh token · con sesión | `AuthContext` |
| `GET /api/v1/categorias` | Público | `CATEGORIAS` |
| `GET /api/v1/productos?categoria=mouse` | Público | `todosLosProductos`, `productosPorCategoria` |
| `GET /api/v1/productos/slug/{slug}` · `/{id}` · `/ofertas` · `/destacados` | Público | página `Producto.js` |
| `GET /api/v1/productos/criticos` · `/reportes/inventario` | Administrador | `AdminProductos` / `AdminDashboard` |
| `POST/PUT/DELETE /api/v1/productos` y `/categorias` | Administrador | `crearProducto`, `actualizarProducto`, `eliminarProducto` |
| `GET/POST/PUT/DELETE /api/v1/usuarios` | Administrador | `AdminUsuarios`, `AdminUsuarioForm` |
| `GET/PUT /api/v1/perfil` | Con sesión | perfil del usuario |
| `POST /api/v1/pedidos` | Público (con o sin token) | `confirmarCompra` |
| `GET /api/v1/pedidos/mias` · `/{id}` | Con sesión (un cliente solo ve los suyos) | historial de pedidos |
| `GET /api/v1/pedidos` · `/usuario/{id}` | Administrador y vendedor · administrador | `AdminDashboard` |
| `PATCH /api/v1/pedidos/{id}/estado` | Administrador | — |
| `GET /api/v1/reportes/resumen` | Administrador | totales del dashboard |

### Compra (`POST /api/v1/pedidos`)

El frontend envía solo **qué** y **cuánto**; el backend lee el precio de la base de datos y calcula los totales:

```json
{
  "nombre": "Pedro Hacker", "correo": "pedro@gmail.com", "telefono": "+56 9 1234 5678",
  "region": "Región Metropolitana", "comuna": "Maipú", "direccion": "Los Crisantemos 123",
  "cupon": "CACHE10",
  "items": [ { "slug": "mouse-1", "cantidad": 1 } ]
}
```

Reglas (las mismas del `CarritoContext`): envío $2.990, gratis desde $20.000 o con `ENVIOGRATIS`; `CACHE10` = 10 % de descuento;
cupón desconocido → 409; stock insuficiente → 409 y no se guarda nada; al anular un pedido el stock vuelve al inventario.
La respuesta trae `cliente`, `productos` y `totales` con la misma forma que hoy guarda `at_pedidos`.

## 4. Conectar con el frontend

- CORS ya permite `http://localhost:3000` y `http://localhost:5173`. Para otro origen, agrégalo en `app.cors.origenes`
  (y en `APP_CORS_ORIGENES` de `docker-compose.yml`).
- Las imágenes siguen en `public/images` del frontend: el campo `imagen` del producto es la misma ruta (`mouse/mouse1.png`).
- Los datos de usuario usan los mismos nombres que el frontend: `run` (sin guion, ej. `123456785`), `tipoUsuario`
  (`administrador`, `vendedor`, `cliente`), `region`, `comuna`, `direccion`. La contraseña se acepta como `clave` o `password`.
- Envía el token en cada petición protegida: `Authorization: Bearer <accessToken>`.

## 5. Estructura

```
src/main/java/cl/cache/cache_backend
├── config/      DataLoader (datos de prueba), SeedProductos (catálogo), OpenApiConfig (Swagger)
├── controller/  Auth, Usuario, Perfil, Categoria, Producto, Pedido, Reporte
├── dto/         Request/Response (lo que entra y sale por la API)
├── exception/   GlobalExceptionHandler + ApiError (errores JSON uniformes)
├── model/       Usuario, Categoria, Producto, Pedido, DetallePedido, Rol, EstadoPedido
├── repository/  Spring Data JPA
├── security/    JwtService, JwtAuthFilter, SecurityConfig (quién accede a cada ruta)
└── service/     Lógica de negocio
```

> **Antes de producción:** define tu propia clave `JWT_SECRET` (`openssl rand -base64 64`) y una contraseña real para MySQL.
