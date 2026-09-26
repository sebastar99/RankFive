package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tallerwebi.dominio.implementacion.ServicioArbitroImpl;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioArbitroTest {

  private RepositorioArbitro repoArbitro;
  private RepositorioEncuentroTorneo repoEncuentro;
  private ServicioArbitroImpl servicio;
  private Arbitro bueno;
  private Arbitro regular;

  @BeforeEach
  public void init() {
    repoArbitro = mock(RepositorioArbitro.class);
    repoEncuentro = mock(RepositorioEncuentroTorneo.class);
    servicio = new ServicioArbitroImpl(repoArbitro, repoEncuentro);
    bueno = arbitro("Bueno", 2000, 45, 10);
    regular = arbitro("Regular", 3000, 30, 10);
    when(repoArbitro.listarTodos()).thenReturn(new ArrayList<>(List.of(regular, bueno)));
  }

  @Test
  public void rankingOrdenaPorPromedioYLuegoPorPl() {
    Arbitro empatado = arbitro("Empatado", 2500, 45, 10);
    when(repoArbitro.listarTodos()).thenReturn(new ArrayList<>(List.of(regular, bueno, empatado)));

    List<Arbitro> ranking = servicio.listarRanking();

    assertThat(ranking.get(0), equalTo(empatado));
    assertThat(ranking.get(1), equalTo(bueno));
    assertThat(ranking.get(2), equalTo(regular));
  }

  @Test
  public void designaArbitrosSoloAPartidosPendientesEnOrdenDeRanking() {
    EncuentroTorneo primero = encuentro("PENDIENTE");
    EncuentroTorneo bye = encuentro("BYE");
    EncuentroTorneo segundo = encuentro("PENDIENTE");

    servicio.designar(List.of(primero, bye, segundo));

    assertThat(primero.getArbitro(), equalTo(bueno));
    assertThat(bye.getArbitro(), nullValue());
    assertThat(segundo.getArbitro(), equalTo(regular));
  }

  @Test
  public void jugadorQueParticipoPuedeCalificarUnaVez() {
    Usuario jugador = usuario(5L);
    EncuentroTorneo jugado = encuentroJugadoCon(jugador);
    when(repoEncuentro.buscarPorId(1L)).thenReturn(jugado);

    boolean ok = servicio.calificar(jugador, 1L, 5);

    assertThat(ok, is(true));
    assertThat(bueno.getCantidadCalificaciones(), equalTo(11));
    verify(repoArbitro).guardarCalificacion(any(CalificacionArbitro.class));
  }

  @Test
  public void noPuedeCalificarSiYaVotoOSiNoParticipo() {
    Usuario jugador = usuario(5L);
    EncuentroTorneo jugado = encuentroJugadoCon(jugador);
    when(repoEncuentro.buscarPorId(1L)).thenReturn(jugado);
    when(repoArbitro.existeCalificacion(5L, 1L)).thenReturn(true);

    assertThat(servicio.calificar(jugador, 1L, 4), is(false));
    assertThat(servicio.calificar(usuario(99L), 1L, 4), is(false));
    assertThat(servicio.calificar(jugador, 1L, 9), is(false));
    verify(repoArbitro, never()).guardarCalificacion(any(CalificacionArbitro.class));
  }

  @Test
  public void promedioDeArbitroSinCalificacionesEsCero() {
    Arbitro nuevo = new Arbitro();
    assertThat(nuevo.getPromedio(), equalTo(0.0));
    nuevo.calificar(4);
    nuevo.calificar(0);
    assertThat(nuevo.getPromedio(), equalTo(4.0));
  }

  private EncuentroTorneo encuentroJugadoCon(Usuario jugador) {
    Equipo equipo = new Equipo();
    equipo.agregarJugador(jugador);
    InscripcionTorneo insc = new InscripcionTorneo();
    insc.setUsuario(usuario(1L));
    insc.setEquipo(equipo);
    EncuentroTorneo encuentro = encuentro("JUGADO");
    encuentro.setId(1L);
    encuentro.setParticipanteA(insc);
    encuentro.setArbitro(bueno);
    return encuentro;
  }

  private static EncuentroTorneo encuentro(String estado) {
    EncuentroTorneo encuentro = new EncuentroTorneo();
    encuentro.setEstado(estado);
    return encuentro;
  }

  private static Usuario usuario(Long id) {
    Usuario usuario = new Usuario();
    usuario.setId(id);
    return usuario;
  }

  private static Arbitro arbitro(String nombre, int pl, int total, int cantidad) {
    Arbitro arbitro = new Arbitro();
    arbitro.setNombre(nombre);
    arbitro.setPl(pl);
    arbitro.setPuntajeTotal(total);
    arbitro.setCantidadCalificaciones(cantidad);
    return arbitro;
  }
}
