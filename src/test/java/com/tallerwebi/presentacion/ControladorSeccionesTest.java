package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.EncuentroTorneo;
import com.tallerwebi.dominio.FaseTorneo;
import com.tallerwebi.dominio.Liga;
import com.tallerwebi.dominio.ResultadoInscripcion;
import com.tallerwebi.dominio.ServicioArbitro;
import com.tallerwebi.dominio.ServicioCompetencia;
import com.tallerwebi.dominio.ServicioEstadisticasTorneo;
import com.tallerwebi.dominio.ServicioLiga;
import com.tallerwebi.dominio.ServicioPartido;
import com.tallerwebi.dominio.ServicioRelacionAmistad;
import com.tallerwebi.dominio.Torneo;
import com.tallerwebi.dominio.Usuario;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

public class ControladorSeccionesTest {

  private ServicioPartido servicioPartido;
  private ServicioCompetencia servicioCompetencia;
  private ServicioArbitro servicioArbitro;
  private ServicioLiga servicioLiga;
  private ControladorSecciones controlador;
  private HttpServletRequest request;
  private HttpSession session;
  private Usuario usuario;

  @BeforeEach
  public void init() {
    servicioPartido = mock(ServicioPartido.class);
    servicioCompetencia = mock(ServicioCompetencia.class);
    servicioArbitro = mock(ServicioArbitro.class);
    servicioLiga = mock(ServicioLiga.class);
    controlador =
      new ControladorSecciones(
        mock(ServicioRelacionAmistad.class),
        servicioPartido,
        servicioCompetencia,
        servicioArbitro,
        servicioLiga
      );
    request = mock(HttpServletRequest.class);
    session = mock(HttpSession.class);
    usuario = new Usuario();
    usuario.setId(9L);
  }

  @Test
  public void generarFixtureNoAdminDebeSerInvalidoYSinEjecutarServicio() {
    when(request.getSession(false)).thenReturn(session);
    when(session.getAttribute("usuario")).thenReturn(usuario);
    usuario.setRol("USER");

    ModelAndView gf = controlador.generarFixture(request, 7L);
    assertThat(
      gf.getViewName(),
      equalTo("redirect:/torneos/detalle?id=7&aviso=" + Avisos.FIXTURE_INVALIDO)
    );
    verify(servicioCompetencia, never()).generarFixtureSiNoExiste(anyLong());
  }

  @Test
  public void registrarResultadoNoAdminDebeSerInvalidoYSinEjecutarServicio() {
    when(request.getSession(false)).thenReturn(session);
    when(session.getAttribute("usuario")).thenReturn(usuario);
    usuario.setRol("USER");

    ModelAndView rr = controlador.registrarResultadoEncuentro(request, 7L, 1L, 2, 0);
    assertThat(
      rr.getViewName(),
      equalTo("redirect:/torneos/detalle?id=7&aviso=" + Avisos.RESULTADO_INVALIDO)
    );
    verify(servicioCompetencia, never()).registrarResultadoEncuentro(anyLong(), anyInt(), anyInt());
  }

  @Test
  public void nuevoYCrearTorneoSoloAdmin() {
    when(request.getSession(false)).thenReturn(session);
    when(session.getAttribute("usuario")).thenReturn(usuario);

    // no admin: redirige
    usuario.setRol("USER");
    assertThat(controlador.nuevoTorneo(request).getViewName(), equalTo("redirect:/torneos"));
    ModelAndView noAdminCrear = controlador.crearTorneo(
      request,
      "T1",
      "desc",
      1,
      8,
      java.time.LocalDate.now().toString(),
      "BA",
      true
    );
    assertThat(noAdminCrear.getViewName(), equalTo("redirect:/torneos"));
    verify(servicioCompetencia, never()).guardarTorneo(any());

    // admin: permite y guarda
    usuario.setRol("ADMIN");
    assertThat(controlador.nuevoTorneo(request).getViewName(), equalTo("torneoForm"));
    ModelAndView adminCrear = controlador.crearTorneo(
      request,
      "T1",
      "desc",
      1,
      8,
      java.time.LocalDate.now().toString(),
      "BA",
      true
    );
    assertThat(adminCrear.getViewName(), equalTo("redirect:/torneos"));
    verify(servicioCompetencia, atLeastOnce()).guardarTorneo(any());
  }

  @Test
  public void nuevaYCrearLigaSoloAdmin() {
    when(request.getSession(false)).thenReturn(session);
    when(session.getAttribute("usuario")).thenReturn(usuario);

    // no admin: redirige
    usuario.setRol("USER");
    assertThat(controlador.nuevaLiga(request).getViewName(), equalTo("redirect:/torneos"));
    ModelAndView noAdminCrear = controlador.crearLiga(
      request,
      "L1",
      "desc",
      1,
      18,
      java.time.LocalDate.now().toString(),
      "BA",
      true
    );
    assertThat(noAdminCrear.getViewName(), equalTo("redirect:/torneos"));
    verify(servicioCompetencia, never()).guardarLiga(any());

    // admin: permite y guarda
    usuario.setRol("ADMIN");
    assertThat(controlador.nuevaLiga(request).getViewName(), equalTo("ligaForm"));
    ModelAndView adminCrear = controlador.crearLiga(
      request,
      "L1",
      "desc",
      1,
      18,
      java.time.LocalDate.now().toString(),
      "BA",
      true
    );
    assertThat(adminCrear.getViewName(), equalTo("redirect:/torneos"));
    verify(servicioCompetencia, atLeastOnce()).guardarLiga(any());
  }

  @Test
  public void torneosSinSesionRedirigeAlLogin() {
    when(request.getSession(false)).thenReturn(null);

    ModelAndView mav = controlador.torneos(request, null);

    assertThat(mav.getViewName(), equalTo("redirect:/login"));
  }

  @Test
  public void torneosConSesionMuestraListasYElegibles() {
    when(request.getSession(false)).thenReturn(session);
    when(session.getAttribute("usuario")).thenReturn(usuario);

    Torneo t = new Torneo();
    t.setId(1L);
    Liga l = new Liga();
    l.setId(2L);
    when(servicioCompetencia.listarTorneos()).thenReturn(List.of(t));
    when(servicioCompetencia.listarLigas()).thenReturn(Collections.singletonList(l));
    when(servicioCompetencia.yaInscripto(usuario, 1L)).thenReturn(true);
    when(servicioLiga.yaInscripto(usuario, 2L)).thenReturn(false);
    when(servicioCompetencia.tieneEquipoActivo(usuario)).thenReturn(true);

    ModelAndView mav = controlador.torneos(request, ResultadoInscripcion.INSCRIPTO.name());

    assertThat(mav.getViewName(), equalTo("ligaTorneos"));
    assertThat(((List<?>) mav.getModel().get("torneos")).size(), equalTo(1));
    assertThat(((List<?>) mav.getModel().get("ligas")).size(), equalTo(1));
    assertThat(((java.util.Set<?>) mav.getModel().get("torneosInscriptos")).isEmpty(), is(false));
    assertThat(mav.getModel().get("tieneEquipoActivo"), equalTo(true));
    assertThat(mav.getModel().get("avisoOk"), equalTo(true));
  }

  @Test
  public void inscribirseTorneoRedirigeConAviso() {
    when(request.getSession(false)).thenReturn(session);
    when(session.getAttribute("usuario")).thenReturn(usuario);
    when(servicioCompetencia.inscribirATorneo(usuario, 3L))
      .thenReturn(ResultadoInscripcion.INSCRIPTO);

    ModelAndView ok = controlador.inscribirseTorneo(request, 3L);
    assertThat(ok.getViewName(), equalTo("redirect:/torneos/detalle?id=3&aviso=INSCRIPTO"));

    when(servicioCompetencia.inscribirATorneo(usuario, 3L))
      .thenReturn(ResultadoInscripcion.SIN_EQUIPO_ACTIVO);
    ModelAndView fail = controlador.inscribirseTorneo(request, 3L);
    assertThat(fail.getViewName(), equalTo("redirect:/torneos?aviso=SIN_EQUIPO_ACTIVO"));
  }

  @Test
  public void detalleTorneoArmaModeloCompleto() {
    when(request.getSession(false)).thenReturn(session);
    when(session.getAttribute("usuario")).thenReturn(usuario);

    Torneo torneo = new Torneo();
    torneo.setId(7L);
    FaseTorneo fase = new FaseTorneo();
    fase.setNumero(1);
    EncuentroTorneo e = new EncuentroTorneo();
    e.setEstado("PENDIENTE");
    fase.getEncuentros().add(e);
    when(servicioCompetencia.buscarTorneoPorId(7L)).thenReturn(torneo);
    when(servicioCompetencia.obtenerFases(7L)).thenReturn(List.of(fase));
    when(servicioCompetencia.calcularTablasPorGrupo(7L)).thenReturn(Collections.emptyMap());
    when(servicioCompetencia.obtenerCampeon(7L)).thenReturn(null);
    when(servicioArbitro.listarRanking()).thenReturn(Collections.emptyList());
    when(servicioCompetencia.yaInscripto(usuario, 7L)).thenReturn(false);
    when(servicioCompetencia.tieneEquipoActivo(usuario)).thenReturn(false);

    ModelAndView mav = controlador.detalleTorneo(request, 7L, Avisos.FIXTURE_GENERADO);

    assertThat(mav.getViewName(), equalTo("torneoDetalle"));
    assertThat(mav.getModel().get("torneo"), equalTo(torneo));
    assertThat(mav.getModel().get("fases"), instanceOf(List.class));
    assertThat(mav.getModel().get("proximoEncuentro"), equalTo(e));
    assertThat(mav.getModel().get("avisoOk"), equalTo(true));
  }

  @Test
  public void generarFixtureYRegistrarResultadoDevuelvenAvisos() {
    when(request.getSession(false)).thenReturn(session);
    when(session.getAttribute("usuario")).thenReturn(usuario);
    usuario.setRol("ADMIN");

    when(servicioCompetencia.generarFixtureSiNoExiste(7L)).thenReturn(true);
    ModelAndView gf = controlador.generarFixture(request, 7L);
    assertThat(gf.getViewName(), equalTo("redirect:/torneos/detalle?id=7&aviso=FIXTURE_GENERADO"));

    when(servicioCompetencia.registrarResultadoEncuentro(1L, 2, 0)).thenReturn(true);
    ModelAndView rr = controlador.registrarResultadoEncuentro(request, 7L, 1L, 2, 0);
    assertThat(
      rr.getViewName(),
      equalTo("redirect:/torneos/detalle?id=7&aviso=RESULTADO_GUARDADO")
    );

    when(servicioCompetencia.registrarResultadoEncuentro(1L, 1, 1)).thenReturn(false);
    ModelAndView ri = controlador.registrarResultadoEncuentro(request, 7L, 1L, 1, 1);
    assertThat(
      ri.getViewName(),
      equalTo("redirect:/torneos/detalle?id=7&aviso=RESULTADO_INVALIDO")
    );
  }

  @Test
  public void detalleTorneoIncluyeEstadisticasYCalificables() {
    when(request.getSession(false)).thenReturn(session);
    when(session.getAttribute("usuario")).thenReturn(usuario);
    Torneo torneo = new Torneo();
    torneo.setId(7L);
    when(servicioCompetencia.buscarTorneoPorId(7L)).thenReturn(torneo);
    when(servicioArbitro.encuentrosCalificables(usuario, 7L)).thenReturn(Set.of(3L));
    ServicioEstadisticasTorneo estadisticas = mock(ServicioEstadisticasTorneo.class);
    controlador.setServicioEstadisticasTorneo(estadisticas);

    ModelAndView mav = controlador.detalleTorneo(request, 7L, null);

    verify(estadisticas).goleadores(7L);
    verify(estadisticas).golesPorEquipo(7L);
    assertThat(mav.getModel().get("calificables"), equalTo(Set.of(3L)));
    assertThat(mav.getModel().containsKey("jugadoresPorEquipo"), equalTo(true));
    assertThat(mav.getModel().get("faseActual"), equalTo(1));
  }

  @Test
  public void detalleTorneoSinServicioDeEstadisticasUsaValoresVacios() {
    when(request.getSession(false)).thenReturn(session);
    when(session.getAttribute("usuario")).thenReturn(usuario);
    Torneo torneo = new Torneo();
    torneo.setId(7L);
    when(servicioCompetencia.buscarTorneoPorId(7L)).thenReturn(torneo);

    ModelAndView mav = controlador.detalleTorneo(request, 7L, null);

    assertThat(mav.getModel().get("goleadores"), equalTo(Collections.emptyList()));
    assertThat(mav.getModel().get("golesPorEquipo"), equalTo(List.of()));
  }

  @Test
  public void registrarGolEncuentroSoloAdmin() {
    when(request.getSession(false)).thenReturn(session);
    when(session.getAttribute("usuario")).thenReturn(usuario);
    ServicioEstadisticasTorneo estadisticas = mock(ServicioEstadisticasTorneo.class);
    controlador.setServicioEstadisticasTorneo(estadisticas);
    when(estadisticas.registrarGol(1L, 2L, "Messi", null)).thenReturn(true);

    usuario.setRol("USER");
    assertThat(
      controlador.registrarGolEncuentro(request, 7L, 1L, 2L, "Messi", null).getViewName(),
      equalTo("redirect:/torneos/detalle?id=7&aviso=" + Avisos.GOL_INVALIDO)
    );
    verify(estadisticas, never()).registrarGol(any(), any(), any(), any());

    usuario.setRol("ADMIN");
    assertThat(
      controlador.registrarGolEncuentro(request, 7L, 1L, 2L, "Messi", null).getViewName(),
      equalTo("redirect:/torneos/detalle?id=7&aviso=" + Avisos.GOL_REGISTRADO)
    );
  }

  @Test
  public void registrarGolEncuentroSinSesionRedirigeALogin() {
    assertThat(
      controlador.registrarGolEncuentro(request, 7L, 1L, 2L, "Messi", null).getViewName(),
      equalTo("redirect:/login")
    );
  }

  @Test
  public void faseActualEsLaPrimeraConEncuentrosPendientesOLaUltima() {
    FaseTorneo grupos = fase(1, "JUGADO");
    FaseTorneo cuartos = fase(2, "PENDIENTE");
    FaseTorneo semis = fase(3, "PENDIENTE");

    assertThat(ControladorSecciones.faseActual(List.of(grupos, cuartos, semis)), equalTo(2));
    assertThat(ControladorSecciones.faseActual(List.of(grupos)), equalTo(1));
    assertThat(ControladorSecciones.faseActual(List.of()), equalTo(1));
  }

  private static FaseTorneo fase(int numero, String estado) {
    FaseTorneo fase = new FaseTorneo();
    fase.setNumero(numero);
    EncuentroTorneo encuentro = new EncuentroTorneo();
    encuentro.setEstado(estado);
    fase.getEncuentros().add(encuentro);
    return fase;
  }
}
