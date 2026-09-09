package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tallerwebi.dominio.ServicioAmigos;
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

  @BeforeEach
  public void init() {
    usuarioMock = mock(Usuario.class);
    requestMock = mock(HttpServletRequest.class);
    sessionMock = mock(HttpSession.class);
    servicioAmigosMock = mock(ServicioAmigos.class);
    controladorAmigos = new ControladorAmigos(servicioAmigosMock);
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
}
