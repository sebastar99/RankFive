package com.tallerwebi.dominio;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public final class TablaPosiciones {

  private static final String ESTADO_JUGADO = "JUGADO";

  private TablaPosiciones() {}

  public static List<FilaPosicion> calcular(
    List<InscripcionTorneo> inscripciones,
    List<FaseTorneo> fases
  ) {
    Map<Long, FilaPosicion> filas = new LinkedHashMap<>();
    for (InscripcionTorneo insc : inscripciones) {
      filas.put(insc.getId(), new FilaPosicion(insc.getId(), nombreVisible(insc)));
    }
    for (FaseTorneo fase : fases) {
      for (EncuentroTorneo encuentro : fase.getEncuentros()) {
        if (esComputable(encuentro)) {
          sumar(filas, encuentro);
        }
      }
    }
    List<FilaPosicion> tabla = new ArrayList<>(filas.values());
    tabla.sort(FilaPosicion.ORDEN);
    return tabla;
  }

  public static Map<String, List<FilaPosicion>> calcularPorGrupo(
    List<InscripcionTorneo> inscripciones,
    List<FaseTorneo> fases
  ) {
    Map<String, List<InscripcionTorneo>> porGrupo = new TreeMap<>();
    for (InscripcionTorneo insc : inscripciones) {
      if (insc.getGrupo() != null) {
        porGrupo.computeIfAbsent(insc.getGrupo(), g -> new ArrayList<>()).add(insc);
      }
    }
    List<FaseTorneo> faseGrupos = new ArrayList<>();
    for (FaseTorneo fase : fases) {
      if (fase.esDeGrupos()) {
        faseGrupos.add(fase);
      }
    }
    Map<String, List<FilaPosicion>> tablas = new TreeMap<>();
    porGrupo.forEach((grupo, miembros) -> tablas.put(grupo, calcular(miembros, faseGrupos)));
    return tablas;
  }

  private static boolean esComputable(EncuentroTorneo encuentro) {
    return (
      ESTADO_JUGADO.equals(encuentro.getEstado()) &&
      encuentro.getParticipanteA() != null &&
      encuentro.getParticipanteB() != null &&
      encuentro.getGolesA() != null &&
      encuentro.getGolesB() != null
    );
  }

  private static void sumar(Map<Long, FilaPosicion> filas, EncuentroTorneo encuentro) {
    FilaPosicion filaA = filas.get(encuentro.getParticipanteA().getId());
    FilaPosicion filaB = filas.get(encuentro.getParticipanteB().getId());
    if (filaA != null) {
      filaA.registrar(encuentro.getGolesA(), encuentro.getGolesB());
    }
    if (filaB != null) {
      filaB.registrar(encuentro.getGolesB(), encuentro.getGolesA());
    }
  }

  public static String nombreVisible(InscripcionTorneo insc) {
    if (insc.getEquipo() != null) {
      return insc.getEquipo().getNombre();
    }
    if (insc.getNombreEquipo() != null) {
      return insc.getNombreEquipo();
    }
    return insc.getUsuario().getPerfil().getNombreUsuario();
  }
}
