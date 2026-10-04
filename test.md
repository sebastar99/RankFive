# Brecha de tests según JaCoCo (clases con 0% de cobertura)

A partir de jacoco.csv, estas son las clases/inner classes que actualmente no tienen ninguna línea cubierta por tests.

- com.tallerwebi.presentacion.ControladorSecciones.Resumen
- com.tallerwebi.presentacion.DTO.DatosEquipoVista
- com.tallerwebi.dominio.ServicioCompetencia
- com.tallerwebi.dominio.RepositorioInscripcionTorneo

Notas:
- Las interfaces (p. ej., `ServicioCompetencia`, `RepositorioInscripcionTorneo`) suelen cubrirse indirectamente a través de sus implementaciones; sin embargo, JaCoCo reporta 0% en la interfaz misma.
- La inner class `ControladorSecciones.Resumen` aparece sin cobertura; podría cubrirse vía tests que ejerciten el cálculo de historial/resumen desde el controlador o aislando su lógica.
- `DatosEquipoVista` es un DTO: si tiene lógica (p. ej. validaciones/derivaciones), conviene agregar tests; si sólo es contenedor, su 0% puede ser aceptable según la política del proyecto.

## Clases con cobertura parcial (faltan líneas, ramas y/o métodos)

Listado de clases donde JaCoCo reporta algo sin cubrir. Entre paréntesis: líneas faltantes, ramas faltantes, métodos faltantes.

- com.tallerwebi.dominio.implementacion.ServicioPartidoImpl (líneas 4, ramas 13, métodos 1)
- com.tallerwebi.dominio.implementacion.ServicioLoginImpl (líneas 1, ramas 5, métodos 0)
- com.tallerwebi.dominio.implementacion.ServicioRelacionAmistadImpl (líneas 3, ramas 9, métodos 0)
- com.tallerwebi.dominio.implementacion.ServicioCompetenciaImpl (líneas 58, ramas 64, métodos 12)
- com.tallerwebi.dominio.implementacion.ServicioEquipoImpl (líneas 14, ramas 29, métodos 4)
- com.tallerwebi.dominio.implementacion.ServicioLigaImpl (líneas 16, ramas 36, métodos 4)
- com.tallerwebi.dominio.implementacion.ServicioArbitroImpl (líneas 6, ramas 15, métodos 0)
- com.tallerwebi.dominio.implementacion.ServicioChatImpl (líneas 3, ramas 12, métodos 1)
- com.tallerwebi.presentacion.ControladorSecciones (líneas 101, ramas 63, métodos 11)
- com.tallerwebi.presentacion.ControladorChat (líneas 2, ramas 4, métodos 0)
- com.tallerwebi.presentacion.ControladorSecciones.Resumen (líneas 7, ramas 0, métodos 1) [0% de cobertura]
- com.tallerwebi.presentacion.ControladorAmigos (líneas 2, ramas 2, métodos 0)
- com.tallerwebi.presentacion.ControladorBase (líneas 4, ramas 2, métodos 1)
- com.tallerwebi.presentacion.Avisos (líneas 4, ramas 3, métodos 0)
- com.tallerwebi.presentacion.ControladorLogin (líneas 4, ramas 4, métodos 0)
- com.tallerwebi.presentacion.ControladorEquipo (líneas 3, ramas 3, métodos 1)
- com.tallerwebi.presentacion.ControladorPartido (líneas 8, ramas 9, métodos 1)
- com.tallerwebi.presentacion.NotificacionesAdvice (líneas 4, ramas 4, métodos 0)
- com.tallerwebi.presentacion.ControladorLiga (líneas 4, ramas 6, métodos 0)
- com.tallerwebi.presentacion.ControladorTransferencias (líneas 0, ramas 1, métodos 0)
- com.tallerwebi.presentacion.DTO.MensajeChatDto (líneas 0, ramas 3, métodos 0)
- com.tallerwebi.infraestructura.RepositorioPartidoLigaImpl (líneas 10, ramas 0, métodos 4)
- com.tallerwebi.infraestructura.RepositorioInscripcionTorneoImpl (líneas 19, ramas 4, métodos 6)
- com.tallerwebi.infraestructura.RepositorioLigaImpl (líneas 7, ramas 0, métodos 3)
- com.tallerwebi.infraestructura.RepositorioNotificacionEncuentroImpl (líneas 7, ramas 0, métodos 2)
- com.tallerwebi.infraestructura.RepositorioNotificacionPlImpl (líneas 18, ramas 0, métodos 4)
- com.tallerwebi.infraestructura.RepositorioTorneoImpl (líneas 5, ramas 0, métodos 2)
- com.tallerwebi.infraestructura.RepositorioEncuentroTorneoImpl (líneas 3, ramas 0, métodos 2)
- com.tallerwebi.infraestructura.RepositorioAmistadImpl (líneas 1, ramas 3, métodos 0)
- com.tallerwebi.infraestructura.RepositorioArbitroImpl (líneas 18, ramas 4, métodos 6)
- com.tallerwebi.infraestructura.RepositorioInscripcionLigaImpl (líneas 12, ramas 4, métodos 4)
- com.tallerwebi.infraestructura.RepositorioInvitacionEquipoImpl (líneas 0, ramas 1, métodos 0)
- com.tallerwebi.infraestructura.RepositorioUsuarioImpl (líneas 6, ramas 0, métodos 2)
- com.tallerwebi.infraestructura.AlmacenamientoImagenesImpl (líneas 16, ramas 15, métodos 2)
- com.tallerwebi.dominio.Equipo (líneas 2, ramas 5, métodos 0)
- com.tallerwebi.dominio.ChatMensaje (líneas 2, ramas 0, métodos 1)
- com.tallerwebi.dominio.Conversacion (líneas 1, ramas 5, métodos 1)
- com.tallerwebi.dominio.InscripcionLiga (líneas 1, ramas 1, métodos 0)
- com.tallerwebi.dominio.GeneradorFixtureTorneo (líneas 14, ramas 21, métodos 0)
- com.tallerwebi.dominio.Arbitro (líneas 0, ramas 4, métodos 0)
- com.tallerwebi.dominio.Partido (líneas 0, ramas 1, métodos 0)
- com.tallerwebi.dominio.NotificacionPl (líneas 2, ramas 4, métodos 1)
- com.tallerwebi.dominio.Rango (líneas 1, ramas 1, métodos 1)
- com.tallerwebi.dominio.RangoEquipo (líneas 8, ramas 2, métodos 7)
- com.tallerwebi.dominio.Amistad (líneas 0, ramas 2, métodos 0)
- com.tallerwebi.dominio.TablaPosiciones (líneas 0, ramas 6, métodos 0)

## Siguientes pasos sugeridos
- Priorizar controladores y servicios con mayor brecha (p. ej., ControladorSecciones, ServicioCompetenciaImpl, ServicioLigaImpl, ServicioEquipoImpl), enfocando en ramas no cubiertas.
- Agregar casos de error/edge para bajar `BRANCH_MISSED` (validaciones, estados alternativos, paths de error).
- Para repositorios con ramas/métodos faltantes, cubrir consultas y escenarios de borde (sin resultados, duplicados, transacciones).

## Detalle por método con faltantes

### com.tallerwebi.dominio.implementacion.com.tallerwebi.dominio.implementacion.ServicioPartidoImpl
- registrarResultado (Ljava/lang/Long;II)Z (línea 54): instr=0, ramas=2, líneas=0, complejidad=2
- listarNotificacionesNoLeidas (Lcom/tallerwebi/dominio/Usuario;)Ljava/util/List; (línea 83): instr=0, ramas=1, líneas=0, complejidad=1
- marcarNotificacionesLeidas (Lcom/tallerwebi/dominio/Usuario;)V (línea 91): instr=1, ramas=2, líneas=1, complejidad=2
- listarNotificacionesDe (Lcom/tallerwebi/dominio/Usuario;)Ljava/util/List; (línea 99): instr=13, ramas=4, líneas=3, complejidad=3
- sonEquiposValidos (Ljava/util/List;Ljava/util/List;)Z (línea 106): instr=0, ramas=3, líneas=0, complejidad=3
- ajustarPl (Lcom/tallerwebi/dominio/Partido;II)V (línea 122): instr=0, ramas=1, líneas=0, complejidad=1

### com.tallerwebi.dominio.implementacion.com.tallerwebi.dominio.implementacion.ServicioLoginImpl
- registrar (Lcom/tallerwebi/dominio/Usuario;)V (línea 29): instr=9, ramas=5, líneas=1, complejidad=4

### com.tallerwebi.dominio.implementacion.com.tallerwebi.dominio.implementacion.ServicioRelacionAmistadImpl
- listarAmigosDe (Lcom/tallerwebi/dominio/Usuario;)Ljava/util/List; (línea 63): instr=6, ramas=1, líneas=1, complejidad=1
- listarSolicitudesPendientes (Lcom/tallerwebi/dominio/Usuario;)Ljava/util/List; (línea 71): instr=9, ramas=3, líneas=1, complejidad=2
- contarSolicitudesPendientes (Lcom/tallerwebi/dominio/Usuario;)J (línea 79): instr=9, ramas=3, líneas=1, complejidad=2
- sonUsuariosValidos (Lcom/tallerwebi/dominio/Usuario;Lcom/tallerwebi/dominio/Usuario;)Z (línea 86): instr=0, ramas=1, líneas=0, complejidad=1
- responderSolicitud (Ljava/lang/Long;Lcom/tallerwebi/dominio/Usuario;Lcom/tallerwebi/dominio/EstadoAmistad;)Z (línea 112): instr=0, ramas=1, líneas=0, complejidad=1

### com.tallerwebi.dominio.implementacion.com.tallerwebi.dominio.implementacion.ServicioCompetenciaImpl
- listarTorneos ()Ljava/util/List; (línea 70): instr=10, ramas=2, líneas=2, complejidad=2
- listarLigas ()Ljava/util/List; (línea 76): instr=10, ramas=2, líneas=2, complejidad=2
- inscribirATorneo (Lcom/tallerwebi/dominio/Usuario;Ljava/lang/Long;)Lcom/tallerwebi/dominio/ResultadoInscripcion; (línea 82): instr=0, ramas=3, líneas=0, complejidad=3
- validarInscripcion (Lcom/tallerwebi/dominio/Usuario;Lcom/tallerwebi/dominio/Torneo;)Lcom/tallerwebi/dominio/ResultadoInscripcion; (línea 108): instr=2, ramas=3, líneas=1, complejidad=3
- tieneEquipoActivo (Lcom/tallerwebi/dominio/Usuario;)Z (línea 129): instr=2, ramas=2, líneas=0, complejidad=2
- calcularTablasPorGrupo (Ljava/lang/Long;)Ljava/util/Map; (línea 135): instr=14, ramas=2, líneas=5, complejidad=2
- obtenerCampeon (Ljava/lang/Long;)Lcom/tallerwebi/dominio/InscripcionTorneo; (línea 146): instr=10, ramas=2, líneas=3, complejidad=2
- listarEquiposInscriptos (Ljava/lang/Long;)Ljava/util/List; (línea 154): instr=9, ramas=2, líneas=3, complejidad=2
- yaInscripto (Lcom/tallerwebi/dominio/Usuario;Ljava/lang/Long;)Z (línea 162): instr=16, ramas=6, líneas=3, complejidad=4
- listarInscripciones (Ljava/lang/Long;)Ljava/util/List; (línea 170): instr=9, ramas=2, líneas=3, complejidad=2
- buscarTorneoPorId (Ljava/lang/Long;)Lcom/tallerwebi/dominio/Torneo; (línea 178): instr=9, ramas=2, líneas=3, complejidad=2
- obtenerFases (Ljava/lang/Long;)Ljava/util/List; (línea 186): instr=9, ramas=2, líneas=3, complejidad=2
- generarFixtureSiNoExiste (Ljava/lang/Long;)Z (línea 194): instr=4, ramas=4, líneas=2, complejidad=4
- guardarTorneo (Lcom/tallerwebi/dominio/Torneo;)V (línea 216): instr=8, ramas=2, líneas=4, complejidad=2
- registrarResultadoEncuentro (Ljava/lang/Long;Ljava/lang/Integer;Ljava/lang/Integer;)Z (línea 224): instr=2, ramas=4, líneas=1, complejidad=4
- golesValidos (Ljava/lang/Integer;Ljava/lang/Integer;)Z (línea 245): instr=1, ramas=4, líneas=0, complejidad=4
- avanzarSiCorresponde (Ljava/lang/Long;Ljava/lang/Long;)V (línea 249): instr=8, ramas=5, líneas=2, complejidad=5
- siguienteDeGrupos (Lcom/tallerwebi/dominio/FaseTorneo;Ljava/lang/Long;)Lcom/tallerwebi/dominio/FaseTorneo; (línea 263): instr=12, ramas=2, líneas=4, complejidad=2
- siguienteEliminatoria (Lcom/tallerwebi/dominio/FaseTorneo;)Lcom/tallerwebi/dominio/FaseTorneo; (línea 273): instr=2, ramas=1, líneas=1, complejidad=1
- asignarGanador (Lcom/tallerwebi/dominio/EncuentroTorneo;II)V (línea 281): instr=11, ramas=3, líneas=3, complejidad=2
- notificarParticipantes (Lcom/tallerwebi/dominio/EncuentroTorneo;Ljava/lang/String;)V (línea 291): instr=36, ramas=7, líneas=9, complejidad=6
- guardarLiga (Lcom/tallerwebi/dominio/Liga;)V (línea 318): instr=8, ramas=2, líneas=4, complejidad=2

### com.tallerwebi.dominio.implementacion.com.tallerwebi.dominio.implementacion.ServicioEquipoImpl
- listarEquiposActivosDe (Lcom/tallerwebi/dominio/Usuario;)Ljava/util/List; (línea 114): instr=13, ramas=4, líneas=3, complejidad=3
- listarInvitacionesPendientes (Lcom/tallerwebi/dominio/Usuario;)Ljava/util/List; (línea 122): instr=13, ramas=4, líneas=3, complejidad=3
- contarInvitacionesPendientes (Lcom/tallerwebi/dominio/Usuario;)J (línea 130): instr=13, ramas=4, líneas=3, complejidad=3
- validar (Lcom/tallerwebi/dominio/Usuario;Ljava/lang/String;Ljava/lang/Integer;Ljava/util/Set;)Lcom/tallerwebi/dominio/ResultadoCreacionEquipo; (línea 142): instr=2, ramas=4, líneas=1, complejidad=4
- normalizar (Ljava/util/List;Lcom/tallerwebi/dominio/Usuario;)Ljava/util/Set; (línea 181): instr=4, ramas=6, líneas=2, complejidad=6
- invitacionRespondible (Ljava/lang/Long;Lcom/tallerwebi/dominio/Usuario;)Lcom/tallerwebi/dominio/InvitacionEquipo; (línea 196): instr=4, ramas=5, líneas=2, complejidad=5
- lambda$resolverAmigos$2 (Lcom/tallerwebi/dominio/Usuario;Lcom/tallerwebi/dominio/Usuario;)Lcom/tallerwebi/dominio/Usuario; (línea 166): instr=2, ramas=0, líneas=1, complejidad=1
- lambda$resolverAmigos$0 (Lcom/tallerwebi/dominio/Usuario;)Z (línea 164): instr=1, ramas=2, líneas=0, complejidad=2

### com.tallerwebi.dominio.implementacion.com.tallerwebi.dominio.implementacion.ServicioLigaImpl
- buscarPorId (Ljava/lang/Long;)Lcom/tallerwebi/dominio/Liga; (línea 58): instr=9, ramas=2, líneas=1, complejidad=2
- inscribir (Lcom/tallerwebi/dominio/Usuario;Ljava/lang/Long;)Lcom/tallerwebi/dominio/ResultadoInscripcion; (línea 63): instr=2, ramas=4, líneas=1, complejidad=4
- validar (Lcom/tallerwebi/dominio/Usuario;Lcom/tallerwebi/dominio/Liga;)Lcom/tallerwebi/dominio/ResultadoInscripcion; (línea 85): instr=4, ramas=4, líneas=2, complejidad=4
- yaInscripto (Lcom/tallerwebi/dominio/Usuario;Ljava/lang/Long;)Z (línea 106): instr=16, ramas=6, líneas=3, complejidad=4
- listarInscripciones (Ljava/lang/Long;)Ljava/util/List; (línea 114): instr=9, ramas=2, líneas=1, complejidad=2
- generarFixtureSiNoExiste (Ljava/lang/Long;)Z (línea 119): instr=4, ramas=5, líneas=2, complejidad=5
- obtenerFechas (Ljava/lang/Long;)Ljava/util/Map; (línea 149): instr=2, ramas=1, líneas=1, complejidad=1
- registrarResultado (Ljava/lang/Long;Ljava/lang/Integer;Ljava/lang/Integer;)Z (línea 161): instr=4, ramas=6, líneas=2, complejidad=6
- calcularTabla (Ljava/lang/Long;)Ljava/util/List; (línea 180): instr=3, ramas=4, líneas=2, complejidad=4
- listarEquiposInscriptos (Ljava/lang/Long;)Ljava/util/List; (línea 205): instr=9, ramas=2, líneas=1, complejidad=2

### com.tallerwebi.dominio.implementacion.com.tallerwebi.dominio.implementacion.ServicioArbitroImpl
- designar (Ljava/util/List;)V (línea 51): instr=2, ramas=3, líneas=2, complejidad=3
- datosValidos (Lcom/tallerwebi/dominio/Usuario;Ljava/lang/Long;Ljava/lang/Integer;)Z (línea 91): instr=2, ramas=5, líneas=1, complejidad=5
- puedeCalificar (Lcom/tallerwebi/dominio/Usuario;Lcom/tallerwebi/dominio/EncuentroTorneo;)Z (línea 98): instr=2, ramas=3, líneas=1, complejidad=3
- participo (Lcom/tallerwebi/dominio/Usuario;Lcom/tallerwebi/dominio/EncuentroTorneo;)Z (línea 108): instr=0, ramas=1, líneas=0, complejidad=1
- perteneceA (Lcom/tallerwebi/dominio/Usuario;Lcom/tallerwebi/dominio/InscripcionTorneo;)Z (línea 115): instr=4, ramas=3, líneas=2, complejidad=3

### com.tallerwebi.dominio.implementacion.com.tallerwebi.dominio.implementacion.ServicioChatImpl
- marcarLeidos (Lcom/tallerwebi/dominio/Usuario;Ljava/lang/Long;)V (línea 76): instr=0, ramas=1, líneas=0, complejidad=1
- conversaciones (Lcom/tallerwebi/dominio/Usuario;)Ljava/util/List; (línea 85): instr=2, ramas=1, líneas=1, complejidad=1
- buscarUsuario (Ljava/lang/Long;)Lcom/tallerwebi/dominio/Usuario; (línea 114): instr=9, ramas=2, líneas=1, complejidad=2
- validarTexto (Ljava/lang/String;)Ljava/lang/String; (línea 118): instr=2, ramas=1, líneas=0, complejidad=1
- validarParticipantes (Lcom/tallerwebi/dominio/Usuario;Ljava/lang/Long;)V (línea 129): instr=0, ramas=2, líneas=0, complejidad=2
- conversacionEntre (Lcom/tallerwebi/dominio/Usuario;Ljava/lang/Long;)Lcom/tallerwebi/dominio/Conversacion; (línea 143): instr=2, ramas=2, líneas=1, complejidad=2
- obtenerOCrearConversacion (Lcom/tallerwebi/dominio/Usuario;Lcom/tallerwebi/dominio/Usuario;)Lcom/tallerwebi/dominio/Conversacion; (línea 150): instr=6, ramas=3, líneas=0, complejidad=3

### com.tallerwebi.presentacion.com.tallerwebi.presentacion.ControladorSecciones
- torneos (Ljakarta/servlet/http/HttpServletRequest;Ljava/lang/String;)Lorg/springframework/web/servlet/ModelAndView; (línea 62): instr=5, ramas=2, líneas=1, complejidad=2
- inscribirseTorneo (Ljakarta/servlet/http/HttpServletRequest;Ljava/lang/Long;)Lorg/springframework/web/servlet/ModelAndView; (línea 95): instr=5, ramas=1, líneas=1, complejidad=1
- detalleTorneo (Ljakarta/servlet/http/HttpServletRequest;Ljava/lang/Long;Ljava/lang/String;)Lorg/springframework/web/servlet/ModelAndView; (línea 112): instr=10, ramas=2, líneas=2, complejidad=2
- calificarArbitro (Ljakarta/servlet/http/HttpServletRequest;Ljava/lang/Long;Ljava/lang/Long;Ljava/lang/Integer;)Lorg/springframework/web/servlet/ModelAndView; (línea 142): instr=31, ramas=4, líneas=6, complejidad=3
- proximoEncuentro (Ljava/util/List;)Lcom/tallerwebi/dominio/EncuentroTorneo; (línea 152): instr=4, ramas=3, líneas=3, complejidad=3
- generarFixture (Ljakarta/servlet/http/HttpServletRequest;Ljava/lang/Long;)Lorg/springframework/web/servlet/ModelAndView; (línea 167): instr=6, ramas=2, líneas=1, complejidad=2
- nuevoTorneo (Ljakarta/servlet/http/HttpServletRequest;)Lorg/springframework/web/servlet/ModelAndView; (línea 178): instr=24, ramas=2, líneas=5, complejidad=2
- crearTorneo (Ljakarta/servlet/http/HttpServletRequest;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Integer;Ljava/lang/Integer;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Boolean;)Lorg/springframework/web/servlet/ModelAndView; (línea 197): instr=46, ramas=2, líneas=13, complejidad=2
- registrarResultadoEncuentro (Ljakarta/servlet/http/HttpServletRequest;Ljava/lang/Long;Ljava/lang/Long;Ljava/lang/Integer;Ljava/lang/Integer;)Lorg/springframework/web/servlet/ModelAndView; (línea 221): instr=5, ramas=1, líneas=1, complejidad=1
- nuevaLiga (Ljakarta/servlet/http/HttpServletRequest;)Lorg/springframework/web/servlet/ModelAndView; (línea 232): instr=24, ramas=2, líneas=5, complejidad=2
- crearLiga (Ljakarta/servlet/http/HttpServletRequest;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Integer;Ljava/lang/Integer;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Boolean;)Lorg/springframework/web/servlet/ModelAndView; (línea 251): instr=46, ramas=2, líneas=13, complejidad=2
- historial (Ljakarta/servlet/http/HttpServletRequest;)Lorg/springframework/web/servlet/ModelAndView; (línea 269): instr=29, ramas=2, líneas=6, complejidad=2
- modeloHistorial (Lcom/tallerwebi/dominio/Usuario;)Ljava/util/Map; (línea 280): instr=63, ramas=0, líneas=11, complejidad=1
- deltasPorPartido (Lcom/tallerwebi/dominio/Usuario;)Ljava/util/Map; (línea 294): instr=36, ramas=6, líneas=7, complejidad=4
- resumen (Ljava/util/List;Lcom/tallerwebi/dominio/Usuario;)Lcom/tallerwebi/presentacion/ControladorSecciones$Resumen; (línea 305): instr=66, ramas=10, líneas=18, complejidad=6
- esEmpate (Lcom/tallerwebi/dominio/Partido;)Z (línea 329): instr=25, ramas=6, líneas=3, complejidad=4
- ganoUsuario (Lcom/tallerwebi/dominio/Partido;Lcom/tallerwebi/dominio/Usuario;)Z (línea 335): instr=48, ramas=16, líneas=5, complejidad=9

### com.tallerwebi.presentacion.com.tallerwebi.presentacion.ControladorChat
- chat (Ljakarta/servlet/http/HttpServletRequest;Ljava/lang/Long;)Lorg/springframework/web/servlet/ModelAndView; (línea 50): instr=0, ramas=2, líneas=0, complejidad=2
- historial (Ljakarta/servlet/http/HttpServletRequest;Ljava/lang/Long;)Lorg/springframework/http/ResponseEntity; (línea 80): instr=4, ramas=1, líneas=1, complejidad=1
- marcarLeidos (Ljakarta/servlet/http/HttpServletRequest;Ljava/lang/Long;)Lorg/springframework/http/ResponseEntity; (línea 120): instr=4, ramas=1, líneas=1, complejidad=1

### com.tallerwebi.presentacion.com.tallerwebi.presentacion.ControladorSecciones$Resumen
- <init> (IIIII)V (línea 350): instr=18, ramas=0, líneas=7, complejidad=1

### com.tallerwebi.presentacion.com.tallerwebi.presentacion.ControladorAmigos
- listarPendientes (Lcom/tallerwebi/dominio/Usuario;)Ljava/util/List; (línea 141): instr=2, ramas=1, líneas=1, complejidad=1
- contarPendientes (Lcom/tallerwebi/dominio/Usuario;)J (línea 148): instr=2, ramas=1, líneas=1, complejidad=1

### com.tallerwebi.presentacion.com.tallerwebi.presentacion.ControladorBase
- vistaProtegida (Ljakarta/servlet/http/HttpServletRequest;Ljava/lang/String;)Lorg/springframework/web/servlet/ModelAndView; (línea 42): instr=19, ramas=2, líneas=4, complejidad=2

### com.tallerwebi.presentacion.com.tallerwebi.presentacion.Avisos
- agregar (Ljava/util/Map;Ljava/lang/String;)V (línea 39): instr=10, ramas=3, líneas=4, complejidad=3

### com.tallerwebi.presentacion.com.tallerwebi.presentacion.ControladorLogin
- obtenerRankingAmigos (Lcom/tallerwebi/dominio/Usuario;)Ljava/util/List; (línea 151): instr=2, ramas=1, líneas=1, complejidad=1
- obtenerEquipos (Lcom/tallerwebi/dominio/Usuario;)Ljava/util/List; (línea 158): instr=6, ramas=1, líneas=1, complejidad=1
- obtenerSolicitudesPendientes (Lcom/tallerwebi/dominio/Usuario;)Ljava/util/List; (línea 165): instr=2, ramas=1, líneas=1, complejidad=1
- contarSolicitudesPendientes (Lcom/tallerwebi/dominio/Usuario;)J (línea 172): instr=2, ramas=1, líneas=1, complejidad=1

### com.tallerwebi.presentacion.com.tallerwebi.presentacion.ControladorEquipo
- aceptarInvitacion (Ljakarta/servlet/http/HttpServletRequest;Ljava/lang/Long;)Lorg/springframework/web/servlet/ModelAndView; (línea 89): instr=1, ramas=1, líneas=0, complejidad=1
- rechazarInvitacion (Ljakarta/servlet/http/HttpServletRequest;Ljava/lang/Long;)Lorg/springframework/web/servlet/ModelAndView; (línea 104): instr=2, ramas=1, líneas=0, complejidad=1
- guardarEscudo (Lorg/springframework/web/multipart/MultipartFile;)Ljava/lang/String; (línea 115): instr=3, ramas=1, líneas=2, complejidad=1
- rankingEquipos (Ljakarta/servlet/http/HttpServletRequest;)Lorg/springframework/web/servlet/ModelAndView; (línea 132): instr=5, ramas=0, líneas=1, complejidad=1

### com.tallerwebi.presentacion.com.tallerwebi.presentacion.ControladorPartido
- marcarNotificacionesLeidas (Ljakarta/servlet/http/HttpServletRequest;)V (línea 65): instr=11, ramas=2, líneas=4, complejidad=2
- crearPartido (Ljakarta/servlet/http/HttpServletRequest;Ljava/lang/String;Ljava/lang/Integer;Ljava/util/List;Ljava/util/List;)Lorg/springframework/web/servlet/ModelAndView; (línea 79): instr=21, ramas=5, líneas=4, complejidad=5
- actualizarUsuarioEnSesion (Ljakarta/servlet/http/HttpServletRequest;Lcom/tallerwebi/dominio/Usuario;)V (línea 137): instr=0, ramas=1, líneas=0, complejidad=1
- filtrarPorIds (Ljava/util/List;Ljava/util/List;)Ljava/util/List; (línea 172): instr=0, ramas=1, líneas=0, complejidad=1

### com.tallerwebi.presentacion.com.tallerwebi.presentacion.NotificacionesAdvice
- chatConversaciones (Ljakarta/servlet/http/HttpServletRequest;)Ljava/util/List; (línea 45): instr=5, ramas=1, líneas=1, complejidad=1
- avisosPl (Ljakarta/servlet/http/HttpServletRequest;)Ljava/util/List; (línea 54): instr=5, ramas=1, líneas=1, complejidad=1
- invitacionesEquipo (Ljakarta/servlet/http/HttpServletRequest;)Ljava/util/List; (línea 63): instr=5, ramas=1, líneas=1, complejidad=1
- usuarioDe (Ljakarta/servlet/http/HttpServletRequest;)Lcom/tallerwebi/dominio/Usuario; (línea 71): instr=5, ramas=1, líneas=1, complejidad=1

### com.tallerwebi.presentacion.com.tallerwebi.presentacion.ControladorLiga
- detalle (Ljakarta/servlet/http/HttpServletRequest;Ljava/lang/Long;Ljava/lang/String;)Lorg/springframework/web/servlet/ModelAndView; (línea 45): instr=5, ramas=1, líneas=1, complejidad=1
- inscribirse (Ljakarta/servlet/http/HttpServletRequest;Ljava/lang/Long;)Lorg/springframework/web/servlet/ModelAndView; (línea 66): instr=5, ramas=1, líneas=1, complejidad=1
- generarFixture (Ljakarta/servlet/http/HttpServletRequest;Ljava/lang/Long;)Lorg/springframework/web/servlet/ModelAndView; (línea 79): instr=6, ramas=2, líneas=1, complejidad=2
- registrarResultado (Ljakarta/servlet/http/HttpServletRequest;Ljava/lang/Long;Ljava/lang/Long;Ljava/lang/Integer;Ljava/lang/Integer;)Lorg/springframework/web/servlet/ModelAndView; (línea 96): instr=7, ramas=2, líneas=1, complejidad=2

### com.tallerwebi.presentacion.com.tallerwebi.presentacion.ControladorTransferencias
- nombreCompetencia (ZLjava/lang/Long;)Ljava/lang/String; (línea 82): instr=2, ramas=1, líneas=0, complejidad=1

### com.tallerwebi.presentacion.DTO.com.tallerwebi.presentacion.DTO.MensajeChatDto
- nombreVisible (Lcom/tallerwebi/dominio/Usuario;)Ljava/lang/String; (línea 32): instr=0, ramas=3, líneas=0, complejidad=3

### com.tallerwebi.presentacion.DTO.com.tallerwebi.presentacion.DTO.DatosEquipoVista
- <init> (Lcom/tallerwebi/dominio/Equipo;)V (línea 22): instr=42, ramas=0, líneas=14, complejidad=1
- desde (Ljava/util/List;)Ljava/util/List; (línea 39): instr=6, ramas=0, líneas=1, complejidad=1
- plDe (Lcom/tallerwebi/dominio/Usuario;)I (línea 43): instr=14, ramas=4, líneas=3, complejidad=3

### com.tallerwebi.infraestructura.com.tallerwebi.infraestructura.RepositorioPartidoLigaImpl
- guardar (Lcom/tallerwebi/dominio/PartidoLiga;)V (línea 22): instr=6, ramas=0, líneas=2, complejidad=1
- actualizar (Lcom/tallerwebi/dominio/PartidoLiga;)V (línea 27): instr=7, ramas=0, líneas=2, complejidad=1
- buscarPorId (Ljava/lang/Long;)Lcom/tallerwebi/dominio/PartidoLiga; (línea 32): instr=8, ramas=0, líneas=1, complejidad=1
- listarPorLiga (Ljava/lang/Long;)Ljava/util/List; (línea 37): instr=11, ramas=0, líneas=5, complejidad=1

### com.tallerwebi.infraestructura.com.tallerwebi.infraestructura.RepositorioInscripcionTorneoImpl
- guardar (Lcom/tallerwebi/dominio/InscripcionTorneo;)V (línea 22): instr=6, ramas=0, líneas=2, complejidad=1
- existePara (Ljava/lang/Long;Ljava/lang/Long;)Z (línea 27): instr=26, ramas=4, líneas=7, complejidad=3
- listarPorTorneo (Ljava/lang/Long;)Ljava/util/List; (línea 41): instr=11, ramas=0, líneas=5, complejidad=1
- listarEquipos (Ljava/lang/Long;)Ljava/util/List; (línea 53): instr=11, ramas=0, líneas=5, complejidad=1

### com.tallerwebi.infraestructura.com.tallerwebi.infraestructura.RepositorioLigaImpl
- guardar (Lcom/tallerwebi/dominio/Liga;)V (línea 22): instr=6, ramas=0, líneas=2, complejidad=1
- buscarPorId (Ljava/lang/Long;)Lcom/tallerwebi/dominio/Liga; (línea 27): instr=8, ramas=0, líneas=1, complejidad=1
- listarTodos ()Ljava/util/List; (línea 32): instr=8, ramas=0, líneas=4, complejidad=1

### com.tallerwebi.infraestructura.com.tallerwebi.infraestructura.RepositorioNotificacionEncuentroImpl
- guardar (Lcom/tallerwebi/dominio/NotificacionEncuentro;)V (línea 22): instr=6, ramas=0, líneas=2, complejidad=1
- listarNoLeidas (Ljava/lang/Long;)Ljava/util/List; (línea 27): instr=11, ramas=0, líneas=5, complejidad=1

### com.tallerwebi.infraestructura.com.tallerwebi.infraestructura.RepositorioNotificacionPlImpl
- guardar (Lcom/tallerwebi/dominio/NotificacionPl;)V (línea 22): instr=6, ramas=0, líneas=2, complejidad=1
- listarNoLeidasDe (Ljava/lang/Long;)Ljava/util/List; (línea 27): instr=11, ramas=0, líneas=5, complejidad=1
- marcarLeidasDe (Ljava/lang/Long;)V (línea 39): instr=11, ramas=0, líneas=6, complejidad=1
- listarDe (Ljava/lang/Long;)Ljava/util/List; (línea 50): instr=11, ramas=0, líneas=5, complejidad=1

### com.tallerwebi.infraestructura.com.tallerwebi.infraestructura.RepositorioTorneoImpl
- buscarPorId (Ljava/lang/Long;)Lcom/tallerwebi/dominio/Torneo; (línea 27): instr=8, ramas=0, líneas=1, complejidad=1
- listarTodos ()Ljava/util/List; (línea 32): instr=8, ramas=0, líneas=4, complejidad=1

### com.tallerwebi.infraestructura.com.tallerwebi.infraestructura.RepositorioEncuentroTorneoImpl
- buscarPorId (Ljava/lang/Long;)Lcom/tallerwebi/dominio/EncuentroTorneo; (línea 21): instr=8, ramas=0, líneas=1, complejidad=1
- actualizar (Lcom/tallerwebi/dominio/EncuentroTorneo;)V (línea 31): instr=7, ramas=0, líneas=2, complejidad=1

### com.tallerwebi.infraestructura.com.tallerwebi.infraestructura.RepositorioAmistadImpl
- contarSolicitudesPendientesPara (Ljava/lang/Long;)J (línea 98): instr=1, ramas=1, líneas=0, complejidad=1
- puntosDe (Lcom/tallerwebi/dominio/Usuario;)I (línea 117): instr=2, ramas=2, líneas=1, complejidad=2

### com.tallerwebi.infraestructura.com.tallerwebi.infraestructura.RepositorioArbitroImpl
- guardar (Lcom/tallerwebi/dominio/Arbitro;)V (línea 23): instr=6, ramas=0, líneas=2, complejidad=1
- actualizar (Lcom/tallerwebi/dominio/Arbitro;)V (línea 28): instr=7, ramas=0, líneas=2, complejidad=1
- buscarPorId (Ljava/lang/Long;)Lcom/tallerwebi/dominio/Arbitro; (línea 33): instr=8, ramas=0, líneas=1, complejidad=1
- listarTodos ()Ljava/util/List; (línea 38): instr=8, ramas=0, líneas=4, complejidad=1
- guardarCalificacion (Lcom/tallerwebi/dominio/CalificacionArbitro;)V (línea 46): instr=6, ramas=0, líneas=2, complejidad=1
- existeCalificacion (Ljava/lang/Long;Ljava/lang/Long;)Z (línea 51): instr=26, ramas=4, líneas=7, complejidad=3

### com.tallerwebi.infraestructura.com.tallerwebi.infraestructura.RepositorioInscripcionLigaImpl
- existePara (Ljava/lang/Long;Ljava/lang/Long;)Z (línea 28): instr=26, ramas=4, líneas=7, complejidad=3
- listarEquipos (Ljava/lang/Long;)Ljava/util/List; (línea 54): instr=11, ramas=0, líneas=5, complejidad=1

### com.tallerwebi.infraestructura.com.tallerwebi.infraestructura.RepositorioInvitacionEquipoImpl
- contarPendientesPara (Ljava/lang/Long;)J (línea 72): instr=1, ramas=1, líneas=0, complejidad=1

### com.tallerwebi.infraestructura.com.tallerwebi.infraestructura.RepositorioUsuarioImpl
- buscarPorNombreUsuario (Ljava/lang/String;)Lcom/tallerwebi/dominio/Usuario; (línea 46): instr=12, ramas=0, líneas=5, complejidad=1
- buscarPorId (Ljava/lang/Long;)Lcom/tallerwebi/dominio/Usuario; (línea 55): instr=8, ramas=0, líneas=1, complejidad=1

### com.tallerwebi.infraestructura.com.tallerwebi.infraestructura.AlmacenamientoImagenesImpl
- directorioPorDefecto ()Ljava/nio/file/Path; (línea 29): instr=8, ramas=3, líneas=1, complejidad=2
- guardarEscudo (Ljava/lang/String;[B)Ljava/lang/String; (línea 38): instr=38, ramas=4, líneas=8, complejidad=3
- extensionDe (Ljava/lang/String;)Ljava/lang/String; (línea 52): instr=32, ramas=8, líneas=7, complejidad=5

### com.tallerwebi.dominio.com.tallerwebi.dominio.Equipo
- agregarJugador (Lcom/tallerwebi/dominio/Usuario;)V (línea 65): instr=0, ramas=1, líneas=0, complejidad=1
- getPromedioPl ()I (línea 71): instr=2, ramas=2, líneas=1, complejidad=2
- plDe (Lcom/tallerwebi/dominio/Usuario;)I (línea 86): instr=2, ramas=2, líneas=1, complejidad=2

### com.tallerwebi.dominio.com.tallerwebi.dominio.ChatMensaje
- setLeido (Z)V (línea 95): instr=4, ramas=0, líneas=2, complejidad=1

### com.tallerwebi.dominio.com.tallerwebi.dominio.Amistad
- fueEnviadaA (Ljava/lang/Long;)Z (línea 48): instr=0, ramas=2, líneas=0, complejidad=2

### com.tallerwebi.dominio.com.tallerwebi.dominio.Conversacion
- otroParticipante (Ljava/lang/Long;)Lcom/tallerwebi/dominio/Usuario; (línea 44): instr=2, ramas=1, líneas=0, complejidad=1
- participa (Ljava/lang/Long;)Z (línea 48): instr=16, ramas=4, líneas=1, complejidad=3

### com.tallerwebi.dominio.com.tallerwebi.dominio.InscripcionLiga
- getNombreVisible ()Ljava/lang/String; (línea 42): instr=4, ramas=1, líneas=1, complejidad=1

### com.tallerwebi.dominio.com.tallerwebi.dominio.GeneradorFixtureTorneo
- grupoDisponible (Ljava/util/List;)Ljava/lang/String; (línea 28): instr=33, ramas=6, líneas=6, complejidad=4
- crearFaseDeGrupos (Lcom/tallerwebi/dominio/Torneo;Ljava/util/List;)Lcom/tallerwebi/dominio/FaseTorneo; (línea 50): instr=0, ramas=1, líneas=0, complejidad=1
- crearPrimeraEliminatoria (Lcom/tallerwebi/dominio/FaseTorneo;Ljava/util/List;)Lcom/tallerwebi/dominio/FaseTorneo; (línea 75): instr=2, ramas=3, líneas=0, complejidad=3
- estaCompleta (Lcom/tallerwebi/dominio/FaseTorneo;)Z (línea 105): instr=3, ramas=2, líneas=1, complejidad=2
- ganadoresSiCompleta (Lcom/tallerwebi/dominio/FaseTorneo;)Ljava/util/List; (línea 114): instr=4, ramas=2, líneas=2, complejidad=2
- campeon (Ljava/util/List;)Lcom/tallerwebi/dominio/InscripcionTorneo; (línea 128): instr=4, ramas=3, líneas=2, complejidad=3
- puesto (Ljava/util/List;ILjava/util/Map;)Lcom/tallerwebi/dominio/InscripcionTorneo; (línea 160): instr=2, ramas=1, líneas=1, complejidad=1
- agregarCruce (Lcom/tallerwebi/dominio/FaseTorneo;Lcom/tallerwebi/dominio/InscripcionTorneo;Lcom/tallerwebi/dominio/InscripcionTorneo;)V (línea 190): instr=7, ramas=3, líneas=2, complejidad=2

### com.tallerwebi.dominio.com.tallerwebi.dominio.Arbitro
- getPromedio ()D (línea 38): instr=0, ramas=1, líneas=0, complejidad=1
- calificar (I)V (línea 45): instr=4, ramas=3, líneas=0, complejidad=3

### com.tallerwebi.dominio.com.tallerwebi.dominio.NotificacionPl
- getMensaje ()Ljava/lang/String; (línea 40): instr=17, ramas=4, líneas=2, complejidad=3

### com.tallerwebi.dominio.com.tallerwebi.dominio.Rango
- getMinPl ()I (línea 36): instr=3, ramas=0, líneas=1, complejidad=1
- de (Ljava/lang/Integer;)Lcom/tallerwebi/dominio/Rango; (línea 40): instr=2, ramas=1, líneas=0, complejidad=1

### com.tallerwebi.dominio.com.tallerwebi.dominio.InvitacionEquipo
- fueEnviadaA (Ljava/lang/Long;)Z (línea 52): instr=0, ramas=2, líneas=0, complejidad=2

### com.tallerwebi.dominio.com.tallerwebi.dominio.RangoEquipo
- getNombre ()Ljava/lang/String; (línea 35): instr=3, ramas=0, líneas=1, complejidad=1
- getSlug ()Ljava/lang/String; (línea 39): instr=3, ramas=0, líneas=1, complejidad=1
- getMinInclusive ()I (línea 43): instr=3, ramas=0, líneas=1, complejidad=1
- getMaxInclusive ()I (línea 47): instr=3, ramas=0, líneas=1, complejidad=1
- getIconoAsset ()Ljava/lang/String; (línea 51): instr=3, ramas=0, líneas=1, complejidad=1
- getOrden ()I (línea 55): instr=3, ramas=0, líneas=1, complejidad=1
- dePromedio (I)Lcom/tallerwebi/dominio/RangoEquipo; (línea 59): instr=2, ramas=2, líneas=1, complejidad=2
- assetPath (Lcom/tallerwebi/dominio/RangoEquipo;)Ljava/lang/String; (línea 70): instr=4, ramas=0, líneas=1, complejidad=1

### com.tallerwebi.dominio.com.tallerwebi.dominio.ServicioCompetencia
- buscarTorneoPorId (Ljava/lang/Long;)Lcom/tallerwebi/dominio/Torneo; (línea 24): instr=2, ramas=0, líneas=1, complejidad=1
- obtenerFases (Ljava/lang/Long;)Ljava/util/List; (línea 28): instr=2, ramas=0, líneas=1, complejidad=1
- listarTorneos (II)Ljava/util/List; (línea 34): instr=3, ramas=0, líneas=1, complejidad=1
- listarLigas (II)Ljava/util/List; (línea 38): instr=3, ramas=0, líneas=1, complejidad=1
- guardarTorneo (Lcom/tallerwebi/dominio/Torneo;)V (línea 41): instr=1, ramas=0, líneas=1, complejidad=1
- guardarLiga (Lcom/tallerwebi/dominio/Liga;)V (línea 45): instr=1, ramas=0, líneas=1, complejidad=1

### com.tallerwebi.dominio.com.tallerwebi.dominio.RepositorioInscripcionTorneo
- existePara (Ljava/lang/Long;Ljava/lang/Long;)Z (línea 8): instr=2, ramas=0, líneas=1, complejidad=1
- listarPorTorneo (Ljava/lang/Long;)Ljava/util/List; (línea 12): instr=2, ramas=0, líneas=1, complejidad=1
- listarEquipos (Ljava/lang/Long;)Ljava/util/List; (línea 16): instr=2, ramas=0, líneas=1, complejidad=1

### com.tallerwebi.dominio.com.tallerwebi.dominio.Partido
- fueJugado ()Z (línea 50): instr=0, ramas=1, líneas=0, complejidad=1

### com.tallerwebi.dominio.com.tallerwebi.dominio.TablaPosiciones
- calcularPorGrupo (Ljava/util/List;Ljava/util/List;)Ljava/util/Map; (línea 39): instr=0, ramas=2, líneas=0, complejidad=2
- esComputable (Lcom/tallerwebi/dominio/EncuentroTorneo;)Z (línea 57): instr=0, ramas=4, líneas=0, complejidad=4
