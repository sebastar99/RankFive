package com.tallerwebi.presentacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.tallerwebi.dominio.Partido;
import com.tallerwebi.dominio.ServicioPartido;
import com.tallerwebi.dominio.Usuario;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

class ControladorPreviaPartidoTest {

  private ServicioPartido servicioPartido;
  private ControladorPreviaPartido controlador;
  private HttpServletRequest request;
  private HttpSession session;
  private Usuario usuario;
  private Partido partido;

  @BeforeEach
  void setUp() {
    servicioPartido = mock(ServicioPartido.class);
    controlador = new ControladorPreviaPartido(servicioPartido);
    request = mock(HttpServletRequest.class);
    session = mock(HttpSession.class);
    usuario = new Usuario();
    partido = new Partido();
    partido.setId(7L);
  }

  @Test
  void redirigeAlLoginSinSesion() {
    when(request.getSession(false)).thenReturn(null);

    assertEquals("redirect:/login", controlador.verPrevia(request, 7L).getViewName());
  }

  @Test
  void rechazaPartidosQueNoPertenecenAlUsuario() {
    iniciarSesion();
    when(servicioPartido.listarPartidosDe(usuario)).thenReturn(List.of());

    assertEquals(
      "redirect:/partidos?aviso=No%20ten%C3%A9s%20acceso%20a%20ese%20partido",
      controlador.verPrevia(request, 7L).getViewName()
    );
  }

  @Test
  void muestraLaPreviaParaUnParticipante() {
    iniciarSesion();
    when(servicioPartido.listarPartidosDe(usuario)).thenReturn(List.of(partido));

    ModelAndView resultado = controlador.verPrevia(request, 7L);

    assertEquals("PreviaPartido", resultado.getViewName());
    assertEquals(usuario, resultado.getModel().get("usuario"));
    assertEquals(partido, resultado.getModel().get("partido"));
  }

  private void iniciarSesion() {
    when(request.getSession(false)).thenReturn(session);
    when(session.getAttribute("usuario")).thenReturn(usuario);
  }
}
