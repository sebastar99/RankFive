package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tallerwebi.dominio.ResultadoSolicitud;
import com.tallerwebi.dominio.ServicioAmigos;
import com.tallerwebi.dominio.ServicioRelacionAmistad;
import com.tallerwebi.dominio.Usuario;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

public class ControladorAmigosTest {

  private ControladorAmigos controladorAmigos;
  private Usuario usuarioMock;
  private HttpServletRequest requestMock;
  private HttpSession sessionMock;
  private ServicioAmigos servicioAmigosMock;
  private ServicioRelacionAmistad servicioRelacionAmistadMock;

  @BeforeEach
  public void init() {
    usuarioMock = mock(Usuario.class);
    requestMock = mock(HttpServletRequest.class);
    sessionMock = mock(HttpSession.class);
    servicioAmigosMock = mock(ServicioAmigos.class);
    servicioRelacionAmistadMock = mock(ServicioRelacionAmistad.class);
    controladorAmigos = new ControladorAmigos(servicioAmigosMock);
    controladorAmigos.setServicioRelacionAmistad(servicioRelacionAmistadMock);
  }

  private void conSesionActiva() {
    when(requestMock.getSession(false)).thenReturn(sessionMock);
    when(sessionMock.getAttribute("usuario")).thenReturn(usuarioMock);
  }

  @Test
  public void buscarAmigoExistenteDeberiaRetornarVistaConUsuario() {
    // preparacion
    when(requestMock.getSession(false)).thenReturn(sessionMock);
    when(sessionMock.getAttribute("usuario")).thenReturn(usuarioMock);
    when(servicioAmigosMock.buscarAmigoPorNombreDeUsuario("user01")).thenReturn(usuarioMock);

    // ejecucion
    ModelAndView modelAndView = controladorAmigos.buscarAmigos(requestMock, "user01");

    // validacion
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("buscarAmigos"));
    assertThat(modelAndView.getModel().get("amigo"), equalTo(usuarioMock));
    verify(servicioAmigosMock, times(1)).buscarAmigoPorNombreDeUsuario("user01");
  }

  @Test
  public void buscarAmigoInexistenteDeberiaRetornarVistaConMensaje() {
    // preparacion
    when(requestMock.getSession(false)).thenReturn(sessionMock);
    when(sessionMock.getAttribute("usuario")).thenReturn(usuarioMock);
    when(servicioAmigosMock.buscarAmigoPorNombreDeUsuario("user02")).thenReturn(null);

    // ejecucion
    ModelAndView modelAndView = controladorAmigos.buscarAmigos(requestMock, "user02");

    // validacion
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("buscarAmigos"));
    assertThat(modelAndView.getModel().get("error"), equalTo("Usuario no encontrado"));
    verify(servicioAmigosMock, times(1)).buscarAmigoPorNombreDeUsuario("user02");
  }

  @Test
  public void enviarSolicitudExitosaDeberiaMostrarMensajeDeConfirmacion() {
    // preparacion
    conSesionActiva();
    Usuario destinatario = mock(Usuario.class);
    when(servicioAmigosMock.buscarAmigoPorNombreDeUsuario("user10")).thenReturn(destinatario);
    when(servicioRelacionAmistadMock.enviarSolicitud(usuarioMock, destinatario))
      .thenReturn(ResultadoSolicitud.ENVIADA);

    // ejecucion
    ModelAndView modelAndView = controladorAmigos.enviarSolicitud(requestMock, "user10");

    // validacion
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("buscarAmigos"));
    assertThat(modelAndView.getModel().get("ok"), equalTo(ResultadoSolicitud.ENVIADA.getMensaje()));
    verify(servicioRelacionAmistadMock, times(1)).enviarSolicitud(usuarioMock, destinatario);
  }

  @Test
  public void enviarSolicitudCuandoYaSonAmigosDeberiaMostrarError() {
    // preparacion
    conSesionActiva();
    Usuario destinatario = mock(Usuario.class);
    when(servicioAmigosMock.buscarAmigoPorNombreDeUsuario("user10")).thenReturn(destinatario);
    when(servicioRelacionAmistadMock.enviarSolicitud(usuarioMock, destinatario))
      .thenReturn(ResultadoSolicitud.YA_SON_AMIGOS);

    // ejecucion
    ModelAndView modelAndView = controladorAmigos.enviarSolicitud(requestMock, "user10");

    // validacion
    assertThat(
      modelAndView.getModel().get("error"),
      equalTo(ResultadoSolicitud.YA_SON_AMIGOS.getMensaje())
    );
  }

  @Test
  public void aceptarSolicitudDeberiaRedirigirAlDashboard() {
    // preparacion
    conSesionActiva();
    when(servicioRelacionAmistadMock.aceptarSolicitud(5L, usuarioMock)).thenReturn(true);

    // ejecucion
    ModelAndView modelAndView = controladorAmigos.aceptarSolicitud(requestMock, 5L);

    // validacion
    assertThat(modelAndView.getViewName(), startsWith("redirect:/dashboard"));
    verify(servicioRelacionAmistadMock, times(1)).aceptarSolicitud(5L, usuarioMock);
  }

  @Test
  public void rechazarSolicitudDeberiaRedirigirAlDashboard() {
    // preparacion
    conSesionActiva();
    when(servicioRelacionAmistadMock.rechazarSolicitud(5L, usuarioMock)).thenReturn(true);

    // ejecucion
    ModelAndView modelAndView = controladorAmigos.rechazarSolicitud(requestMock, 5L);

    // validacion
    assertThat(modelAndView.getViewName(), startsWith("redirect:/dashboard"));
    verify(servicioRelacionAmistadMock, times(1)).rechazarSolicitud(5L, usuarioMock);
  }

  @Test
  public void sinSesionActivaDeberiaRedirigirAlLogin() {
    // preparacion
    when(requestMock.getSession(false)).thenReturn(null);

    // ejecucion
    ModelAndView modelAndView = controladorAmigos.aceptarSolicitud(requestMock, 5L);

    // validacion
    assertThat(modelAndView.getViewName(), equalTo("redirect:/login"));
  }

  @Test
  public void buscarAmigosSinNombreDeberiaRetornarVistaSinBusqueda() {
    conSesionActiva();

    ModelAndView modelAndView = controladorAmigos.buscarAmigos(requestMock, null);

    assertThat(modelAndView.getViewName(), equalToIgnoringCase("buscarAmigos"));
    assertThat(modelAndView.getModel().get("amigo"), equalTo(null));
    assertThat(modelAndView.getModel().get("error"), equalTo(null));
  }

  @Test
  public void buscarAmigosConNombreEnBlancoDeberiaRetornarVistaSinBusqueda() {
    conSesionActiva();

    ModelAndView modelAndView = controladorAmigos.buscarAmigos(requestMock, "   ");

    assertThat(modelAndView.getViewName(), equalToIgnoringCase("buscarAmigos"));
    assertThat(modelAndView.getModel().get("amigo"), equalTo(null));
  }

  @Test
  public void buscarAmigosSinSesionDeberiaRedirigirAlLogin() {
    when(requestMock.getSession(false)).thenReturn(null);

    ModelAndView modelAndView = controladorAmigos.buscarAmigos(requestMock, "user01");

    assertThat(modelAndView.getViewName(), equalTo("redirect:/login"));
  }

  @Test
  public void enviarSolicitudSinSesionDeberiaRedirigirAlLogin() {
    when(requestMock.getSession(false)).thenReturn(null);

    ModelAndView modelAndView = controladorAmigos.enviarSolicitud(requestMock, "user10");

    assertThat(modelAndView.getViewName(), equalTo("redirect:/login"));
  }

  @Test
  public void enviarSolicitudADestinatarioInexistenteDeberiaMostrarError() {
    conSesionActiva();
    when(servicioAmigosMock.buscarAmigoPorNombreDeUsuario("noexiste")).thenReturn(null);

    ModelAndView modelAndView = controladorAmigos.enviarSolicitud(requestMock, "noexiste");

    assertThat(modelAndView.getViewName(), equalToIgnoringCase("buscarAmigos"));
    assertThat(modelAndView.getModel().get("error"), equalTo("Usuario no encontrado"));
  }

  @Test
  public void rechazarSolicitudSinSesionDeberiaRedirigirAlLogin() {
    when(requestMock.getSession(false)).thenReturn(null);

    ModelAndView modelAndView = controladorAmigos.rechazarSolicitud(requestMock, 5L);

    assertThat(modelAndView.getViewName(), equalTo("redirect:/login"));
  }

  @Test
  public void aceptarSolicitudFallidaDeberiaRedirigirConMensajeDeError() {
    conSesionActiva();
    when(servicioRelacionAmistadMock.aceptarSolicitud(5L, usuarioMock)).thenReturn(false);

    ModelAndView modelAndView = controladorAmigos.aceptarSolicitud(requestMock, 5L);

    assertThat(modelAndView.getViewName(), startsWith("redirect:/dashboard"));
    assertThat(
      modelAndView.getViewName(),
      org.hamcrest.Matchers.containsString("No+se+pudo+aceptar")
    );
  }

  @Test
  public void rechazarSolicitudFallidaDeberiaRedirigirConMensajeDeError() {
    conSesionActiva();
    when(servicioRelacionAmistadMock.rechazarSolicitud(5L, usuarioMock)).thenReturn(false);

    ModelAndView modelAndView = controladorAmigos.rechazarSolicitud(requestMock, 5L);

    assertThat(modelAndView.getViewName(), startsWith("redirect:/dashboard"));
  }
}
