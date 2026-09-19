package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tallerwebi.dominio.AlmacenamientoImagenes;
import com.tallerwebi.dominio.ResultadoCreacionEquipo;
import com.tallerwebi.dominio.ServicioEquipo;
import com.tallerwebi.dominio.ServicioRelacionAmistad;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.presentacion.DTO.DatosEquipo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.servlet.ModelAndView;

public class ControladorEquipoTest {

  private ControladorEquipo controlador;
  private ServicioEquipo servicioEquipo;
  private ServicioRelacionAmistad servicioAmistad;
  private AlmacenamientoImagenes almacenamiento;
  private HttpServletRequest request;
  private HttpSession session;
  private Usuario usuario;

  @BeforeEach
  public void init() {
    servicioEquipo = mock(ServicioEquipo.class);
    servicioAmistad = mock(ServicioRelacionAmistad.class);
    almacenamiento = mock(AlmacenamientoImagenes.class);
    request = mock(HttpServletRequest.class);
    session = mock(HttpSession.class);
    usuario = new Usuario();
    usuario.setId(1L);
    controlador = new ControladorEquipo(servicioAmistad, servicioEquipo, almacenamiento);
  }

  private void conSesion() {
    when(request.getSession(false)).thenReturn(session);
    when(session.getAttribute("usuario")).thenReturn(usuario);
  }

  private DatosEquipo datos(String nombre, int cantidad, List<String> jugadores) {
    DatosEquipo datos = new DatosEquipo();
    datos.setNombre(nombre);
    datos.setCantidad(cantidad);
    datos.setJugadores(jugadores);
    return datos;
  }

  @Test
  public void sinSesionRedirigeALogin() {
    when(request.getSession(false)).thenReturn(null);

    assertThat(controlador.agregarEquipo(request).getViewName(), equalTo("redirect:/login"));
    assertThat(
      controlador.crearEquipo(request, new DatosEquipo(), null).getViewName(),
      equalTo("redirect:/login")
    );
    assertThat(
      controlador.aceptarInvitacion(request, 1L).getViewName(),
      equalTo("redirect:/login")
    );
    assertThat(
      controlador.rechazarInvitacion(request, 1L).getViewName(),
      equalTo("redirect:/login")
    );
  }

  @Test
  public void agregarEquipoMuestraFormularioConAmigos() {
    conSesion();
    Usuario amigo = new Usuario();
    when(servicioAmistad.listarAmigosDe(usuario)).thenReturn(List.of(amigo));

    ModelAndView mav = controlador.agregarEquipo(request);

    assertThat(mav.getViewName(), equalTo("agregarEquipo"));
    assertThat(mav.getModel().get("amigos"), equalTo(List.of(amigo)));
    assertThat(mav.getModel().get("datosEquipo"), is(notNullValue()));
  }

  @Test
  public void crearEquipoExitosoGuardaEscudoYRedirigeConAviso() {
    conSesion();
    MockMultipartFile escudo = new MockMultipartFile(
      "escudo",
      "logo.png",
      "image/png",
      new byte[] { 1, 2 }
    );
    when(almacenamiento.guardarEscudo(eq("logo.png"), any())).thenReturn("/escudos/x.png");
    when(
      servicioEquipo.crearEquipo(eq(usuario), eq("Pibes"), eq(5), eq("/escudos/x.png"), anyList())
    )
      .thenReturn(ResultadoCreacionEquipo.CREADO);

    ModelAndView mav = controlador.crearEquipo(
      request,
      datos("Pibes", 5, List.of("user07")),
      escudo
    );

    assertThat(mav.getViewName(), startsWith("redirect:/dashboard?aviso="));
    verify(servicioEquipo).crearEquipo(usuario, "Pibes", 5, "/escudos/x.png", List.of("user07"));
  }

  @Test
  public void crearEquipoSinEscudoPasaFotoNula() {
    conSesion();
    when(servicioEquipo.crearEquipo(any(), anyString(), any(), any(), anyList()))
      .thenReturn(ResultadoCreacionEquipo.CREADO);

    controlador.crearEquipo(request, datos("Pibes", 5, List.of("user07")), null);

    verify(almacenamiento, never()).guardarEscudo(any(), any());
    verify(servicioEquipo).crearEquipo(usuario, "Pibes", 5, null, List.of("user07"));
  }

  @Test
  public void crearEquipoConErrorVuelveAlFormularioConMensaje() {
    conSesion();
    when(servicioEquipo.crearEquipo(any(), any(), any(), any(), anyList()))
      .thenReturn(ResultadoCreacionEquipo.SIN_JUGADORES);
    DatosEquipo datos = datos("Pibes", 5, List.of());

    ModelAndView mav = controlador.crearEquipo(request, datos, null);

    assertThat(mav.getViewName(), equalTo("agregarEquipo"));
    assertThat(
      mav.getModel().get("error"),
      equalTo(ResultadoCreacionEquipo.SIN_JUGADORES.getMensaje())
    );
    assertThat(mav.getModel().get("datosEquipo"), equalTo(datos));
  }

  @Test
  public void aceptarYRechazarInvitacionRedirigenAlDashboard() {
    conSesion();
    when(servicioEquipo.aceptarInvitacion(10L, usuario)).thenReturn(true);
    when(servicioEquipo.rechazarInvitacion(11L, usuario)).thenReturn(false);

    assertThat(
      controlador.aceptarInvitacion(request, 10L).getViewName(),
      startsWith("redirect:/dashboard?aviso=")
    );
    assertThat(
      controlador.rechazarInvitacion(request, 11L).getViewName(),
      startsWith("redirect:/dashboard?aviso=")
    );
    verify(servicioEquipo).aceptarInvitacion(10L, usuario);
    verify(servicioEquipo).rechazarInvitacion(11L, usuario);
  }
}
