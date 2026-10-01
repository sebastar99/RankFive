package com.tallerwebi.dominio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public final class GeneradorFixtureTorneo {

  public static final List<String> GRUPOS = List.of("A", "B", "C", "D", "E", "F", "G", "H");
  public static final String PENDIENTE = "PENDIENTE";
  public static final String BYE = "BYE";
  public static final String JUGADO = "JUGADO";

  private static final int FINAL = 2;
  private static final int SEMIFINAL = 4;
  private static final int CUARTOS = 8;
  private static final int OCTAVOS = 16;
  private static final int CLASIFICADOS_POR_GRUPO = 2;
  private static final int EQUIPOS_POR_GRUPO = 4;

  private GeneradorFixtureTorneo() {}

  public static String grupoDisponible(List<InscripcionTorneo> inscripciones) {
    Map<String, Integer> conteo = new LinkedHashMap<>();
    GRUPOS.forEach(g -> conteo.put(g, 0));
    for (InscripcionTorneo insc : inscripciones) {
      if (insc.getGrupo() != null && conteo.containsKey(insc.getGrupo())) {
        conteo.merge(insc.getGrupo(), 1, Integer::sum);
      }
    }
    for (Map.Entry<String, Integer> entrada : conteo.entrySet()) {
      if (entrada.getValue() < EQUIPOS_POR_GRUPO) {
        return entrada.getKey();
      }
    }
    String elegido = GRUPOS.get(0);
    for (Map.Entry<String, Integer> entrada : conteo.entrySet()) {
      if (entrada.getValue() < conteo.get(elegido)) {
        elegido = entrada.getKey();
      }
    }
    return elegido;
  }

  public static FaseTorneo crearFaseDeGrupos(Torneo torneo, List<InscripcionTorneo> inscripciones) {
    for (InscripcionTorneo insc : inscripciones) {
      if (insc.getGrupo() == null) {
        insc.setGrupo(grupoDisponible(inscripciones));
      }
    }
    Map<String, List<InscripcionTorneo>> porGrupo = new TreeMap<>();
    for (InscripcionTorneo insc : inscripciones) {
      porGrupo.computeIfAbsent(insc.getGrupo(), g -> new ArrayList<>()).add(insc);
    }
    FaseTorneo fase = nuevaFase(torneo, 1, "Fase de grupos", FaseTorneo.TIPO_GRUPOS);
    porGrupo.forEach((grupo, miembros) -> {
      for (List<Cruce<InscripcionTorneo>> fecha : RoundRobin.soloIda(miembros)) {
        for (Cruce<InscripcionTorneo> cruce : fecha) {
          EncuentroTorneo encuentro = encuentro(fase, cruce.getLocal(), cruce.getVisitante());
          encuentro.setGrupo(grupo);
        }
      }
    });
    return fase;
  }

  public static FaseTorneo crearPrimeraEliminatoria(
    FaseTorneo faseGrupos,
    List<InscripcionTorneo> inscripciones
  ) {
    Map<Long, InscripcionTorneo> porId = new HashMap<>();
    inscripciones.forEach(i -> porId.put(i.getId(), i));
    Map<String, List<FilaPosicion>> tablas = TablaPosiciones.calcularPorGrupo(
      inscripciones,
      List.of(faseGrupos)
    );
    List<List<FilaPosicion>> grupos = new ArrayList<>(tablas.values());
    List<InscripcionTorneo> orden = new ArrayList<>();
    for (int i = 0; i < grupos.size(); i += CLASIFICADOS_POR_GRUPO) {
      List<FilaPosicion> actual = grupos.get(i);
      boolean hayVecino = i + 1 < grupos.size();
      List<FilaPosicion> vecino = hayVecino ? grupos.get(i + 1) : actual;
      orden.add(puesto(actual, 0, porId));
      orden.add(puesto(vecino, 1, porId));
      if (hayVecino) {
        orden.add(puesto(vecino, 0, porId));
        orden.add(puesto(actual, 1, porId));
      }
    }
    return crearEliminatoria(faseGrupos.getTorneo(), faseGrupos.getNumero() + 1, orden);
  }

  public static FaseTorneo crearSiguienteEliminatoria(
    FaseTorneo actual,
    List<InscripcionTorneo> ganadores
  ) {
    return crearEliminatoria(actual.getTorneo(), actual.getNumero() + 1, ganadores);
  }

  public static boolean estaCompleta(FaseTorneo fase) {
    for (EncuentroTorneo encuentro : fase.getEncuentros()) {
      if (PENDIENTE.equals(encuentro.getEstado())) {
        return false;
      }
    }
    return !fase.getEncuentros().isEmpty();
  }

  public static List<InscripcionTorneo> ganadoresSiCompleta(FaseTorneo fase) {
    if (!estaCompleta(fase)) {
      return Collections.emptyList();
    }
    List<InscripcionTorneo> ganadores = new ArrayList<>();
    for (EncuentroTorneo encuentro : fase.getEncuentros()) {
      if (encuentro.getGanador() == null) {
        return Collections.emptyList();
      }
      ganadores.add(encuentro.getGanador());
    }
    return ganadores;
  }

  public static InscripcionTorneo campeon(List<FaseTorneo> fases) {
    if (fases.isEmpty()) {
      return null;
    }
    FaseTorneo ultima = fases.get(fases.size() - 1);
    if (ultima.esDeGrupos() || ultima.getEncuentros().size() != 1) {
      return null;
    }
    EncuentroTorneo fin = ultima.getEncuentros().get(0);
    return JUGADO.equals(fin.getEstado()) ? fin.getGanador() : null;
  }

  public static String nombreEliminatoria(int equipos) {
    if (equipos <= FINAL) {
      return "Final";
    }
    if (equipos <= SEMIFINAL) {
      return "Semifinal";
    }
    if (equipos <= CUARTOS) {
      return "Cuartos de final";
    }
    if (equipos <= OCTAVOS) {
      return "Octavos de final";
    }
    return "Dieciseisavos de final";
  }

  private static InscripcionTorneo puesto(
    List<FilaPosicion> tabla,
    int indice,
    Map<Long, InscripcionTorneo> porId
  ) {
    if (indice >= tabla.size()) {
      return null;
    }
    return porId.get(tabla.get(indice).getParticipanteId());
  }

  private static FaseTorneo crearEliminatoria(
    Torneo torneo,
    int numero,
    List<InscripcionTorneo> participantes
  ) {
    FaseTorneo fase = nuevaFase(
      torneo,
      numero,
      nombreEliminatoria(participantes.size()),
      FaseTorneo.TIPO_ELIMINACION
    );
    for (int i = 0; i < participantes.size(); i += CLASIFICADOS_POR_GRUPO) {
      InscripcionTorneo primero = participantes.get(i);
      InscripcionTorneo segundo = i + 1 < participantes.size() ? participantes.get(i + 1) : null;
      agregarCruce(fase, primero, segundo);
    }
    return fase;
  }

  private static void agregarCruce(
    FaseTorneo fase,
    InscripcionTorneo primero,
    InscripcionTorneo segundo
  ) {
    if (primero != null) {
      encuentro(fase, primero, segundo);
    } else if (segundo != null) {
      encuentro(fase, segundo, null);
    }
  }

  private static FaseTorneo nuevaFase(Torneo torneo, int numero, String nombre, String tipo) {
    FaseTorneo fase = new FaseTorneo();
    fase.setTorneo(torneo);
    fase.setNumero(numero);
    fase.setNombre(nombre);
    fase.setTipo(tipo);
    return fase;
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
    if (visitante == null) {
      encuentro.setEstado(BYE);
      encuentro.setGanador(local);
    } else {
      encuentro.setEstado(PENDIENTE);
    }
    fase.getEncuentros().add(encuentro);
    return encuentro;
  }
}
