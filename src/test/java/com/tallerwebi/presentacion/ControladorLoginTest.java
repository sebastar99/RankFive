package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.PerfilJugador;
import com.tallerwebi.dominio.ServicioLogin;
import com.tallerwebi.dominio.ServicioRelacionAmistad;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

public class ControladorLoginTest {

  private ControladorLogin controladorLogin;
  private Usuario usuarioMock;
  private DatosLogin datosLoginMock;
  private HttpServletRequest requestMock;
  private HttpSession sessionMock;
  private ServicioLogin servicioLoginMock;
  private ServicioRelacionAmistad servicioRelacionAmistadMock;

  @BeforeEach
  public void init() {
    datosLoginMock = new DatosLogin("dami@unlam.com", "123");
    usuarioMock = mock(Usuario.class);
    when(usuarioMock.getEmail()).thenReturn("dami@unlam.com");
    requestMock = mock(HttpServletRequest.class);
    sessionMock = mock(HttpSession.class);
    servicioLoginMock = mock(ServicioLogin.class);
    servicioRelacionAmistadMock = mock(ServicioRelacionAmistad.class);
    controladorLogin = new ControladorLogin(servicioLoginMock);
    controladorLogin.setServicioRelacionAmistad(servicioRelacionAmistadMock);
  }

  @Test
  public void loginConUsuarioYPasswordInorrectosDeberiaLlevarALoginNuevamente() {
    // preparacion
    when(servicioLoginMock.consultarUsuario(anyString(), anyString())).thenReturn(null);

    // ejecucion
    ModelAndView modelAndView = controladorLogin.validarLogin(datosLoginMock, requestMock);

    // validacion
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("login"));
    assertThat(
      modelAndView.getModel().get("error").toString(),
      equalToIgnoringCase("Usuario o clave incorrecta")
    );
    verify(sessionMock, times(0)).setAttribute("ROL", "ADMIN");
  }

  @Test
  public void loginConUsuarioYPasswordCorrectosDeberiaLlevarAlDashboard() {
    // preparacion
    Usuario usuarioEncontradoMock = mock(Usuario.class);
    when(usuarioEncontradoMock.getRol()).thenReturn("ADMIN");

    when(requestMock.getSession()).thenReturn(sessionMock);
    when(servicioLoginMock.consultarUsuario(anyString(), anyString()))
      .thenReturn(usuarioEncontradoMock);

    // ejecucion
    ModelAndView modelAndView = controladorLogin.validarLogin(datosLoginMock, requestMock);

    // validacion
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/dashboard"));
    verify(sessionMock, times(1)).setAttribute("ROL", usuarioEncontradoMock.getRol());
  }

  @Test
  public void registrameSiUsuarioNoExisteDeberiaCrearUsuarioYVolverAlLogin()
    throws UsuarioExistente {
    // ejecucion
    ModelAndView modelAndView = controladorLogin.registrarme(usuarioMock);

    // validacion
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/login"));
    verify(servicioLoginMock, times(1)).registrar(usuarioMock);
  }

  @Test
  public void registrarmeSiUsuarioExisteDeberiaVolverAFormularioYMostrarError()
    throws UsuarioExistente {
    // preparacion
    doThrow(UsuarioExistente.class).when(servicioLoginMock).registrar(usuarioMock);

    // ejecucion
    ModelAndView modelAndView = controladorLogin.registrarme(usuarioMock);

    // validacion
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("nuevo-usuario"));
    assertThat(
      modelAndView.getModel().get("error").toString(),
      equalToIgnoringCase("El usuario ya existe")
    );
  }

  @Test
  public void errorEnRegistrarmeDeberiaVolverAFormularioYMostrarError() throws UsuarioExistente {
    // preparacion
    doThrow(RuntimeException.class).when(servicioLoginMock).registrar(usuarioMock);

    // ejecucion
    ModelAndView modelAndView = controladorLogin.registrarme(usuarioMock);

    // validacion
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("nuevo-usuario"));
    assertThat(
      modelAndView.getModel().get("error").toString(),
      equalToIgnoringCase("Error al registrar el nuevo usuario")
    );
  }

  @Test
  public void irALoginDeberiaRetornarVistaLoginConDatosLogin() {
    // ejecucion
    ModelAndView modelAndView = controladorLogin.irALogin();

    // validacion
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("login"));
    assertThat(modelAndView.getModel().get("datosLogin"), instanceOf(DatosLogin.class));
  }

  @Test
  public void nuevoUsuarioDeberiaRetornarVistaNuevoUsuarioConUsuarioVacio() {
    // ejecucion
    ModelAndView modelAndView = controladorLogin.nuevoUsuario();

    // validacion
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("nuevo-usuario"));
    assertThat(modelAndView.getModel().get("usuario"), instanceOf(Usuario.class));
  }

  @Test
  public void irAHomeDeberiaRetornarVistaHome() {
    // ejecucion
    ModelAndView modelAndView = controladorLogin.irAHome();

    // validacion
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("home"));
  }

  @Test
  public void inicioDeberiaRedirigirAHome() {
    // ejecucion
    ModelAndView modelAndView = controladorLogin.inicio();

    // validacion
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/home"));
  }

  @Test
  public void irALoginConSesionActivaDeberiaRedirigirAlDashboard() {
    when(requestMock.getSession(false)).thenReturn(sessionMock);
    when(sessionMock.getAttribute("usuario")).thenReturn(usuarioMock);

    ModelAndView modelAndView = controladorLogin.irALogin(requestMock);

    assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/dashboard"));
  }

  @Test
  public void irALoginSinSesionDeberiaRetornarVistaLogin() {
    when(requestMock.getSession(false)).thenReturn(null);

    ModelAndView modelAndView = controladorLogin.irALogin(requestMock);

    assertThat(modelAndView.getViewName(), equalToIgnoringCase("login"));
    assertThat(modelAndView.getModel().get("datosLogin"), instanceOf(DatosLogin.class));
  }

  @Test
  public void irAlDashboardSinUsuarioEnSesionDeberiaRedirigirAlLogin() {
    when(requestMock.getSession()).thenReturn(sessionMock);
    when(sessionMock.getAttribute("usuario")).thenReturn(null);

    ModelAndView modelAndView = controladorLogin.irADashboard(requestMock, null);

    assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/login"));
  }

  @Test
  public void irAlDashboardConUsuarioSinPerfilDeberiaMostrarPuntosCeroYRangoBronce() {
    prepararSesionYDashboard(usuarioMock);
    when(usuarioMock.getPerfil()).thenReturn(null);

    ModelAndView modelAndView = controladorLogin.irADashboard(requestMock, null);

    assertThat(modelAndView.getViewName(), equalToIgnoringCase("dashboard"));
    assertThat(modelAndView.getModel().get("puntos"), equalTo(0));
    assertThat(modelAndView.getModel().get("rangoNombre"), equalTo("Bronce"));
  }

  @Test
  public void irAlDashboardConPerfilYPlNuloDeberiaMostrarPuntosCeroYRangoBronce() {
    PerfilJugador perfil = new PerfilJugador();
    perfil.setPl(null);
    prepararSesionYDashboard(usuarioMock);
    when(usuarioMock.getPerfil()).thenReturn(perfil);

    ModelAndView modelAndView = controladorLogin.irADashboard(requestMock, null);

    assertThat(modelAndView.getModel().get("puntos"), equalTo(0));
    assertThat(modelAndView.getModel().get("rangoNombre"), equalTo("Bronce"));
  }

  @Test
  public void irAlDashboardConPlAltoDeberiaMostrarRangoLegendario() {
    PerfilJugador perfil = new PerfilJugador();
    perfil.setPl(4000);
    prepararSesionYDashboard(usuarioMock);
    when(usuarioMock.getPerfil()).thenReturn(perfil);

    ModelAndView modelAndView = controladorLogin.irADashboard(requestMock, "Solicitud aceptada");

    assertThat(modelAndView.getModel().get("rangoNombre"), equalTo("Legendario"));
    assertThat(modelAndView.getModel().get("rangoCss"), equalTo("rank-legend"));
    assertThat(modelAndView.getModel().get("aviso"), equalTo("Solicitud aceptada"));
  }

  @Test
  public void irAlDashboardConPlDiamanteDeberiaMostrarRangoDiamante() {
    PerfilJugador perfil = new PerfilJugador();
    perfil.setPl(3100);
    prepararSesionYDashboard(usuarioMock);
    when(usuarioMock.getPerfil()).thenReturn(perfil);

    ModelAndView modelAndView = controladorLogin.irADashboard(requestMock, null);

    assertThat(modelAndView.getModel().get("rangoNombre"), equalTo("Diamante"));
  }

  @Test
  public void irAlDashboardConPlPlatinoDeberiaMostrarRangoPlatino() {
    PerfilJugador perfil = new PerfilJugador();
    perfil.setPl(2600);
    prepararSesionYDashboard(usuarioMock);
    when(usuarioMock.getPerfil()).thenReturn(perfil);

    ModelAndView modelAndView = controladorLogin.irADashboard(requestMock, null);

    assertThat(modelAndView.getModel().get("rangoNombre"), equalTo("Platino"));
  }

  @Test
  public void irAlDashboardConPlOroDeberiaMostrarRangoOro() {
    PerfilJugador perfil = new PerfilJugador();
    perfil.setPl(2100);
    prepararSesionYDashboard(usuarioMock);
    when(usuarioMock.getPerfil()).thenReturn(perfil);

    ModelAndView modelAndView = controladorLogin.irADashboard(requestMock, null);

    assertThat(modelAndView.getModel().get("rangoNombre"), equalTo("Oro"));
  }

  @Test
  public void irAlDashboardConPlPlataDeberiaMostrarRangoPlata() {
    PerfilJugador perfil = new PerfilJugador();
    perfil.setPl(1600);
    prepararSesionYDashboard(usuarioMock);
    when(usuarioMock.getPerfil()).thenReturn(perfil);

    ModelAndView modelAndView = controladorLogin.irADashboard(requestMock, null);

    assertThat(modelAndView.getModel().get("rangoNombre"), equalTo("Plata"));
  }

  @Test
  public void irAlDashboardConSolicitudesPendientesDeberiaIncluirCantidadYLista() {
    PerfilJugador perfil = new PerfilJugador();
    perfil.setPl(1200);
    prepararSesionYDashboard(usuarioMock);
    when(usuarioMock.getPerfil()).thenReturn(perfil);
    when(servicioRelacionAmistadMock.contarSolicitudesPendientes(usuarioMock)).thenReturn(3L);

    ModelAndView modelAndView = controladorLogin.irADashboard(requestMock, null);

    assertThat(modelAndView.getModel().get("cantidadSolicitudes"), equalTo(3L));
    assertThat(modelAndView.getModel().get("solicitudesPendientes"), is(notNullValue()));
  }

  @Test
  public void logoutConSesionActivaDeberiaInvalidarlaYRedirigir() {
    when(requestMock.getSession(false)).thenReturn(sessionMock);

    ModelAndView modelAndView = controladorLogin.logout(requestMock);

    assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/login"));
    verify(sessionMock, times(1)).invalidate();
  }

  @Test
  public void logoutSinSesionDeberiaRedirigirSinInvalidar() {
    when(requestMock.getSession(false)).thenReturn(null);

    ModelAndView modelAndView = controladorLogin.logout(requestMock);

    assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/login"));
    verify(sessionMock, never()).invalidate();
  }

  private void prepararSesionYDashboard(Usuario usuario) {
    when(requestMock.getSession()).thenReturn(sessionMock);
    when(sessionMock.getAttribute("usuario")).thenReturn(usuario);
    when(servicioRelacionAmistadMock.contarSolicitudesPendientes(usuario)).thenReturn(0L);
    when(servicioRelacionAmistadMock.listarSolicitudesPendientes(usuario))
      .thenReturn(Collections.emptyList());
    when(servicioRelacionAmistadMock.listarAmigosDe(usuario)).thenReturn(Collections.emptyList());
  }
}
