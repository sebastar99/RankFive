package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Equipo;
import com.tallerwebi.dominio.InvitacionEquipo;
import com.tallerwebi.dominio.Liga;
import com.tallerwebi.dominio.ServicioCompetencia;
import com.tallerwebi.dominio.ServicioEquipo;
import com.tallerwebi.dominio.ServicioLiga;
import com.tallerwebi.dominio.ServicioRelacionAmistad;
import com.tallerwebi.dominio.Torneo;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.enums.ResultadoTransferencia;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorTransferencias extends ControladorBase {

  private static final String TIPO_LIGA = "liga";
  private static final String TIPO_TORNEO = "torneo";

  private final ServicioCompetencia servicioCompetencia;
  private final ServicioLiga servicioLiga;
  private final ServicioEquipo servicioEquipo;

  @Autowired
  public ControladorTransferencias(
    ServicioRelacionAmistad servicioRelacionAmistad,
    ServicioCompetencia servicioCompetencia,
    ServicioLiga servicioLiga,
    ServicioEquipo servicioEquipo
  ) {
    super(servicioRelacionAmistad);
    this.servicioCompetencia = servicioCompetencia;
    this.servicioLiga = servicioLiga;
    this.servicioEquipo = servicioEquipo;
  }

  @RequestMapping(path = "/transferencias", method = RequestMethod.GET)
  public ModelAndView transferencias(
    HttpServletRequest request,
    @RequestParam(name = "tipo", defaultValue = TIPO_TORNEO) String tipo,
    @RequestParam("id") Long competenciaId,
    @RequestParam(name = "equipo", required = false) Long equipoId,
    @RequestParam(name = "jugador", required = false) Long jugadorId
  ) {
    Usuario usuario = usuarioDeSesion(request);
    if (usuario == null) {
      return new ModelAndView(REDIRECT_LOGIN);
    }
    boolean esLiga = TIPO_LIGA.equals(tipo);
    String nombre = nombreCompetencia(esLiga, competenciaId);
    if (nombre == null) {
      return new ModelAndView("redirect:/torneos");
    }
    List<Equipo> equipos = esLiga
      ? servicioLiga.listarEquiposInscriptos(competenciaId)
      : servicioCompetencia.listarEquiposInscriptos(competenciaId);
    Equipo equipo = buscarEquipo(equipos, equipoId);
    List<Usuario> jugadores = jugadoresOrdenados(equipo);

    Map<String, Object> modelo = new HashMap<>(modeloBase(usuario));
    modelo.put("tipo", esLiga ? TIPO_LIGA : TIPO_TORNEO);
    modelo.put("competenciaId", competenciaId);
    modelo.put("nombreCompetencia", nombre);
    modelo.put(
      "volverUrl",
      (esLiga ? "/ligas/detalle?id=" : "/torneos/detalle?id=") + competenciaId
    );
    modelo.put("equipos", equipos);
    modelo.put("equipoSeleccionado", equipo);
    Equipo propio = buscarEquipoDelCapitan(equipos, usuario);
    Usuario seleccionado = buscarJugador(jugadores, jugadorId);
    modelo.put("equipoPropio", propio);
    modelo.put("puedeOfertar", puedeOfertar(propio, seleccionado));
    modelo.put("jugadores", jugadores);
    modelo.put("jugadorSeleccionado", seleccionado);
    return new ModelAndView("transferencias", modelo);
  }

  @RequestMapping(path = "/transferencias/ofertar", method = RequestMethod.POST)
  public ModelAndView ofertar(
    HttpServletRequest request,
    @RequestParam(name = "tipo", defaultValue = TIPO_TORNEO) String tipo,
    @RequestParam("id") Long competenciaId,
    @RequestParam("equipo") Long origenId,
    @RequestParam("jugador") Long jugadorId
  ) {
    Usuario usuario = usuarioDeSesion(request);
    if (usuario == null || usuario.getId() == null) {
      return new ModelAndView(REDIRECT_LOGIN);
    }

    boolean esLiga = TIPO_LIGA.equals(tipo);
    if (nombreCompetencia(esLiga, competenciaId) == null) {
      return new ModelAndView("redirect:/torneos");
    }

    List<Equipo> equipos = esLiga
      ? servicioLiga.listarEquiposInscriptos(competenciaId)
      : servicioCompetencia.listarEquiposInscriptos(competenciaId);
    Equipo destino = buscarEquipoDelCapitan(equipos, usuario);
    Equipo origen = buscarEquipo(equipos, origenId);

    ResultadoTransferencia resultado;
    if (destino == null) {
      resultado = ResultadoTransferencia.SIN_EQUIPO;
    } else if (origen == null) {
      resultado = ResultadoTransferencia.EQUIPO_INVALIDO;
    } else {
      resultado =
        servicioEquipo.ofrecerTransferencia(usuario, destino.getId(), origen.getId(), jugadorId);
    }

    String tipoNormalizado = esLiga ? TIPO_LIGA : TIPO_TORNEO;
    String aviso = URLEncoder.encode(resultado.getMensaje(), StandardCharsets.UTF_8);
    String url =
      "redirect:/transferencias?tipo=" +
      tipoNormalizado +
      "&id=" +
      competenciaId +
      "&equipo=" +
      origenId +
      "&jugador=" +
      jugadorId +
      "&aviso=" +
      aviso;

    return new ModelAndView(url);
  }

  @RequestMapping(path = "/transferencias/ofertas/{id}", method = RequestMethod.GET)
  public ModelAndView verOferta(HttpServletRequest request, @PathVariable("id") Long invitacionId) {
    Usuario usuario = usuarioDeSesion(request);
    if (usuario == null) {
      return new ModelAndView(REDIRECT_LOGIN);
    }
    InvitacionEquipo oferta = servicioEquipo.buscarOfertaPendiente(invitacionId, usuario);
    if (oferta == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Oferta no disponible");
    }
    Map<String, Object> modelo = modeloBase(usuario);
    modelo.put("oferta", oferta);
    modelo.put("equipo", oferta.getEquipo());
    modelo.put("jugadores", jugadoresOrdenados(oferta.getEquipo()));
    return new ModelAndView("ofertaTransferencia", modelo);
  }

  @RequestMapping(path = "/transferencias/ofertas/postergar", method = RequestMethod.POST)
  public ModelAndView postergarOferta(
    HttpServletRequest request,
    @RequestParam("invitacionId") Long invitacionId
  ) {
    Usuario usuario = usuarioDeSesion(request);
    if (usuario == null) {
      return new ModelAndView(REDIRECT_LOGIN);
    }
    if (!servicioEquipo.postergarOferta(invitacionId, usuario)) {
      return new ModelAndView("redirect:/dashboard");
    }
    return new ModelAndView("redirect:/transferencias/ofertas/" + invitacionId);
  }

  @RequestMapping(path = "/transferencias/respuestas/leidas", method = RequestMethod.POST)
  public ModelAndView marcarRespuestaLeida(
    HttpServletRequest request,
    @RequestParam("invitacionId") Long invitacionId
  ) {
    Usuario usuario = usuarioDeSesion(request);
    if (usuario == null) {
      return new ModelAndView(REDIRECT_LOGIN);
    }
    servicioEquipo.marcarRespuestaLeida(invitacionId, usuario);
    return new ModelAndView("redirect:/dashboard");
  }

  private static Equipo buscarEquipoDelCapitan(List<Equipo> equipos, Usuario usuario) {
    if (usuario.getId() == null) {
      return null;
    }
    return equipos
      .stream()
      .filter(e -> e.getOwner() != null && usuario.getId().equals(e.getOwner().getId()))
      .findFirst()
      .orElse(null);
  }

  private static boolean puedeOfertar(Equipo propio, Usuario jugador) {
    return (
      propio != null &&
      jugador != null &&
      propio.getJugadores().stream().noneMatch(j -> j.getId().equals(jugador.getId()))
    );
  }

  private String nombreCompetencia(boolean esLiga, Long id) {
    if (esLiga) {
      Liga liga = servicioLiga.buscarPorId(id);
      return liga == null ? null : liga.getNombre();
    }
    Torneo torneo = servicioCompetencia.buscarTorneoPorId(id);
    return torneo == null ? null : torneo.getNombre();
  }

  private static Equipo buscarEquipo(List<Equipo> equipos, Long equipoId) {
    if (equipoId == null) {
      return null;
    }
    return equipos.stream().filter(e -> equipoId.equals(e.getId())).findFirst().orElse(null);
  }

  private static List<Usuario> jugadoresOrdenados(Equipo equipo) {
    List<Usuario> jugadores = new ArrayList<>();
    if (equipo == null) {
      return jugadores;
    }
    jugadores.addAll(equipo.getJugadores());
    Comparator<Double> mayorPrimero = Comparator.nullsLast(Comparator.<Double>reverseOrder());
    jugadores.sort(Comparator.comparing(u -> u.getEstadisticas().getPuntaje(), mayorPrimero));
    return jugadores;
  }

  private static Usuario buscarJugador(List<Usuario> jugadores, Long jugadorId) {
    if (jugadorId == null) {
      return null;
    }
    return jugadores.stream().filter(j -> jugadorId.equals(j.getId())).findFirst().orElse(null);
  }
}
