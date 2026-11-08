package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tallerwebi.dominio.enums.TipoTarjeta;
import com.tallerwebi.dominio.implementacion.ServicioEstadisticasLigaImpl;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

public class ServicioEstadisticasLigaTest {

  private RepositorioGolLiga repoGol;
  private RepositorioPartidoLiga repoPartido;
  private RepositorioInscripcionLiga repoInscripcion;
  private RepositorioTarjetaLiga repoTarjeta;
  private ServicioEstadisticasLiga servicio;
  private InscripcionLiga local;
  private InscripcionLiga visitante;
  private PartidoLiga partido;
  private Usuario goleador;
  private Usuario asistidor;
  private Usuario rival;

  @BeforeEach
  public void init() {
    repoGol = mock(RepositorioGolLiga.class);
    repoPartido = mock(RepositorioPartidoLiga.class);
    repoInscripcion = mock(RepositorioInscripcionLiga.class);
    repoTarjeta = mock(RepositorioTarjetaLiga.class);
    servicio = new ServicioEstadisticasLigaImpl(repoGol, repoPartido, repoInscripcion, repoTarjeta);

    goleador = usuario(1L, "delantero");
    asistidor = usuario(2L, "enganche");
    rival = usuario(9L, "rival");
    local = inscripcion(1L, "Local FC", equipo(goleador, asistidor));
    visitante = inscripcion(2L, "Visitante FC", equipo(rival));
    partido = new PartidoLiga();
    partido.setId(10L);
    partido.setLocal(local);
    partido.setVisitante(visitante);
    partido.setGolesLocal(2);
    partido.setGolesVisitante(1);
    partido.setEstado(PartidoLiga.JUGADO);
    when(repoPartido.buscarPorId(10L)).thenReturn(partido);
  }

  @Test
  public void registraGolesYTarjetasReemplazandoLasAnteriores() {
    DetallesEncuentro detalles = new DetallesEncuentro(
      List.of(1L, 1L),
      java.util.Arrays.asList(2L, null),
      List.of("ROJA"),
      List.of(1L),
      List.of(9L),
      List.of(),
      List.of("AMARILLA"),
      List.of(9L)
    );

    assertThat(servicio.registrarDetalles(10L, detalles), equalTo(true));

    verify(repoGol).borrarPorPartido(10L);
    verify(repoTarjeta).borrarPorPartido(10L);
    ArgumentCaptor<GolLiga> goles = ArgumentCaptor.forClass(GolLiga.class);
    verify(repoGol, times(3)).guardar(goles.capture());
    GolLiga primero = goles.getAllValues().get(0);
    assertThat(primero.getUsuarioGoleador(), equalTo(goleador));
    assertThat(primero.getUsuarioAsistente(), equalTo(asistidor));
    assertThat(primero.getGoleador(), equalTo("delantero"));
    assertThat(primero.getInscripcion(), equalTo(local));
    assertThat(goles.getAllValues().get(1).getUsuarioAsistente(), nullValue());
    assertThat(goles.getAllValues().get(2).getInscripcion(), equalTo(visitante));

    ArgumentCaptor<TarjetaLiga> tarjetas = ArgumentCaptor.forClass(TarjetaLiga.class);
    verify(repoTarjeta, times(2)).guardar(tarjetas.capture());
    assertThat(tarjetas.getAllValues().get(0).getTipo(), equalTo(TipoTarjeta.ROJA));
    assertThat(tarjetas.getAllValues().get(0).getInscripcion(), equalTo(local));
    assertThat(tarjetas.getAllValues().get(1).getTipo(), equalTo(TipoTarjeta.AMARILLA));
    assertThat(tarjetas.getAllValues().get(1).getUsuarioAmonestado(), equalTo(rival));
  }

  @Test
  public void noRegistraSiSuperaLosGolesDelMarcador() {
    DetallesEncuentro detalles = new DetallesEncuentro(
      List.of(1L, 1L, 1L),
      null,
      null,
      null,
      null,
      null,
      null,
      null
    );

    assertThat(servicio.registrarDetalles(10L, detalles), equalTo(false));
    verify(repoGol, never()).guardar(any());
    verify(repoGol, never()).borrarPorPartido(any());
  }

  @Test
  public void ignoraJugadoresQueNoSonDelEquipo() {
    DetallesEncuentro detalles = new DetallesEncuentro(
      List.of(1L, 9L),
      null,
      null,
      null,
      null,
      null,
      null,
      null
    );

    assertThat(servicio.registrarDetalles(10L, detalles), equalTo(true));
    ArgumentCaptor<GolLiga> goles = ArgumentCaptor.forClass(GolLiga.class);
    verify(repoGol).guardar(goles.capture());
    assertThat(goles.getValue().getUsuarioGoleador(), equalTo(goleador));
  }

  @Test
  public void noRegistraSiElPartidoNoFueJugadoOEsInexistente() {
    partido.setEstado(PartidoLiga.PENDIENTE);
    assertThat(servicio.registrarDetalles(10L, detallesUnGol()), equalTo(false));
    assertThat(servicio.registrarDetalles(99L, detallesUnGol()), equalTo(false));
    assertThat(servicio.registrarDetalles(null, detallesUnGol()), equalTo(false));
    assertThat(servicio.registrarDetalles(10L, null), equalTo(false));
    verify(repoGol, never()).guardar(any());
  }

  @Test
  public void laAsistenciaNoPuedeSerDelMismoGoleador() {
    DetallesEncuentro detalles = new DetallesEncuentro(
      List.of(1L),
      List.of(1L),
      null,
      null,
      null,
      null,
      null,
      null
    );

    assertThat(servicio.registrarDetalles(10L, detalles), equalTo(true));
    ArgumentCaptor<GolLiga> goles = ArgumentCaptor.forClass(GolLiga.class);
    verify(repoGol).guardar(goles.capture());
    assertThat(goles.getValue().getUsuarioAsistente(), nullValue());
    assertThat(goles.getValue().getAsistidor(), nullValue());
  }

  @Test
  public void rankingDeGoleadoresYAsistenciasOrdenadoPorCantidad() {
    when(repoGol.listarPorLiga(5L))
      .thenReturn(
        List.of(
          gol(local, "Messi", "Di María"),
          gol(local, "Messi", null),
          gol(visitante, "Alvarez", "Messi")
        )
      );

    List<FilaEstadistica> goleadores = servicio.goleadores(5L);
    List<FilaEstadistica> asistencias = servicio.asistencias(5L);

    assertThat(goleadores, hasSize(2));
    assertThat(goleadores.get(0).getJugador(), equalTo("Messi"));
    assertThat(goleadores.get(0).getCantidad(), equalTo(2));
    assertThat(goleadores.get(0).getEquipo(), equalTo("Local FC"));
    assertThat(asistencias, hasSize(2));
    assertThat(asistencias.get(0).getCantidad(), equalTo(1));
  }

  @Test
  public void rankingSinLigaEsVacio() {
    assertThat(servicio.goleadores(null), empty());
  }

  @Test
  public void agrupaGolesYTarjetasPorPartido() {
    GolLiga gol = gol(local, "Messi", null);
    when(repoGol.listarPorLiga(5L)).thenReturn(List.of(gol));
    TarjetaLiga tarjeta = new TarjetaLiga();
    tarjeta.setPartido(partido);
    tarjeta.setInscripcion(local);
    tarjeta.setJugador("Messi");
    tarjeta.setTipo(TipoTarjeta.AMARILLA);
    when(repoTarjeta.listarPorLiga(5L)).thenReturn(List.of(tarjeta));

    assertThat(servicio.golesPorPartido(5L).get(10L), contains(gol));
    assertThat(servicio.tarjetasPorPartido(5L).get(10L), contains(tarjeta));
    assertThat(servicio.tarjetasPorPartido(null).isEmpty(), equalTo(true));
  }

  @Test
  public void listaPlantelesPorInscripcion() {
    when(repoInscripcion.listarPorLiga(5L)).thenReturn(List.of(local, visitante));

    Map<Long, List<Usuario>> planteles = servicio.plantelesPorInscripcion(5L);

    assertThat(planteles.get(1L), hasSize(2));
    assertThat(planteles.get(1L), org.hamcrest.Matchers.containsInAnyOrder(goleador, asistidor));
    assertThat(planteles.get(2L), hasSize(1));
    assertThat(servicio.plantelesPorInscripcion(null).isEmpty(), equalTo(true));
  }

  private DetallesEncuentro detallesUnGol() {
    return new DetallesEncuentro(List.of(1L), null, null, null, null, null, null, null);
  }

  private GolLiga gol(InscripcionLiga equipo, String goleador, String asistidor) {
    GolLiga gol = new GolLiga();
    gol.setPartido(partido);
    gol.setInscripcion(equipo);
    gol.setGoleador(goleador);
    gol.setAsistidor(asistidor);
    return gol;
  }

  private static InscripcionLiga inscripcion(Long id, String nombre, Equipo equipo) {
    InscripcionLiga insc = new InscripcionLiga();
    insc.setId(id);
    insc.setNombreEquipo(nombre);
    equipo.setNombre(nombre);
    insc.setEquipo(equipo);
    return insc;
  }

  private static Equipo equipo(Usuario... jugadores) {
    Equipo equipo = new Equipo();
    equipo.setOwner(jugadores[0]);
    for (Usuario jugador : jugadores) {
      equipo.agregarJugador(jugador);
    }
    return equipo;
  }

  private static Usuario usuario(Long id, String nombre) {
    Usuario usuario = new Usuario();
    usuario.setId(id);
    usuario.getPerfil().setNombreUsuario(nombre);
    usuario.setEmail(nombre + "@mail.com");
    return usuario;
  }
}
