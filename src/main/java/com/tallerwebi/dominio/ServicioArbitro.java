package com.tallerwebi.dominio;

import java.util.List;
import java.util.Set;

public interface ServicioArbitro {
  List<Arbitro> listarRanking();

  void designar(List<EncuentroTorneo> encuentros);

  boolean calificar(Usuario usuario, Long encuentroId, Integer puntaje);

  boolean calificarPartidoLiga(Usuario usuario, Long partidoId, Integer puntaje);

  Set<Long> encuentrosCalificables(Usuario usuario, Long torneoId);

  Set<Long> partidosLigaCalificables(Usuario usuario, Long ligaId);
}
