package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.Arbitro;
import com.tallerwebi.dominio.Liga;
import com.tallerwebi.dominio.PartidoLiga;
import com.tallerwebi.dominio.ResultadoInscripcion;
import com.tallerwebi.dominio.ServicioArbitro;
import com.tallerwebi.dominio.ServicioCompetencia;
import com.tallerwebi.dominio.ServicioEstadisticasLiga;
import com.tallerwebi.dominio.ServicioLiga;
import com.tallerwebi.dominio.ServicioRelacionAmistad;
import com.tallerwebi.dominio.Usuario;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

public class ControladorLigaTest {

  private ServicioLiga servicioLiga;
  private ControladorLiga controlador;
  private HttpServletRequest request;
  private Usuario usuario;

  @BeforeEach
  public void init() {
    servicioLiga = mock(ServicioLiga.class);
    controlador =
      new ControladorLiga(
        mock(ServicioRelacionAmistad.class),
        servicioLiga,
        mock(ServicioCompetencia.class)
      );
    request = mock(HttpServletRequest.class);
    HttpSession session = mock(HttpSession.class);
    usuario = new Usuario();
    when(request.getSession(false)).thenReturn(session);
    when(session.getAttribute("usuario")).thenReturn(usuario);
  }

  @Test
  public void detalleMuestraLigaConAviso() {
    Liga liga = new Liga();
    liga.setId(4L);
    when(servicioLiga.buscarPorId(4L)).thenReturn(liga);

    ModelAndView mav = controlador.detalle(request, 4L, Avisos.FIXTURE_GENERADO);

    assertThat(mav.getViewName(), equalTo("ligaDetalle"));
    assertThat(mav.getModel().get("liga"), equalTo(liga));
    assertThat(mav.getModel().get("avisoOk"), equalTo(true));
  }

  @Test
  public void detalleIncluyeRankingDeArbitros() {
    Liga liga = new Liga();
    liga.setId(4L);
    when(servicioLiga.buscarPorId(4L)).thenReturn(liga);
    ServicioArbitro servicioArbitro = mock(ServicioArbitro.class);
    List<Arbitro> arbitros = List.of(new Arbitro());
    when(servicioArbitro.listarRanking()).thenReturn(arbitros);
    controlador.setServicioArbitro(servicioArbitro);

    ModelAndView mav = controlador.detalle(request, 4L, null);

    assertThat(mav.getModel().get("arbitros"), equalTo(arbitros));
  }

  @Test
  public void detalleSinServicioArbitroDevuelveListaVacia() {
    Liga liga = new Liga();
    liga.setId(4L);
    when(servicioLiga.buscarPorId(4L)).thenReturn(liga);

    ModelAndView mav = controlador.detalle(request, 4L, null);

    assertThat(mav.getModel().get("arbitros"), equalTo(Collections.emptyList()));
  }

  @Test
  public void calificarArbitroDeLigaInformaResultado() {
    ServicioArbitro servicioArbitro = mock(ServicioArbitro.class);
    controlador.setServicioArbitro(servicioArbitro);
    when(servicioArbitro.calificarPartidoLiga(usuario, 7L, 5)).thenReturn(true);

    assertThat(
      controlador.calificarArbitro(request, 4L, 7L, 5).getViewName(),
      equalTo("redirect:/ligas/detalle?id=4&aviso=" + Avisos.CALIFICADO)
    );
    assertThat(
      controlador.calificarArbitro(request, 4L, 7L, 2).getViewName(),
      equalTo("redirect:/ligas/detalle?id=4&aviso=" + Avisos.CALIFICACION_INVALIDA)
    );
  }

  @Test
  public void calificarArbitroSinServicioEsInvalido() {
    assertThat(
      controlador.calificarArbitro(request, 4L, 7L, 5).getViewName(),
      equalTo("redirect:/ligas/detalle?id=4&aviso=" + Avisos.CALIFICACION_INVALIDA)
    );
  }

  @Test
  public void detalleIncluyePartidosCalificables() {
    Liga liga = new Liga();
    liga.setId(4L);
    when(servicioLiga.buscarPorId(4L)).thenReturn(liga);
    ServicioArbitro servicioArbitro = mock(ServicioArbitro.class);
    when(servicioArbitro.partidosLigaCalificables(usuario, 4L)).thenReturn(java.util.Set.of(9L));
    controlador.setServicioArbitro(servicioArbitro);

    ModelAndView mav = controlador.detalle(request, 4L, null);

    assertThat(mav.getModel().get("calificables"), equalTo(java.util.Set.of(9L)));
  }

  @Test
  public void registrarGolComoAdminInformaElResultado() {
    usuario.setRol("ADMIN");
    ServicioEstadisticasLiga estadisticas = mock(ServicioEstadisticasLiga.class);
    controlador.setServicioEstadisticas(estadisticas);
    when(estadisticas.registrarGol(7L, 1L, "Messi", null)).thenReturn(true);

    assertThat(
      controlador.registrarGol(request, 4L, 7L, 1L, "Messi", null).getViewName(),
      equalTo("redirect:/ligas/detalle?id=4&aviso=" + Avisos.GOL_REGISTRADO)
    );
  }

  @Test
  public void registrarGolNoAdminEsInvalidoYNoSeEjecuta() {
    usuario.setRol("USER");
    ServicioEstadisticasLiga estadisticas = mock(ServicioEstadisticasLiga.class);
    controlador.setServicioEstadisticas(estadisticas);

    assertThat(
      controlador.registrarGol(request, 4L, 7L, 1L, "Messi", null).getViewName(),
      equalTo("redirect:/ligas/detalle?id=4&aviso=" + Avisos.GOL_INVALIDO)
    );
    verify(estadisticas, never()).registrarGol(any(), any(), any(), any());
  }

  @Test
  public void detalleIncluyeEstadisticasDeJugadores() {
    Liga liga = new Liga();
    liga.setId(4L);
    when(servicioLiga.buscarPorId(4L)).thenReturn(liga);
    ServicioEstadisticasLiga estadisticas = mock(ServicioEstadisticasLiga.class);
    controlador.setServicioEstadisticas(estadisticas);

    ModelAndView mav = controlador.detalle(request, 4L, null);

    verify(estadisticas).goleadores(4L);
    verify(estadisticas).asistencias(4L);
    assertThat(mav.getModel().containsKey("golesPorPartido"), equalTo(true));
    assertThat(mav.getModel().containsKey("jugadoresPorEquipo"), equalTo(true));
  }

  @Test
  public void fechaActualEsLaPrimeraConPartidosPendientes() {
    Map<Integer, List<PartidoLiga>> fechas = new TreeMap<>();
    fechas.put(1, List.of(partido(PartidoLiga.JUGADO)));
    fechas.put(2, List.of(partido(PartidoLiga.JUGADO), partido(PartidoLiga.PENDIENTE)));
    fechas.put(3, List.of(partido(PartidoLiga.PENDIENTE)));

    assertThat(ControladorLiga.fechaActual(fechas), equalTo(2));
  }

  @Test
  public void fechaActualEsLaUltimaSiTodoFueJugadoYUnoSiNoHayFixture() {
    Map<Integer, List<PartidoLiga>> fechas = new TreeMap<>();
    fechas.put(1, List.of(partido(PartidoLiga.JUGADO)));
    fechas.put(2, List.of(partido(PartidoLiga.JUGADO)));

    assertThat(ControladorLiga.fechaActual(fechas), equalTo(2));
    assertThat(ControladorLiga.fechaActual(new TreeMap<>()), equalTo(1));
  }

  private static PartidoLiga partido(String estado) {
    PartidoLiga partido = new PartidoLiga();
    partido.setEstado(estado);
    return partido;
  }

  @Test
  public void detalleDeLigaInexistenteRedirige() {
    assertThat(controlador.detalle(request, 9L, null).getViewName(), equalTo("redirect:/torneos"));
  }

  @Test
  public void inscripcionRedirigeSegunResultado() {
    when(servicioLiga.inscribir(usuario, 4L)).thenReturn(ResultadoInscripcion.INSCRIPTO);
    assertThat(
      controlador.inscribirse(request, 4L).getViewName(),
      equalTo("redirect:/ligas/detalle?id=4&aviso=INSCRIPTO")
    );

    when(servicioLiga.inscribir(usuario, 4L)).thenReturn(ResultadoInscripcion.SIN_EQUIPO_ACTIVO);
    assertThat(
      controlador.inscribirse(request, 4L).getViewName(),
      equalTo("redirect:/torneos?aviso=SIN_EQUIPO_ACTIVO")
    );
  }

  @Test
  public void generarFixtureYResultadoInformanElResultado() {
    usuario.setRol("ADMIN");
    when(servicioLiga.generarFixtureSiNoExiste(4L)).thenReturn(true);
    assertThat(
      controlador.generarFixture(request, 4L).getViewName(),
      equalTo("redirect:/ligas/detalle?id=4&aviso=FIXTURE_GENERADO")
    );

    when(servicioLiga.registrarResultado(7L, 1, 0)).thenReturn(false);
    usuario.setRol("ADMIN");
    assertThat(
      controlador.registrarResultado(request, 4L, 7L, 1, 0).getViewName(),
      equalTo("redirect:/ligas/detalle?id=4&aviso=RESULTADO_INVALIDO")
    );
  }

  @Test
  public void generarFixtureYResultadoNoAdminDebenSerInvalidosYSinEjecutar() {
    usuario.setRol("USER");
    assertThat(
      controlador.generarFixture(request, 4L).getViewName(),
      equalTo("redirect:/ligas/detalle?id=4&aviso=" + Avisos.FIXTURE_INVALIDO)
    );
    verify(servicioLiga, never()).generarFixtureSiNoExiste(anyLong());

    assertThat(
      controlador.registrarResultado(request, 4L, 7L, 1, 0).getViewName(),
      equalTo("redirect:/ligas/detalle?id=4&aviso=" + Avisos.RESULTADO_INVALIDO)
    );
    verify(servicioLiga, never()).registrarResultado(anyLong(), anyInt(), anyInt());
  }
}
