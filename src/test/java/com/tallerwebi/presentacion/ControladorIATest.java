package com.tallerwebi.presentacion;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.tallerwebi.dominio.ImagenGenerada;
import com.tallerwebi.dominio.Partido;
import com.tallerwebi.dominio.ServicioIA;
import com.tallerwebi.dominio.ServicioPartido;
import com.tallerwebi.dominio.Usuario;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

class ControladorIATest {

  private ServicioIA servicioIA;
  private ServicioPartido servicioPartido;
  private ControladorIA controlador;
  private HttpServletRequest request;
  private HttpSession session;
  private Usuario usuario;
  private Partido partido;

  @BeforeEach
  void setUp() {
    servicioIA = mock(ServicioIA.class);
    servicioPartido = mock(ServicioPartido.class);
    controlador = new ControladorIA(servicioIA, servicioPartido);
    request = mock(HttpServletRequest.class);
    session = mock(HttpSession.class);
    usuario = new Usuario();
    partido = new Partido();
    partido.setId(7L);
  }

  @Test
  void rechazaLaPreviaSinSesionOSinParticipacion() {
    when(request.getSession(false)).thenReturn(null);
    assertEquals(403, controlador.generarPrevia(7L, request).getStatusCode().value());

    iniciarSesion();
    when(session.getAttribute("usuario")).thenReturn(null);
    assertEquals(403, controlador.generarPrevia(7L, request).getStatusCode().value());

    when(session.getAttribute("usuario")).thenReturn(usuario);
    when(servicioPartido.listarPartidosDe(usuario)).thenReturn(List.of());
    assertEquals(403, controlador.generarPrevia(7L, request).getStatusCode().value());
  }

  @Test
  void devuelveLaPreviaEnTextoUtf8AlParticipante() throws Exception {
    iniciarSesion();
    when(servicioPartido.listarPartidosDe(usuario)).thenReturn(List.of(partido));
    when(servicioIA.generarPreviaPartido(7L)).thenReturn("La previa");

    ResponseEntity<String> respuesta = controlador.generarPrevia(7L, request);

    assertEquals(200, respuesta.getStatusCode().value());
    assertEquals("La previa", respuesta.getBody());
    assertTrue(respuesta.getHeaders().getContentType().isCompatibleWith(MediaType.TEXT_PLAIN));
    assertEquals("UTF-8", respuesta.getHeaders().getContentType().getCharset().name());
  }

  @Test
  void traduceErroresDeLaPrevia() throws Exception {
    iniciarSesion();
    when(servicioPartido.listarPartidosDe(usuario)).thenReturn(List.of(partido));
    when(servicioIA.generarPreviaPartido(7L)).thenThrow(new IllegalStateException("falló"));

    ResponseEntity<String> respuesta = controlador.generarPrevia(7L, request);

    assertEquals(500, respuesta.getStatusCode().value());
    assertEquals("Error generando previa: falló", respuesta.getBody());
  }

  @Test
  void protegeElBannerYDevuelveSuContenido() throws Exception {
    when(request.getSession(false)).thenReturn(null);
    assertEquals(403, controlador.generarBanner(7L, request).getStatusCode().value());

    iniciarSesion();
    when(servicioPartido.listarPartidosDe(usuario)).thenReturn(List.of(partido));
    byte[] datos = new byte[] { 1, 2, 3 };
    when(servicioIA.generarBannerPartido(7L)).thenReturn(new ImagenGenerada(datos, "image/png"));

    ResponseEntity<?> respuesta = controlador.generarBanner(7L, request);

    assertEquals(200, respuesta.getStatusCode().value());
    assertEquals(MediaType.IMAGE_PNG, respuesta.getHeaders().getContentType());
    assertArrayEquals(datos, (byte[]) respuesta.getBody());
  }

  @Test
  void respondeConErrorCuandoNoPuedeGenerarElBanner() throws Exception {
    iniciarSesion();
    when(servicioPartido.listarPartidosDe(usuario)).thenReturn(List.of(partido));
    when(servicioIA.generarBannerPartido(7L)).thenThrow(new IllegalStateException("sin imagen"));

    ResponseEntity<?> respuesta = controlador.generarBanner(7L, request);

    assertEquals(500, respuesta.getStatusCode().value());
    assertEquals("sin imagen", respuesta.getBody());
  }

  private void iniciarSesion() {
    when(request.getSession(false)).thenReturn(session);
    when(session.getAttribute("usuario")).thenReturn(usuario);
  }
}
