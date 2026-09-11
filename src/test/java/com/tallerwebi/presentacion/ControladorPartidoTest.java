package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tallerwebi.dominio.Partido;
import com.tallerwebi.dominio.ServicioPartido;
import com.tallerwebi.dominio.ServicioRelacionAmistad;
import com.tallerwebi.dominio.Usuario;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.servlet.ModelAndView;

public class ControladorPartidoTest {

  private ControladorPartido controladorPartido;
  private ServicioPartido servicioPartidoMock;
  private ServicioRelacionAmistad servicioRelacionAmistadMock;
  private HttpServletRequest requestMock;
  private HttpSession sessionMock;
  private Usuario usuarioActual;

  @BeforeEach
  public void init() {
    servicioPartidoMock = mock(ServicioPartido.class);
    servicioRelacionAmistadMock = mock(ServicioRelacionAmistad.class);
    requestMock = mock(HttpServletRequest.class);
    sessionMock = mock(HttpSession.class);
    usuarioActual = usuarioConId(1L, "user01@test.com");
    controladorPartido = new ControladorPartido(servicioPartidoMock, servicioRelacionAmistadMock);
  }

  private Usuario usuarioConId(Long id, String email) {
    Usuario usuario = new Usuario();
    usuario.setId(id);
    usuario.setEmail(email);
    return usuario;
  }

  private Usuario usuarioConId(Long id) {
    return usuarioConId(id, null);
  }

  private void conSesionActiva() {
    when(requestMock.getSession(false)).thenReturn(sessionMock);
    when(sessionMock.getAttribute("usuario")).thenReturn(usuarioActual);
  }

  @Test
  public void verPartidosSinSesionDeberiaRedirigirALogin() {
    when(requestMock.getSession(false)).thenReturn(null);

    ModelAndView modelAndView = controladorPartido.verPartidos(requestMock, null);

    assertThat(modelAndView.getViewName(), equalTo("redirect:/login"));
  }

  @Test
  public void verPartidosConSesionDeberiaMostrarAmigosEHistorial() {
    conSesionActiva();
    Usuario amigo = usuarioConId(2L);
    Partido partido = new Partido();
    when(servicioRelacionAmistadMock.listarAmigosDe(usuarioActual)).thenReturn(List.of(amigo));
    when(servicioPartidoMock.listarPartidosDe(usuarioActual)).thenReturn(List.of(partido));

    ModelAndView modelAndView = controladorPartido.verPartidos(requestMock, null);

    assertThat(modelAndView.getViewName(), equalToIgnoringCase("partidos"));
    assertThat(modelAndView.getModel().get("amigos"), equalTo(List.of(amigo)));
    assertThat(modelAndView.getModel().get("historial"), equalTo(List.of(partido)));
  }

  @Test
  public void crearPartidoSinSesionDeberiaRedirigirALogin() {
    when(requestMock.getSession(false)).thenReturn(null);

    ModelAndView modelAndView = controladorPartido.crearPartido(
      requestMock,
      "A",
      List.of(2L),
      List.of(3L)
    );

    assertThat(modelAndView.getViewName(), equalTo("redirect:/login"));
  }

  @Test
  public void crearPartidoValidoDeberiaRedirigirAPartidosConAviso() {
    conSesionActiva();
    Usuario amigo1 = usuarioConId(2L);
    Usuario amigo2 = usuarioConId(3L);
    when(servicioRelacionAmistadMock.listarAmigosDe(usuarioActual))
      .thenReturn(List.of(amigo1, amigo2));
    when(servicioPartidoMock.crearPartido(anyList(), anyList())).thenReturn(new Partido());

    ModelAndView modelAndView = controladorPartido.crearPartido(
      requestMock,
      "A",
      List.of(2L),
      List.of(3L)
    );

    assertThat(modelAndView.getViewName(), startsWith("redirect:/partidos"));
  }

  @Test
  public void crearPartidoDeberiaAgregarAlUsuarioActualEnElEquipoElegido() {
    conSesionActiva();
    Usuario amigo1 = usuarioConId(2L);
    Usuario amigo2 = usuarioConId(3L);
    when(servicioRelacionAmistadMock.listarAmigosDe(usuarioActual))
      .thenReturn(List.of(amigo1, amigo2));
    when(servicioPartidoMock.crearPartido(anyList(), anyList())).thenReturn(new Partido());

    controladorPartido.crearPartido(requestMock, "A", List.of(2L), List.of(3L));

    ArgumentCaptor<List<Usuario>> equipoACaptor = ArgumentCaptor.forClass(List.class);
    ArgumentCaptor<List<Usuario>> equipoBCaptor = ArgumentCaptor.forClass(List.class);
    verify(servicioPartidoMock).crearPartido(equipoACaptor.capture(), equipoBCaptor.capture());
    assertThat(equipoACaptor.getValue(), containsInAnyOrder(amigo1, usuarioActual));
    assertThat(equipoBCaptor.getValue(), containsInAnyOrder(amigo2));
  }

  @Test
  public void crearPartidoInvalidoDeberiaMostrarErrorEnLaMismaVista() {
    conSesionActiva();
    when(servicioRelacionAmistadMock.listarAmigosDe(usuarioActual)).thenReturn(List.of());
    when(servicioPartidoMock.crearPartido(anyList(), anyList())).thenReturn(null);

    ModelAndView modelAndView = controladorPartido.crearPartido(
      requestMock,
      "A",
      List.of(),
      List.of()
    );

    assertThat(modelAndView.getViewName(), equalToIgnoringCase("partidos"));
    assertThat(
      modelAndView.getModel().get("error"),
      equalTo("No se pudo crear el partido. Revisá los equipos elegidos.")
    );
  }

  @Test
  public void registrarResultadoSinSesionDeberiaRedirigirALogin() {
    when(requestMock.getSession(false)).thenReturn(null);

    ModelAndView modelAndView = controladorPartido.registrarResultado(requestMock, 1L, 2, 1);

    assertThat(modelAndView.getViewName(), equalTo("redirect:/login"));
  }

  @Test
  public void registrarResultadoExitosoDeberiaRedirigirConAviso() {
    conSesionActiva();
    when(servicioPartidoMock.registrarResultado(1L, 2, 1)).thenReturn(true);

    ModelAndView modelAndView = controladorPartido.registrarResultado(requestMock, 1L, 2, 1);

    assertThat(modelAndView.getViewName(), startsWith("redirect:/partidos"));
    verify(servicioPartidoMock, times(1)).registrarResultado(1L, 2, 1);
  }

  @Test
  public void registrarResultadoExitosoDeberiaActualizarElUsuarioDeLaSesion() {
    conSesionActiva();
    Usuario usuarioActualizado = usuarioConId(1L, "user01@test.com");
    when(servicioPartidoMock.registrarResultado(1L, 2, 1)).thenReturn(true);
    when(servicioPartidoMock.buscarPorEmail("user01@test.com")).thenReturn(usuarioActualizado);

    controladorPartido.registrarResultado(requestMock, 1L, 2, 1);

    verify(sessionMock, times(1)).setAttribute("usuario", usuarioActualizado);
  }

  @Test
  public void registrarResultadoFallidoNoDeberiaActualizarElUsuarioDeLaSesion() {
    conSesionActiva();
    when(servicioPartidoMock.registrarResultado(1L, 2, 1)).thenReturn(false);

    controladorPartido.registrarResultado(requestMock, 1L, 2, 1);

    verify(sessionMock, never()).setAttribute(eq("usuario"), any());
  }

  @Test
  public void registrarResultadoFallidoDeberiaRedirigirConAvisoDeError() {
    conSesionActiva();
    when(servicioPartidoMock.registrarResultado(eq(1L), anyInt(), anyInt())).thenReturn(false);

    ModelAndView modelAndView = controladorPartido.registrarResultado(requestMock, 1L, 2, 1);

    assertThat(modelAndView.getViewName(), startsWith("redirect:/partidos"));
  }
}
