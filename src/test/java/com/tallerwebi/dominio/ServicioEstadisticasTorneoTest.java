package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tallerwebi.dominio.implementacion.ServicioEstadisticasTorneoImpl;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

public class ServicioEstadisticasTorneoTest {

  private RepositorioGolTorneo repoGol;
  private RepositorioEncuentroTorneo repoEncuentro;
  private RepositorioInscripcionTorneo repoInscripcion;
  private RepositorioFaseTorneo repoFase;
  private ServicioEstadisticasTorneo servicio;
  private InscripcionTorneo a;
  private InscripcionTorneo b;
  private EncuentroTorneo encuentro;

  @BeforeEach
  public void init() {
    repoGol = mock(RepositorioGolTorneo.class);
    repoEncuentro = mock(RepositorioEncuentroTorneo.class);
    repoInscripcion = mock(RepositorioInscripcionTorneo.class);
    repoFase = mock(RepositorioFaseTorneo.class);
    servicio =
      new ServicioEstadisticasTorneoImpl(repoGol, repoEncuentro, repoInscripcion, repoFase);

    a = inscripcion(1L, "Alfa");
    b = inscripcion(2L, "Beta");
    encuentro = new EncuentroTorneo();
    encuentro.setId(20L);
    encuentro.setParticipanteA(a);
    encuentro.setParticipanteB(b);
    encuentro.setGolesA(1);
    encuentro.setGolesB(3);
    encuentro.setEstado("JUGADO");
    when(repoEncuentro.buscarPorId(20L)).thenReturn(encuentro);
  }

  @Test
  public void registraGolDelParticipanteB() {
    assertThat(servicio.registrarGol(20L, 2L, " Julián  Álvarez ", "Enzo"), equalTo(true));

    ArgumentCaptor<GolTorneo> captor = ArgumentCaptor.forClass(GolTorneo.class);
    verify(repoGol).guardar(captor.capture());
    assertThat(captor.getValue().getGoleador(), equalTo("Julián Álvarez"));
    assertThat(captor.getValue().getInscripcion(), equalTo(b));
    assertThat(captor.getValue().getPartidoId(), equalTo(20L));
    assertThat(captor.getValue().getEquipoNombre(), equalTo("Beta"));
  }

  @Test
  public void noSuperaElMarcadorDelParticipanteA() {
    when(repoGol.contarPor(20L, 1L)).thenReturn(1L);
    assertThat(servicio.registrarGol(20L, 1L, "Messi", null), equalTo(false));
    verify(repoGol, never()).guardar(any());
  }

  @Test
  public void rechazaEncuentrosPendientesEquiposAjenosYNombresInvalidos() {
    assertThat(servicio.registrarGol(20L, 99L, "Messi", null), equalTo(false));
    assertThat(servicio.registrarGol(20L, 1L, "Messi", "MESSI"), equalTo(false));
    assertThat(servicio.registrarGol(null, 1L, "Messi", null), equalTo(false));
    encuentro.setEstado("PENDIENTE");
    assertThat(servicio.registrarGol(20L, 1L, "Messi", null), equalTo(false));
    verify(repoGol, never()).guardar(any());
  }

  @Test
  public void rankingsYGolesPorPartido() {
    GolTorneo uno = gol(b, "Alvarez", "Enzo");
    GolTorneo dos = gol(b, "Alvarez", null);
    GolTorneo tres = gol(a, "Messi", "Alvarez");
    when(repoGol.listarPorTorneo(7L)).thenReturn(List.of(uno, dos, tres));

    List<FilaEstadistica> goleadores = servicio.goleadores(7L);
    assertThat(goleadores, hasSize(2));
    assertThat(goleadores.get(0).getJugador(), equalTo("Alvarez"));
    assertThat(goleadores.get(0).getCantidad(), equalTo(2));
    assertThat(servicio.asistencias(7L), hasSize(2));
    assertThat(servicio.golesPorPartido(7L).get(20L), contains(uno, dos, tres));
    assertThat(servicio.goleadores(null), empty());
  }

  @Test
  public void golesPorEquipoSumaSoloEncuentrosJugados() {
    EncuentroTorneo pendiente = new EncuentroTorneo();
    pendiente.setParticipanteA(a);
    pendiente.setParticipanteB(b);
    pendiente.setEstado("PENDIENTE");
    EncuentroTorneo bye = new EncuentroTorneo();
    bye.setParticipanteA(a);
    bye.setEstado("JUGADO");
    FaseTorneo fase = new FaseTorneo();
    fase.setEncuentros(new ArrayList<>(List.of(encuentro, pendiente, bye)));
    when(repoInscripcion.listarPorTorneo(7L)).thenReturn(List.of(a, b));
    when(repoFase.listarPorTorneo(7L)).thenReturn(List.of(fase));

    List<FilaPosicion> tabla = servicio.golesPorEquipo(7L);

    assertThat(tabla.get(0).getNombreEquipo(), equalTo("Beta"));
    assertThat(tabla.get(0).getGolesFavor(), equalTo(3));
    assertThat(tabla.get(0).getDiferencia(), equalTo(2));
    assertThat(tabla.get(1).getGolesContra(), equalTo(3));
    assertThat(tabla.get(1).getJugados(), equalTo(1));
    assertThat(servicio.golesPorEquipo(null), empty());
  }

  @Test
  public void jugadoresPorInscripcion() {
    Usuario capitan = new Usuario();
    capitan.getPerfil().setNombreUsuario("capi");
    Equipo equipo = new Equipo();
    equipo.setOwner(capitan);
    a.setEquipo(equipo);
    when(repoInscripcion.listarPorTorneo(7L)).thenReturn(List.of(a, b));

    Map<Long, List<String>> jugadores = servicio.jugadoresPorInscripcion(7L);

    assertThat(jugadores.get(1L), contains("capi"));
    assertThat(jugadores.get(2L), empty());
    assertThat(servicio.jugadoresPorInscripcion(null).isEmpty(), equalTo(true));
  }

  @Test
  public void normalizarRecortaNombresLargos() {
    String largo = "x".repeat(GolBase.LARGO_NOMBRE + 10);
    assertThat(EstadisticasGoles.normalizar(largo).length(), equalTo(GolBase.LARGO_NOMBRE));
    assertThat(EstadisticasGoles.normalizar("   "), equalTo(null));
  }

  private GolTorneo gol(InscripcionTorneo equipo, String goleador, String asistidor) {
    GolTorneo gol = EstadisticasGoles.completar(new GolTorneo(), goleador, asistidor);
    gol.setEncuentro(encuentro);
    gol.setInscripcion(equipo);
    return gol;
  }

  private static InscripcionTorneo inscripcion(Long id, String nombre) {
    InscripcionTorneo insc = new InscripcionTorneo();
    insc.setId(id);
    insc.setNombreEquipo(nombre);
    return insc;
  }
}
