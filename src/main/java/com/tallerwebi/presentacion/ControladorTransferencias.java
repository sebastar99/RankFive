package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Equipo;
import com.tallerwebi.dominio.Liga;
import com.tallerwebi.dominio.ServicioCompetencia;
import com.tallerwebi.dominio.ServicioLiga;
import com.tallerwebi.dominio.ServicioRelacionAmistad;
import com.tallerwebi.dominio.Torneo;
import com.tallerwebi.dominio.Usuario;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorTransferencias extends ControladorBase {

  private static final String TIPO_LIGA = "liga";
  private static final String TIPO_TORNEO = "torneo";

  private final ServicioCompetencia servicioCompetencia;
  private final ServicioLiga servicioLiga;

  @Autowired
  public ControladorTransferencias(
    ServicioRelacionAmistad servicioRelacionAmistad,
    ServicioCompetencia servicioCompetencia,
    ServicioLiga servicioLiga
  ) {
    super(servicioRelacionAmistad);
    this.servicioCompetencia = servicioCompetencia;
    this.servicioLiga = servicioLiga;
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
    modelo.put("jugadores", jugadores);
    modelo.put("jugadorSeleccionado", buscarJugador(jugadores, jugadorId));
    return new ModelAndView("transferencias", modelo);
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
