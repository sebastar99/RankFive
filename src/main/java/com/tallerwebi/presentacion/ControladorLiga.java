package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Liga;
import com.tallerwebi.dominio.ResultadoInscripcion;
import com.tallerwebi.dominio.ServicioCompetencia;
import com.tallerwebi.dominio.ServicioLiga;
import com.tallerwebi.dominio.ServicioRelacionAmistad;
import com.tallerwebi.dominio.Usuario;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorLiga extends ControladorBase {

  private static final String REDIRECT_DETALLE = "redirect:/ligas/detalle?id=";
  private static final String PARAM_AVISO = "&aviso=";

  private final ServicioLiga servicioLiga;
  private final ServicioCompetencia servicioCompetencia;

  @Autowired
  public ControladorLiga(
    ServicioRelacionAmistad servicioRelacionAmistad,
    ServicioLiga servicioLiga,
    ServicioCompetencia servicioCompetencia
  ) {
    super(servicioRelacionAmistad);
    this.servicioLiga = servicioLiga;
    this.servicioCompetencia = servicioCompetencia;
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
    modelo.put("fechas", servicioLiga.obtenerFechas(ligaId));
    modelo.put("tabla", servicioLiga.calcularTabla(ligaId));
    modelo.put("yaInscripto", servicioLiga.yaInscripto(usuario, ligaId));
    modelo.put("tieneEquipoActivo", servicioCompetencia.tieneEquipoActivo(usuario));
    Avisos.agregar(modelo, aviso);
    return new ModelAndView("ligaDetalle", modelo);
  }

  @RequestMapping(path = "/ligas/inscribirse", method = RequestMethod.POST)
  public ModelAndView inscribirse(HttpServletRequest request, @RequestParam("ligaId") Long ligaId) {
    Usuario usuario = usuarioDeSesion(request);
    if (usuario == null) {
      return new ModelAndView(REDIRECT_LOGIN);
    }
    ResultadoInscripcion resultado = servicioLiga.inscribir(usuario, ligaId);
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
    boolean ok = servicioLiga.generarFixtureSiNoExiste(ligaId);
    String codigo = ok ? Avisos.FIXTURE_GENERADO : Avisos.FIXTURE_INVALIDO;
    return new ModelAndView(REDIRECT_DETALLE + ligaId + PARAM_AVISO + codigo);
  }

  @RequestMapping(path = "/ligas/partido/resultado", method = RequestMethod.POST)
  public ModelAndView registrarResultado(
    HttpServletRequest request,
    @RequestParam("ligaId") Long ligaId,
    @RequestParam("partidoId") Long partidoId,
    @RequestParam("golesLocal") Integer golesLocal,
    @RequestParam("golesVisitante") Integer golesVisitante
  ) {
    Usuario usuario = usuarioDeSesion(request);
    if (usuario == null) {
      return new ModelAndView(REDIRECT_LOGIN);
    }
    boolean ok = servicioLiga.registrarResultado(partidoId, golesLocal, golesVisitante);
    String codigo = ok ? Avisos.RESULTADO_GUARDADO : Avisos.RESULTADO_INVALIDO;
    return new ModelAndView(REDIRECT_DETALLE + ligaId + PARAM_AVISO + codigo);
  }
}
