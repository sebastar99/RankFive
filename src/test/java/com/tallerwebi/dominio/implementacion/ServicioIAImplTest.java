package com.tallerwebi.dominio.implementacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tallerwebi.dominio.EstadisticasJugador;
import com.tallerwebi.dominio.ImagenGenerada;
import com.tallerwebi.dominio.Partido;
import com.tallerwebi.dominio.PerfilJugador;
import com.tallerwebi.dominio.RepositorioPartido;
import com.tallerwebi.dominio.ServicioEstadisticasJugador;
import com.tallerwebi.dominio.ServicioGemini;
import com.tallerwebi.dominio.Usuario;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class ServicioIAImplTest {

  private RepositorioPartido repositorioPartido;
  private ServicioGemini servicioGemini;
  private ServicioEstadisticasJugador servicioEstadisticasJugador;
  private ServicioIAImpl servicio;
  private Partido partido;

  @BeforeEach
  void setUp() {
    repositorioPartido = mock(RepositorioPartido.class);
    servicioGemini = mock(ServicioGemini.class);
    servicioEstadisticasJugador = mock(ServicioEstadisticasJugador.class);

    servicio = new ServicioIAImpl(repositorioPartido, servicioGemini, servicioEstadisticasJugador);

    partido = new Partido();
    partido.setId(12L);
    partido.setEquipoA(Set.of(jugador("Ana")));
    partido.setEquipoB(Set.of(jugador("Beto")));

    when(repositorioPartido.buscarPorId(12L)).thenReturn(partido);
    when(servicioEstadisticasJugador.obtenerEstadisticasDelUsuario(any()))
      .thenReturn(new EstadisticasJugador());
  }

  @Test
  void generaPreviaConDatosDeAmbosEquipos() throws Exception {
    when(servicioGemini.generarTexto(anyString())).thenReturn("Previa generada");

    assertEquals("Previa generada", servicio.generarPreviaPartido(12L));

    ArgumentCaptor<String> prompt = ArgumentCaptor.forClass(String.class);

    verify(servicioGemini).generarTexto(prompt.capture());

    assertEquals(true, prompt.getValue().contains("ID del partido: 12"));
    assertEquals(true, prompt.getValue().contains("Ana"));
    assertEquals(true, prompt.getValue().contains("Beto"));
  }

  @Test
  void generaBannerConLosNombresDeLosEquipos() throws Exception {
    ImagenGenerada imagen = new ImagenGenerada(new byte[] { 1 }, "image/png");

    when(servicioGemini.generarImagen(anyString())).thenReturn(imagen);

    assertEquals(imagen, servicio.generarBannerPartido(12L));

    ArgumentCaptor<String> prompt = ArgumentCaptor.forClass(String.class);

    verify(servicioGemini).generarImagen(prompt.capture());

    assertEquals(true, prompt.getValue().contains("Equipo A: Ana"));

    assertEquals(true, prompt.getValue().contains("Equipo B: Beto"));
  }

  @Test
  void rechazaUnPartidoInexistenteAntesDeConsultarGemini() {
    when(repositorioPartido.buscarPorId(404L)).thenReturn(null);

    IllegalArgumentException error = assertThrows(
      IllegalArgumentException.class,
      () -> servicio.generarPreviaPartido(404L)
    );

    assertEquals("No existe el partido con id: 404", error.getMessage());
  }

  private static Usuario jugador(String nombre) {
    Usuario usuario = new Usuario();

    PerfilJugador perfil = new PerfilJugador();
    perfil.setNombreCompleto(nombre);

    usuario.setPerfil(perfil);

    return usuario;
  }
}
