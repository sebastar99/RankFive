# Rank5 MVP (RankFive)

## 1. Descripción general del proyecto

**Rank5**, también denominado **RankFive**, será una aplicación web desarrollada en **Java** orientada a la gestión de partidos de fútbol amateur, clasificación de jugadores, equipos y competencias.

El objetivo principal del sistema es crear una plataforma que permita organizar partidos, registrar sus resultados y utilizar un sistema de puntuación basado en el rendimiento competitivo para determinar el nivel de jugadores y equipos.

A partir de esta funcionalidad inicial, el sistema podrá evolucionar hacia una plataforma deportiva más completa, incorporando:

- ranking individual de jugadores;
- ranking y estadísticas de equipos;
- ligas y torneos;
- ranking de goleadores;
- árbitros con puntuación propia;
- análisis deportivos realizados mediante inteligencia artificial;
- generación automática de una previa de cada partido;
- generación de contenido visual o "flyer" del partido;
- análisis posterior al encuentro;
- competencias regionales entre los mejores equipos de distintas ligas.

La idea central es que **Rank5 no sea solamente una aplicación que almacene resultados, sino un sistema que utilice la información obtenida de los partidos para generar rankings, estadísticas, análisis y competencias.**

---

# 2. Problema que busca resolver

En muchos grupos de fútbol amateur la organización de partidos y torneos se realiza mediante herramientas independientes, por ejemplo:

- grupos de WhatsApp para organizar encuentros;
- planillas para registrar resultados;
- aplicaciones diferentes para estadísticas;
- votaciones manuales para determinar los mejores jugadores;
- información dispersa sobre equipos y torneos.

Esto genera varios problemas:

- no existe una clasificación centralizada;
- es difícil determinar cuáles son realmente los mejores jugadores;
- la formación de equipos puede ser desequilibrada;
- las estadísticas históricas se pierden o quedan dispersas;
- no existe una forma objetiva de comparar equipos;
- la organización de ligas y torneos puede requerir trabajo manual;
- la información de un partido no se aprovecha para generar análisis posteriores.

Rank5 busca centralizar estas funciones en una única aplicación web.

---

# 3. Objetivo general

Desarrollar una aplicación web capaz de administrar jugadores, equipos, partidos y competencias de fútbol, asignando un sistema de puntuación que permita establecer rankings individuales y colectivos.

Como evolución del sistema, se incorporará inteligencia artificial para analizar los encuentros y generar automáticamente información previa y posterior a cada partido.

---

# 4. Objetivos específicos

El sistema deberá permitir inicialmente:

1. Registrar jugadores.
2. Asignar un nivel inicial a cada jugador.
3. Crear partidos de fútbol 5.
4. Registrar los equipos participantes.
5. Registrar el ganador del encuentro.
6. Actualizar automáticamente el nivel de los jugadores.
7. Consultar el ranking general.
8. Armar equipos manualmente.
9. Mantener un historial de resultados.

Como funcionalidades futuras se incorporarán:

- creación de ligas;
- inscripción de equipos;
- torneos;
- estadísticas de jugadores;
- estadísticas de equipos;
- árbitros;
- sistema de votación;
- inteligencia artificial;
- análisis previos y posteriores;
- generación de flyers;
- torneo regional.

---

# 5. Funcionamiento básico de Rank5

El funcionamiento inicial será relativamente sencillo.

### Paso 1: Registro de jugadores

Cada jugador tendrá un perfil dentro del sistema.

Se almacenarán datos como:

- nombre;
- usuario, en futuras versiones;
- nivel o PL;
- cantidad de partidos;
- victorias;
- derrotas;
- empates;
- estadísticas acumuladas.

El administrador o usuario autorizado podrá registrar inicialmente el **PL (Player Level)** del jugador.

Por ejemplo:

| Jugador | PL inicial |
|---|---:|
| Juan | 1200 |
| Pedro | 1150 |
| Lucas | 1300 |
| Martín | 1050 |

El PL permitirá determinar aproximadamente el nivel competitivo de cada jugador.

---

# 6. Sistema de PL / Elo

Uno de los componentes centrales de Rank5 será el sistema de puntuación.

La idea inicial es utilizar un sistema inspirado en **Elo**, adaptado al fútbol amateur.

Cada jugador tendrá un valor numérico que representará su nivel estimado.

Por ejemplo:

**Juan → 1324 PL**

Luego de cada partido, su PL podrá aumentar o disminuir.

La cantidad de puntos que gana o pierde dependerá principalmente de:

- nivel de los equipos;
- resultado esperado;
- resultado real;
- factor K configurado por el sistema.

La ventaja de utilizar este mecanismo es que **ganarle a un equipo considerado superior debería otorgar una recompensa mayor que ganarle a un equipo considerado inferior**.

Por ejemplo:

Si un equipo con PL promedio 1100 enfrenta a otro con PL promedio 1400, el sistema considera que el segundo equipo tiene mayores probabilidades de ganar.

Si gana el equipo de 1100, sus jugadores podrían obtener una mayor cantidad de puntos debido a que lograron un resultado inesperado.

---

# 7. Políticas configurables del sistema

Rank5 deberá permitir definir determinadas reglas de funcionamiento.

Entre ellas:

### PL inicial

Determina con qué puntuación comienza un nuevo jugador.

Ejemplo:

**PL inicial = 1000**

### Factor K

Determina cuánto puede variar la puntuación después de un partido.

Ejemplo conceptual:

**K = 32**

Cuanto mayor sea K, más rápido cambiará la puntuación de un jugador.

### Cantidad de jugadores

La aplicación podrá establecer una cantidad de jugadores por equipo dependiendo del tipo de fútbol.

Por ejemplo:

- fútbol 5 → 5 jugadores;
- fútbol 7 → 7 jugadores;
- fútbol 11 → 11 jugadores.

Esto permitirá que el sistema pueda ampliarse posteriormente sin tener que diseñar una aplicación completamente diferente.

### Cantidad impar de jugadores

El sistema también deberá definir qué ocurre cuando existe una cantidad impar de jugadores disponibles.

Por ejemplo, si hay 11 jugadores para un partido de fútbol 5, podría ocurrir:

- 5 vs 5 + 1 suplente;
- 6 vs 5;
- guardar al jugador como suplente;
- permitir que el organizador decida manualmente.

La opción seleccionada dependerá de las reglas definidas por el administrador.

---

# 8. Formación de equipos

Inicialmente Rank5 permitirá formar equipos manualmente.

El organizador seleccionará los jugadores que participarán y determinará qué jugadores pertenecen a cada equipo.

Ejemplo:

### Equipo A
- Juan — 1300
- Pedro — 1250
- Lucas — 1220
- Martín — 1100
- Diego — 1080

**PL promedio: 1190**

### Equipo B
- Carlos — 1280
- Federico — 1210
- Pablo — 1160
- Andrés — 1140
- Nicolás — 1120

**PL promedio: 1182**

En una futura versión, Rank5 podrá utilizar estos valores para **crear automáticamente equipos equilibrados**.

El sistema intentaría minimizar la diferencia de PL entre ambos equipos.

---

# 9. Registro del partido

Una vez creado el partido, el sistema almacenará información como:

- fecha;
- hora;
- cancha;
- modalidad;
- equipo local;
- equipo visitante;
- jugadores de cada equipo;
- resultado;
- ganador;
- árbitro;
- estadísticas.

En la primera versión, el dato más importante será el resultado.

Ejemplo:

**Equipo A 7 - 5 Equipo B**

Ganador:

**Equipo A**

A partir de este resultado, el sistema ejecutará automáticamente el cálculo del nuevo PL.

---

# 10. Ranking general de jugadores

Una de las principales pantallas de Rank5 será el ranking general.

Ejemplo:

| Posición | Jugador | PL | PJ | PG | PP |
|---:|---|---:|---:|---:|---:|
| 1 | Juan | 1456 | 24 | 18 | 6 |
| 2 | Lucas | 1412 | 21 | 16 | 5 |
| 3 | Pedro | 1398 | 29 | 20 | 9 |
| 4 | Carlos | 1364 | 18 | 12 | 6 |

El ranking podrá ordenarse por:

- PL;
- victorias;
- partidos jugados;
- goles, en una futura versión;
- rendimiento en una liga;
- rendimiento en torneos.

---

# 11. Evolución hacia ligas y torneos

Una de las principales ampliaciones de Rank5 será incorporar el concepto de **competencia organizada**.

En lugar de manejar solamente partidos independientes, se podrán crear **ligas**.

Por ejemplo:

## Liga Oeste Fútbol 5

Participan:

- Equipo A
- Equipo B
- Equipo C
- Equipo D
- Equipo E
- Equipo F

Cada equipo podrá disputar diferentes partidos durante una temporada.

El sistema registrará automáticamente:

- partidos jugados;
- victorias;
- empates;
- derrotas;
- goles a favor;
- goles en contra;
- diferencia de goles;
- puntos;
- Elo del equipo.

---

# 12. Diferencia entre PL de jugadores y Elo de equipos

Aquí es importante separar dos conceptos.

## PL / Elo del jugador

Representa el nivel estimado de un jugador individual.

## Elo del equipo

Representa el nivel competitivo del equipo completo.

Por ejemplo:

**Jugador**

Juan → 1450 PL

**Equipo**

Los Tigres → 1520 Elo

Esto permite que la plataforma evalúe tanto al individuo como al conjunto.

El Elo del equipo se utilizará principalmente para:

- comparar equipos;
- ordenar competencias;
- generar enfrentamientos;
- analizar partidos;
- determinar favoritos.

---

# 13. Solución al problema de los goles y el Elo individual

Este es un punto importante del proyecto.

No sería recomendable que un jugador gane una gran cantidad de Elo simplemente porque alguien ingresó que hizo muchos goles.

Por ejemplo:

Juan supuestamente hizo 50 goles.

Si esos 50 goles fueran utilizados directamente para modificar su Elo, podría terminar con una puntuación artificialmente alta aunque los datos fueran incorrectos.

Por este motivo, **el Elo no debería depender directamente de la cantidad de goles.**

La propuesta es dividir el sistema en diferentes indicadores.

### Elo competitivo

El Elo principal del jugador se modifica principalmente por el **resultado del partido**, no por los goles.

Por ejemplo:

Si un jugador participa en un equipo que gana, su puntuación recibe una modificación positiva.

Si pierde, recibe una modificación negativa.

De esta manera, un jugador no puede incrementar artificialmente su Elo simplemente declarando que convirtió muchos goles.

### Estadísticas individuales

Los goles, asistencias, tarjetas y otros datos se almacenan por separado.

Ejemplo:

**Juan**

PL: 1432  
Partidos: 25  
Goles: 31  
Asistencias: 14  
MVP: 5

Estas estadísticas pueden utilizarse para otros rankings, pero no necesariamente modifican de forma significativa el Elo principal.

---

# 14. Verificación de estadísticas

Cuando posteriormente se incorporen estadísticas avanzadas, se puede agregar un sistema de validación.

Por ejemplo, para registrar un gol podrían intervenir:

- capitán del equipo;
- capitán rival;
- árbitro.

Si los datos coinciden, el gol queda validado.

Ejemplo:

Capitán A → Juan marcó gol.

Capitán B → Juan marcó gol.

Árbitro → Juan marcó gol.

Resultado:

**Gol validado.**

Esto reduce considerablemente la posibilidad de manipulación.

Otra alternativa sería permitir que solamente el árbitro o un administrador pueda confirmar oficialmente determinadas estadísticas.

---

# 15. Ranking de mejores jugadores

La aplicación podrá tener un ranking separado del Elo.

Por ejemplo:

### Ranking competitivo

Ordenado por:

**PL / Elo**

### Ranking de goleadores

Ordenado por:

**Goles**

### Ranking de asistencias

Ordenado por:

**Asistencias**

### Ranking MVP

Ordenado por:

**Cantidad de premios MVP**

Esto permite evitar que todos los conceptos se mezclen.

Un jugador podría, por ejemplo, ser:

- 1.º en PL;
- 5.º en goles;
- 2.º en asistencias.

Eso ofrece una visión mucho más completa que utilizar solamente una estadística.

---

# 16. Sistema de ligas

Cada liga tendrá información propia.

Ejemplo:

**Liga Zona Oeste — Temporada 2027**

La liga podrá tener:

- nombre;
- región;
- ciudad;
- temporada;
- fecha de inicio;
- fecha de finalización;
- equipos participantes;
- partidos;
- tabla de posiciones.

Cada equipo disputará sus partidos y el sistema actualizará automáticamente la tabla.

---

# 17. Tabla de posiciones

Ejemplo:

| Pos | Equipo | PJ | PG | PE | PP | GF | GC | Pts |
|---:|---|---:|---:|---:|---:|---:|---:|---:|
| 1 | Los Tigres | 10 | 8 | 1 | 1 | 42 | 20 | 25 |
| 2 | La Banda | 10 | 7 | 2 | 1 | 38 | 22 | 23 |
| 3 | Deportivo Oeste | 10 | 6 | 1 | 3 | 35 | 27 | 19 |
| 4 | Los Amigos | 10 | 5 | 2 | 3 | 31 | 25 | 17 |

Los criterios exactos para desempatar podrán definirse como política del sistema.

Por ejemplo:

1. puntos;
2. diferencia de goles;
3. goles a favor;
4. resultado entre ambos;
5. Elo.

---

# 18. Elo de equipos

Además de la tabla tradicional, cada equipo tendrá un Elo.

Por ejemplo:

**Los Tigres → 1624**

**La Banda → 1557**

Este Elo podrá utilizarse para:

- determinar favoritos;
- generar previas;
- comparar equipos;
- realizar sorteos;
- organizar futuros torneos;
- seleccionar los mejores equipos regionales.

Esto también permite que un equipo tenga una medida de su nivel competitivo incluso si todavía no terminó la temporada.

---

# 19. Inteligencia artificial

Una de las funcionalidades más interesantes de las futuras versiones será la integración de inteligencia artificial.

La IA recibirá información estructurada de Rank5 y realizará análisis deportivos.

La IA **no debería inventar estadísticas**, sino trabajar con la información almacenada por el sistema.

---

# 20. Previa de un partido

Antes de cada partido de liga o torneo, la aplicación podrá generar automáticamente una previa.

Por ejemplo:

## Los Tigres vs La Banda

La IA analizará:

- Elo de ambos equipos;
- rendimiento reciente;
- partidos anteriores;
- cantidad de victorias;
- cantidad de derrotas;
- goles a favor;
- goles en contra;
- principales goleadores;
- jugadores con mejor PL;
- enfrentamientos anteriores.

A partir de esos datos, podrá generar un análisis similar a una previa deportiva.

Por ejemplo:

> Los Tigres llegan al encuentro con un Elo superior y mejores resultados en sus últimos partidos. La Banda, sin embargo, presenta un rendimiento ofensivo destacado y cuenta con uno de los principales goleadores de la competición.

El objetivo no será que la IA adivine el resultado, sino que **analice los datos existentes y presente una interpretación comprensible.**

---

# 21. Análisis posterior al partido

Una vez terminado el encuentro, Rank5 dispondrá de información adicional.

La IA podrá generar un análisis posterior teniendo en cuenta:

- resultado;
- rendimiento esperado;
- resultado real;
- evolución del Elo;
- goles;
- goleadores;
- estadísticas;
- jugadores destacados;
- rendimiento de los equipos.

Por ejemplo:

## Análisis post partido

**Resultado: Los Tigres 6 - 3 La Banda**

La IA podría explicar que Los Tigres consiguieron un resultado superior al esperado y que el equipo incrementó su Elo como consecuencia de la victoria.

También podría destacar que determinado jugador tuvo una actuación relevante según las estadísticas verificadas.

---

# 22. Flyer automático del partido

Otra funcionalidad será la creación automática de una especie de **flyer digital** para cada partido.

Este flyer podrá contener:

**LOS TIGRES vs LA BANDA**

Fecha  
Hora  
Cancha

**Elo**
Los Tigres: 1624  
La Banda: 1557

**Jugador destacado**
Juan — 1456 PL

**Goleadores**
Carlos — 18 goles  
Juan — 16 goles

**Previa**
Análisis generado por IA.

El objetivo sería que cada partido tenga una presentación visual similar a una publicación deportiva.

La aplicación podría generar este contenido automáticamente al momento de programarse el encuentro.

---

# 23. Sistema de árbitros

Otra sección futura será la creación de un módulo exclusivo para árbitros.

Los árbitros tendrán:

- perfil;
- cantidad de partidos dirigidos;
- valoración;
- Elo;
- estadísticas;
- historial de actuaciones.

Cada partido podrá tener asignado un árbitro.

---

# 24. Elo de árbitros

El sistema de puntuación de los árbitros funcionará de manera diferente al de los jugadores.

Después de un partido, los jugadores podrán valorar al árbitro.

Por ejemplo:

**Califique al árbitro**

1 a 5 estrellas.

O utilizando diferentes categorías:

- aplicación de reglas;
- imparcialidad;
- control del partido;
- comunicación;
- puntualidad.

Con estas evaluaciones, el sistema calculará una puntuación del árbitro.

---

# 25. Asignación de árbitros según Elo

A medida que aumente el nivel de las competiciones, el sistema podrá utilizar el Elo del árbitro.

Por ejemplo:

### Partido de nivel bajo
Árbitros con Elo:

1100–1300

### Partido de nivel medio
Árbitros con Elo:

1300–1500

### Partido de alto nivel
Árbitros con Elo:

1500+

De esta manera, los partidos más importantes podrían ser dirigidos por los árbitros mejor valorados.

Esto introduce una lógica similar a la de los jugadores:

**mejor rendimiento → mayor Elo → acceso a partidos de mayor nivel.**

---

# 26. Torneo regional

La siguiente etapa sería crear una competencia superior a las ligas.

El sistema dividirá las competiciones por regiones.

Por ejemplo:

- Zona Oeste;
- Zona Sur;
- Zona Norte;
- Zona Centro;
- etc.

Cada región podrá tener varias ligas.

Ejemplo:

### Zona Oeste

Liga A  
Liga B  
Liga C

Al terminar la temporada, los **4 mejores equipos de cada liga** clasificarán al torneo regional.

---

# 27. Torneo de campeones regional

Por ejemplo:

Zona Oeste:

Liga A:
1.º Equipo A  
2.º Equipo B  
3.º Equipo C  
4.º Equipo D

Liga B:
1.º Equipo E  
2.º Equipo F  
3.º Equipo G  
4.º Equipo H

Liga C:
1.º Equipo I  
2.º Equipo J  
3.º Equipo K  
4.º Equipo L

Todos estos equipos podrán clasificarse al:

# Torneo Regional Zona Oeste

El sistema podrá generar automáticamente:

- sorteo;
- cruces;
- calendario;
- sedes;
- resultados;
- estadísticas;
- goleadores;
- mejores jugadores;
- árbitros;
- análisis de IA.

El campeón del torneo será considerado el **mejor equipo de la región durante esa temporada**.

---

# 28. Estructura jerárquica de las competencias

De esta manera, Rank5 tendría una estructura similar a:

**PARTIDO**

↓

**LIGA**

↓

**CLASIFICACIÓN**

↓

**TOP 4 DE CADA LIGA**

↓

**TORNEO REGIONAL**

↓

**CAMPEÓN REGIONAL**

Esto convierte al sistema en una plataforma de competición y no solamente en un registro de partidos.

---

# 29. Propuesta de estructura por etapas

Para que el proyecto sea viable y no resulte demasiado grande para una primera entrega, se propone dividir su desarrollo.

## Etapa 1 — MVP

La primera versión tendrá solamente las funciones fundamentales.

### Jugadores
- registrar jugador;
- nombre;
- PL inicial;
- ranking.

### Partidos
- crear partido;
- seleccionar jugadores;
- crear equipos;
- registrar resultado;
- determinar ganador.

### Sistema de PL
- cálculo automático después del partido;
- actualización del ranking.

### Equipos
- formación manual.

Esta etapa demuestra el funcionamiento básico del sistema.

---

# 30. Etapa 2 — Estadísticas

Posteriormente se incorporarán:

- goleadores;
- asistencias;
- MVP;
- historial de partidos;
- historial individual;
- estadísticas de equipos;
- ranking de goleadores.

También se podrán agregar mecanismos de validación de estadísticas.

---

# 31. Etapa 3 — Ligas y torneos

Se incorporarán:

- creación de ligas;
- registro de equipos;
- temporadas;
- tabla de posiciones;
- Elo de equipos;
- clasificación;
- torneos;
- estadísticas por torneo.

---

# 32. Etapa 4 — Árbitros

Se agregará:

- registro de árbitros;
- partidos dirigidos;
- votaciones;
- valoración;
- Elo del árbitro;
- asignación según nivel.

---

# 33. Etapa 5 — Inteligencia artificial

La IA permitirá:

- previa de partidos;
- comparación entre equipos;
- comparación entre jugadores;
- análisis de estadísticas;
- predicciones orientativas;
- análisis post partido;
- generación de contenido para el partido;
- generación del texto utilizado en el flyer.

---

# 34. Etapa 6 — Torneo regional

Finalmente se podrá implementar:

- clasificación de equipos;
- selección automática del Top 4;
- agrupación por región;
- generación del torneo;
- cruces;
- semifinales;
- final;
- campeón regional.

---

# 35. Funcionalidades adicionales futuras

También quedan planteadas como posibles mejoras:

### Login

Cada jugador podrá tener:

- usuario;
- contraseña;
- perfil personal.

### Propuesta de partido

Un usuario podrá proponer:

- fecha;
- cancha;
- horario;
- cantidad de jugadores.

### Votación de horarios

Los jugadores podrán indicar qué horario prefieren.

El sistema determinará automáticamente el horario con mayor cantidad de votos.

### Asistencia

Cada jugador podrá indicar:

- confirmado;
- no disponible;
- pendiente.

### Ubicación

La cancha podrá visualizarse mediante un enlace a Google Maps.

---

# 36. Arquitectura conceptual

La aplicación podría dividirse en diferentes módulos.

### Módulo de usuarios
Gestiona jugadores, administradores y árbitros.

### Módulo de jugadores
Gestiona:

- PL;
- estadísticas;
- historial;
- ranking.

### Módulo de equipos
Gestiona:

- equipos;
- planteles;
- Elo;
- resultados.

### Módulo de partidos
Gestiona:

- fecha;
- cancha;
- jugadores;
- resultado;
- estadísticas.

### Módulo de competencias
Gestiona:

- ligas;
- torneos;
- temporadas;
- tablas.

### Módulo de árbitros
Gestiona:

- árbitros;
- evaluaciones;
- Elo.

### Módulo de inteligencia artificial
Obtiene información del sistema y genera:

- previas;
- análisis;
- contenidos.

---

# 37. Base de datos conceptual

Entre las principales entidades del sistema podrían encontrarse:

**Jugador**

- id
- nombre
- PL
- partidos jugados
- victorias
- derrotas

**Equipo**

- id
- nombre
- Elo
- región

**Partido**

- id
- fecha
- cancha
- resultado
- equipo A
- equipo B
- árbitro

**Liga**

- id
- nombre
- región
- temporada

**Torneo**

- id
- nombre
- región
- temporada

**Estadística**

- id
- partido
- jugador
- goles
- asistencias

**Árbitro**

- id
- nombre
- Elo
- valoración

Esto permitirá construir una base sólida para futuras ampliaciones.

---

# 38. Ventaja del diseño propuesto

Una de las principales ventajas del proyecto es que las funcionalidades pueden crecer de manera progresiva.

El MVP no necesita implementar toda la plataforma.

La primera versión puede demostrar:

**Jugador → Partido → Resultado → PL → Ranking**

A partir de esa estructura se puede agregar:

**Ranking → Equipos → Ligas → Torneos → Árbitros → IA → Torneo Regional**

Por lo tanto, el proyecto comienza siendo técnicamente manejable y luego puede evolucionar sin tener que modificar completamente su estructura.

---

# 39. Concepto final de Rank5

La idea final de Rank5 es construir una plataforma donde toda la actividad competitiva genere información que luego pueda ser utilizada por el sistema.

Por ejemplo:

Un jugador participa en un partido.

↓

Se registra el resultado.

↓

Se actualiza su PL.

↓

Se actualiza el Elo del equipo.

↓

Se actualizan las estadísticas.

↓

Se actualiza la tabla de la liga.

↓

Se modifica la clasificación regional.

↓

La IA puede utilizar la nueva información para analizar futuros partidos.

↓

Los mejores equipos pueden clasificar al torneo regional.

De esta manera, **cada partido alimenta todo el sistema.**

---

# 40. Resumen de la propuesta

Rank5 comenzará como una aplicación web para organizar partidos de fútbol 5 y administrar el nivel de sus jugadores mediante un sistema de puntuación.

Su evolución permitirá administrar equipos, ligas y torneos completos, además de incorporar estadísticas, árbitros y competencias regionales.

La integración de inteligencia artificial permitirá transformar los datos registrados por la aplicación en contenido deportivo, como previas, análisis posteriores y presentaciones de cada partido.

La propuesta busca combinar tres elementos:

**Gestión deportiva + sistema de ranking + análisis mediante IA.**

El objetivo final es desarrollar una plataforma capaz de gestionar el ecosistema competitivo de fútbol amateur desde la organización de un partido hasta la determinación del mejor equipo de una región.