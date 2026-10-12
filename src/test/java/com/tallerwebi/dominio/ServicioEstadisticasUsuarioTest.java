package com.tallerwebi.dominio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tallerwebi.dominio.enums.TipoTarjeta;
import com.tallerwebi.dominio.implementacion.ServicioEstadisticasJugadorImpl;
import java.util.List;
import org.junit.jupiter.api.Test;

public class ServicioEstadisticasUsuarioTest {

  @Test
  public void soloDebenContarLosPartidosJugadosDelUsuarioConResultadoRegistrado() {
    long usuario = 1L;
    RepositorioPartido repositorioPartido = mock(RepositorioPartido.class);
    RepositorioGolPartido repositorioGolPartido = mock(RepositorioGolPartido.class);
    ServicioEstadisticasJugador servicio = new ServicioEstadisticasJugadorImpl(
      repositorioPartido,
      repositorioGolPartido,
      mock(RepositorioTarjetaPartido.class)
    );

    Partido partidoJugado = new Partido();
    partidoJugado.registrarResultado(3, 1);

    Partido partidoEmpatado = new Partido();
    partidoEmpatado.registrarResultado(2, 2);

    Partido partidoPerdido = new Partido();
    partidoPerdido.registrarResultado(1, 3);

    when(repositorioPartido.listarPartidosDe(usuario))
      .thenReturn(List.of(partidoJugado, partidoEmpatado, partidoPerdido));

    EstadisticasJugador estadisticas = servicio.obtenerEstadisticasDelUsuario(usuario);

    assertEquals(3, estadisticas.getPartidosJugados().intValue());
    verify(repositorioPartido).listarPartidosDe(usuario);
  }

  @Test
  public void unPartidoEmpatadoCeroACeroDebeContarComoPartidoJugado() {
    long usuario = 1L;
    RepositorioPartido repositorioPartido = mock(RepositorioPartido.class);
    RepositorioGolPartido repositorioGolPartido = mock(RepositorioGolPartido.class);
    ServicioEstadisticasJugador servicio = new ServicioEstadisticasJugadorImpl(
      repositorioPartido,
      repositorioGolPartido,
      mock(RepositorioTarjetaPartido.class)
    );

    Partido partidoEmpatado = new Partido();
    partidoEmpatado.registrarResultado(0, 0);

    when(repositorioPartido.listarPartidosDe(usuario)).thenReturn(List.of(partidoEmpatado));

    EstadisticasJugador estadisticas = servicio.obtenerEstadisticasDelUsuario(usuario);

    assertEquals(1, estadisticas.getPartidosJugados().intValue());
    verify(repositorioPartido).listarPartidosDe(usuario);
  }

  @Test
  public void losPartidosPendientesNoDebenContarComoPartidosJugados() {
    long usuario = 1L;
    RepositorioPartido repositorioPartido = mock(RepositorioPartido.class);
    RepositorioGolPartido repositorioGolPartido = mock(RepositorioGolPartido.class);
    ServicioEstadisticasJugador servicio = new ServicioEstadisticasJugadorImpl(
      repositorioPartido,
      repositorioGolPartido,
      mock(RepositorioTarjetaPartido.class)
    );

    Partido partidoPendiente = new Partido();

    when(repositorioPartido.listarPartidosDe(usuario)).thenReturn(List.of(partidoPendiente));

    EstadisticasJugador estadisticas = servicio.obtenerEstadisticasDelUsuario(usuario);

    assertEquals(0, estadisticas.getPartidosJugados().intValue());
    verify(repositorioPartido).listarPartidosDe(usuario);
  }

  @Test
  public void losPartidosDebenSerCeroCuandoUnJugadorNoTienePartidosJugados() {
    long usuario = 1L;
    RepositorioPartido repositorioPartido = mock(RepositorioPartido.class);
    RepositorioGolPartido repositorioGolPartido = mock(RepositorioGolPartido.class);
    ServicioEstadisticasJugador servicio = new ServicioEstadisticasJugadorImpl(
      repositorioPartido,
      repositorioGolPartido,
      mock(RepositorioTarjetaPartido.class)
    );

    when(repositorioPartido.listarPartidosDe(usuario)).thenReturn(List.of());

    EstadisticasJugador estadisticas = servicio.obtenerEstadisticasDelUsuario(usuario);

    assertEquals(0, estadisticas.getPartidosJugados().intValue());
    verify(repositorioPartido).listarPartidosDe(usuario);
  }

  @Test
  public void soloDebenContarLosPartidosQueElUsuarioJugoAunqueHayanPartidosPendientes() {
    long usuario = 1L;
    RepositorioPartido repositorioPartido = mock(RepositorioPartido.class);
    RepositorioGolPartido repositorioGolPartido = mock(RepositorioGolPartido.class);
    ServicioEstadisticasJugador servicio = new ServicioEstadisticasJugadorImpl(
      repositorioPartido,
      repositorioGolPartido,
      mock(RepositorioTarjetaPartido.class)
    );

    Partido partidoEmpatado = new Partido();
    partidoEmpatado.registrarResultado(0, 0);

    Partido partidoPendiente = new Partido();

    when(repositorioPartido.listarPartidosDe(usuario))
      .thenReturn(List.of(partidoEmpatado, partidoPendiente));

    EstadisticasJugador estadisticas = servicio.obtenerEstadisticasDelUsuario(usuario);

    assertEquals(1, estadisticas.getPartidosJugados().intValue());
    verify(repositorioPartido).listarPartidosDe(usuario);
  }

  @Test
  public void seDebenContarLosGolesDelUsuarioConsultado() {
    RepositorioPartido repositorioPartido = mock(RepositorioPartido.class);
    RepositorioGolPartido repositorioGolPartido = mock(RepositorioGolPartido.class);
    ServicioEstadisticasJugador servicio = new ServicioEstadisticasJugadorImpl(
      repositorioPartido,
      repositorioGolPartido,
      mock(RepositorioTarjetaPartido.class)
    );

    Usuario usuario = new Usuario();
    usuario.setId(1L);
    Usuario usuario2 = new Usuario();
    usuario2.setId(2L);

    Partido partido = new Partido();
    partido.setId(10L);
    partido.registrarResultado(2, 1);

    GolPartido gol1 = new GolPartido();
    gol1.setUsuarioGoleador(usuario);
    gol1.setPartido(partido);

    GolPartido gol2 = new GolPartido();
    gol2.setUsuarioGoleador(usuario);
    gol2.setPartido(partido);

    GolPartido gol3 = new GolPartido();
    gol3.setUsuarioGoleador(usuario2);
    gol3.setPartido(partido);

    when(repositorioGolPartido.listarPorPartido(10L)).thenReturn(List.of(gol1, gol2, gol3));

    when(repositorioPartido.listarPartidosDe(1L)).thenReturn(List.of(partido));

    EstadisticasJugador estadisticas = servicio.obtenerEstadisticasDelUsuario(1L);

    assertEquals(2, estadisticas.getGoles().intValue());
    verify(repositorioGolPartido).listarPorPartido(10L);
  }

  @Test
  public void noSeDebenContarLosGolesDeOtrosUsuarios() {
    RepositorioPartido repositorioPartido = mock(RepositorioPartido.class);
    RepositorioGolPartido repositorioGolPartido = mock(RepositorioGolPartido.class);
    ServicioEstadisticasJugador servicio = new ServicioEstadisticasJugadorImpl(
      repositorioPartido,
      repositorioGolPartido,
      mock(RepositorioTarjetaPartido.class)
    );

    Usuario usuario = new Usuario();
    usuario.setId(1L);
    Usuario usuario2 = new Usuario();
    usuario2.setId(2L);

    Partido partido = new Partido();
    partido.setId(10L);
    partido.registrarResultado(2, 1);

    GolPartido gol1 = new GolPartido();
    gol1.setUsuarioGoleador(usuario);
    gol1.setPartido(partido);

    GolPartido gol2 = new GolPartido();
    gol2.setUsuarioGoleador(usuario2);
    gol2.setPartido(partido);

    when(repositorioGolPartido.listarPorPartido(10L)).thenReturn(List.of(gol1, gol2));

    when(repositorioPartido.listarPartidosDe(1L)).thenReturn(List.of(partido));

    EstadisticasJugador estadisticas = servicio.obtenerEstadisticasDelUsuario(1L);

    assertEquals(1, estadisticas.getGoles().intValue());
    verify(repositorioGolPartido).listarPorPartido(10L);
  }

  @Test
  public void noSeDebenContarLosGolesDeUsuariosQueNoSonElUsuario() {
    RepositorioPartido repositorioPartido = mock(RepositorioPartido.class);
    RepositorioGolPartido repositorioGolPartido = mock(RepositorioGolPartido.class);
    ServicioEstadisticasJugador servicio = new ServicioEstadisticasJugadorImpl(
      repositorioPartido,
      repositorioGolPartido,
      mock(RepositorioTarjetaPartido.class)
    );

    Usuario usuario = new Usuario();
    usuario.setId(1L);
    Usuario usuario2 = new Usuario();
    usuario2.setId(2L);

    Partido partido = new Partido();
    partido.setId(10L);
    partido.registrarResultado(2, 1);

    GolPartido gol1 = new GolPartido();
    gol1.setUsuarioGoleador(usuario);
    gol1.setPartido(partido);

    GolPartido gol2 = new GolPartido();
    gol2.setUsuarioGoleador(usuario2);
    gol2.setPartido(partido);

    when(repositorioGolPartido.listarPorPartido(10L)).thenReturn(List.of(gol1, gol2));

    when(repositorioPartido.listarPartidosDe(1L)).thenReturn(List.of(partido));

    EstadisticasJugador estadisticas = servicio.obtenerEstadisticasDelUsuario(1L);

    assertEquals(1, estadisticas.getGoles().intValue());
    verify(repositorioGolPartido).listarPorPartido(10L);
  }

  @Test
  public void seDebenContarSoloLasAsistenciasDelUsuarioConsultado() {
    RepositorioPartido repositorioPartido = mock(RepositorioPartido.class);
    RepositorioGolPartido repositorioGolPartido = mock(RepositorioGolPartido.class);
    ServicioEstadisticasJugador servicio = new ServicioEstadisticasJugadorImpl(
      repositorioPartido,
      repositorioGolPartido,
      mock(RepositorioTarjetaPartido.class)
    );

    Usuario usuario = new Usuario();
    usuario.setId(1L);
    Usuario usuario2 = new Usuario();
    usuario2.setId(2L);
    Usuario goleador = new Usuario();
    goleador.setId(3L);

    Partido partido = new Partido();
    partido.setId(10L);
    partido.registrarResultado(2, 0);

    GolPartido gol1 = new GolPartido();
    gol1.setUsuarioGoleador(goleador);
    gol1.setUsuarioAsistente(usuario);
    gol1.setPartido(partido);

    GolPartido gol2 = new GolPartido();
    gol2.setUsuarioGoleador(goleador);
    gol2.setUsuarioAsistente(usuario2);
    gol2.setPartido(partido);

    when(repositorioGolPartido.listarPorPartido(10L)).thenReturn(List.of(gol1, gol2));

    when(repositorioPartido.listarPartidosDe(1L)).thenReturn(List.of(partido));

    EstadisticasJugador estadisticas = servicio.obtenerEstadisticasDelUsuario(1L);

    assertEquals(1, estadisticas.getAsistencias().intValue());
    assertEquals(0, estadisticas.getGoles().intValue());
    verify(repositorioGolPartido).listarPorPartido(10L);
  }

  @Test
  public void seDebenAcumularLosGolesYAsistenciasDelUsuarioEnVariosPartidos() {
    RepositorioPartido repositorioPartido = mock(RepositorioPartido.class);
    RepositorioGolPartido repositorioGolPartido = mock(RepositorioGolPartido.class);
    ServicioEstadisticasJugador servicio = new ServicioEstadisticasJugadorImpl(
      repositorioPartido,
      repositorioGolPartido,
      mock(RepositorioTarjetaPartido.class)
    );

    Usuario usuario = new Usuario();
    usuario.setId(1L);
    Usuario usuario2 = new Usuario();
    usuario2.setId(2L);

    Partido partido1 = new Partido();
    partido1.setId(10L);
    partido1.registrarResultado(2, 0);

    Partido partido2 = new Partido();
    partido2.setId(11L);
    partido2.registrarResultado(1, 0);

    GolPartido gol1 = new GolPartido();
    gol1.setUsuarioGoleador(usuario);
    gol1.setUsuarioAsistente(usuario2);
    gol1.setPartido(partido1);

    GolPartido gol2 = new GolPartido();
    gol2.setUsuarioGoleador(usuario);
    gol2.setUsuarioAsistente(usuario2);
    gol2.setPartido(partido1);

    GolPartido gol3 = new GolPartido();
    gol3.setUsuarioGoleador(usuario);
    gol3.setPartido(partido2);

    GolPartido gol4 = new GolPartido();
    gol4.setUsuarioGoleador(usuario2);
    gol4.setUsuarioAsistente(usuario);
    gol4.setPartido(partido2);

    when(repositorioGolPartido.listarPorPartido(10L)).thenReturn(List.of(gol1, gol2));

    when(repositorioGolPartido.listarPorPartido(11L)).thenReturn(List.of(gol3, gol4));

    when(repositorioPartido.listarPartidosDe(1L)).thenReturn(List.of(partido1, partido2));

    EstadisticasJugador estadisticas = servicio.obtenerEstadisticasDelUsuario(1L);

    assertEquals(1, estadisticas.getAsistencias().intValue());
    assertEquals(3, estadisticas.getGoles().intValue());
    assertEquals(2, estadisticas.getPartidosJugados().intValue());
    assertEquals(1.50, estadisticas.getPromedioGolesPorPartido(), 0.01);
    assertEquals(0.50, estadisticas.getPromedioAsistenciasPorPartido(), 0.01);
    verify(repositorioGolPartido).listarPorPartido(10L);
    verify(repositorioGolPartido).listarPorPartido(11L);
  }

  @Test
  public void seDebenContarVictoriasEmpatesYDerrotasDelUsuario() {
    RepositorioPartido repositorioPartido = mock(RepositorioPartido.class);
    RepositorioGolPartido repositorioGolPartido = mock(RepositorioGolPartido.class);
    ServicioEstadisticasJugador servicio = new ServicioEstadisticasJugadorImpl(
      repositorioPartido,
      repositorioGolPartido,
      mock(RepositorioTarjetaPartido.class)
    );

    Usuario usuario = new Usuario();
    usuario.setId(1L);
    Usuario usuario2 = new Usuario();
    usuario2.setId(2L);

    Partido partidoGanado = new Partido();
    partidoGanado.getEquipoA().add(usuario);
    partidoGanado.getEquipoB().add(usuario2);
    partidoGanado.setId(10L);
    partidoGanado.registrarResultado(2, 0);

    Partido partidoGanado2 = new Partido();
    partidoGanado2.getEquipoA().add(usuario);
    partidoGanado2.getEquipoB().add(usuario2);
    partidoGanado2.setId(11L);
    partidoGanado2.registrarResultado(2, 1);

    Partido partidoEmpatado = new Partido();
    partidoEmpatado.getEquipoA().add(usuario);
    partidoEmpatado.getEquipoB().add(usuario2);
    partidoEmpatado.setId(12L);
    partidoEmpatado.registrarResultado(1, 1);

    when(repositorioPartido.listarPartidosDe(1L))
      .thenReturn(List.of(partidoGanado, partidoGanado2, partidoEmpatado));

    EstadisticasJugador estadisticas = servicio.obtenerEstadisticasDelUsuario(1L);

    assertEquals(3, estadisticas.getPartidosJugados().intValue());
    assertEquals(2, estadisticas.getPartidosGanados().intValue());
    assertEquals(1, estadisticas.getPartidosEmpatados().intValue());
    assertEquals(0, estadisticas.getPartidosPerdidos().intValue());
  }

  @Test
  public void seDebenContarLasTarjetasAmarillasYRojasDelUsuario() {
    RepositorioPartido repositorioPartido = mock(RepositorioPartido.class);
    RepositorioGolPartido repositorioGolPartido = mock(RepositorioGolPartido.class);
    RepositorioTarjetaPartido repositorioTarjetaPartido = mock(RepositorioTarjetaPartido.class);
    ServicioEstadisticasJugador servicio = new ServicioEstadisticasJugadorImpl(
      repositorioPartido,
      repositorioGolPartido,
      repositorioTarjetaPartido
    );

    Usuario usuario = new Usuario();
    usuario.setId(1L);
    Usuario usuario2 = new Usuario();
    usuario2.setId(2L);

    Partido partido = new Partido();
    partido.setId(10L);
    partido.registrarResultado(2, 1);

    TarjetaPartido amarilla = new TarjetaPartido();
    amarilla.setPartido(partido);
    amarilla.setUsuarioAmonestado(usuario);
    amarilla.setTipo(TipoTarjeta.AMARILLA);

    TarjetaPartido roja = new TarjetaPartido();
    roja.setPartido(partido);
    roja.setUsuarioAmonestado(usuario);
    roja.setTipo(TipoTarjeta.ROJA);

    TarjetaPartido deOtro = new TarjetaPartido();
    deOtro.setPartido(partido);
    deOtro.setUsuarioAmonestado(usuario2);
    deOtro.setTipo(TipoTarjeta.AMARILLA);

    when(repositorioPartido.listarPartidosDe(1L)).thenReturn(List.of(partido));
    when(repositorioTarjetaPartido.listarTarjetasPorPartido(10L))
      .thenReturn(List.of(amarilla, roja, deOtro));

    EstadisticasJugador estadisticas = servicio.obtenerEstadisticasDelUsuario(1L);

    assertEquals(1, estadisticas.getTarjetasAmarillas().intValue());
    assertEquals(1, estadisticas.getTarjetasRojas().intValue());
    verify(repositorioTarjetaPartido).listarTarjetasPorPartido(10L);
  }
}
