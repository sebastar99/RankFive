package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

public class GeneradorFixtureTorneoTest {

  @Test
  public void treintaYDosEquiposGeneranOchoGruposDeSeisPartidosSoloIda() {
    List<InscripcionTorneo> equipos = equipos(32);

    FaseTorneo grupos = GeneradorFixtureTorneo.crearFaseDeGrupos(new Torneo(), equipos);

    assertThat(grupos.esDeGrupos(), is(true));
    assertThat(grupos.getEncuentros(), hasSize(48));
    long partidosGrupoA = grupos
      .getEncuentros()
      .stream()
      .filter(e -> "A".equals(e.getGrupo()))
      .count();
    assertThat(partidosGrupoA, equalTo(6L));
    assertThat(equipos.get(31).getGrupo(), equalTo("H"));
  }

  @Test
  public void grupoDisponibleLlenaEnOrdenYDevuelveElGrupoIncompleto() {
    List<InscripcionTorneo> equipos = equipos(31);
    GeneradorFixtureTorneo.crearFaseDeGrupos(new Torneo(), equipos);

    assertThat(GeneradorFixtureTorneo.grupoDisponible(equipos), equalTo("H"));
  }

  @Test
  public void alCompletarLosGruposClasificanLosDosMejoresAOctavos() {
    List<InscripcionTorneo> equipos = equipos(32);
    FaseTorneo grupos = GeneradorFixtureTorneo.crearFaseDeGrupos(new Torneo(), equipos);
    grupos.setNumero(1);
    jugarTodos(grupos);

    assertThat(GeneradorFixtureTorneo.estaCompleta(grupos), is(true));
    FaseTorneo octavos = GeneradorFixtureTorneo.crearPrimeraEliminatoria(grupos, equipos);

    assertThat(octavos.getNombre(), equalTo("Octavos de final"));
    assertThat(octavos.getEncuentros(), hasSize(8));
    assertThat(octavos.esDeGrupos(), is(false));
  }

  @Test
  public void laEliminacionDirectaAvanzaHastaElCampeon() {
    List<InscripcionTorneo> equipos = equipos(4);
    FaseTorneo semis = GeneradorFixtureTorneo.crearSiguienteEliminatoria(fase(0), equipos);
    assertThat(semis.getNombre(), equalTo("Semifinal"));
    jugarTodos(semis);

    List<InscripcionTorneo> ganadores = GeneradorFixtureTorneo.ganadoresSiCompleta(semis);
    FaseTorneo fin = GeneradorFixtureTorneo.crearSiguienteEliminatoria(semis, ganadores);
    assertThat(fin.getNombre(), equalTo("Final"));
    assertThat(GeneradorFixtureTorneo.campeon(List.of(semis, fin)), nullValue());

    jugarTodos(fin);
    assertThat(GeneradorFixtureTorneo.campeon(List.of(semis, fin)), equalTo(equipos.get(0)));
  }

  @Test
  public void equipoSinRivalQuedaLibre() {
    List<InscripcionTorneo> equipos = equipos(3);
    FaseTorneo fase = GeneradorFixtureTorneo.crearSiguienteEliminatoria(fase(0), equipos);

    assertThat(fase.getEncuentros().get(1).getEstado(), equalTo(GeneradorFixtureTorneo.BYE));
    assertThat(fase.getEncuentros().get(1).getGanador(), equalTo(equipos.get(2)));
    assertThat(GeneradorFixtureTorneo.nombreEliminatoria(32), equalTo("Dieciseisavos de final"));
    assertThat(GeneradorFixtureTorneo.nombreEliminatoria(8), equalTo("Cuartos de final"));
  }

  @Test
  public void nombreEliminatoriaRetornaNombreCorrecto() {
    assertThat(GeneradorFixtureTorneo.nombreEliminatoria(32), equalTo("Dieciseisavos de final"));
    assertThat(GeneradorFixtureTorneo.nombreEliminatoria(16), equalTo("Octavos de final"));
    assertThat(GeneradorFixtureTorneo.nombreEliminatoria(8), equalTo("Cuartos de final"));
    assertThat(GeneradorFixtureTorneo.nombreEliminatoria(4), equalTo("Semifinal"));
    assertThat(GeneradorFixtureTorneo.nombreEliminatoria(2), equalTo("Final"));
  }

  @Test
  public void campeonRetornaNullCuandoNoHayCampeon() {
    List<InscripcionTorneo> equipos = equipos(4);
    FaseTorneo semis = GeneradorFixtureTorneo.crearSiguienteEliminatoria(fase(0), equipos);
    assertThat(GeneradorFixtureTorneo.campeon(List.of(semis)), nullValue());
  }

  @Test
  public void unaFaseSinEncuentrosNoEstaCompleta() {
    assertThat(GeneradorFixtureTorneo.estaCompleta(fase(1)), is(false));
  }

  @Test
  public void sinFasesNoHayCampeon() {
    assertThat(GeneradorFixtureTorneo.campeon(List.of()), nullValue());
  }

  @Test
  public void siLaUltimaFaseEsDeGruposNoHayCampeon() {
    FaseTorneo grupos = new FaseTorneo();
    grupos.setTipo(FaseTorneo.TIPO_GRUPOS);
    assertThat(GeneradorFixtureTorneo.campeon(List.of(grupos)), nullValue());
  }

  @Test
  public void siUnEncuentroJugadoNoTieneGanadorNoHayGanadores() {
    List<InscripcionTorneo> equipos = equipos(2);
    FaseTorneo fase = GeneradorFixtureTorneo.crearSiguienteEliminatoria(fase(0), equipos);
    EncuentroTorneo e = fase.getEncuentros().get(0);
    e.setEstado(GeneradorFixtureTorneo.JUGADO);
    assertThat(GeneradorFixtureTorneo.ganadoresSiCompleta(fase).isEmpty(), is(true));
  }

  @Test
  public void cuandoTodosLosGruposEstanLlenosDevuelveElMenosCargado() {
    List<InscripcionTorneo> equipos = equipos(33);
    GeneradorFixtureTorneo.crearFaseDeGrupos(new Torneo(), equipos);
    assertThat(GeneradorFixtureTorneo.grupoDisponible(equipos), equalTo("B"));
  }

  @Test
  public void unaInscripcionConGrupoYaAsignadoLoConserva() {
    List<InscripcionTorneo> equipos = equipos(4);
    equipos.get(0).setGrupo("H");
    GeneradorFixtureTorneo.crearFaseDeGrupos(new Torneo(), equipos);
    assertThat(equipos.get(0).getGrupo(), equalTo("H"));
  }

  @Test
  public void siElPrimerClasificadoEsNullElSegundoQuedaLibre() {
    List<InscripcionTorneo> participantes = new ArrayList<>();
    participantes.add(null);
    participantes.add(equipos(1).get(0));
    FaseTorneo fase = GeneradorFixtureTorneo.crearSiguienteEliminatoria(fase(0), participantes);
    EncuentroTorneo e = fase.getEncuentros().get(0);
    assertThat(e.getEstado(), equalTo(GeneradorFixtureTorneo.BYE));
    assertThat(e.getParticipanteA(), equalTo(participantes.get(1)));
  }

  @Test
  public void conGruposImparesElUltimoGrupoCruzaSusDosPrimeros() {
    List<InscripcionTorneo> equipos = equipos(12);
    FaseTorneo grupos = GeneradorFixtureTorneo.crearFaseDeGrupos(new Torneo(), equipos);
    grupos.setNumero(1);
    jugarTodos(grupos);
    FaseTorneo siguiente = GeneradorFixtureTorneo.crearPrimeraEliminatoria(grupos, equipos);
    assertThat(siguiente.getEncuentros(), hasSize(3));
    assertThat(siguiente.getNombre(), equalTo("Cuartos de final"));
  }

  private static void jugarTodos(FaseTorneo fase) {
    for (EncuentroTorneo e : fase.getEncuentros()) {
      if (GeneradorFixtureTorneo.PENDIENTE.equals(e.getEstado())) {
        boolean ganaA = e.getParticipanteA().getId() < e.getParticipanteB().getId();
        e.setGolesA(ganaA ? 2 : 0);
        e.setGolesB(ganaA ? 0 : 2);
        e.setEstado(GeneradorFixtureTorneo.JUGADO);
        e.setGanador(ganaA ? e.getParticipanteA() : e.getParticipanteB());
      }
    }
  }

  private static FaseTorneo fase(int numero) {
    FaseTorneo fase = new FaseTorneo();
    fase.setNumero(numero);
    return fase;
  }

  private static List<InscripcionTorneo> equipos(int cantidad) {
    List<InscripcionTorneo> lista = new ArrayList<>();
    for (long i = 1; i <= cantidad; i++) {
      InscripcionTorneo insc = new InscripcionTorneo();
      insc.setId(i);
      insc.setNombreEquipo("Equipo " + i);
      lista.add(insc);
    }
    return lista;
  }
}
