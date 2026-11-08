package com.tallerwebi.dominio;

import java.util.List;
import java.util.Map;

public interface ServicioEstadisticasTorneo {
  boolean registrarDetalles(Long encuentroId, DetallesEncuentro detalles);

  List<FilaEstadistica> goleadores(Long torneoId);

  List<FilaEstadistica> asistencias(Long torneoId);

  Map<Long, List<GolTorneo>> golesPorPartido(Long torneoId);

  Map<Long, List<TarjetaTorneo>> tarjetasPorPartido(Long torneoId);

  Map<Long, List<Usuario>> plantelesPorInscripcion(Long torneoId);

  List<FilaPosicion> golesPorEquipo(Long torneoId);
}
