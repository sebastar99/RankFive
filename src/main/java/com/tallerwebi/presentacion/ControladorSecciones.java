package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.EncuentroTorneo;
import com.tallerwebi.dominio.FaseTorneo;
import com.tallerwebi.dominio.Liga;
import com.tallerwebi.dominio.NotificacionPl;
import com.tallerwebi.dominio.Partido;
import com.tallerwebi.dominio.ResultadoInscripcion;
import com.tallerwebi.dominio.ServicioArbitro;
import com.tallerwebi.dominio.ServicioCompetencia;
import com.tallerwebi.dominio.ServicioLiga;
import com.tallerwebi.dominio.ServicioPartido;
import com.tallerwebi.dominio.ServicioRelacionAmistad;
import com.tallerwebi.dominio.Torneo;
import com.tallerwebi.dominio.Usuario;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorSecciones extends ControladorBase {

  private final ServicioPartido servicioPartido;
  private final ServicioCompetencia servicioCompetencia;
  private final ServicioArbitro servicioArbitro;
  private final ServicioLiga servicioLiga;

  private static final String AVISO = "aviso";
  private static final String PARAM_AVISO = "&aviso=";
  private static final String REDIRECT_DETALLE = "redirect:/torneos/detalle?id=";
  private static final String ESTADO_PENDIENTE = "PENDIENTE";

  @Autowired
  public ControladorSecciones(
    ServicioRelacionAmistad servicioRelacionAmistad,
    ServicioPartido servicioPartido,
    ServicioCompetencia servicioCompetencia,
    ServicioArbitro servicioArbitro,
    ServicioLiga servicioLiga
  ) {
    super(servicioRelacionAmistad);
    this.servicioPartido = servicioPartido;
    this.servicioCompetencia = servicioCompetencia;
    this.servicioArbitro = servicioArbitro;
    this.servicioLiga = servicioLiga;
  }

  @RequestMapping(path = "/torneos", method = RequestMethod.GET)
  public ModelAndView torneos(
    HttpServletRequest request,
    @RequestParam(name = AVISO, required = false) String aviso
  ) {
    Usuario usuario = usuarioDeSesion(request);
    if (usuario == null) {
      return new ModelAndView(REDIRECT_LOGIN);
    }
    Map<String, Object> modelo = new HashMap<>(modeloBase(usuario));
    List<Torneo> torneos = servicioCompetencia.listarTorneos();
    Set<Long> inscriptos = new HashSet<>();
    for (Torneo torneo : torneos) {
      if (servicioCompetencia.yaInscripto(usuario, torneo.getId())) {
        inscriptos.add(torneo.getId());
      }
    }
    List<Liga> ligas = servicioCompetencia.listarLigas();
    Set<Long> ligasInscriptas = new HashSet<>();
    for (Liga liga : ligas) {
      if (servicioLiga.yaInscripto(usuario, liga.getId())) {
        ligasInscriptas.add(liga.getId());
      }
    }
    modelo.put("torneos", torneos);
    modelo.put("ligas", ligas);
    modelo.put("torneosInscriptos", inscriptos);
    modelo.put("ligasInscriptas", ligasInscriptas);
    modelo.put("tieneEquipoActivo", servicioCompetencia.tieneEquipoActivo(usuario));
    Avisos.agregar(modelo, aviso);
    return new ModelAndView("ligaTorneos", modelo);
  }

  @RequestMapping(path = "/torneos/inscribirse", method = RequestMethod.POST)
  public ModelAndView inscribirseTorneo(
    HttpServletRequest request,
    @RequestParam("torneoId") Long torneoId
  ) {
    Usuario usuario = usuarioDeSesion(request);
    if (usuario == null) {
      return new ModelAndView(REDIRECT_LOGIN);
    }
    ResultadoInscripcion resultado = servicioCompetencia.inscribirATorneo(usuario, torneoId);
    if (resultado.esExitoso()) {
      return new ModelAndView(REDIRECT_DETALLE + torneoId + PARAM_AVISO + resultado.name());
    }
    return new ModelAndView("redirect:/torneos?aviso=" + resultado.name());
  }

  @RequestMapping(path = "/torneos/detalle", method = RequestMethod.GET)
  public ModelAndView detalleTorneo(
    HttpServletRequest request,
    @RequestParam("id") Long torneoId,
    @RequestParam(name = AVISO, required = false) String aviso
  ) {
    Usuario usuario = usuarioDeSesion(request);
    if (usuario == null) {
      return new ModelAndView(REDIRECT_LOGIN);
    }
    Torneo torneo = servicioCompetencia.buscarTorneoPorId(torneoId);
    if (torneo == null) {
      return new ModelAndView("redirect:/torneos");
    }
    List<FaseTorneo> fases = servicioCompetencia.obtenerFases(torneoId);
    Map<String, Object> modelo = new HashMap<>(modeloBase(usuario));
    modelo.put("torneo", torneo);
    modelo.put("inscripciones", servicioCompetencia.listarInscripciones(torneoId));
    modelo.put("fases", fases);
    modelo.put("tablasGrupos", servicioCompetencia.calcularTablasPorGrupo(torneoId));
    modelo.put("campeon", servicioCompetencia.obtenerCampeon(torneoId));
    modelo.put("arbitros", servicioArbitro.listarRanking());
    modelo.put("proximoEncuentro", proximoEncuentro(fases));
    modelo.put("yaInscripto", servicioCompetencia.yaInscripto(usuario, torneoId));
    modelo.put("tieneEquipoActivo", servicioCompetencia.tieneEquipoActivo(usuario));
    Avisos.agregar(modelo, aviso);
    return new ModelAndView("torneoDetalle", modelo);
  }

  @RequestMapping(path = "/torneos/encuentro/calificar-arbitro", method = RequestMethod.POST)
  public ModelAndView calificarArbitro(
    HttpServletRequest request,
    @RequestParam("torneoId") Long torneoId,
    @RequestParam("encuentroId") Long encuentroId,
    @RequestParam("puntaje") Integer puntaje
  ) {
    Usuario usuario = usuarioDeSesion(request);
    if (usuario == null) {
      return new ModelAndView(REDIRECT_LOGIN);
    }
    boolean ok = servicioArbitro.calificar(usuario, encuentroId, puntaje);
    String codigo = ok ? Avisos.CALIFICADO : Avisos.CALIFICACION_INVALIDA;
    return new ModelAndView(REDIRECT_DETALLE + torneoId + PARAM_AVISO + codigo);
  }

  private static EncuentroTorneo proximoEncuentro(List<FaseTorneo> fases) {
    for (FaseTorneo fase : fases) {
      for (EncuentroTorneo encuentro : fase.getEncuentros()) {
        if (ESTADO_PENDIENTE.equals(encuentro.getEstado())) {
          return encuentro;
        }
      }
    }
    return null;
  }

  @RequestMapping(path = "/torneos/generar-fixture", method = RequestMethod.POST)
  public ModelAndView generarFixture(
    HttpServletRequest request,
    @RequestParam("id") Long torneoId
  ) {
    Usuario usuario = usuarioDeSesion(request);
    if (usuario == null) {
      return new ModelAndView(REDIRECT_LOGIN);
    }
    boolean ok = servicioCompetencia.generarFixtureSiNoExiste(torneoId);
    String codigo = ok ? Avisos.FIXTURE_GENERADO : Avisos.FIXTURE_INVALIDO;
    return new ModelAndView(REDIRECT_DETALLE + torneoId + PARAM_AVISO + codigo);
  }

  @RequestMapping(path = "/torneos/nuevo", method = RequestMethod.GET)
  public ModelAndView nuevoTorneo(HttpServletRequest request) {
    Usuario usuario = usuarioDeSesion(request);
    if (usuario == null) {
      return new ModelAndView(REDIRECT_LOGIN);
    }
    Map<String, Object> modelo = new HashMap<>(modeloBase(usuario));
    return new ModelAndView("torneoForm", modelo);
  }

  @RequestMapping(path = "/torneos/crear", method = RequestMethod.POST)
  public ModelAndView crearTorneo(
    HttpServletRequest request,
    @RequestParam("nombre") String nombre,
    @RequestParam("descripcion") String descripcion,
    @RequestParam("formato") Integer formato,
    @RequestParam("cupoEquipos") Integer cupoEquipos,
    @RequestParam("fechaInicio") String fechaInicio,
    @RequestParam("ubicacion") String ubicacion,
    @RequestParam(name = "inscripcionAbierta", defaultValue = "false") Boolean inscripcionAbierta
  ) {
    Usuario usuario = usuarioDeSesion(request);
    if (usuario == null) {
      return new ModelAndView(REDIRECT_LOGIN);
    }
    com.tallerwebi.dominio.Torneo nuevo = new com.tallerwebi.dominio.Torneo();
    nuevo.setNombre(nombre);
    nuevo.setDescripcion(descripcion);
    nuevo.setFormato(formato);
    nuevo.setCupoEquipos(cupoEquipos);
    nuevo.setFechaInicio(java.time.LocalDate.parse(fechaInicio));
    nuevo.setUbicacion(ubicacion);
    nuevo.setInscripcionAbierta(inscripcionAbierta);
    servicioCompetencia.guardarTorneo(nuevo);
    return new ModelAndView("redirect:/torneos");
  }

  @RequestMapping(path = "/torneos/encuentro/resultado", method = RequestMethod.POST)
  public ModelAndView registrarResultadoEncuentro(
    HttpServletRequest request,
    @RequestParam("torneoId") Long torneoId,
    @RequestParam("encuentroId") Long encuentroId,
    @RequestParam("golesA") Integer golesA,
    @RequestParam("golesB") Integer golesB
  ) {
    Usuario usuario = usuarioDeSesion(request);
    if (usuario == null) {
      return new ModelAndView(REDIRECT_LOGIN);
    }
    boolean ok = servicioCompetencia.registrarResultadoEncuentro(encuentroId, golesA, golesB);
    String codigo = ok ? Avisos.RESULTADO_GUARDADO : Avisos.RESULTADO_INVALIDO;
    return new ModelAndView(REDIRECT_DETALLE + torneoId + PARAM_AVISO + codigo);
  }

  @RequestMapping(path = "/ligas/nueva", method = RequestMethod.GET)
  public ModelAndView nuevaLiga(HttpServletRequest request) {
    Usuario usuario = usuarioDeSesion(request);
    if (usuario == null) {
      return new ModelAndView(REDIRECT_LOGIN);
    }
    Map<String, Object> modelo = new HashMap<>(modeloBase(usuario));
    return new ModelAndView("ligaForm", modelo);
  }

  @RequestMapping(path = "/ligas/crear", method = RequestMethod.POST)
  public ModelAndView crearLiga(
    HttpServletRequest request,
    @RequestParam("nombre") String nombre,
    @RequestParam("descripcion") String descripcion,
    @RequestParam("formato") Integer formato,
    @RequestParam("cupoEquipos") Integer cupoEquipos,
    @RequestParam("fechaInicio") String fechaInicio,
    @RequestParam("ubicacion") String ubicacion,
    @RequestParam(name = "inscripcionAbierta", defaultValue = "false") Boolean inscripcionAbierta
  ) {
    Usuario usuario = usuarioDeSesion(request);
    if (usuario == null) {
      return new ModelAndView(REDIRECT_LOGIN);
    }
    com.tallerwebi.dominio.Liga nueva = new com.tallerwebi.dominio.Liga();
    nueva.setNombre(nombre);
    nueva.setDescripcion(descripcion);
    nueva.setFormato(formato);
    nueva.setCupoEquipos(cupoEquipos);
    nueva.setFechaInicio(java.time.LocalDate.parse(fechaInicio));
    nueva.setUbicacion(ubicacion);
    nueva.setInscripcionAbierta(inscripcionAbierta);
    servicioCompetencia.guardarLiga(nueva);
    return new ModelAndView("redirect:/torneos");
  }

  @RequestMapping(path = "/historial", method = RequestMethod.GET)
  public ModelAndView historial(HttpServletRequest request) {
    Usuario usuario = usuarioDeSesion(request);
    if (usuario == null) {
      return new ModelAndView(REDIRECT_LOGIN);
    }

    Map<String, Object> modelo = new java.util.HashMap<>(modeloBase(usuario));
    modelo.putAll(modeloHistorial(usuario));
    return new ModelAndView("historial", modelo);
  }

  private Map<String, Object> modeloHistorial(Usuario usuario) {
    Map<String, Object> modelo = new HashMap<>();
    List<Partido> partidos = servicioPartido.listarPartidosDe(usuario);
    modelo.put("historial", partidos);
    modelo.put("deltas", deltasPorPartido(usuario));
    Resumen resumen = resumen(partidos, usuario);
    modelo.put("jugados", resumen.jugados);
    modelo.put("ganados", resumen.ganados);
    modelo.put("empatados", resumen.empatados);
    modelo.put("perdidos", resumen.perdidos);
    modelo.put("efectividad", resumen.efectividad);
    return modelo;
  }

  private Map<Long, Integer> deltasPorPartido(Usuario usuario) {
    Map<Long, Integer> deltas = new HashMap<>();
    for (NotificacionPl n : servicioPartido.listarNotificacionesDe(usuario)) {
      if (n.getPartido() == null || n.getPartido().getId() == null) {
        continue;
      }
      deltas.putIfAbsent(n.getPartido().getId(), n.getDelta());
    }
    return deltas;
  }

  private Resumen resumen(List<Partido> partidos, Usuario usuario) {
    int jugados = 0;
    int ganados = 0;
    int empatados = 0;
    int perdidos = 0;
    for (Partido partido : partidos) {
      if (!partido.fueJugado()) {
        continue;
      }
      jugados++;
      if (esEmpate(partido)) {
        empatados++;
      } else if (ganoUsuario(partido, usuario)) {
        ganados++;
      } else {
        perdidos++;
      }
    }
    int efectividad = jugados == 0
      ? 0
      : (int) Math.round(((ganados + empatados * 0.5) / (double) jugados) * 100);
    return new Resumen(jugados, ganados, empatados, perdidos, efectividad);
  }

  private boolean esEmpate(Partido partido) {
    int ga = partido.getGolesEquipoA() == null ? 0 : partido.getGolesEquipoA();
    int gb = partido.getGolesEquipoB() == null ? 0 : partido.getGolesEquipoB();
    return ga == gb;
  }

  private boolean ganoUsuario(Partido partido, Usuario usuario) {
    boolean soyA = partido.getEquipoA().contains(usuario);
    boolean soyB = !soyA && partido.getEquipoB().contains(usuario);
    int ga = partido.getGolesEquipoA() == null ? 0 : partido.getGolesEquipoA();
    int gb = partido.getGolesEquipoB() == null ? 0 : partido.getGolesEquipoB();
    return (soyA && ga > gb) || (soyB && gb > ga);
  }

  private static final class Resumen {

    final int jugados;
    final int ganados;
    final int empatados;
    final int perdidos;
    final int efectividad;

    Resumen(int jugados, int ganados, int empatados, int perdidos, int efectividad) {
      this.jugados = jugados;
      this.ganados = ganados;
      this.empatados = empatados;
      this.perdidos = perdidos;
      this.efectividad = efectividad;
    }
  }
}
