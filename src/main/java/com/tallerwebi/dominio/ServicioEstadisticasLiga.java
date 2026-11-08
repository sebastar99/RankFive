package com.tallerwebi.dominio;

import java.util.List;
import java.util.Map;

public interface ServicioEstadisticasLiga {
  boolean registrarDetalles(Long partidoId, DetallesEncuentro detalles);

  List<FilaEstadistica> goleadores(Long ligaId);

  List<FilaEstadistica> asistencias(Long ligaId);

  Map<Long, List<GolLiga>> golesPorPartido(Long ligaId);

  Map<Long, List<TarjetaLiga>> tarjetasPorPartido(Long ligaId);

  Map<Long, List<Usuario>> plantelesPorInscripcion(Long ligaId);
}
