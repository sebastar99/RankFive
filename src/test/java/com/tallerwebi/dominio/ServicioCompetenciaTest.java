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

import com.tallerwebi.dominio.implementacion.ServicioCompetenciaImpl;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

public class ServicioCompetenciaTest {

  private RepositorioTorneo repoTorneo;
  private RepositorioLiga repoLiga;
  private RepositorioInscripcionTorneo repoInscripcion;
  private RepositorioFaseTorneo repoFase;
  private RepositorioEncuentroTorneo repoEncuentro;
  private RepositorioNotificacionEncuentro repoNotificacion;
  private ServicioEquipo servicioEquipo;
  private ServicioArbitro servicioArbitro;
  private ServicioCompetenciaImpl servicio;
  private Usuario usuario;
  private Torneo torneo;

  @BeforeEach
  public void init() {
    repoTorneo = mock(RepositorioTorneo.class);
    repoLiga = mock(RepositorioLiga.class);
    repoInscripcion = mock(RepositorioInscripcionTorneo.class);
    repoFase = mock(RepositorioFaseTorneo.class);
    repoEncuentro = mock(RepositorioEncuentroTorneo.class);
    repoNotificacion = mock(RepositorioNotificacionEncuentro.class);
    servicioEquipo = mock(ServicioEquipo.class);
    servicioArbitro = mock(ServicioArbitro.class);
    servicio =
      new ServicioCompetenciaImpl(
        repoTorneo,
        repoLiga,
        repoInscripcion,
        repoFase,
        repoEncuentro,
        repoNotificacion,
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

  @Test
  public void metodosDeConsultaManejanIdsNulosYResultadosDelRepositorio() {
    when(repoTorneo.listarTodos()).thenReturn(null);
    when(repoLiga.listarTodos()).thenReturn(null);
    assertThat(servicio.listarTorneos().isEmpty(), is(true));
    assertThat(servicio.listarLigas().isEmpty(), is(true));

    List<Torneo> torneos = List.of(torneo);
    List<Liga> ligas = List.of(new Liga());
    when(repoTorneo.listarTodos()).thenReturn(torneos);
    when(repoLiga.listarTodos()).thenReturn(ligas);
    assertThat(servicio.listarTorneos(), equalTo(torneos));
    assertThat(servicio.listarLigas(), equalTo(ligas));

    assertThat(servicio.calcularTablasPorGrupo(null).isEmpty(), is(true));
    assertThat(servicio.obtenerCampeon(null), equalTo(null));
    assertThat(servicio.listarEquiposInscriptos(null).isEmpty(), is(true));
    assertThat(servicio.yaInscripto(null, 10L), is(false));
    assertThat(servicio.yaInscripto(usuario, null), is(false));
    assertThat(servicio.listarInscripciones(null).isEmpty(), is(true));
    assertThat(servicio.buscarTorneoPorId(null), equalTo(null));
    assertThat(servicio.obtenerFases(null).isEmpty(), is(true));

    List<InscripcionTorneo> inscripciones = List.of(new InscripcionTorneo());
    List<Equipo> equipos = List.of(new Equipo());
    List<FaseTorneo> fases = List.of(new FaseTorneo());
    when(repoInscripcion.listarPorTorneo(10L)).thenReturn(inscripciones);
    when(repoInscripcion.listarEquipos(10L)).thenReturn(equipos);
    when(repoInscripcion.existePara(1L, 10L)).thenReturn(true);
    when(repoFase.listarPorTorneo(10L)).thenReturn(fases);

    assertThat(servicio.listarInscripciones(10L), equalTo(inscripciones));
    assertThat(servicio.listarEquiposInscriptos(10L), equalTo(equipos));
    assertThat(servicio.yaInscripto(usuario, 10L), is(true));
    assertThat(servicio.buscarTorneoPorId(10L), equalTo(torneo));
    assertThat(servicio.obtenerFases(10L), equalTo(fases));
  }

  @Test
  public void validaInscripcionYFixtureEnCasosInvalidos() {
    Usuario sinId = new Usuario();
    assertThat(servicio.inscribirATorneo(null, 10L), equalTo(ResultadoInscripcion.INVALIDO));
    assertThat(servicio.inscribirATorneo(sinId, 10L), equalTo(ResultadoInscripcion.INVALIDO));
    assertThat(servicio.inscribirATorneo(usuario, null), equalTo(ResultadoInscripcion.INVALIDO));

    when(repoTorneo.buscarPorId(10L)).thenReturn(null);
    assertThat(servicio.inscribirATorneo(usuario, 10L), equalTo(ResultadoInscripcion.INVALIDO));

    when(repoTorneo.buscarPorId(10L)).thenReturn(torneo);
    when(repoFase.listarPorTorneo(10L)).thenReturn(List.of(new FaseTorneo()));
    assertThat(
      servicio.inscribirATorneo(usuario, 10L),
      equalTo(ResultadoInscripcion.INSCRIPCION_CERRADA)
    );

    when(repoFase.listarPorTorneo(10L)).thenReturn(Collections.emptyList());
    torneo.setCupoEquipos(null);
    when(servicioEquipo.listarEquiposActivosDe(usuario)).thenReturn(null);
    assertThat(
      servicio.inscribirATorneo(usuario, 10L),
      equalTo(ResultadoInscripcion.SIN_EQUIPO_ACTIVO)
    );
    assertThat(servicio.tieneEquipoActivo(usuario), is(false));

    assertThat(servicio.generarFixtureSiNoExiste(null), is(false));
    when(repoFase.listarPorTorneo(10L)).thenReturn(List.of(new FaseTorneo()));
    assertThat(servicio.generarFixtureSiNoExiste(10L), is(false));
    when(repoFase.listarPorTorneo(10L)).thenReturn(Collections.emptyList());
    when(repoTorneo.buscarPorId(10L)).thenReturn(null);
    when(repoInscripcion.listarPorTorneo(10L)).thenReturn(List.of(new InscripcionTorneo()));
    assertThat(servicio.generarFixtureSiNoExiste(10L), is(false));
    when(repoTorneo.buscarPorId(10L)).thenReturn(torneo);
    assertThat(servicio.generarFixtureSiNoExiste(10L), is(false));
  }

  @Test
  public void registraResultadoDeEliminatoriaYNotificaALosParticipantes() {
    FaseTorneo semifinal = new FaseTorneo();
    semifinal.setId(2L);
    semifinal.setNumero(2);
    semifinal.setTorneo(torneo);

    InscripcionTorneo ganadorPrevio = inscripcionConUsuario(3L, 3L, "Equipo C");
    InscripcionTorneo perdedorPrevio = inscripcionConUsuario(4L, 4L, "Equipo D");
    InscripcionTorneo local = inscripcionConUsuario(1L, 1L, "Equipo A");
    InscripcionTorneo visitante = inscripcionConUsuario(2L, 2L, "Equipo B");
    EncuentroTorneo previo = encuentro(semifinal, ganadorPrevio, perdedorPrevio);
    previo.setEstado(GeneradorFixtureTorneo.JUGADO);
    previo.setGanador(ganadorPrevio);
    EncuentroTorneo pendiente = encuentro(semifinal, local, visitante);
    pendiente.setId(200L);
    when(repoEncuentro.buscarPorId(200L)).thenReturn(pendiente);
    when(repoFase.listarPorTorneo(10L)).thenReturn(List.of(semifinal));

    assertThat(servicio.registrarResultadoEncuentro(null, 1, 0), is(false));
    assertThat(servicio.registrarResultadoEncuentro(200L, null, 0), is(false));
    assertThat(servicio.registrarResultadoEncuentro(200L, -1, 0), is(false));
    when(repoEncuentro.buscarPorId(200L)).thenReturn(null);
    assertThat(servicio.registrarResultadoEncuentro(200L, 1, 0), is(false));

    when(repoEncuentro.buscarPorId(200L)).thenReturn(pendiente);
    assertThat(servicio.registrarResultadoEncuentro(200L, 1, 1), is(false));
    assertThat(servicio.registrarResultadoEncuentro(200L, 0, 2), is(true));

    ArgumentCaptor<FaseTorneo> captor = ArgumentCaptor.forClass(FaseTorneo.class);
    verify(repoFase, times(1)).guardar(captor.capture());
    assertThat(captor.getValue().getNombre(), equalTo("Final"));
    assertThat(pendiente.getGanador(), equalTo(visitante));
    verify(repoNotificacion, times(4)).guardar(any());
    verify(servicioArbitro, times(1)).designar(any());
  }

  @Test
  public void guardaCompeticionesSoloCuandoNoSonNulas() {
    servicio.guardarTorneo(null);
    servicio.guardarLiga(null);
    verify(repoTorneo, never()).guardar(any());
    verify(repoLiga, never()).guardar(any());

    Liga liga = new Liga();
    servicio.guardarTorneo(torneo);
    servicio.guardarLiga(liga);
    verify(repoTorneo).guardar(torneo);
    verify(repoLiga).guardar(liga);
  }

  @Test
  public void completaFaseDeGruposYGeneraLaPrimeraEliminatoria() {
    FaseTorneo grupos = new FaseTorneo();
    grupos.setId(1L);
    grupos.setNumero(1);
    grupos.setTipo(FaseTorneo.TIPO_GRUPOS);
    grupos.setTorneo(torneo);

    InscripcionTorneo local = inscripcionConUsuario(1L, 1L, "Equipo A");
    InscripcionTorneo visitante = inscripcionConUsuario(2L, 2L, "Equipo B");
    local.setGrupo("A");
    visitante.setGrupo("A");
    local.getEquipo().agregarJugador(new Usuario());
    EncuentroTorneo primerPartido = encuentro(grupos, local, visitante);
    primerPartido.setId(300L);
    EncuentroTorneo segundoPartido = encuentro(grupos, local, visitante);
    segundoPartido.setId(301L);

    when(repoEncuentro.buscarPorId(300L)).thenReturn(primerPartido);
    when(repoEncuentro.buscarPorId(301L)).thenReturn(segundoPartido);
    when(repoFase.listarPorTorneo(10L)).thenReturn(List.of(grupos));
    when(repoInscripcion.listarPorTorneo(10L)).thenReturn(List.of(local, visitante));

    assertThat(servicio.registrarResultadoEncuentro(300L, 1, 0), is(true));
    verify(repoFase, never()).guardar(any(FaseTorneo.class));

    assertThat(servicio.registrarResultadoEncuentro(301L, 0, 0), is(true));

    ArgumentCaptor<FaseTorneo> captor = ArgumentCaptor.forClass(FaseTorneo.class);
    verify(repoFase).guardar(captor.capture());
    assertThat(captor.getValue().getNumero(), equalTo(2));
    assertThat(captor.getValue().getNombre(), equalTo("Final"));
    verify(repoNotificacion, times(4)).guardar(any());
  }

  @Test
  public void noAvanzaSiLaFaseNoEsLaUltimaONoHaySuficientesGanadores() {
    FaseTorneo semifinal = new FaseTorneo();
    semifinal.setId(2L);
    semifinal.setNumero(2);
    semifinal.setTorneo(torneo);
    InscripcionTorneo local = inscripcion(1L);
    InscripcionTorneo visitante = inscripcion(2L);
    EncuentroTorneo encuentroNoFinal = encuentro(semifinal, local, visitante);
    encuentroNoFinal.setId(400L);
    when(repoEncuentro.buscarPorId(400L)).thenReturn(encuentroNoFinal);
    FaseTorneo finalExistente = new FaseTorneo();
    finalExistente.setId(3L);
    when(repoFase.listarPorTorneo(10L)).thenReturn(List.of(semifinal, finalExistente));
    assertThat(servicio.registrarResultadoEncuentro(400L, 2, 1), is(true));
    verify(repoFase, never()).guardar(any(FaseTorneo.class));

    FaseTorneo otraSemifinal = new FaseTorneo();
    otraSemifinal.setId(4L);
    otraSemifinal.setNumero(2);
    otraSemifinal.setTorneo(torneo);
    EncuentroTorneo unicoEncuentro = encuentro(otraSemifinal, inscripcion(3L), inscripcion(4L));
    unicoEncuentro.setId(401L);
    when(repoEncuentro.buscarPorId(401L)).thenReturn(unicoEncuentro);
    when(repoFase.listarPorTorneo(10L)).thenReturn(List.of(otraSemifinal));
    assertThat(servicio.registrarResultadoEncuentro(401L, 1, 0), is(true));
    verify(repoFase, never()).guardar(any(FaseTorneo.class));
  }

  private static InscripcionTorneo inscripcionConUsuario(long id, long usuarioId, String nombre) {
    Usuario usuario = new Usuario();
    usuario.setId(usuarioId);
    Equipo equipo = new Equipo();
    equipo.setNombre(nombre);
    Usuario jugador = new Usuario();
    jugador.setId(usuarioId + 100);
    equipo.agregarJugador(jugador);
    InscripcionTorneo inscripcion = inscripcion(id);
    inscripcion.setUsuario(usuario);
    inscripcion.setEquipo(equipo);
    return inscripcion;
  }
}
