package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.tallerwebi.dominio.implementacion.ServicioRankingEquiposImpl;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioRankingEquiposTest {

  private RepositorioEquipo repositorio;
  private ServicioRankingEquiposImpl servicio;
  private long siguienteId = 1L;

  @BeforeEach
  public void init() {
    repositorio = mock(RepositorioEquipo.class);
    servicio = new ServicioRankingEquiposImpl(repositorio);
  }

  @Test
  public void ordenaPorPlPromedioSinImportarLaCantidadDeIntegrantes() {
    Equipo chico = equipo("Chico", 3000);
    Equipo grande = equipo("Grande", 2000, 2000, 2000, 2000, 2000, 2000, 2000);
    Equipo medio = equipo("Medio", 2500, 2500);
    when(repositorio.listarActivos()).thenReturn(List.of(grande, chico, medio));

    List<PosicionEquipo> ranking = servicio.obtenerRanking(null, 10);

    assertThat(nombres(ranking), contains("Chico", "Medio", "Grande"));
    assertThat(ranking.get(0).posicion(), equalTo(1));
    assertThat(ranking.get(2).posicion(), equalTo(3));
  }

  @Test
  public void desempataPorNombreSinDistinguirMayusculas() {
    when(repositorio.listarActivos())
      .thenReturn(List.of(equipo("beta", 2000), equipo("Alfa", 2000), equipo("Gamma", 2000)));

    assertThat(nombres(servicio.obtenerRanking(null, 10)), contains("Alfa", "beta", "Gamma"));
  }

  @Test
  public void limitaAlTopIndicado() {
    List<Equipo> equipos = new ArrayList<>();
    for (int i = 1; i <= 12; i++) {
      equipos.add(equipo("Equipo " + i, 1000 + i * 10));
    }
    when(repositorio.listarActivos()).thenReturn(equipos);

    List<PosicionEquipo> ranking = servicio.obtenerRanking(null, 10);

    assertThat(ranking, hasSize(10));
    assertThat(ranking.get(0).equipo().getNombre(), equalTo("Equipo 12"));
  }

  @Test
  public void agregaLosEquiposPropiosQueQuedanFueraDelTopConSuPosicionReal() {
    Usuario usuario = usuario(99L);
    List<Equipo> equipos = new ArrayList<>();
    for (int i = 1; i <= 12; i++) {
      equipos.add(equipo("Equipo " + i, 2000 + i * 10));
    }
    Equipo propio = equipo("Propio", 1000);
    propio.agregarJugador(usuario);
    equipos.add(propio);
    when(repositorio.listarActivos()).thenReturn(equipos);

    List<PosicionEquipo> ranking = servicio.obtenerRanking(usuario, 10);

    assertThat(ranking, hasSize(11));
    PosicionEquipo ultimo = ranking.get(10);
    assertThat(ultimo.equipo().getNombre(), equalTo("Propio"));
    assertThat(ultimo.posicion(), equalTo(13));
    assertThat(ultimo.propio(), is(true));
    assertThat(ranking.get(0).propio(), is(false));
  }

  @Test
  public void noDuplicaUnEquipoPropioQueYaEstaEnElTop() {
    Usuario usuario = usuario(99L);
    Equipo propio = equipo("Propio", 3000);
    propio.agregarJugador(usuario);
    when(repositorio.listarActivos()).thenReturn(List.of(propio, equipo("Otro", 1000)));

    List<PosicionEquipo> ranking = servicio.obtenerRanking(usuario, 10);

    assertThat(ranking, hasSize(2));
    assertThat(ranking.get(0).propio(), is(true));
    assertThat(ranking.get(1).propio(), is(false));
  }

  @Test
  public void sinEquiposDevuelveListaVacia() {
    when(repositorio.listarActivos()).thenReturn(Collections.emptyList());

    assertThat(servicio.obtenerRanking(usuario(1L), 10), is(empty()));
  }

  private static List<String> nombres(List<PosicionEquipo> ranking) {
    return ranking.stream().map(p -> p.equipo().getNombre()).toList();
  }

  private static Usuario usuario(Long id) {
    Usuario usuario = new Usuario();
    usuario.setId(id);
    return usuario;
  }

  private Equipo equipo(String nombre, int... pls) {
    Equipo equipo = new Equipo();
    equipo.setId(siguienteId++);
    equipo.setNombre(nombre);
    equipo.setFormato(5);
    equipo.setEstado(EstadoEquipo.ACTIVO);
    for (int pl : pls) {
      Usuario jugador = usuario(1000L + siguienteId++);
      jugador.getPerfil().setPl(pl);
      equipo.agregarJugador(jugador);
    }
    return equipo;
  }
}
