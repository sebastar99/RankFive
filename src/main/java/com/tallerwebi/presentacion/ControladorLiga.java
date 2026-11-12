package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Liga;
import com.tallerwebi.dominio.PartidoLiga;
import com.tallerwebi.dominio.ResultadoInscripcion;
import com.tallerwebi.dominio.SeleccionEquipo;
import com.tallerwebi.dominio.ServicioArbitro;
import com.tallerwebi.dominio.ServicioCompetencia;
import com.tallerwebi.dominio.ServicioEquipo;
import com.tallerwebi.dominio.ServicioEstadisticasLiga;
import com.tallerwebi.dominio.ServicioInscripciones;
import com.tallerwebi.dominio.ServicioLiga;
import com.tallerwebi.dominio.ServicioRelacionAmistad;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.presentacion.DTO.DatosDetallesEncuentro;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorLiga extends ControladorBase {

  private static final String REDIRECT_DETALLE = "redirect:/ligas/detalle?id=";
  private static final String PARAM_AVISO = "&aviso=";
  private static final String PARAM_LIGA = "ligaId";

  private final ServicioLiga servicioLiga;
  private final ServicioCompetencia servicioCompetencia;
  private final ServicioEquipo servicioEquipo;
  private ServicioArbitro servicioArbitro;
  private ServicioEstadisticasLiga servicioEstadisticas;
  private ServicioInscripciones servicioInscripciones;

  @Autowired
  public ControladorLiga(
    ServicioRelacionAmistad servicioRelacionAmistad,
    ServicioLiga servicioLiga,
    ServicioCompetencia servicioCompetencia,
    ServicioEquipo servicioEquipo
  ) {
    super(servicioRelacionAmistad);
    this.servicioLiga = servicioLiga;
    this.servicioCompetencia = servicioCompetencia;
    this.servicioEquipo = servicioEquipo;
  }

  @Autowired(required = false)
  public void setServicioArbitro(ServicioArbitro servicioArbitro) {
    this.servicioArbitro = servicioArbitro;
  }

  @Autowired(required = false)
  public void setServicioEstadisticas(ServicioEstadisticasLiga servicioEstadisticas) {
    this.servicioEstadisticas = servicioEstadisticas;
  }

  @Autowired(required = false)
  public void setServicioInscripciones(ServicioInscripciones servicioInscripciones) {
    this.servicioInscripciones = servicioInscripciones;
  }

  @RequestMapping(path = "/ligas/detalle", method = RequestMethod.GET)
  public ModelAndView detalle(
    HttpServletRequest request,
    @RequestParam("id") Long ligaId,
    @RequestParam(name = "aviso", required = false) String aviso
  ) {
    Usuario usuario = usuarioDeSesion(request);
    if (usuario == null) {
      return new ModelAndView(REDIRECT_LOGIN);
    }
    Liga liga = servicioLiga.buscarPorId(ligaId);
    if (liga == null) {
      return new ModelAndView("redirect:/torneos");
    }
    Map<String, Object> modelo = new HashMap<>(modeloBase(usuario));
    modelo.put("liga", liga);
    modelo.put("inscripciones", servicioLiga.listarInscripciones(ligaId));
    Map<Integer, List<PartidoLiga>> fechas = servicioLiga.obtenerFechas(ligaId);
    modelo.put("fechas", fechas);
    modelo.put("fechaActual", fechaActual(fechas));
    modelo.put("tabla", servicioLiga.calcularTabla(ligaId));
    modelo.put("yaInscripto", servicioLiga.yaInscripto(usuario, ligaId));
    modelo.put("tieneEquipoActivo", servicioCompetencia.tieneEquipoActivo(usuario));
    modelo.put(
      "equiposCompatibles",
      servicioLiga.listarEquiposCompatibles(usuario, liga.getFormato())
    );
    modelo.put(
      "soloIntegrante",
      SeleccionEquipo.soloEsIntegrante(
        servicioEquipo.listarEquiposCapitaneados(usuario),
        servicioEquipo.listarEquiposActivosDe(usuario),
        liga.getFormato()
      )
    );
    if (servicioArbitro != null) {
      ModeloCompetencia.arbitraje(
        modelo,
        servicioArbitro.listarRanking(),
        servicioArbitro.partidosLigaCalificables(usuario, ligaId)
      );
    } else {
      ModeloCompetencia.arbitraje(modelo, Collections.emptyList(), Collections.emptySet());
    }
    agregarEstadisticas(modelo, ligaId);
    Avisos.agregar(modelo, aviso);
    return new ModelAndView("ligaDetalle", modelo);
  }

  @RequestMapping(path = "/ligas/inscribirse", method = RequestMethod.POST)
  public ModelAndView inscribirse(
    HttpServletRequest request,
    @RequestParam(PARAM_LIGA) Long ligaId,
    @RequestParam(name = "equipoId", required = false) Long equipoId
  ) {
    Usuario usuario = usuarioDeSesion(request);
    if (usuario == null) {
      return new ModelAndView(REDIRECT_LOGIN);
    }
    ResultadoInscripcion resultado = servicioLiga.inscribir(usuario, ligaId, equipoId);
    if (resultado.esExitoso()) {
      return new ModelAndView(REDIRECT_DETALLE + ligaId + PARAM_AVISO + resultado.name());
    }
    return new ModelAndView("redirect:/torneos?aviso=" + resultado.name());
  }

  @RequestMapping(path = "/ligas/generar-fixture", method = RequestMethod.POST)
  public ModelAndView generarFixture(HttpServletRequest request, @RequestParam("id") Long ligaId) {
    Usuario usuario = usuarioDeSesion(request);
    if (usuario == null) {
      return new ModelAndView(REDIRECT_LOGIN);
    }
    if (!ModeloCompetencia.esAdmin(usuario)) {
      String codigo = Avisos.FIXTURE_INVALIDO;
      return new ModelAndView(REDIRECT_DETALLE + ligaId + PARAM_AVISO + codigo);
    }
    boolean ok = servicioLiga.generarFixtureSiNoExiste(ligaId);
    String codigo = ok ? Avisos.FIXTURE_GENERADO : Avisos.FIXTURE_INVALIDO;
    return new ModelAndView(REDIRECT_DETALLE + ligaId + PARAM_AVISO + codigo);
  }

  @RequestMapping(path = "/ligas/abrir-inscripcion", method = RequestMethod.POST)
  public ModelAndView abrirInscripcion(
    HttpServletRequest request,
    @RequestParam(PARAM_LIGA) Long ligaId
  ) {
    Usuario usuario = usuarioDeSesion(request);
    if (usuario == null) {
      return new ModelAndView(REDIRECT_LOGIN);
    }
    if (!ModeloCompetencia.esAdmin(usuario)) {
      return new ModelAndView("redirect:/torneos");
    }
    servicioInscripciones.abrirInscripcionLiga(ligaId);
    return new ModelAndView(REDIRECT_DETALLE + ligaId + PARAM_AVISO + Avisos.INSCRIPCION_ABIERTA);
  }

  @RequestMapping(path = "/ligas/partido/resultado", method = RequestMethod.POST)
  public ModelAndView registrarResultado(
    HttpServletRequest request,
    @RequestParam(PARAM_LIGA) Long ligaId,
    @RequestParam("partidoId") Long partidoId,
    @RequestParam("golesLocal") Integer golesLocal,
    @RequestParam("golesVisitante") Integer golesVisitante,
    @ModelAttribute DatosDetallesEncuentro detalles
  ) {
    Usuario usuario = usuarioDeSesion(request);
    if (usuario == null) {
      return new ModelAndView(REDIRECT_LOGIN);
    }
    if (!ModeloCompetencia.esAdmin(usuario)) {
      return new ModelAndView(REDIRECT_DETALLE + ligaId + PARAM_AVISO + Avisos.RESULTADO_INVALIDO);
    }
    if (!servicioLiga.registrarResultado(partidoId, golesLocal, golesVisitante)) {
      return new ModelAndView(REDIRECT_DETALLE + ligaId + PARAM_AVISO + Avisos.RESULTADO_INVALIDO);
    }
    boolean sinDetalles = detalles == null || detalles.aDominio().estaVacio();
    String codigo = sinDetalles || guardarDetalles(partidoId, detalles)
      ? Avisos.RESULTADO_GUARDADO
      : Avisos.DETALLES_INVALIDOS;
    return new ModelAndView(REDIRECT_DETALLE + ligaId + PARAM_AVISO + codigo);
  }

  @RequestMapping(path = "/ligas/partido/detalles", method = RequestMethod.POST)
  public ModelAndView registrarDetalles(
    HttpServletRequest request,
    @RequestParam(PARAM_LIGA) Long ligaId,
    @RequestParam("partidoId") Long partidoId,
    @ModelAttribute DatosDetallesEncuentro detalles
  ) {
    Usuario usuario = usuarioDeSesion(request);
    if (usuario == null) {
      return new ModelAndView(REDIRECT_LOGIN);
    }
    boolean ok = ModeloCompetencia.esAdmin(usuario) && guardarDetalles(partidoId, detalles);
    String codigo = ok ? Avisos.DETALLES_GUARDADOS : Avisos.DETALLES_INVALIDOS;
    return new ModelAndView(REDIRECT_DETALLE + ligaId + PARAM_AVISO + codigo);
  }

  private boolean guardarDetalles(Long partidoId, DatosDetallesEncuentro detalles) {
    return (
      servicioEstadisticas != null &&
      detalles != null &&
      servicioEstadisticas.registrarDetalles(partidoId, detalles.aDominio())
    );
  }

  @RequestMapping(path = "/ligas/partido/calificar-arbitro", method = RequestMethod.POST)
  public ModelAndView calificarArbitro(
    HttpServletRequest request,
    @RequestParam(PARAM_LIGA) Long ligaId,
    @RequestParam("partidoId") Long partidoId,
    @RequestParam("puntaje") Integer puntaje
  ) {
    Usuario usuario = usuarioDeSesion(request);
    if (usuario == null) {
      return new ModelAndView(REDIRECT_LOGIN);
    }
    boolean ok =
      servicioArbitro != null && servicioArbitro.calificarPartidoLiga(usuario, partidoId, puntaje);
    String codigo = ok ? Avisos.CALIFICADO : Avisos.CALIFICACION_INVALIDA;
    return new ModelAndView(REDIRECT_DETALLE + ligaId + PARAM_AVISO + codigo);
  }

  private void agregarEstadisticas(Map<String, Object> modelo, Long ligaId) {
    if (servicioEstadisticas == null) {
      ModeloCompetencia.sinEstadisticas(modelo);
      return;
    }
    ModeloCompetencia.estadisticas(
      modelo,
      servicioEstadisticas.goleadores(ligaId),
      servicioEstadisticas.asistencias(ligaId),
      servicioEstadisticas.golesPorPartido(ligaId),
      servicioEstadisticas.tarjetasPorPartido(ligaId),
      servicioEstadisticas.plantelesPorInscripcion(ligaId)
    );
  }

  static int fechaActual(Map<Integer, List<PartidoLiga>> fechas) {
    if (fechas == null || fechas.isEmpty()) {
      return 1;
    }
    int ultima = 1;
    for (Map.Entry<Integer, List<PartidoLiga>> fecha : fechas.entrySet()) {
      ultima = fecha.getKey();
      if (fecha.getValue().stream().anyMatch(p -> !p.estaJugado())) {
        return ultima;
      }
    }
    return ultima;
  }
}
