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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
  private ServicioEstadisticasLiga servicio;
  private InscripcionLiga local;
  private InscripcionLiga visitante;
  private PartidoLiga partido;

  @BeforeEach
  public void init() {
    repoGol = mock(RepositorioGolLiga.class);
    repoPartido = mock(RepositorioPartidoLiga.class);
    repoInscripcion = mock(RepositorioInscripcionLiga.class);
    servicio = new ServicioEstadisticasLigaImpl(repoGol, repoPartido, repoInscripcion);

    local = inscripcion(1L, "Local FC");
    visitante = inscripcion(2L, "Visitante FC");
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
  public void registraGolConAsistenciaNormalizandoNombres() {
    boolean ok = servicio.registrarGol(10L, 1L, "  Lionel   Messi ", " Di María ");

    assertThat(ok, equalTo(true));
    ArgumentCaptor<GolLiga> captor = ArgumentCaptor.forClass(GolLiga.class);
    verify(repoGol).guardar(captor.capture());
    assertThat(captor.getValue().getGoleador(), equalTo("Lionel Messi"));
    assertThat(captor.getValue().getAsistidor(), equalTo("Di María"));
    assertThat(captor.getValue().getInscripcion(), equalTo(local));
  }

  @Test
  public void registraGolSinAsistencia() {
    assertThat(servicio.registrarGol(10L, 2L, "Messi", "  "), equalTo(true));
    ArgumentCaptor<GolLiga> captor = ArgumentCaptor.forClass(GolLiga.class);
    verify(repoGol).guardar(captor.capture());
    assertThat(captor.getValue().getAsistidor(), nullValue());
  }

  @Test
  public void noRegistraSiSuperaLosGolesDelMarcador() {
    when(repoGol.contarPor(10L, 2L)).thenReturn(1L);

    assertThat(servicio.registrarGol(10L, 2L, "Messi", null), equalTo(false));
    verify(repoGol, never()).guardar(any());
  }

  @Test
  public void noRegistraSiElPartidoNoFueJugado() {
    partido.setEstado(PartidoLiga.PENDIENTE);
    assertThat(servicio.registrarGol(10L, 1L, "Messi", null), equalTo(false));
  }

  @Test
  public void noRegistraSiElEquipoNoJuegaElPartido() {
    assertThat(servicio.registrarGol(10L, 99L, "Messi", null), equalTo(false));
  }

  @Test
  public void noRegistraSinGoleadorOConAsistidorIgualAlGoleador() {
    assertThat(servicio.registrarGol(10L, 1L, " ", null), equalTo(false));
    assertThat(servicio.registrarGol(10L, 1L, "Messi", "messi"), equalTo(false));
    assertThat(servicio.registrarGol(null, 1L, "Messi", null), equalTo(false));
    verify(repoGol, never()).guardar(any());
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
  public void agrupaGolesPorPartido() {
    GolLiga gol = gol(local, "Messi", null);
    when(repoGol.listarPorLiga(5L)).thenReturn(List.of(gol));

    Map<Long, List<GolLiga>> goles = servicio.golesPorPartido(5L);

    assertThat(goles.get(10L), contains(gol));
  }

  @Test
  public void listaNombresDeJugadoresPorInscripcion() {
    Usuario owner = usuario("capitan");
    Usuario jugador = usuario("delantero");
    Equipo equipo = new Equipo();
    equipo.setOwner(owner);
    equipo.agregarJugador(owner);
    equipo.agregarJugador(jugador);
    local.setEquipo(equipo);
    when(repoInscripcion.listarPorLiga(5L)).thenReturn(List.of(local, visitante));

    Map<Long, List<String>> jugadores = servicio.jugadoresPorInscripcion(5L);

    assertThat(jugadores.get(1L), hasSize(2));
    assertThat(jugadores.get(1L).get(0), equalTo("capitan"));
    assertThat(jugadores.get(2L), empty());
  }

  private GolLiga gol(InscripcionLiga equipo, String goleador, String asistidor) {
    GolLiga gol = new GolLiga();
    gol.setPartido(partido);
    gol.setInscripcion(equipo);
    gol.setGoleador(goleador);
    gol.setAsistidor(asistidor);
    return gol;
  }

  private static InscripcionLiga inscripcion(Long id, String nombre) {
    InscripcionLiga insc = new InscripcionLiga();
    insc.setId(id);
    insc.setNombreEquipo(nombre);
    return insc;
  }

  private static Usuario usuario(String nombre) {
    Usuario usuario = new Usuario();
    usuario.getPerfil().setNombreUsuario(nombre);
    usuario.setEmail(nombre + "@mail.com");
    return usuario;
  }
}
