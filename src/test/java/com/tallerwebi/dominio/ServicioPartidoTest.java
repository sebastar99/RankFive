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
import org.mockito.ArgumentCaptor;

public class ServicioPartidoTest {

  private ServicioPartido servicioPartido;
  private RepositorioPartido repositorioPartidoMock;
  private RepositorioUsuario repositorioUsuarioMock;
  private RepositorioNotificacionPl repositorioNotificacionPlMock;

  @BeforeEach
  public void init() {
    this.repositorioPartidoMock = mock(RepositorioPartido.class);
    this.repositorioUsuarioMock = mock(RepositorioUsuario.class);
    this.repositorioNotificacionPlMock = mock(RepositorioNotificacionPl.class);
    this.servicioPartido =
      new ServicioPartidoImpl(
        repositorioPartidoMock,
        repositorioUsuarioMock,
        repositorioNotificacionPlMock
      );
  }

  @Test
  public void registrarResultadoConDiferenciaExtremaFavoritoEnEquipoBDeberiaAplicarAjusteMinimo() {
    // Favorito en equipo B con PL muy alto vs rival muy bajo en equipo A
    Usuario rivalA = usuarioConPl(1L, 1100);
    Usuario favoritoB = usuarioConPl(2L, 3800);
    Partido partido = partidoConEquipos(1L, List.of(rivalA), List.of(favoritoB));
    when(repositorioPartidoMock.buscarPorId(1L)).thenReturn(partido);

    boolean ok = servicioPartido.registrarResultado(1L, 0, 4); // gana el favorito (equipo B)

    assertThat(ok, is(true));
    // Ajuste mínimo forzado: favorito +1, rival -1
    assertThat(favoritoB.getPerfil().getPl(), equalTo(3801));
    assertThat(rivalA.getPerfil().getPl(), equalTo(1099));
    verify(repositorioPartidoMock, times(1)).actualizar(partido);
    verify(repositorioUsuarioMock, times(1)).modificar(favoritoB);
    verify(repositorioUsuarioMock, times(1)).modificar(rivalA);
  }

  @Test
  public void registrarResultadoConDiferenciaExtremaDeberiaAplicarAjusteMinimoAlFavorito() {
    // Favorito con PL muy alto vs rival muy bajo
    Usuario favorito = usuarioConPl(1L, 3800);
    Usuario rival = usuarioConPl(2L, 1100);
    Partido partido = partidoConEquipos(1L, List.of(favorito), List.of(rival));
    when(repositorioPartidoMock.buscarPorId(1L)).thenReturn(partido);

    boolean ok = servicioPartido.registrarResultado(1L, 5, 0); // gana el favorito

    assertThat(ok, is(true));
    // Con diferencias extremas el redondeo daría 0; ahora se fuerza +1/-1
    assertThat(favorito.getPerfil().getPl(), equalTo(3801));
    assertThat(rival.getPerfil().getPl(), equalTo(1099));
    verify(repositorioPartidoMock, times(1)).actualizar(partido);
    verify(repositorioUsuarioMock, times(1)).modificar(favorito);
    verify(repositorioUsuarioMock, times(1)).modificar(rival);
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
  public void registrarResultadoDeberiaCrearUnaNotificacionPorCadaJugadorConSuPropioDelta() {
    Usuario u1 = usuarioConPl(1L, 1000);
    Usuario u2 = usuarioConPl(2L, 1000);
    Partido partido = partidoConEquipos(1L, List.of(u1), List.of(u2));
    when(repositorioPartidoMock.buscarPorId(1L)).thenReturn(partido);

    servicioPartido.registrarResultado(1L, 3, 1);

    ArgumentCaptor<NotificacionPl> captor = ArgumentCaptor.forClass(NotificacionPl.class);
    verify(repositorioNotificacionPlMock, times(2)).guardar(captor.capture());
    List<NotificacionPl> notificaciones = captor.getAllValues();
    NotificacionPl deU1 = notificaciones
      .stream()
      .filter(n -> n.getUsuario() == u1)
      .findFirst()
      .orElse(null);
    NotificacionPl deU2 = notificaciones
      .stream()
      .filter(n -> n.getUsuario() == u2)
      .findFirst()
      .orElse(null);
    assertThat(deU1, is(notNullValue()));
    assertThat(deU2, is(notNullValue()));
    assertThat(deU1.getDelta(), equalTo(16));
    assertThat(deU2.getDelta(), equalTo(-16));
    assertThat(deU1.getPartido(), equalTo(partido));
    assertThat(deU1.getLeida(), is(false));
  }

  @Test
  public void registrarResultadoInvalidoNoDeberiaCrearNotificaciones() {
    when(repositorioPartidoMock.buscarPorId(1L)).thenReturn(null);

    servicioPartido.registrarResultado(1L, 2, 0);

    verify(repositorioNotificacionPlMock, never()).guardar(any(NotificacionPl.class));
  }

  @Test
  public void listarNotificacionesNoLeidasDeberiaDelegarEnElRepositorio() {
    Usuario usuario = usuarioConPl(1L, 1000);
    NotificacionPl notificacion = new NotificacionPl();
    when(repositorioNotificacionPlMock.listarNoLeidasDe(1L)).thenReturn(List.of(notificacion));

    List<NotificacionPl> resultado = servicioPartido.listarNotificacionesNoLeidas(usuario);

    assertThat(resultado, equalTo(List.of(notificacion)));
  }

  @Test
  public void listarNotificacionesNoLeidasDeUsuarioNuloDeberiaRetornarListaVacia() {
    List<NotificacionPl> resultado = servicioPartido.listarNotificacionesNoLeidas(null);

    assertThat(resultado.isEmpty(), is(true));
    verify(repositorioNotificacionPlMock, never()).listarNoLeidasDe(any());
  }

  @Test
  public void marcarNotificacionesLeidasDeberiaDelegarEnElRepositorio() {
    Usuario usuario = usuarioConPl(7L, 1000);

    servicioPartido.marcarNotificacionesLeidas(usuario);

    verify(repositorioNotificacionPlMock, times(1)).marcarLeidasDe(7L);
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
