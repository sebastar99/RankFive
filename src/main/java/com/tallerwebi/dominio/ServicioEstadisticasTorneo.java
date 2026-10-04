package com.tallerwebi.dominio;

import java.util.List;
import java.util.Map;

public interface ServicioEstadisticasTorneo {
  boolean registrarGol(Long encuentroId, Long inscripcionId, String goleador, String asistidor);

  List<FilaEstadistica> goleadores(Long torneoId);

  List<FilaEstadistica> asistencias(Long torneoId);

  Map<Long, List<GolTorneo>> golesPorPartido(Long torneoId);

  Map<Long, List<String>> jugadoresPorInscripcion(Long torneoId);

  List<FilaPosicion> golesPorEquipo(Long torneoId);
}
