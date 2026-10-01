package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.tallerwebi.dominio.Equipo;
import com.tallerwebi.dominio.Liga;
import com.tallerwebi.dominio.ServicioCompetencia;
import com.tallerwebi.dominio.ServicioLiga;
import com.tallerwebi.dominio.ServicioRelacionAmistad;
import com.tallerwebi.dominio.Torneo;
import com.tallerwebi.dominio.Usuario;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

public class ControladorTransferenciasTest {

  private ServicioCompetencia servicioCompetencia;
  private ServicioLiga servicioLiga;
  private ControladorTransferencias controlador;
  private HttpServletRequest request;
  private Equipo equipo;
  private Usuario goleador;

  @BeforeEach
  public void init() {
    servicioCompetencia = mock(ServicioCompetencia.class);
    servicioLiga = mock(ServicioLiga.class);
    controlador =
      new ControladorTransferencias(
        mock(ServicioRelacionAmistad.class),
        servicioCompetencia,
        servicioLiga
      );
    request = mock(HttpServletRequest.class);
    HttpSession session = mock(HttpSession.class);
    when(request.getSession(false)).thenReturn(session);
    when(session.getAttribute("usuario")).thenReturn(new Usuario());

    goleador = jugador(10L, 8.5);
    equipo = new Equipo();
    equipo.setId(3L);
    equipo.setNombre("Los Halcones");
    equipo.agregarJugador(jugador(11L, 6.0));
    equipo.agregarJugador(goleador);

    Torneo torneo = new Torneo();
    torneo.setNombre("Copa");
    when(servicioCompetencia.buscarTorneoPorId(1L)).thenReturn(torneo);
    when(servicioCompetencia.listarEquiposInscriptos(1L)).thenReturn(List.of(equipo));
  }

  @Test
  public void muestraEquiposDelTorneoSinSeleccion() {
    ModelAndView mav = controlador.transferencias(request, "torneo", 1L, null, null);

    assertThat(mav.getViewName(), equalTo("transferencias"));
    assertThat((List<?>) mav.getModel().get("equipos"), hasSize(1));
    assertThat(mav.getModel().get("equipoSeleccionado"), nullValue());
    assertThat(mav.getModel().get("volverUrl"), equalTo("/torneos/detalle?id=1"));
  }

  @Test
  public void alElegirEquipoYJugadorMuestraPlantelOrdenadoYSeleccion() {
    ModelAndView mav = controlador.transferencias(request, "torneo", 1L, 3L, 10L);

    List<?> jugadores = (List<?>) mav.getModel().get("jugadores");
    assertThat(jugadores, hasSize(2));
    assertThat(jugadores.get(0), equalTo(goleador));
    assertThat(mav.getModel().get("jugadorSeleccionado"), equalTo(goleador));
  }

  @Test
  public void funcionaParaLigasYRedirigeSiNoExiste() {
    Liga liga = new Liga();
    liga.setNombre("Premier");
    when(servicioLiga.buscarPorId(2L)).thenReturn(liga);
    when(servicioLiga.listarEquiposInscriptos(2L)).thenReturn(List.of(equipo));

    ModelAndView mav = controlador.transferencias(request, "liga", 2L, null, null);
    assertThat(mav.getModel().get("nombreCompetencia"), equalTo("Premier"));

    ModelAndView inexistente = controlador.transferencias(request, "liga", 99L, null, null);
    assertThat(inexistente.getViewName(), equalTo("redirect:/torneos"));
  }

  @Test
  public void sinSesionRedirigeAlLogin() {
    when(request.getSession(false)).thenReturn(null);

    ModelAndView mav = controlador.transferencias(request, "torneo", 1L, null, null);

    assertThat(mav.getViewName(), equalTo("redirect:/login"));
  }

  private static Usuario jugador(Long id, double puntaje) {
    Usuario usuario = new Usuario();
    usuario.setId(id);
    usuario.getEstadisticas().setPuntaje(puntaje);
    return usuario;
  }
}
