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

import com.tallerwebi.dominio.enums.TipoTarjeta;
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
  private RepositorioTarjetaTorneo repoTarjeta;
  private ServicioEstadisticasTorneo servicio;
  private InscripcionTorneo a;
  private InscripcionTorneo b;
  private EncuentroTorneo encuentro;
  private Usuario goleador;
  private Usuario rival;

  @BeforeEach
  public void init() {
    repoGol = mock(RepositorioGolTorneo.class);
    repoEncuentro = mock(RepositorioEncuentroTorneo.class);
    repoInscripcion = mock(RepositorioInscripcionTorneo.class);
    repoFase = mock(RepositorioFaseTorneo.class);
    repoTarjeta = mock(RepositorioTarjetaTorneo.class);
    servicio =
      new ServicioEstadisticasTorneoImpl(
        repoGol,
        repoEncuentro,
        repoInscripcion,
        repoFase,
        repoTarjeta
      );

    goleador = usuario(1L, "goleador");
    rival = usuario(9L, "rival");
    a = inscripcion(1L, "Alfa", equipo(goleador));
    b = inscripcion(2L, "Beta", equipo(rival));
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
  public void registraGolesYTarjetasDelEncuentro() {
    DetallesEncuentro detalles = new DetallesEncuentro(
      List.of(1L),
      null,
      List.of("AMARILLA"),
      List.of(1L),
      List.of(9L, 9L),
      null,
      List.of("ROJA"),
      List.of(9L)
    );

    assertThat(servicio.registrarDetalles(20L, detalles), equalTo(true));

    verify(repoGol).borrarPorEncuentro(20L);
    verify(repoTarjeta).borrarPorEncuentro(20L);
    ArgumentCaptor<GolTorneo> goles = ArgumentCaptor.forClass(GolTorneo.class);
    verify(repoGol, org.mockito.Mockito.times(3)).guardar(goles.capture());
    GolTorneo primero = goles.getAllValues().get(0);
    assertThat(primero.getUsuarioGoleador(), equalTo(goleador));
    assertThat(primero.getGoleador(), equalTo("goleador"));
    assertThat(primero.getInscripcion(), equalTo(a));
    assertThat(primero.getEquipoNombre(), equalTo("Alfa"));
    assertThat(goles.getAllValues().get(2).getInscripcion(), equalTo(b));

    ArgumentCaptor<TarjetaTorneo> tarjetas = ArgumentCaptor.forClass(TarjetaTorneo.class);
    verify(repoTarjeta, org.mockito.Mockito.times(2)).guardar(tarjetas.capture());
    assertThat(tarjetas.getAllValues().get(0).getTipo(), equalTo(TipoTarjeta.AMARILLA));
    assertThat(tarjetas.getAllValues().get(1).getTipo(), equalTo(TipoTarjeta.ROJA));
    assertThat(tarjetas.getAllValues().get(1).getUsuarioAmonestado(), equalTo(rival));
  }

  @Test
  public void noSuperaElMarcadorNiAdmiteEncuentrosInvalidos() {
    DetallesEncuentro dos = new DetallesEncuentro(
      List.of(1L, 1L),
      null,
      null,
      null,
      null,
      null,
      null,
      null
    );
    assertThat(servicio.registrarDetalles(20L, dos), equalTo(false));
    assertThat(servicio.registrarDetalles(99L, dos), equalTo(false));
    assertThat(servicio.registrarDetalles(null, dos), equalTo(false));
    assertThat(servicio.registrarDetalles(20L, null), equalTo(false));
    encuentro.setEstado("PENDIENTE");
    assertThat(servicio.registrarDetalles(20L, dos), equalTo(false));
    verify(repoGol, never()).guardar(any());
  }

  @Test
  public void noRegistraSinAmbosParticipantes() {
    encuentro.setParticipanteB(null);
    DetallesEncuentro detalles = new DetallesEncuentro(
      List.of(1L),
      null,
      null,
      null,
      null,
      null,
      null,
      null
    );
    assertThat(servicio.registrarDetalles(20L, detalles), equalTo(false));
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
  public void tarjetasPorPartidoLasAgrupaPorEncuentro() {
    TarjetaTorneo tarjeta = new TarjetaTorneo();
    tarjeta.setEncuentro(encuentro);
    tarjeta.setInscripcion(a);
    tarjeta.setJugador("goleador");
    tarjeta.setTipo(TipoTarjeta.AMARILLA);
    when(repoTarjeta.listarPorTorneo(7L)).thenReturn(List.of(tarjeta));

    Map<Long, List<TarjetaTorneo>> agrupadas = servicio.tarjetasPorPartido(7L);

    assertThat(agrupadas.get(20L), contains(tarjeta));
    assertThat(servicio.tarjetasPorPartido(null).isEmpty(), equalTo(true));
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
  public void plantelesPorInscripcion() {
    when(repoInscripcion.listarPorTorneo(7L)).thenReturn(List.of(a, b));

    Map<Long, List<Usuario>> planteles = servicio.plantelesPorInscripcion(7L);

    assertThat(planteles.get(1L), hasSize(1));
    assertThat(planteles.get(1L).get(0).getId(), equalTo(1L));
    assertThat(planteles.get(2L), hasSize(1));
    assertThat(servicio.plantelesPorInscripcion(null).isEmpty(), equalTo(true));
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

  private static InscripcionTorneo inscripcion(Long id, String nombre, Equipo equipo) {
    InscripcionTorneo insc = new InscripcionTorneo();
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
