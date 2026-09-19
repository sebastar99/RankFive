package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tallerwebi.dominio.implementacion.ServicioRelacionAmistadImpl;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

public class ServicioRelacionAmistadTest {

  private ServicioRelacionAmistad servicioRelacionAmistad;
  private RepositorioAmistad repositorioAmistadMock;

  @BeforeEach
  public void init() {
    this.repositorioAmistadMock = mock(RepositorioAmistad.class);
    this.servicioRelacionAmistad = new ServicioRelacionAmistadImpl(this.repositorioAmistadMock);
  }

  private Usuario usuarioConId(Long id) {
    Usuario usuario = new Usuario();
    usuario.setId(id);
    return usuario;
  }

  private Amistad solicitudPendiente(Long id, Usuario solicitante, Usuario destinatario) {
    Amistad solicitud = new Amistad();
    solicitud.setId(id);
    solicitud.setUsuario(solicitante);
    solicitud.setAmigo(destinatario);
    solicitud.setEstado(EstadoAmistad.PENDIENTE);
    return solicitud;
  }

  @Test
  public void enviarSolicitudSinRelacionPreviaDeberiaGuardarlaComoPendiente() {
    Usuario solicitante = usuarioConId(1L);
    Usuario destinatario = usuarioConId(2L);
    when(repositorioAmistadMock.buscarRelacionEntre(1L, 2L)).thenReturn(null);

    ResultadoSolicitud resultado = servicioRelacionAmistad.enviarSolicitud(
      solicitante,
      destinatario
    );

    assertThat(resultado, equalTo(ResultadoSolicitud.ENVIADA));
    ArgumentCaptor<Amistad> captor = ArgumentCaptor.forClass(Amistad.class);
    verify(repositorioAmistadMock, times(1)).guardar(captor.capture());
    assertThat(captor.getValue().getEstado(), equalTo(EstadoAmistad.PENDIENTE));
    assertThat(captor.getValue().getUsuario(), equalTo(solicitante));
    assertThat(captor.getValue().getAmigo(), equalTo(destinatario));
  }

  @Test
  public void enviarSolicitudAUnoMismoNoDeberiaGuardarNada() {
    Usuario solicitante = usuarioConId(1L);

    ResultadoSolicitud resultado = servicioRelacionAmistad.enviarSolicitud(
      solicitante,
      usuarioConId(1L)
    );

    assertThat(resultado, equalTo(ResultadoSolicitud.ES_UNO_MISMO));
    verify(repositorioAmistadMock, never()).guardar(any(Amistad.class));
  }

  @Test
  public void enviarSolicitudCuandoYaSonAmigosDeberiaInformarlo() {
    Usuario solicitante = usuarioConId(1L);
    Usuario destinatario = usuarioConId(2L);
    Amistad existente = solicitudPendiente(9L, solicitante, destinatario);
    existente.setEstado(EstadoAmistad.ACEPTADA);
    when(repositorioAmistadMock.buscarRelacionEntre(1L, 2L)).thenReturn(existente);

    ResultadoSolicitud resultado = servicioRelacionAmistad.enviarSolicitud(
      solicitante,
      destinatario
    );

    assertThat(resultado, equalTo(ResultadoSolicitud.YA_SON_AMIGOS));
    verify(repositorioAmistadMock, never()).guardar(any(Amistad.class));
  }

  @Test
  public void enviarSolicitudDuplicadaDeberiaInformarQueYaFueEnviada() {
    Usuario solicitante = usuarioConId(1L);
    Usuario destinatario = usuarioConId(2L);
    when(repositorioAmistadMock.buscarRelacionEntre(1L, 2L))
      .thenReturn(solicitudPendiente(9L, solicitante, destinatario));

    ResultadoSolicitud resultado = servicioRelacionAmistad.enviarSolicitud(
      solicitante,
      destinatario
    );

    assertThat(resultado, equalTo(ResultadoSolicitud.SOLICITUD_YA_ENVIADA));
    verify(repositorioAmistadMock, never()).guardar(any(Amistad.class));
  }

  @Test
  public void enviarSolicitudCuandoElOtroYaEnvioLaSuyaDeberiaAvisarQueRevise() {
    Usuario solicitante = usuarioConId(1L);
    Usuario destinatario = usuarioConId(2L);
    when(repositorioAmistadMock.buscarRelacionEntre(1L, 2L))
      .thenReturn(solicitudPendiente(9L, destinatario, solicitante));

    ResultadoSolicitud resultado = servicioRelacionAmistad.enviarSolicitud(
      solicitante,
      destinatario
    );

    assertThat(resultado, equalTo(ResultadoSolicitud.SOLICITUD_RECIBIDA_PENDIENTE));
    verify(repositorioAmistadMock, never()).guardar(any(Amistad.class));
  }

  @Test
  public void aceptarSolicitudRecibidaDeberiaMarcarlaComoAceptada() {
    Usuario solicitante = usuarioConId(1L);
    Usuario destinatario = usuarioConId(2L);
    Amistad solicitud = solicitudPendiente(9L, solicitante, destinatario);
    when(repositorioAmistadMock.buscarPorId(9L)).thenReturn(solicitud);

    boolean aceptada = servicioRelacionAmistad.aceptarSolicitud(9L, destinatario);

    assertThat(aceptada, is(true));
    assertThat(solicitud.getEstado(), equalTo(EstadoAmistad.ACEPTADA));
    verify(repositorioAmistadMock, times(1)).actualizar(solicitud);
  }

  @Test
  public void rechazarSolicitudRecibidaDeberiaMarcarlaComoRechazada() {
    Usuario solicitante = usuarioConId(1L);
    Usuario destinatario = usuarioConId(2L);
    Amistad solicitud = solicitudPendiente(9L, solicitante, destinatario);
    when(repositorioAmistadMock.buscarPorId(9L)).thenReturn(solicitud);

    boolean rechazada = servicioRelacionAmistad.rechazarSolicitud(9L, destinatario);

    assertThat(rechazada, is(true));
    assertThat(solicitud.getEstado(), equalTo(EstadoAmistad.RECHAZADA));
    verify(repositorioAmistadMock, times(1)).actualizar(solicitud);
  }

  @Test
  public void elSolicitanteNoDeberiaPoderAceptarSuPropiaSolicitud() {
    Usuario solicitante = usuarioConId(1L);
    Usuario destinatario = usuarioConId(2L);
    Amistad solicitud = solicitudPendiente(9L, solicitante, destinatario);
    when(repositorioAmistadMock.buscarPorId(9L)).thenReturn(solicitud);

    boolean aceptada = servicioRelacionAmistad.aceptarSolicitud(9L, solicitante);

    assertThat(aceptada, is(false));
    assertThat(solicitud.getEstado(), equalTo(EstadoAmistad.PENDIENTE));
    verify(repositorioAmistadMock, never()).actualizar(any(Amistad.class));
  }

  @Test
  public void aceptarUnaSolicitudYaResueltaDeberiaFallar() {
    Usuario solicitante = usuarioConId(1L);
    Usuario destinatario = usuarioConId(2L);
    Amistad solicitud = solicitudPendiente(9L, solicitante, destinatario);
    solicitud.setEstado(EstadoAmistad.ACEPTADA);
    when(repositorioAmistadMock.buscarPorId(9L)).thenReturn(solicitud);

    boolean aceptada = servicioRelacionAmistad.aceptarSolicitud(9L, destinatario);

    assertThat(aceptada, is(false));
    verify(repositorioAmistadMock, never()).actualizar(any(Amistad.class));
  }

  @Test
  public void aceptarUnaSolicitudInexistenteDeberiaFallar() {
    when(repositorioAmistadMock.buscarPorId(99L)).thenReturn(null);

    boolean aceptada = servicioRelacionAmistad.aceptarSolicitud(99L, usuarioConId(2L));

    assertThat(aceptada, is(false));
    verify(repositorioAmistadMock, never()).actualizar(any(Amistad.class));
  }

  @Test
  public void enviarSolicitudConSolicitanteNuloDeberiaRetornarUsuarioInvalido() {
    ResultadoSolicitud resultado = servicioRelacionAmistad.enviarSolicitud(null, usuarioConId(2L));
    assertThat(resultado, equalTo(ResultadoSolicitud.USUARIO_INVALIDO));
    verify(repositorioAmistadMock, never()).guardar(any(Amistad.class));
  }

  @Test
  public void enviarSolicitudConDestinatarioNuloDeberiaRetornarUsuarioInvalido() {
    ResultadoSolicitud resultado = servicioRelacionAmistad.enviarSolicitud(usuarioConId(1L), null);
    assertThat(resultado, equalTo(ResultadoSolicitud.USUARIO_INVALIDO));
    verify(repositorioAmistadMock, never()).guardar(any(Amistad.class));
  }

  @Test
  public void enviarSolicitudConSolicitanteSinIdDeberiaRetornarUsuarioInvalido() {
    Usuario solicitante = new Usuario();
    ResultadoSolicitud resultado = servicioRelacionAmistad.enviarSolicitud(
      solicitante,
      usuarioConId(2L)
    );
    assertThat(resultado, equalTo(ResultadoSolicitud.USUARIO_INVALIDO));
  }

  @Test
  public void listarAmigosDeUsuarioNuloDeberiaRetornarListaVacia() {
    List<Usuario> amigos = servicioRelacionAmistad.listarAmigosDe(null);
    assertThat(amigos.isEmpty(), is(true));
  }

  @Test
  public void listarAmigosDeUsuarioSinIdDeberiaRetornarListaVacia() {
    List<Usuario> amigos = servicioRelacionAmistad.listarAmigosDe(new Usuario());
    assertThat(amigos.isEmpty(), is(true));
  }

  @Test
  public void listarSolicitudesPendientesDeUsuarioNuloDeberiaRetornarListaVacia() {
    List<Amistad> pendientes = servicioRelacionAmistad.listarSolicitudesPendientes(null);
    assertThat(pendientes.isEmpty(), is(true));
  }

  @Test
  public void contarSolicitudesPendientesDeUsuarioNuloDeberiaRetornarCero() {
    long count = servicioRelacionAmistad.contarSolicitudesPendientes(null);
    assertThat(count, equalTo(0L));
  }

  @Test
  public void aceptarSolicitudConIdNuloDeberiaRetornarFalse() {
    boolean aceptada = servicioRelacionAmistad.aceptarSolicitud(null, usuarioConId(2L));
    assertThat(aceptada, is(false));
  }

  @Test
  public void aceptarSolicitudConUsuarioNuloDeberiaRetornarFalse() {
    boolean aceptada = servicioRelacionAmistad.aceptarSolicitud(9L, null);
    assertThat(aceptada, is(false));
  }

  @Test
  public void rechazarSolicitudConIdNuloDeberiaRetornarFalse() {
    boolean rechazada = servicioRelacionAmistad.rechazarSolicitud(null, usuarioConId(2L));
    assertThat(rechazada, is(false));
  }
}
