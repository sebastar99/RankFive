package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.FilaEstadistica;
import com.tallerwebi.dominio.GolBase;
import com.tallerwebi.dominio.Usuario;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class ModeloCompetencia {

  static final String GOLEADORES = "goleadores";
  static final String ASISTENCIAS = "asistencias";
  static final String GOLES_POR_PARTIDO = "golesPorPartido";
  static final String JUGADORES_POR_EQUIPO = "jugadoresPorEquipo";
  static final String CALIFICABLES = "calificables";
  static final String ARBITROS = "arbitros";

  private ModeloCompetencia() {}

  static void estadisticas(
    Map<String, Object> modelo,
    List<FilaEstadistica> goleadores,
    List<FilaEstadistica> asistencias,
    Map<Long, ? extends List<? extends GolBase>> golesPorPartido,
    Map<Long, List<String>> jugadoresPorEquipo
  ) {
    modelo.put(GOLEADORES, goleadores);
    modelo.put(ASISTENCIAS, asistencias);
    modelo.put(GOLES_POR_PARTIDO, golesPorPartido);
    modelo.put(JUGADORES_POR_EQUIPO, jugadoresPorEquipo);
  }

  static void sinEstadisticas(Map<String, Object> modelo) {
    estadisticas(
      modelo,
      Collections.emptyList(),
      Collections.emptyList(),
      Collections.emptyMap(),
      Collections.emptyMap()
    );
  }

  static void arbitraje(
    Map<String, Object> modelo,
    Collection<?> arbitros,
    Set<Long> calificables
  ) {
    modelo.put(ARBITROS, arbitros);
    modelo.put(CALIFICABLES, calificables);
  }

  static boolean esAdmin(Usuario usuario) {
    return usuario != null && "ADMIN".equalsIgnoreCase(usuario.getRol());
  }
}
