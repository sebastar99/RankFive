package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tallerwebi.dominio.implementacion.ServicioCompetenciaImpl;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

public class ServicioCompetenciaTest {

  private RepositorioTorneo repoTorneo;
  private RepositorioInscripcionTorneo repoInscripcion;
  private RepositorioFaseTorneo repoFase;
  private RepositorioEncuentroTorneo repoEncuentro;
  private ServicioEquipo servicioEquipo;
  private ServicioArbitro servicioArbitro;
  private ServicioCompetenciaImpl servicio;
  private Usuario usuario;
  private Torneo torneo;

  @BeforeEach
  public void init() {
    repoTorneo = mock(RepositorioTorneo.class);
    repoInscripcion = mock(RepositorioInscripcionTorneo.class);
    repoFase = mock(RepositorioFaseTorneo.class);
    repoEncuentro = mock(RepositorioEncuentroTorneo.class);
    servicioEquipo = mock(ServicioEquipo.class);
    servicioArbitro = mock(ServicioArbitro.class);
    servicio =
      new ServicioCompetenciaImpl(
        repoTorneo,
        mock(RepositorioLiga.class),
        repoInscripcion,
        repoFase,
        repoEncuentro,
        mock(RepositorioNotificacionEncuentro.class),
        servicioEquipo,
        servicioArbitro
      );
    usuario = new Usuario();
    usuario.setId(1L);
    torneo = new Torneo();
    torneo.setId(10L);
    torneo.setCupoEquipos(2);
    torneo.setInscripcionAbierta(true);
    when(repoTorneo.buscarPorId(10L)).thenReturn(torneo);
    when(repoInscripcion.listarPorTorneo(10L)).thenReturn(Collections.emptyList());
  }

  @Test
  public void enEliminacionDirectaNoSeAceptaEmpateYElGanadorPasaALaFinal() {
    FaseTorneo semis = new FaseTorneo();
    semis.setId(1L);
    semis.setNumero(2);
    semis.setTorneo(torneo);
    EncuentroTorneo pendiente = encuentro(semis, inscripcion(1L), inscripcion(2L));
    pendiente.setId(100L);
    EncuentroTorneo jugado = encuentro(semis, inscripcion(3L), inscripcion(4L));
    jugado.setEstado(GeneradorFixtureTorneo.JUGADO);
    jugado.setGanador(jugado.getParticipanteA());
    when(repoEncuentro.buscarPorId(100L)).thenReturn(pendiente);
    when(repoFase.listarPorTorneo(10L)).thenReturn(List.of(semis));

    assertThat(servicio.registrarResultadoEncuentro(100L, 1, 1), is(false));
    assertThat(servicio.registrarResultadoEncuentro(100L, 2, 1), is(true));

    ArgumentCaptor<FaseTorneo> captor = ArgumentCaptor.forClass(FaseTorneo.class);
    verify(repoFase).guardar(captor.capture());
    assertThat(captor.getValue().getNombre(), equalTo("Final"));
    assertThat(servicio.registrarResultadoEncuentro(100L, 3, 0), is(false));
  }

  private static InscripcionTorneo inscripcion(Long id) {
    InscripcionTorneo insc = new InscripcionTorneo();
    insc.setId(id);
    return insc;
  }

  private static EncuentroTorneo encuentro(
    FaseTorneo fase,
    InscripcionTorneo local,
    InscripcionTorneo visitante
  ) {
    EncuentroTorneo encuentro = new EncuentroTorneo();
    encuentro.setFase(fase);
    encuentro.setParticipanteA(local);
    encuentro.setParticipanteB(visitante);
    encuentro.setEstado(GeneradorFixtureTorneo.PENDIENTE);
    fase.getEncuentros().add(encuentro);
    return encuentro;
  }

  @Test
  public void inscribeConElEquipoActivoDelUsuario() {
    Equipo equipo = new Equipo();
    equipo.setNombre("Los Pibes");
    when(servicioEquipo.listarEquiposActivosDe(usuario)).thenReturn(List.of(equipo));

    ResultadoInscripcion resultado = servicio.inscribirATorneo(usuario, 10L);

    assertThat(resultado, equalTo(ResultadoInscripcion.INSCRIPTO));
    ArgumentCaptor<InscripcionTorneo> captor = ArgumentCaptor.forClass(InscripcionTorneo.class);
    verify(repoInscripcion).guardar(captor.capture());
    assertThat(captor.getValue().getEquipo(), equalTo(equipo));
    assertThat(captor.getValue().getNombreEquipo(), equalTo("Los Pibes"));
  }

  @Test
  public void noInscribeSinEquipoActivo() {
    when(servicioEquipo.listarEquiposActivosDe(usuario)).thenReturn(Collections.emptyList());

    ResultadoInscripcion resultado = servicio.inscribirATorneo(usuario, 10L);

    assertThat(resultado, equalTo(ResultadoInscripcion.SIN_EQUIPO_ACTIVO));
    assertThat(servicio.tieneEquipoActivo(usuario), is(false));
    verify(repoInscripcion, never()).guardar(any(InscripcionTorneo.class));
  }

  @Test
  public void rechazaInscripcionDuplicadaCerradaOSinCupo() {
    when(repoInscripcion.existePara(1L, 10L)).thenReturn(true);
    assertThat(servicio.inscribirATorneo(usuario, 10L), equalTo(ResultadoInscripcion.YA_INSCRIPTO));

    when(repoInscripcion.existePara(1L, 10L)).thenReturn(false);
    when(repoInscripcion.listarPorTorneo(10L))
      .thenReturn(List.of(new InscripcionTorneo(), new InscripcionTorneo()));
    assertThat(
      servicio.inscribirATorneo(usuario, 10L),
      equalTo(ResultadoInscripcion.CUPO_COMPLETO)
    );

    torneo.setInscripcionAbierta(false);
    assertThat(
      servicio.inscribirATorneo(usuario, 10L),
      equalTo(ResultadoInscripcion.INSCRIPCION_CERRADA)
    );
    assertThat(servicio.inscribirATorneo(null, 10L), equalTo(ResultadoInscripcion.INVALIDO));
    verify(repoInscripcion, never()).guardar(any(InscripcionTorneo.class));
  }

  @Test
  public void generarFixtureDesignaArbitros() {
    InscripcionTorneo a = new InscripcionTorneo();
    InscripcionTorneo b = new InscripcionTorneo();
    when(repoFase.listarPorTorneo(10L)).thenReturn(Collections.emptyList());
    when(repoInscripcion.listarPorTorneo(10L)).thenReturn(List.of(a, b));

    servicio.generarFixtureSiNoExiste(10L);

    verify(servicioArbitro).designar(any());
    verify(repoFase).guardar(any(FaseTorneo.class));
  }
}
