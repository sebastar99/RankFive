package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tallerwebi.dominio.implementacion.ServicioPartidoImpl;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioPartidoTest {

  private ServicioPartido servicioPartido;
  private RepositorioPartido repositorioPartidoMock;
  private RepositorioUsuario repositorioUsuarioMock;

  @BeforeEach
  public void init() {
    this.repositorioPartidoMock = mock(RepositorioPartido.class);
    this.repositorioUsuarioMock = mock(RepositorioUsuario.class);
    this.servicioPartido = new ServicioPartidoImpl(repositorioPartidoMock, repositorioUsuarioMock);
  }

  private Usuario usuarioConPl(Long id, Integer pl) {
    Usuario usuario = new Usuario();
    usuario.setId(id);
    PerfilJugador perfil = new PerfilJugador();
    perfil.setPl(pl);
    usuario.setPerfil(perfil);
    return usuario;
  }

  private Partido partidoConEquipos(Long id, List<Usuario> equipoA, List<Usuario> equipoB) {
    Partido partido = new Partido();
    partido.setId(id);
    partido.setEquipoA(new HashSet<>(equipoA));
    partido.setEquipoB(new HashSet<>(equipoB));
    return partido;
  }

  @Test
  public void registrarResultadoDePartidoInexistenteDeberiaRetornarFalse() {
    when(repositorioPartidoMock.buscarPorId(1L)).thenReturn(null);

    boolean resultado = servicioPartido.registrarResultado(1L, 2, 0);

    assertThat(resultado, is(false));
    verify(repositorioPartidoMock, never()).actualizar(any(Partido.class));
    verify(repositorioUsuarioMock, never()).modificar(any(Usuario.class));
  }

  @Test
  public void registrarResultadoDePartidoYaJugadoDeberiaRetornarFalse() {
    Usuario u1 = usuarioConPl(1L, 1000);
    Usuario u2 = usuarioConPl(2L, 1000);
    Partido partido = partidoConEquipos(1L, List.of(u1), List.of(u2));
    partido.registrarResultado(3, 1);
    when(repositorioPartidoMock.buscarPorId(1L)).thenReturn(partido);

    boolean resultado = servicioPartido.registrarResultado(1L, 2, 0);

    assertThat(resultado, is(false));
    verify(repositorioPartidoMock, never()).actualizar(any(Partido.class));
    verify(repositorioUsuarioMock, never()).modificar(any(Usuario.class));
  }

  @Test
  public void registrarResultadoConGolesNegativosDeberiaRetornarFalse() {
    Usuario u1 = usuarioConPl(1L, 1000);
    Usuario u2 = usuarioConPl(2L, 1000);
    Partido partido = partidoConEquipos(1L, List.of(u1), List.of(u2));
    when(repositorioPartidoMock.buscarPorId(1L)).thenReturn(partido);

    boolean resultado = servicioPartido.registrarResultado(1L, -1, 2);

    assertThat(resultado, is(false));
    verify(repositorioPartidoMock, never()).actualizar(any(Partido.class));
    verify(repositorioUsuarioMock, never()).modificar(any(Usuario.class));
  }

  @Test
  public void crearPartidoConEquipoVacioDeberiaRetornarNull() {
    Usuario u1 = usuarioConPl(1L, 1000);

    Partido resultado = servicioPartido.crearPartido(List.of(), List.of(u1));

    assertThat(resultado, is(nullValue()));
    verify(repositorioPartidoMock, never()).guardar(any(Partido.class));
  }

  @Test
  public void crearPartidoConJugadorRepetidoEnAmbosEquiposDeberiaRetornarNull() {
    Usuario u1 = usuarioConPl(1L, 1000);

    Partido resultado = servicioPartido.crearPartido(List.of(u1), List.of(u1));

    assertThat(resultado, is(nullValue()));
    verify(repositorioPartidoMock, never()).guardar(any(Partido.class));
  }

  @Test
  public void crearPartidoValidoDeberiaPersistirloYDevolverlo() {
    Usuario u1 = usuarioConPl(1L, 1000);
    Usuario u2 = usuarioConPl(2L, 1000);

    Partido resultado = servicioPartido.crearPartido(List.of(u1), List.of(u2));

    assertThat(resultado, is(notNullValue()));
    assertThat(resultado.getEquipoA(), equalTo(Set.of(u1)));
    assertThat(resultado.getEquipoB(), equalTo(Set.of(u2)));
    verify(repositorioPartidoMock, times(1)).guardar(resultado);
  }

  @Test
  public void registrarResultadoEnEquiposParejosDeberiaRepartir16PuntosDeEloPorLaVictoria() {
    Usuario u1 = usuarioConPl(1L, 1000);
    Usuario u2 = usuarioConPl(2L, 1000);
    Partido partido = partidoConEquipos(1L, List.of(u1), List.of(u2));
    when(repositorioPartidoMock.buscarPorId(1L)).thenReturn(partido);

    boolean resultado = servicioPartido.registrarResultado(1L, 3, 1);

    assertThat(resultado, is(true));
    assertThat(partido.fueJugado(), is(true));
    assertThat(u1.getPerfil().getPl(), equalTo(1016));
    assertThat(u2.getPerfil().getPl(), equalTo(984));
    verify(repositorioPartidoMock, times(1)).actualizar(partido);
    verify(repositorioUsuarioMock, times(1)).modificar(u1);
    verify(repositorioUsuarioMock, times(1)).modificar(u2);
  }

  @Test
  public void registrarResultadoConEquiposDesparejosDeberiaRepartirMenosPuntosAlFavoritoQueGana() {
    Usuario u1 = usuarioConPl(1L, 1400);
    Usuario u2 = usuarioConPl(2L, 1000);
    Partido partido = partidoConEquipos(1L, List.of(u1), List.of(u2));
    when(repositorioPartidoMock.buscarPorId(1L)).thenReturn(partido);

    boolean resultado = servicioPartido.registrarResultado(1L, 2, 0);

    assertThat(resultado, is(true));
    assertThat(u1.getPerfil().getPl(), equalTo(1403));
    assertThat(u2.getPerfil().getPl(), equalTo(997));
  }

  @Test
  public void registrarResultadoEmpatadoDeberiaAjustarHaciaElEmpateEsperado() {
    Usuario uA1 = usuarioConPl(1L, 1000);
    Usuario uA2 = usuarioConPl(2L, 1400);
    Usuario uB1 = usuarioConPl(3L, 1000);
    Usuario uB2 = usuarioConPl(4L, 1000);
    Partido partido = partidoConEquipos(1L, List.of(uA1, uA2), List.of(uB1, uB2));
    when(repositorioPartidoMock.buscarPorId(1L)).thenReturn(partido);

    boolean resultado = servicioPartido.registrarResultado(1L, 2, 2);

    assertThat(resultado, is(true));
    assertThat(uA1.getPerfil().getPl(), equalTo(992));
    assertThat(uA2.getPerfil().getPl(), equalTo(1392));
    assertThat(uB1.getPerfil().getPl(), equalTo(1008));
    assertThat(uB2.getPerfil().getPl(), equalTo(1008));
  }

  @Test
  public void listarPartidosDeUsuarioNuloDeberiaRetornarListaVacia() {
    List<Partido> partidos = servicioPartido.listarPartidosDe(null);
    assertThat(partidos.isEmpty(), is(true));
  }

  @Test
  public void listarPartidosDeUsuarioSinIdDeberiaRetornarListaVacia() {
    List<Partido> partidos = servicioPartido.listarPartidosDe(new Usuario());
    assertThat(partidos.isEmpty(), is(true));
  }

  @Test
  public void buscarPorEmailDeberiaDelegarEnElRepositorioDeUsuario() {
    Usuario usuario = usuarioConPl(1L, 1000);
    when(repositorioUsuarioMock.buscar("user01@test.com")).thenReturn(usuario);

    Usuario resultado = servicioPartido.buscarPorEmail("user01@test.com");

    assertThat(resultado, equalTo(usuario));
  }

  @Test
  public void listarPartidosDeDeberiaDelegarEnElRepositorio() {
    Usuario usuario = usuarioConPl(1L, 1000);
    Partido partido = partidoConEquipos(5L, List.of(usuario), List.of(usuarioConPl(2L, 1000)));
    when(repositorioPartidoMock.listarPartidosDe(1L)).thenReturn(List.of(partido));

    List<Partido> partidos = servicioPartido.listarPartidosDe(usuario);

    assertThat(partidos, equalTo(List.of(partido)));
  }
}
