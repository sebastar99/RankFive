package com.tallerwebi.dominio;

import java.util.List;
import java.util.Map;

public interface ServicioEstadisticasLiga {
  boolean registrarGol(Long partidoId, Long inscripcionId, String goleador, String asistidor);

  List<FilaEstadistica> goleadores(Long ligaId);

  List<FilaEstadistica> asistencias(Long ligaId);

  Map<Long, List<GolLiga>> golesPorPartido(Long ligaId);

  Map<Long, List<String>> jugadoresPorInscripcion(Long ligaId);
}
