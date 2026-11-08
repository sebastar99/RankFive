package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.tallerwebi.dominio.enums.TipoTarjeta;
import com.tallerwebi.dominio.implementacion.AporteEstadisticasOficiales;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AporteEstadisticasOficialesTest {

  private RepositorioPartidoLiga repoPartidoLiga;
  private RepositorioEncuentroTorneo repoEncuentro;
  private RepositorioGolLiga repoGolLiga;
  private RepositorioGolTorneo repoGolTorneo;
  private RepositorioTarjetaLiga repoTarjetaLiga;
  private RepositorioTarjetaTorneo repoTarjetaTorneo;
  private AporteEstadisticasOficiales aporte;
  private Usuario jugador;

  @BeforeEach
  public void init() {
    repoPartidoLiga = mock(RepositorioPartidoLiga.class);
    repoEncuentro = mock(RepositorioEncuentroTorneo.class);
    repoGolLiga = mock(RepositorioGolLiga.class);
    repoGolTorneo = mock(RepositorioGolTorneo.class);
    repoTarjetaLiga = mock(RepositorioTarjetaLiga.class);
    repoTarjetaTorneo = mock(RepositorioTarjetaTorneo.class);
    aporte =
      new AporteEstadisticasOficiales(
        repoPartidoLiga,
        repoEncuentro,
        repoGolLiga,
        repoGolTorneo,
        repoTarjetaLiga,
        repoTarjetaTorneo
      );
    jugador = usuario(1L);
  }

  @Test
  public void sumaGolesAsistenciasYTarjetasDeLigaYTorneo() {
    when(repoGolLiga.contarGolesDe(1L)).thenReturn(2L);
    when(repoGolTorneo.contarGolesDe(1L)).thenReturn(1L);
    when(repoGolLiga.contarAsistenciasDe(1L)).thenReturn(1L);
    when(repoGolTorneo.contarAsistenciasDe(1L)).thenReturn(2L);
    when(repoTarjetaLiga.contarDe(1L, TipoTarjeta.AMARILLA)).thenReturn(1L);
    when(repoTarjetaTorneo.contarDe(1L, TipoTarjeta.AMARILLA)).thenReturn(1L);
    when(repoTarjetaTorneo.contarDe(1L, TipoTarjeta.ROJA)).thenReturn(1L);

    EstadisticasJugador estadisticas = new EstadisticasJugador();
    aporte.sumar(1L, estadisticas);

    assertThat(estadisticas.getGoles(), equalTo(3));
    assertThat(estadisticas.getAsistencias(), equalTo(3));
    assertThat(estadisticas.getTarjetasAmarillas(), equalTo(2));
    assertThat(estadisticas.getTarjetasRojas(), equalTo(1));
  }

  @Test
  public void sumaPartidosJugadosGanadosEmpatadosYPerdidos() {
    Equipo mio = equipo(jugador);
    Equipo otro = equipo(usuario(2L));
    PartidoLiga ganado = partidoLiga(inscripcionLiga(mio), inscripcionLiga(otro), 3, 1);
    PartidoLiga empatado = partidoLiga(inscripcionLiga(otro), inscripcionLiga(mio), 2, 2);
    PartidoLiga ajeno = partidoLiga(inscripcionLiga(otro), inscripcionLiga(otro), 5, 0);
    when(repoPartidoLiga.listarJugados()).thenReturn(List.of(ganado, empatado, ajeno));

    EncuentroTorneo perdido = encuentro(inscripcionTorneo(otro), inscripcionTorneo(mio), 2, 0);
    when(repoEncuentro.listarJugados()).thenReturn(List.of(perdido));

    EstadisticasJugador estadisticas = new EstadisticasJugador();
    aporte.sumar(1L, estadisticas);

    assertThat(estadisticas.getPartidosJugados(), equalTo(3));
    assertThat(estadisticas.getPartidosGanados(), equalTo(1));
    assertThat(estadisticas.getPartidosEmpatados(), equalTo(1));
    assertThat(estadisticas.getPartidosPerdidos(), equalTo(1));
  }

  @Test
  public void acumulaSobreEstadisticasExistentesYNoRompeConUsuarioNulo() {
    EstadisticasJugador estadisticas = new EstadisticasJugador();
    estadisticas.setGoles(5);
    when(repoGolLiga.contarGolesDe(1L)).thenReturn(2L);

    aporte.sumar(1L, estadisticas);
    aporte.sumar(null, estadisticas);

    assertThat(estadisticas.getGoles(), equalTo(7));
  }

  private static PartidoLiga partidoLiga(
    InscripcionLiga local,
    InscripcionLiga visitante,
    int golesLocal,
    int golesVisitante
  ) {
    PartidoLiga partido = new PartidoLiga();
    partido.setLocal(local);
    partido.setVisitante(visitante);
    partido.setGolesLocal(golesLocal);
    partido.setGolesVisitante(golesVisitante);
    partido.setEstado(PartidoLiga.JUGADO);
    return partido;
  }

  private static EncuentroTorneo encuentro(
    InscripcionTorneo a,
    InscripcionTorneo b,
    int golesA,
    int golesB
  ) {
    EncuentroTorneo encuentro = new EncuentroTorneo();
    encuentro.setParticipanteA(a);
    encuentro.setParticipanteB(b);
    encuentro.setGolesA(golesA);
    encuentro.setGolesB(golesB);
    encuentro.setEstado("JUGADO");
    return encuentro;
  }

  private static InscripcionLiga inscripcionLiga(Equipo equipo) {
    InscripcionLiga inscripcion = new InscripcionLiga();
    inscripcion.setEquipo(equipo);
    return inscripcion;
  }

  private static InscripcionTorneo inscripcionTorneo(Equipo equipo) {
    InscripcionTorneo inscripcion = new InscripcionTorneo();
    inscripcion.setEquipo(equipo);
    return inscripcion;
  }

  private static Equipo equipo(Usuario... jugadores) {
    Equipo equipo = new Equipo();
    equipo.setOwner(jugadores[0]);
    for (Usuario u : jugadores) {
      equipo.agregarJugador(u);
    }
    return equipo;
  }

  private static Usuario usuario(Long id) {
    Usuario usuario = new Usuario();
    usuario.setId(id);
    usuario.setEmail("u" + id + "@mail.com");
    return usuario;
  }
}
