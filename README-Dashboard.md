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
- Acciones rápidas (solo UI, sin lógica):
  - Crear partido
  - Buscar amigos
  - Ver ranking
- Lista de ranking de amigos (mock visual)
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

## Cómo verlo en local
- Asegurá MySQL corriendo en 3306 (contenedor `tallerwebi-mysql`).
- Levantá la app: `mvn clean jetty:run`.
- Accedé a:
  - Home: `http://localhost:8080/spring`
  - Login: `http://localhost:8080/spring/login`
  - Dashboard: `http://localhost:8080/spring/dashboard`

## Próximos pasos sugeridos
- Conectar la carga de avatar real (almacenamiento y actualización en el perfil).
- Mostrar más métricas (partidos jugados, winrate, últimos resultados).
- Implementar la lógica de “Crear partido”, “Buscar amigos” y “Ver ranking”.
- Proteger rutas con un filtro/interceptor que fuerce sesión para `/dashboard`.
