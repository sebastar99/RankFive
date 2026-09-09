# Rank5 - Dashboard de Usuario

Este documento describe la implementación del dashboard de usuario, su estructura, estilos, rutas y cómo se alimenta con los datos del usuario autenticado.

## Características principales
- Header de dashboard con:
  - Avatar (placeholder con icono)
  - Nombre completo y nombre de usuario
  - Puntos (PL)
  - Rango con badge e ícono según PL
  - Enlaces a Configuración y Cerrar sesión
- Resumen de perfil con:
  - PUNTOS, RANGO, USUARIO y AVATAR (placeholder)
- Acciones rápidas:
  - Crear partido (solo UI, sin lógica)
  - Buscar amigos (funcional, enlaza a `/buscar`)
  - Ver ranking (solo UI, sin lógica)
- Lista de ranking de amigos alimentada desde la base de datos, ordenada por PL descendente
- Fondo estilo "cancha de fútbol" en tonos oscuros acorde al tema del sitio

## Archivos agregados/modificados
- Vista: `src/main/webapp/WEB-INF/views/thymeleaf/dashboard.html`
  - Estructura del dashboard con componentes visuales.
  - Usa Thymeleaf para inyectar: `usuario`, `puntos`, `rangoNombre`, `rangoCss`, `rangoIcon`.
- Estilos: `src/main/webapp/resources/core/css/dashboard.css`
  - Estilos aislados para el dashboard (no modifica `main.css`).
  - Header glass/blur, fondo tipo cancha, badges de rango, tarjetas, lista de ranking.
- Controlador: `src/main/java/com/tallerwebi/presentacion/ControladorLogin.java`
  - En `validar-login` se guarda el usuario autenticado en sesión bajo la clave `"usuario"` y se redirige a `/dashboard`.
  - Nueva ruta `GET /dashboard` que:
    - Verifica sesión activa, si no, redirige a `/login`.
    - Calcula y envía al modelo: `puntos`, `rangoNombre`, `rangoCss`, `rangoIcon`.
  - Nueva ruta `GET /logout` que invalida la sesión y redirige a `/login`.
  - Se inyecta `ServicioRelacionAmistad` por setter (`@Autowired(required = false)`) y se publica `rankingAmigos` en el modelo del dashboard.
  - `irADashboard` fue refactorizado en métodos privados (`calcularPuntos`, `calcularRango`, `obtenerRankingAmigos`) para respetar el límite de complejidad ciclomática de PMD.
- Dominio (ya existente):
  - `Usuario` tiene `@Embedded PerfilJugador`.
  - `PerfilJugador` contiene `nombreUsuario`, `nombreCompleto`, `pl` (puntos), etc.

## Cálculo de rangos por PL
Se determina a partir de los puntos (PL):
- 1000–1500: Bronce → `.rank-bronze` (icono `bi-award`)
- 1501–2000: Plata → `.rank-silver` (icono `bi-trophy`)
- 2001–2500: Oro → `.rank-gold` (icono `bi-trophy-fill`)
- 2501–3000: Platino → `.rank-plat` (icono `bi-diamond`)
- 3001–3500: Diamante → `.rank-diamond` (icono `bi-gem`)
- 3501+: Legendario → `.rank-legend` (icono `bi-lightning-charge-fill`)

Estas clases están definidas en `dashboard.css` y se aplican en la vista con `th:classappend`.

## Navegación y flujo
1. El usuario inicia sesión en `/validar-login`.
2. Si es válido, el controlador guarda `usuario` en sesión y redirige a `/dashboard`.
3. El dashboard lee `usuario` de la sesión, calcula rango y muestra:
   - Nombre completo o email (fallback)
   - `@nombreUsuario` (fallback "usuario")
   - Puntos (PL)
   - Rango con badge e icono
4. `Cerrar sesión` apunta a `/logout` y destruye la sesión.

Además:
- Si el usuario ya tiene sesión activa y navega a `/login`, es redirigido automáticamente a `/dashboard`.

## Búsqueda y gestión de amigos

Esta funcionalidad permite buscar usuarios por su nombre de usuario y agregarlos directamente
como amigos (sin confirmación por parte del otro usuario).

### Capa de dominio

- `src/main/java/com/tallerwebi/dominio/ServicioAmigos.java`
  - Interfaz anotada con `@FunctionalInterface` (requerido por la regla `ImplicitFunctionalInterface` de PMD).
  - Declara `Usuario buscarAmigoPorNombreDeUsuario(String nombreUsuario)`.
- `src/main/java/com/tallerwebi/dominio/ServicioRelacionAmistad.java`
  - Interfaz con `agregarAmigo(Usuario, Usuario)` y `listarAmigosDe(Usuario)`.
- `src/main/java/com/tallerwebi/dominio/Amistad.java`
  - Entidad JPA que modela la relación. Tabla `amistad` con dos `@ManyToOne`:
    `usuario_id` (quien agrega) y `amigo_id` (el agregado).
- `src/main/java/com/tallerwebi/dominio/RepositorioAmistad.java`
  - Interfaz con `guardar`, `existeRelacion(usuarioId, amigoId)` y `listarAmigosDe(usuarioId)`.

### Capa de implementación

Las implementaciones de servicios se movieron al subpaquete `dominio.implementacion`
para separar contratos de implementaciones:

- `src/main/java/com/tallerwebi/dominio/implementacion/ServicioLoginImpl.java`
- `src/main/java/com/tallerwebi/dominio/implementacion/ServicioAmigosImpl.java`
  - Delega la búsqueda a `RepositorioUsuario.buscarPorNombreUsuario`.
- `src/main/java/com/tallerwebi/dominio/implementacion/ServicioRelacionAmistadImpl.java`
  - Valida que no se agregue a sí mismo ni duplique una relación existente antes de persistir.

> El `@ComponentScan` de `SpringWebConfig` apunta a `com.tallerwebi.dominio`, que incluye
> los subpaquetes, por lo que los beans se detectan sin cambios de configuración.

### Capa de infraestructura

- `src/main/java/com/tallerwebi/infraestructura/RepositorioAmistadImpl.java`
  - Implementación con Hibernate. La consulta de listado ordena por
    `a.amigo.perfil.pl desc`, de modo que el ranking ya llega ordenado al controlador.

### Capa de presentación

- `src/main/java/com/tallerwebi/presentacion/ControladorAmigos.java`
  - `GET /buscar`: verifica sesión, busca al usuario si vino el parámetro y devuelve la vista `buscarAmigos`.
    El parámetro se declara como `@RequestParam(name = "nombreUsuario", required = false)`:
    - El **nombre explícito** es obligatorio porque el proyecto no compila con el flag `-parameters`,
      y sin él Spring lanza `IllegalArgumentException`.
    - `required = false` permite entrar a la pantalla desde el dashboard sin haber buscado nada.
  - `POST /agregar-amigo`: busca al usuario y persiste la amistad vía `ServicioRelacionAmistad`.
  - `ServicioRelacionAmistad` se inyecta por setter para no alterar el constructor usado en los tests.
- `src/main/java/com/tallerwebi/presentacion/DatosAmigos.java`
  - DTO con el campo `nombreUsuario` para el formulario.
- `src/main/webapp/WEB-INF/views/thymeleaf/buscarAmigos.html`
  - Formulario de búsqueda, tarjeta de resultado con botón "Agregar",
    mensajes de `error` / `ok` y enlace de vuelta al dashboard.

### Atributos del modelo

| Clave | Vista | Descripción |
|---|---|---|
| `amigo` | `buscarAmigos` | Usuario encontrado en la búsqueda |
| `error` | `buscarAmigos` | Mensaje de error (ej: "Usuario no encontrado") |
| `ok` | `buscarAmigos` | Confirmación (ej: "Amigo agregado") |
| `datosAmigos` | `buscarAmigos` | DTO del formulario |
| `rankingAmigos` | `dashboard` | Lista de amigos ordenada por PL |

### Flujo completo

1. Desde el dashboard, el botón "Buscar amigos" navega a `/buscar`.
2. El usuario escribe un nombre de usuario y envía el formulario (`GET /buscar?nombreUsuario=...`).
3. Si existe, se muestra su nombre, usuario y puntos con un botón "Agregar".
4. "Agregar" hace `POST /agregar-amigo`, se persiste la fila en `amistad` y se muestra "Amigo agregado".
5. Al volver al dashboard, el amigo aparece en el ranking ordenado por PL.

## Tests

- `src/test/java/com/tallerwebi/dominio/ServicioAmigosTest.java`
  - Tests unitarios con mock de `RepositorioUsuario`: usuario existente, inexistente y nombre vacío.
- `src/test/java/com/tallerwebi/presentacion/ControladorAmigosTest.java`
  - Tests unitarios con mocks de `ServicioAmigos`, `HttpServletRequest` y `HttpSession`:
    búsqueda con resultado y búsqueda sin resultado (verifica la clave `error` del modelo).

## Notas sobre calidad de código

El build corre Checkstyle y PMD antes de levantar Jetty. Puntos a tener en cuenta:

- **PMD `ImplicitFunctionalInterface`**: una interfaz con un único método abstracto debe
  anotarse con `@FunctionalInterface`. Aplicado en `ServicioAmigos`.
- **PMD `CyclomaticComplexity`**: el límite se superó al agregar el ranking en `irADashboard`.
  Se resolvió extrayendo métodos privados en lugar de suprimir la regla.
- **Checkstyle `AvoidStarImport`**: se reemplazó `import jakarta.persistence.*` por imports
  explícitos en `Amistad`.
- Los avisos `MissingJavadocType` son warnings y no bloquean el build.

## Cómo verlo en local
- Asegurá MySQL corriendo en 3306 (contenedor `tallerwebi-mysql`).
- Levantá la app: `mvn clean jetty:run`.
- Accedé a:
  - Home: `http://localhost:8080/spring`
  - Login: `http://localhost:8080/spring/login`
  - Dashboard: `http://localhost:8080/spring/dashboard`
  - Buscar amigos: `http://localhost:8080/spring/buscar`

> **Importante:** `hibernate.hbm2ddl.auto` está configurado en `create` en `HibernateConfig`,
> por lo que cada reinicio recrea las tablas y las amistades agregadas se pierden.
> Para que persistan entre reinicios hay que cambiarlo a `update`.

## Próximos pasos sugeridos
- Conectar la carga de avatar real (almacenamiento y actualización en el perfil).
- Mostrar más métricas (partidos jugados, winrate, últimos resultados).
- Implementar la lógica de “Crear partido” y “Ver ranking”.
- Proteger rutas con un filtro/interceptor que fuerce sesión para `/dashboard`.
- Agregar tests de integración y punta a punta para el flujo de amigos.
- Permitir eliminar amigos y, opcionalmente, un flujo de solicitud/confirmación.
