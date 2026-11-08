package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.tallerwebi.dominio.implementacion.ServicioEstadisticasEquipoImpl;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioEstadisticasEquipoTest {

  private ServicioEquipo servicioEquipo;
  private RepositorioPartidoLiga repoPartidoLiga;
  private RepositorioEncuentroTorneo repoEncuentro;
  private RepositorioInscripcionLiga repoInscLiga;
  private RepositorioInscripcionTorneo repoInscTorneo;
  private ServicioEstadisticasEquipoImpl servicio;
  private Usuario usuario;
  private Equipo propio;
  private Equipo rival;

  @BeforeEach
  public void init() {
    servicioEquipo = mock(ServicioEquipo.class);
    repoPartidoLiga = mock(RepositorioPartidoLiga.class);
    repoEncuentro = mock(RepositorioEncuentroTorneo.class);
    repoInscLiga = mock(RepositorioInscripcionLiga.class);
    repoInscTorneo = mock(RepositorioInscripcionTorneo.class);
    servicio =
      new ServicioEstadisticasEquipoImpl(
        servicioEquipo,
        repoPartidoLiga,
        repoEncuentro,
        repoInscLiga,
        repoInscTorneo
      );
    usuario = new Usuario();
    usuario.setId(1L);
    propio = equipo(10L, "Propio");
    rival = equipo(20L, "Rival");
    when(servicioEquipo.listarEquiposActivosDe(usuario)).thenReturn(List.of(propio));
    when(repoPartidoLiga.listarJugados()).thenReturn(Collections.emptyList());
    when(repoEncuentro.listarJugados()).thenReturn(Collections.emptyList());
    when(repoInscLiga.listarTodas()).thenReturn(Collections.emptyList());
    when(repoInscTorneo.listarTodas()).thenReturn(Collections.emptyList());
  }

  @Test
  public void usuarioSinEquiposDevuelveListaVacia() {
    when(servicioEquipo.listarEquiposActivosDe(usuario)).thenReturn(Collections.emptyList());

    assertThat(servicio.estadisticasDelUsuario(usuario), is(empty()));
    assertThat(servicio.estadisticasDelUsuario(null), is(empty()));
  }

  @Test
  public void equipoSinPartidosAparecePeroConCeros() {
    EstadisticaEquipo stats = servicio.estadisticasDelUsuario(usuario).get(0);

    assertThat(stats.getEquipo().getNombre(), equalTo("Propio"));
    assertThat(stats.getJugados(), equalTo(0));
    assertThat(stats.getPuntos(), equalTo(0));
    assertThat(stats.getCompetencias(), equalTo(0));
  }

  @Test
  public void sumaPartidosDeLigaComoLocalYComoVisitante() {
    when(repoPartidoLiga.listarJugados())
      .thenReturn(
        List.of(
          partidoLiga(propio, rival, 3, 1),
          partidoLiga(rival, propio, 2, 2),
          partidoLiga(rival, equipo(30L, "Otro"), 5, 0)
        )
      );

    EstadisticaEquipo stats = servicio.estadisticasDelUsuario(usuario).get(0);

    assertThat(stats.getJugados(), equalTo(2));
    assertThat(stats.getGanados(), equalTo(1));
    assertThat(stats.getEmpatados(), equalTo(1));
    assertThat(stats.getPerdidos(), equalTo(0));
    assertThat(stats.getGolesFavor(), equalTo(5));
    assertThat(stats.getGolesContra(), equalTo(3));
    assertThat(stats.getDiferencia(), equalTo(2));
    assertThat(stats.getPuntos(), equalTo(4));
  }

  @Test
  public void sumaEncuentrosDeTorneoEIgnoraByesYSinResultado() {
    EncuentroTorneo conBye = encuentro(propio, null, 0, 0);
    EncuentroTorneo sinGoles = encuentro(propio, rival, null, null);
    when(repoEncuentro.listarJugados())
      .thenReturn(
        List.of(encuentro(rival, propio, 4, 1), encuentro(propio, rival, 2, 0), conBye, sinGoles)
      );

    EstadisticaEquipo stats = servicio.estadisticasDelUsuario(usuario).get(0);

    assertThat(stats.getJugados(), equalTo(2));
    assertThat(stats.getGanados(), equalTo(1));
    assertThat(stats.getPerdidos(), equalTo(1));
    assertThat(stats.getGolesFavor(), equalTo(3));
    assertThat(stats.getGolesContra(), equalTo(4));
  }

  @Test
  public void mezclaLigaYTorneoYCuentaLasCompeticionesInscriptas() {
    when(repoPartidoLiga.listarJugados()).thenReturn(List.of(partidoLiga(propio, rival, 1, 0)));
    when(repoEncuentro.listarJugados()).thenReturn(List.of(encuentro(propio, rival, 0, 0)));
    when(repoInscLiga.listarTodas()).thenReturn(List.of(inscripcionLiga(propio)));
    when(repoInscTorneo.listarTodas())
      .thenReturn(List.of(inscripcionTorneo(propio), inscripcionTorneo(rival)));

    EstadisticaEquipo stats = servicio.estadisticasDelUsuario(usuario).get(0);

    assertThat(stats.getJugados(), equalTo(2));
    assertThat(stats.getPuntos(), equalTo(4));
    assertThat(stats.getCompetencias(), equalTo(2));
  }

  @Test
  public void ordenaPorPuntosLuegoDiferenciaYNombre() {
    Equipo mejor = equipo(11L, "Mejor");
    Equipo peor = equipo(12L, "Peor");
    when(servicioEquipo.listarEquiposActivosDe(usuario)).thenReturn(List.of(peor, propio, mejor));
    when(repoPartidoLiga.listarJugados())
      .thenReturn(List.of(partidoLiga(mejor, rival, 2, 0), partidoLiga(peor, rival, 0, 3)));

    List<EstadisticaEquipo> lista = servicio.estadisticasDelUsuario(usuario);

    assertThat(
      lista.stream().map(e -> e.getEquipo().getNombre()).toList(),
      contains("Mejor", "Propio", "Peor")
    );
  }

  private static Equipo equipo(Long id, String nombre) {
    Equipo equipo = new Equipo();
    equipo.setId(id);
    equipo.setNombre(nombre);
    equipo.setFormato(5);
    return equipo;
  }

  private static InscripcionLiga inscripcionLiga(Equipo equipo) {
    InscripcionLiga insc = new InscripcionLiga();
    insc.setEquipo(equipo);
    return insc;
  }

  private static InscripcionTorneo inscripcionTorneo(Equipo equipo) {
    InscripcionTorneo insc = new InscripcionTorneo();
    insc.setEquipo(equipo);
    return insc;
  }

  private static PartidoLiga partidoLiga(Equipo local, Equipo visitante, int gl, int gv) {
    PartidoLiga partido = new PartidoLiga();
    partido.setLocal(inscripcionLiga(local));
    partido.setVisitante(inscripcionLiga(visitante));
    partido.setGolesLocal(gl);
    partido.setGolesVisitante(gv);
    partido.setEstado(PartidoLiga.JUGADO);
    return partido;
  }

  private static EncuentroTorneo encuentro(Equipo a, Equipo b, Integer ga, Integer gb) {
    EncuentroTorneo encuentro = new EncuentroTorneo();
    encuentro.setParticipanteA(inscripcionTorneo(a));
    encuentro.setParticipanteB(b == null ? null : inscripcionTorneo(b));
    encuentro.setGolesA(ga);
    encuentro.setGolesB(gb);
    encuentro.setEstado("JUGADO");
    return encuentro;
  }
}
